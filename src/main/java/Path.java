import io.vavr.Tuple;
import io.vavr.Tuple2;
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
  private final Grid grid;
  private final List<Coordinate> path;
  private final List<Direction> directions;
  private final Map<Direction, Integer> directionLimits;

  /**
   * Creates a Path on {@link Grid}
   *
   * @param grid {@link Grid} for this Path
   * @return Path starting at the start {@link Coordinate}
   */
  public static Path onGrid(final Grid grid) {
    return new Path(grid, List.empty(), HashMap.empty());
  }

  private Path(final Grid grid, final List<Coordinate> path, final Map<Direction, Integer> directionLimits) {
    this(grid, path, directionLimits, List.empty());
  }

  @VisibleForTesting
  Path(
      final Grid grid,
      final List<Coordinate> path,
      final Map<Direction, Integer> directionLimits,
      final List<Direction> directions) {
    this.grid = grid;
    this.path = path;
    this.directionLimits = directionLimits;
    this.directions = directions;
  }

  @Override
  public String toString() {
    final String pathString = path
        .map(Coordinate::toString)
        .intersperse(" -> ")
        .foldLeft(new StringBuilder(), StringBuilder::append)
        .toString();

    return pathString + "; " + directions.toString() + "; " + directionLimits.toString();
  }

  public Path startAt(final Coordinate start) {
    return new Path(grid, List.of(start), HashMap.empty());
  }

  public Path withDirectionLimits(final Map<Direction, Integer> directionLimits) {
    return new Path(this.grid, this.path, directionLimits);
  }

  /**
   * Finds {@link Direction}s to {@code destination} {@link Coordinate}
   *
   * @param destination End {@link Coordinate}
   * @return All possible directions given the constraints
   */
  public List<List<Direction>> findDirectionsToDestination(final Coordinate destination) {
    return findPathsToDestination(destination)
        .map(p -> p.directions);
  }

  @VisibleForTesting
  List<Path> findPathsToDestination(final Coordinate destination) {
    return explorePaths(
        Tuple.of(
            List.empty(),
            List.of(this)),
        destination)._1;
  }

  /**
   * Helper function for findPathsToDestination
   *
   * @param current Current List of Paths to destination and List of potential Paths
   * @param destination End {@link Coordinate}
   * @return List of Paths to destination and List of potential Paths
   */
  private Tuple2<List<Path>, List<Path>> explorePaths(
      final Tuple2<List<Path>, List<Path>> current,
      final Coordinate destination) {
    if (current._2.isEmpty()) {
      return current;
    }

    return explorePaths(
        current._2
            .flatMap(Path::nextPaths) // next paths from those paths not having found destination
            .partition(p -> p.path.last().equals(destination)) // partition by destination found versus not found
            .map1(current._1::appendAll), // keep track of paths already leading to destination
        destination);
  }

  /**
   * @return Potential paths
   */
  public List<Path> nextPaths() {
    return directionLimits.keySet()
        .toList()
        .flatMap(d -> grid.followDirectionFrom(Option.of(path.last()), d)
            .flatMap(next -> {
              if (path.contains(next)) { // Don't retrace steps.
                return Option.none();
              } else { // Move onto next coordinate.
                return Option.of(new Path(
                    grid,
                    path.append(next),
                    directionLimits
                        .put(d, directionLimits.get(d).get() - 1) // decrement direction limit
                        .filter(t2 -> t2._2 > 0), // remove exhausted directions
                    directions.append(d)
                ));
              }
            }));
  }
}
