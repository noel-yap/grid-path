import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;
import org.assertj.core.util.VisibleForTesting;

/**
 * List of {@link Coordinate}s each successive one adjacent to the previous
 */
@EqualsAndHashCode
public class Path {
  private final List<Coordinate> path; // in reverse for both cpu and memory performance
  private final List<Direction> directions; // in reverse for both cpu and memory performance
  private final Map<Direction, Integer> directionLimits;

  public Path(final Coordinate start, final Map<Direction, Integer> directionLimits) {
    this(List.of(start), directionLimits);
  }

  private Path(final List<Coordinate> path, final Map<Direction, Integer> directionLimits) {
    this(path, directionLimits, List.empty());
  }

  @VisibleForTesting
  Path(
      final List<Coordinate> path,
      final Map<Direction, Integer> directionLimits,
      final List<Direction> directions) {
    this.path = path;
    this.directionLimits = directionLimits.filter(t2 -> t2._2 > 0);
    this.directions = directions;
  }

  @Override
  public String toString() {
    final String pathString = path
        .reverse()
        .map(Coordinate::toString)
        .intersperse(" -> ")
        .foldLeft(new StringBuilder(), StringBuilder::append)
        .toString();

    return pathString + "; " + directions.reverse().toString() + "; " + directionLimits.toString();
  }

  public List<Direction> getDirections() {
    return directions.reverse();
  }

  public Coordinate last() {
    return path.head();
  }

  public Path reverse() {
    return new Path(
        path.reverse(),
        directionLimits
            .map((sd, l) -> Tuple.of(
                sd.opposite(),
                l)),
        directions
            .map(Direction::opposite)
            .reverse());
  }

  public Option<Path> join(final Path partialPath) {
    final Map<Direction, Integer> firstLegDirectionLimits = directionLimits;
    final Map<Direction, Integer> secondLegDirectionsTaken = HashMap.ofEntries(List.of(Direction.values())
        .map(direction -> Tuple.of(direction, partialPath.directions.count(d -> d == direction))));

    if (!path.head().equals(partialPath.path.last())
        || firstLegDirectionLimits.getOrElse(Direction.UP, 0) < secondLegDirectionsTaken.getOrElse(Direction.UP, 0)
        || firstLegDirectionLimits.getOrElse(Direction.DOWN, 0) < secondLegDirectionsTaken.getOrElse(Direction.DOWN, 0)
        || firstLegDirectionLimits.getOrElse(Direction.LEFT, 0) < secondLegDirectionsTaken.getOrElse(Direction.LEFT, 0)
        || firstLegDirectionLimits.getOrElse(Direction.RIGHT, 0) < secondLegDirectionsTaken.getOrElse(Direction.RIGHT, 0)) {
      return Option.none();
    } else {
      final List<Coordinate> solutionPath = path.prependAll(partialPath.path.init());
      final Map<Direction, Integer> solutionDirectionLimits = directionLimits
          .map((sd, l) -> Tuple.of(
              sd,
              l - partialPath.directions.count(dd -> sd == dd)
          ));
      final List<Direction> solutionDirections = directions.prependAll(partialPath.directions);

      return Option.of(new Path(
          solutionPath,
          solutionDirectionLimits,
          solutionDirections));
    }
  }

  /**
   * @return Potential paths
   */
  public List<Path> nextPaths(final Grid grid) {
    return directionLimits.keySet()
        .toList()
        .flatMap(d -> grid.followDirectionFrom(Option.of(path.head()), d)
            .flatMap(next -> {
              if (path.contains(next)) { // Don't retrace steps.
                return Option.none();
              } else { // Move onto next coordinate.`
                return Option.of(new Path(
                    path.prepend(next),
                    directionLimits
                        .put(d, directionLimits.get(d).get() - 1), // decrement direction limit
                    directions.prepend(d)
                ));
              }
            }));
  }
}
