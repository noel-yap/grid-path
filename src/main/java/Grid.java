import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.assertj.core.util.VisibleForTesting;

import java.text.DecimalFormat;

/**
 * A grid with obstacles
 */
public class Grid {
  private final int width;
  private final int height;
  private final Set<Coordinate> obstacles;

  private static final Map<Direction, Function2<Grid, Coordinate, Coordinate>> directionNextCoordinateMap = HashMap.of(
      Direction.UP, (g, c) -> Coordinate.of(((c.x + g.height - 1) % g.height), c.y),
      Direction.DOWN, (g, c) -> Coordinate.of(((c.x + 1) % g.height), c.y),
      Direction.LEFT, (g, c) -> Coordinate.of(c.x, ((c.y + g.width - 1) % g.width)),
      Direction.RIGHT, (g, c) -> Coordinate.of(c.x, ((c.y + 1) % g.width)));

  public Grid(
      final int width,
      final int height,
      final Set<Coordinate> obstacles) {
    this.width = width;
    this.height = height;
    this.obstacles = obstacles;
  }

  public String draw(final Coordinate start, final Coordinate destination) {
    final StringBuilder result = new StringBuilder();

    result.append("◯: Start\n");
    result.append("⬤: Destination\n");
    result.append("⬜: Available\n");
    result.append("⬛: Blocked\n");
    result.append("\n");

    for (int x = 0; x < height; ++x) {
      for (int y = 0; y < width; ++y) {
        final char c = x == start.x && y == start.y
            ? '◯'
            : x == destination.x && y == destination.y
            ? '⬤'
            : obstacles.contains(Coordinate.of(x, y))
            ? '⬛'
            : '⬜';

        result.append(c);
      }

      result.append('\n');
    }

    return result.toString();
  }

  /**
   * Attempts to follow a {@link Direction} from a {@link Coordinate}
   *
   * @param direction {@link Direction} to follow
   * @return Last Coordinate after following directions or or none if it hits an obstacle
   */
  public Option<Coordinate> followDirectionFrom(final Option<Coordinate> from, final Direction direction) {
    return from
        .flatMap(f -> {
          final Coordinate to = followDirectionFrom(f, direction);

          return obstacles.contains(to)
              ? Option.none()
              : Option.of(to);
        });
  }

  private Coordinate followDirectionFrom(final Coordinate from, final Direction direction) {
    return directionNextCoordinateMap.get(direction).get().apply(this, from);
  }

  /**
   * Finds {@link Direction}s to {@code destination} {@link Coordinate}
   *
   * @param source Start {@link Coordinate}
   * @param destination End {@link Coordinate}
   * @return All possible directions given the constraints
   */
  public List<Direction> findDirections(
      final Coordinate source,
      final Coordinate destination,
      final Map<Direction, Integer> directionLimits) {
    return findPath(source, destination, directionLimits)
        .map(Path::getDirections)
        .headOption()
        .getOrElse(List.empty());
  }

