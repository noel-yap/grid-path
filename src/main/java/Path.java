import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;
import org.assertj.core.util.VisibleForTesting;

/**
 * List of {@link Coordinate}s each successive one adjacent to the previous
 */
@EqualsAndHashCode
public class Path implements Comparable<Path> {
  private final Coordinate start;
  private final Coordinate end;
  private final List<Direction> directions; // in reverse for both cpu and memory performance
  private final Map<Direction, Integer> directionLimits;

  public Path(
      final Coordinate start,
      final Coordinate end,
      final Map<Direction, Integer> directionLimits) {
    this(start, end, directionLimits, List.empty());
  }

  @VisibleForTesting
  Path(
      final Coordinate start,
      final Coordinate end,
      final Map<Direction, Integer> directionLimits,
      final List<Direction> directions) {
    this.start = start;
    this.end = end;
    this.directionLimits = directionLimits.filter(t2 -> t2._2 > 0);
    this.directions = directions;
  }

  @Override
  public String toString() {
    return start + " -> " + end + "; " + directions.reverse().toString() + "; " + directionLimits.toString();
  }

  @Override
  public int compareTo(final Path that) {
    final int lastCompare = this.last().compare(that.last());
    final int upCompare = directionCompare(that, Direction.UP);
    final int downCompare = directionCompare(that, Direction.DOWN);
    final int leftCompare = directionCompare(that, Direction.LEFT);
    final int rightCompare = directionCompare(that, Direction.RIGHT);

    return lastCompare != 0
        ? lastCompare
        : upCompare != 0
        ? upCompare
        : downCompare != 0
        ? downCompare
        : leftCompare != 0
        ? leftCompare
        : rightCompare;
  }

  public List<Direction> getDirections() {
    return directions.reverse();
  }

  public Coordinate last() {
    return end;
  }

  private int directionCompare(final Path that, final Direction d) {
    return this.directionLimits.getOrElse(d, 0) - that.directionLimits.getOrElse(d, 0);
  }

  public Option<Path> meet(final Path partialPath) {
    final Path reversedPartialPath = new Path(
        partialPath.end,
        partialPath.start,
        partialPath.directionLimits
            .map((sd1, l1) -> Tuple.of(
                sd1.opposite(),
                l1)),
        partialPath.directions
            .map(Direction::opposite)
            .reverse());

    final Map<Direction, Integer> firstLegDirectionLimits = directionLimits;
    final Map<Direction, Integer> secondLegDirectionsTaken = HashMap.ofEntries(List.of(Direction.values())
        .map(direction -> Tuple.of(direction, reversedPartialPath.directions.count(d -> d == direction))));

    if (!end.equals(reversedPartialPath.start)
        || firstLegDirectionLimits.getOrElse(Direction.UP, 0) < secondLegDirectionsTaken.getOrElse(Direction.UP, 0)
        || firstLegDirectionLimits.getOrElse(Direction.DOWN, 0) < secondLegDirectionsTaken.getOrElse(Direction.DOWN, 0)
        || firstLegDirectionLimits.getOrElse(Direction.LEFT, 0) < secondLegDirectionsTaken.getOrElse(Direction.LEFT, 0)
        || firstLegDirectionLimits.getOrElse(Direction.RIGHT, 0) < secondLegDirectionsTaken.getOrElse(Direction.RIGHT, 0)) {
      return Option.none();
    } else {
      final Map<Direction, Integer> solutionDirectionLimits = directionLimits
          .map((sd, l) -> Tuple.of(
              sd,
              l - reversedPartialPath.directions.count(dd -> sd == dd)
          ));
      final List<Direction> solutionDirections = directions.prependAll(reversedPartialPath.directions);

      return Option.of(new Path(
          reversedPartialPath.start,
          reversedPartialPath.end,
          solutionDirectionLimits,
          solutionDirections));
    }
  }

  /**
   * @return Potential paths
   */
  public Stream<Path> nextPaths(final Grid grid) {
    return directionLimits.keySet()
        .toStream()
        .flatMap(d -> grid.followDirectionFrom(Option.of(end), d)
            .filterNot(end::equals) // don't backtrack
            .map(nextCoordinate -> new Path(
                start,
                nextCoordinate,
                directionLimits
                    .put(d, directionLimits.get(d).get() - 1), // decrement direction limit
                directions.prepend(d))));
  }
}
