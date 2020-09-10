import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.assertj.core.util.VisibleForTesting;

import java.text.DecimalFormat;
import java.util.function.BiFunction;

/**
 * A grid with obstacles
 */
public class Grid {
  private final int width;
  private final int height;
  private final Set<Coordinate> obstacles;

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

    result.append("start = ").append(start).append("\n");
    result.append("destination = ").append(destination).append("\n");

    if (width * height < 1 << 16) {
      result.append("◯: Start\n");
      result.append("⬤: Destination\n");
      result.append("⬜: Available\n");
      result.append("⬛: Blocked\n");
      result.append("\n");

      for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
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

  @VisibleForTesting
  Coordinate followDirectionFrom(final Coordinate from, final Direction direction) {
    return switch (direction) {
      case UP -> Coordinate.of(from.x, (from.y + height - 1) % height);
      case DOWN -> Coordinate.of(from.x, (from.y + height + 1) % height);
      case LEFT -> Coordinate.of((from.x + width - 1) % width, from.y);
      case RIGHT -> Coordinate.of((from.x + width + 1) % width, from.y);
    };
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
      final DirectionLimits directionLimits) {
    final Map<Coordinate, Array<Directions>> directions = findPath(source, destination, directionLimits);

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
  Map<Coordinate, Array<Directions>> findPath(
      final Coordinate source,
      final Coordinate destination,
      final DirectionLimits directionLimits) {
    final Legs initialFromSourcePaths = new Legs(source, directionLimits);
    final Legs initialFromDestinationPaths = new Legs(destination, directionLimits.opposite());

    final int deltaX = Math.abs(source.x - destination.x);
    final int negativeDeltaX = width - deltaX;
    final int deltaY = Math.abs(source.y - destination.y);
    final int negativeDeltaY = height - deltaY;

    final boolean oddStepCountPathPossible = !isEven(deltaX + deltaY)
        || !isEven(deltaX + negativeDeltaY)
        || !isEven(negativeDeltaX + deltaY)
        || !isEven(negativeDeltaX + negativeDeltaY);
    final BiFunction<Legs, Legs, Map<Coordinate, Array<Directions>>> oddStepCountPathsProvider = oddStepCountPathPossible
        ? (fromSource, nextFromDestination) -> meet(fromSource.legs, nextFromDestination.legs)
        : (fromSource, nextFromDestination) -> HashMap.empty();

    final boolean evenStepCountPathPossible = isEven(deltaX + deltaY)
        || isEven(deltaX + negativeDeltaY)
        || isEven(negativeDeltaX + deltaY)
        || isEven(negativeDeltaX + negativeDeltaY);
    final BiFunction<Legs, Legs, Map<Coordinate, Array<Directions>>> evenStepCountPathsProvider = evenStepCountPathPossible
        ? (fromSource, fromDestination) -> meet(fromSource.legs, fromDestination.legs)
        : (fromSource, fromDestination) -> HashMap.empty();

    return Stream
        .<Either<Tuple3<Integer, Legs, Legs>, Map<Coordinate, Array<Directions>>>>of(Either.left(
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

          System.out.println("0.0: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + currentFromSource.legs.size() + ", " + currentFromSource.priorEnds.size());
          final Legs nextFromSource = currentFromSource.nextPaths(this);
          System.out.println("0.1: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + nextFromSource.legs.size() + ", "  + nextFromSource.priorEnds.size());

          System.out.println("1.0: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + currentFromDestination.legs.size() + ", " + currentFromDestination.priorEnds.size());
          final Legs nextFromDestination = currentFromDestination.nextPaths(this);
          System.out.println("1.1: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + nextFromDestination.legs.size() + ", " + nextFromDestination.priorEnds.size());

          final Map<Coordinate, Array<Directions>> oddStepCountSolutions = oddStepCountPathsProvider.apply(currentFromSource, nextFromDestination);
          System.out.println("2: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", possible = " + oddStepCountPathPossible + ", size = " + oddStepCountSolutions.size());

          final Map<Coordinate, Array<Directions>> evenStepCountSolutions = evenStepCountPathsProvider.apply(nextFromSource, nextFromDestination);
          System.out.println("3: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", possible = " + evenStepCountPathPossible + ", size = " + evenStepCountSolutions.size());

          final Map<Coordinate, Array<Directions>> solutions = oddStepCountSolutions.merge(evenStepCountSolutions);
          System.out.println("4: depth = " + depth + "; time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + solutions.size());

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
  Map<Coordinate, Array<Directions>> meet(final Map<Coordinate, Array<Directions>> lhs, final Map<Coordinate, Array<Directions>> rhs) {
    final long startTime = System.nanoTime();

    System.out.println("0.0: time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + lhs.size() + ", " + rhs.size());
    final Set<Coordinate> meetingPoints = lhs.keySet()
        .intersect(rhs.keySet());
    System.out.println("0.1: time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + meetingPoints.size());

    return meetingPoints
        .map(c -> {
          final Array<Directions> lhsDirections = lhs.get(c).get();
          final Array<Directions> rhsDirections = rhs.get(c).get();

          System.out.println("1: time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", size = " + lhsDirections.size() + ", " + rhsDirections.size());
          return HashMap.ofEntries(
              lhsDirections
                  .toStream()
                  .crossProduct(rhsDirections)
                  .map(t2 -> {
                    final Directions reversedThatDirections = t2._2.reverse();

                    final Coordinate endCoordinate = followDirectionsFrom(Option.of(c), reversedThatDirections.getDirections()).get();

                    return Tuple.of(
                        endCoordinate,
                        t2._1.appendAll(reversedThatDirections));
                  })
                  .peek(t2 -> System.out.println("2: time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", directions = " + t2._2.size()))
                  .filter(cd -> cd._2.directionLimits.isValid())
                  .peek(t2 -> System.out.println("3: time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", directions = " + t2._2.size()))
                  .groupBy(t2 -> t2._1)
                  .mapValues(v -> v.map(t2 -> t2._2))
                  .mapValues(Array::ofAll)
                  .peek(t2 -> System.out.println("4: time = " + TIME_FORMAT.format((System.nanoTime() - startTime) * 1e-9) + ", directions = " + t2._2.size())));
        })
        .foldLeft(
            HashMap.empty(),
            (accum, elt) -> accum.merge(
                elt,
                (lhsDirections, rhsDirections) -> lhsDirections
                    .appendAll(rhsDirections)
                    .distinctBy(Directions::compareTo)));
  }

  private static boolean isEven(final int n) {
    return (n & 1) == 0;
  }
}
