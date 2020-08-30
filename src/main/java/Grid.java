import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeSet;
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

  private static final Map<Direction, Function2<Grid, Coordinate, Coordinate>> DIRECTION_NEXT_COORDINATE_MAP = HashMap.of(
      Direction.UP, (g, c) -> Coordinate.of(((c.x + g.height - 1) % g.height), c.y),
      Direction.DOWN, (g, c) -> Coordinate.of(((c.x + 1) % g.height), c.y),
      Direction.LEFT, (g, c) -> Coordinate.of(c.x, ((c.y + g.width - 1) % g.width)),
      Direction.RIGHT, (g, c) -> Coordinate.of(c.x, ((c.y + 1) % g.width)));

  private static final DecimalFormat TIME_FORMAT = new DecimalFormat("#.#########s");

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

    if (width * height > 1 << 16) {
      result.append("start = " + start + "\n");
      result.append("destination = " + destination + "\n");
    } else {
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
    }

    return result.toString();
  }

  public Option<Coordinate> followDirectionsFrom(final Option<Coordinate> from, final List<Direction> directions) {
    return directions
        .foldLeft(from, this::followDirectionFrom);
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
    return DIRECTION_NEXT_COORDINATE_MAP.get(direction).get().apply(this, from);
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
    final Path initialFromSourcePaths = new Path(source, source, directionLimits);
    final Path initialFromDestinationPaths = new Path(
            destination,
            destination,
            directionLimits.mapKeys(Direction::opposite));

    return Stream.<Either<Tuple3<Integer, Legs, Legs>, Stream<Path>>>of(Either.left(
        Tuple.of(
            1,
            new Legs(initialFromSourcePaths),
            new Legs(initialFromDestinationPaths))))
        .extend(e -> {
          if (e.isRight()) {
            System.out.println("0: e = " + e);

            return e;
          }

          final int depth = e.getLeft()._1;
          final Legs currentFromSource = e.getLeft()._2;
          final Legs currentFromDestination = e.getLeft()._3;

          final long startTime = System.nanoTime();

          System.out.println("0.0: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + currentFromSource.legs.size() + ", " + currentFromSource.legs.values().map(Set::size).sum() + ", " + currentFromSource.alreadyVisited.size());
          final Map<Coordinate, Set<Path>> nextFromSource = nextPaths(currentFromSource);
          System.out.println("0.1: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + nextFromSource.size());
          final Map<Coordinate, Set<Path>> combinedFromSource = currentFromSource.legs // ensure fromSource and fromDestination don't walk passed each other
              .merge(nextFromSource, Set::union);

          System.out.println("1.0: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + currentFromDestination.legs.size() + ", " + currentFromDestination.legs.values().map(Set::size).sum() + ", " + currentFromDestination.alreadyVisited.size());
          final Map<Coordinate, Set<Path>> nextFromDestination = nextPaths(currentFromDestination);
          System.out.println("1.1: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + nextFromDestination.size());
          final Map<Coordinate, Set<Path>> combinedFromDestination = currentFromDestination.legs // ensure fromSource and fromDestination don't walk passed each other
              .merge(nextFromDestination, Set::union);

          // find any of the fromSource and fromDestination paths that meet
          System.out.println("2: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9));
          final Stream<Path> nextSolutions = combinedFromSource.keySet()
              .intersect(combinedFromDestination.keySet())
              .toStream()
              .flatMap(c -> {
                System.out.println("3: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9));
                final Set<Path> fromSourcePaths = combinedFromSource.getOrElse(c, TreeSet.empty(Path::compareTo));
                final Set<Path> fromDestinationPaths = combinedFromDestination.getOrElse(c, TreeSet.empty(Path::compareTo));

                return fromSourcePaths
                    .toStream()
                    .crossProduct(fromDestinationPaths)
                    .flatMap(cp -> {
                      System.out.println("4: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9));
                      final Path fromSourcePath = cp._1;
                      final Path fromDestinationPath = cp._2;

                      return fromSourcePath.meet(fromDestinationPath);
                    });
              });

          System.out.println("5: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9));
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
        .head()
        .get();
  }

  private Map<Coordinate, Set<Path>> nextPaths(final Legs current) {
    return nextPaths(current.legs)
        .filterNotKeys(current.alreadyVisited::contains);
  }

  private Map<Coordinate, Set<Path>> nextPaths(final Map<Coordinate, Set<Path>> currentPaths) {
    return HashMap.ofEntries(
        currentPaths.values()
            .reduce(Set::union)
            .flatMap(p -> p.nextPaths(this))
            .groupBy(Path::last));
  }

  @EqualsAndHashCode
  @ToString
  private static final class Legs {
    public final Map<Coordinate, Set<Path>> legs;
    public final Set<Coordinate> alreadyVisited;

    public Legs(final Path path) {
      this(HashMap.of(path.last(), TreeSet.of(Path::compareTo, path)), HashSet.empty());
    }

    public Legs(final Map<Coordinate, Set<Path>> legs, final Set<Coordinate> alreadyVisited) {
      this.legs = legs;
      this.alreadyVisited = alreadyVisited;
    }

    public Legs andThen(final Map<Coordinate, Set<Path>> next) {
      return new Legs(next, legs.keySet().addAll(next.keySet()));
    }
  }
}