  /**
   * Explore from two different directions and meet in the middle if possible.
   *
   * @param source Start {@link Coordinate}
   * @param destination End {@link Coordinate}
   * @return Solutions if any
   */
  @VisibleForTesting
  Stream<Path> findPath(
      final Coordinate source,
      final Coordinate destination,
      final Map<Direction, Integer> directionLimits) {
    final DecimalFormat timeFormat = new DecimalFormat("#.#########s");

    final Path initialFromSourcePaths = new Path(source, directionLimits);
    final Path initialFromDestinationPaths = new Path(
            destination,
            HashMap.ofEntries(List.of(Direction.values())
                .map(d -> Tuple.of(
                    d.opposite(),
                    directionLimits.getOrElse(d, 0)))));

    return Stream.<Either<Tuple3<Integer, Legs, Legs>, Stream<Path>>>of(Either.left(
        Tuple.of(
            1,
            new Legs(initialFromSourcePaths),
            new Legs(initialFromDestinationPaths))))
        .extend(e -> {
          if (e.isRight()) {
            return e;
          }

          final int depth = e.getLeft()._1;
          final Legs currentFromSource = e.getLeft()._2;
          final Legs currentFromDestination = e.getLeft()._3;

          final long startTime = System.nanoTime();

          System.out.println("0.0: depth = " + depth + "; time = " + timeFormat.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + currentFromSource.legs.size() + ", " + currentFromSource.alreadyVisited.size());
          final Map<Coordinate, Stream<Path>> nextFromSource = nextPaths(currentFromSource.legs)
              .filterNotKeys(currentFromSource.alreadyVisited::contains);
          System.out.println("0.1: depth = " + depth + "; time = " + timeFormat.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + nextFromSource.size());
          final Map<Coordinate, Stream<Path>> combinedFromSource = currentFromSource.legs // ensure fromSource and fromDestination don't walk passed each other
              .merge(nextFromSource, Stream::appendAll);

          System.out.println("1.0: depth = " + depth + "; time = " + timeFormat.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + currentFromDestination.legs.size() + ", " + currentFromDestination.alreadyVisited.size());
          final Map<Coordinate, Stream<Path>> nextFromDestination = nextPaths(currentFromDestination.legs)
              .filterNotKeys(currentFromDestination.alreadyVisited::contains);
          System.out.println("1.1: depth = " + depth + "; time = " + timeFormat.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + nextFromDestination.size());
          final Map<Coordinate, Stream<Path>> combinedFromDestination = currentFromDestination.legs // ensure fromSource and fromDestination don't walk passed each other
              .merge(nextFromDestination, Stream::appendAll);

          // find any of the fromSource and fromDestination paths that meet
          System.out.println("2: depth = " + depth + "; time = " + timeFormat.format((System.nanoTime() - startTime) * 1e-9));
          final Stream<Path> nextSolutions = combinedFromSource.keySet()
              .intersect(combinedFromDestination.keySet())
              .toStream()
              .flatMap(c -> {
                System.out.println("3: depth = " + depth + "; time = " + timeFormat.format((System.nanoTime() - startTime) * 1e-9));
                final Stream<Path> fromSourcePaths = combinedFromSource.getOrElse(c, Stream.empty());
                final Stream<Path> fromDestinationPaths = combinedFromDestination.getOrElse(c, Stream.empty());

                return fromSourcePaths
                    .crossProduct(fromDestinationPaths)
                    .flatMap(cp -> {
                      System.out.println("4: depth = " + depth + "; time = " + timeFormat.format((System.nanoTime() - startTime) * 1e-9));
                      final Path fromSourcePath = cp._1;
                      final Path fromDestinationPath = cp._2;

                      return fromSourcePath.join(fromDestinationPath.reverse());
                    });
              });

          System.out.println("5: depth = " + depth + "; time = " + timeFormat.format((System.nanoTime() - startTime) * 1e-9));
          if (!nextSolutions.isEmpty() || nextFromSource.isEmpty() || nextFromDestination.isEmpty()) {
            return Either.right(nextSolutions);
          } else {
            return Either.left(Tuple.of(
                depth + 1,
                currentFromSource.andThen(nextFromSource),
                currentFromDestination.andThen(nextFromDestination)));
          }
        })
        .dropWhile(Either::isLeft)
        .flatMap(Either::get);
  }

  private Map<Coordinate, Stream<Path>> nextPaths(final Map<Coordinate, Stream<Path>> currentPaths) {
    return HashMap.ofEntries(
        currentPaths.values()
            .reduce(Stream::appendAll)
            .distinctBy(Path::compare)
            .flatMap(p -> p.nextPaths(this))
            .groupBy(Path::last));
  }

  @EqualsAndHashCode
  @ToString
  private static final class Legs {
    public final Map<Coordinate, Stream<Path>> legs;
    public final Set<Coordinate> alreadyVisited;

    public Legs(final Path path) {
      this(HashMap.of(path.head(), Stream.of(path)), HashSet.empty());
    }

    public Legs(final Map<Coordinate, Stream<Path>> legs, final Set<Coordinate> alreadyVisited) {
      this.legs = legs;
      this.alreadyVisited = alreadyVisited;
    }

    public Legs andThen(final Map<Coordinate, Stream<Path>> next) {
      return new Legs(next, alreadyVisited.addAll(next.keySet()));
    }
  }
}
