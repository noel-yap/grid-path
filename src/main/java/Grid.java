import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.SortedSet;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeSet;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.assertj.core.util.VisibleForTesting;

import java.text.DecimalFormat;
import java.util.function.Function;

/**
 * A grid with obstacles
 */
public class Grid {
  private final int width;
  private final int height;
  private final Set<Coordinate> obstacles;

  private static final Map<Direction, Function2<Grid, Coordinate, Coordinate>> DIRECTION_NEXT_COORDINATE_MAP = HashMap.of(
      Direction.UP, (g, c) -> Coordinate.of(c.x, (c.y + g.height - 1) % g.height),
      Direction.DOWN, (g, c) -> Coordinate.of(c.x, (c.y + g.height + 1) % g.height),
      Direction.LEFT, (g, c) -> Coordinate.of((c.x + g.width - 1) % g.width, c.y),
      Direction.RIGHT, (g, c) -> Coordinate.of((c.x + g.width + 1) % g.width, c.y));

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
      result.append("start = ").append(start).append("\n");
      result.append("destination = ").append(destination).append("\n");
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
  public Option<List<Direction>> findDirections(
      final Coordinate source,
      final Coordinate destination,
      final Map<Direction, Integer> directionLimits) {
    final Map<Coordinate, SortedSet<Directions>> directions = findPath(source, destination, directionLimits);

    return directions.isEmpty()
        ? Option.none()
        : Option.of(directions.head()._2.head().getDirections());
  }

  /**
   * Explore from two different directions and meet in the middle if possible.
   *
   * @param source Start {@link Coordinate}
   * @param destination End {@link Coordinate}
   * @return Solutions if any
   */
  @VisibleForTesting
  Map<Coordinate, SortedSet<Directions>> findPath(
      final Coordinate source,
      final Coordinate destination,
      final Map<Direction, Integer> directionLimits) {
    final Legs initialFromSourcePaths = new Legs(source, directionLimits);
    final Legs initialFromDestinationPaths = new Legs(destination, directionLimits.mapKeys(Direction::opposite));

    return Stream
        .<Either<Tuple3<Integer, Legs, Legs>, Map<Coordinate, SortedSet<Directions>>>>of(Either.left(
            Tuple.of(
                1,
                initialFromSourcePaths,
                initialFromDestinationPaths)))
        .extend(e -> {
          if (e.isRight()) {
            return e;
          }

          final int depth = e.getLeft()._1;
          final Legs currentFromSource = e.getLeft()._2;
          final Legs currentFromDestination = e.getLeft()._3;

          final long startTime = System.nanoTime();

          System.out.println("0.0: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + currentFromSource.legs.size() + ", " + currentFromSource.size() + ", " + currentFromSource.alreadyVisited.size());
          final Legs nextFromSource = currentFromSource.nextPaths(this);
          System.out.println("0.1: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + nextFromSource.legs.size() + ", " + nextFromSource.size() + ", " + nextFromSource.alreadyVisited.size());
          final Map<Coordinate, SortedSet<Directions>> combinedFromSource = currentFromSource.legs.merge(nextFromSource.legs); // ensure fromSource and fromDestination don't walk passed each other

          System.out.println("1.0: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + currentFromDestination.legs.size() + ", " + currentFromDestination.size() + ", " + currentFromDestination.alreadyVisited.size());
          final Legs nextFromDestination = currentFromDestination.nextPaths(this);
          System.out.println("1.1: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + nextFromDestination.legs.size() + ", " + nextFromDestination.size() + ", " + nextFromDestination.alreadyVisited.size());
          final Map<Coordinate, SortedSet<Directions>> combinedFromDestination = currentFromDestination.legs.merge(nextFromDestination.legs); // ensure fromDestination and fromDestination don't walk passed each other

          System.out.println("2: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9));
          final Map<Coordinate, SortedSet<Directions>> solutions = meet(combinedFromSource, combinedFromDestination);

          System.out.println("3: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9));
          if (!solutions.isEmpty() || nextFromSource.isEmpty() || nextFromDestination.isEmpty()) {
            return Either.right(solutions);
          } else {
            return Either.left(Tuple.of(
                depth + 1,
                nextFromSource,
                nextFromDestination));
          }
        })
        .dropWhile(Either::isLeft)
        .head()
        .get();
  }

  /**
   * Join together legs rhs meet each other. {@code rhs} will be reversed before joining.
   *
   * @param rhs Other set of legs
   * @return Legs rhs have met each other
   */
  @VisibleForTesting
  Map<Coordinate, SortedSet<Directions>> meet(final Map<Coordinate, SortedSet<Directions>> lhs, final Map<Coordinate, SortedSet<Directions>> rhs) {
    final Set<Coordinate> meetingPoints = lhs.keySet()
        .intersect(rhs.keySet());

    return meetingPoints
        .map(c -> {
          final SortedSet<Directions> thisDirections = lhs.get(c).get();
          final SortedSet<Directions> thatDirections = rhs.get(c).get();

          return HashMap.ofEntries(
              thisDirections
                  .toStream()
                  .crossProduct(thatDirections)
                  .map(t2 -> {
                    final Directions reversedThatDirections = t2._2.reverse();

                    final Coordinate endCoordinate = followDirectionsFrom(Option.of(c), reversedThatDirections.getDirections()).get();

                    return Tuple.of(
                        endCoordinate,
                        t2._1.appendAll(reversedThatDirections));
                  })
                  .filter(cd -> cd._2.directionLimits.forAll(dl -> dl._2 > -1))
                  .groupBy(t2 -> t2._1)
                  .mapValues(v -> v.map(t2 -> t2._2))
                  .mapValues(v -> TreeSet.ofAll(Directions::compareTo, v)));
        })
        .foldLeft(HashMap.<Coordinate, TreeSet<Directions>>empty(), (accum, elt) -> accum.merge(elt, TreeSet::union))
        .mapValues(Function.identity());
  }
}
