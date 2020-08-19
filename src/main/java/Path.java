import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Value;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;
import org.assertj.core.util.VisibleForTesting;

/**
 * List of {@link Coordinate}s each successive one adjacent to the previous
 */
@EqualsAndHashCode
public class Path {
  private final Grid grid;
  private final List<Coordinate> path; // in reverse for both cpu and memory performance
  private final List<Direction> directions; // in reverse for both cpu and memory performance
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
        .reverse()
        .map(Coordinate::toString)
        .intersperse(" -> ")
        .foldLeft(new StringBuilder(), StringBuilder::append)
        .toString();

    return pathString + "; " + directions.reverse().toString() + "; " + directionLimits.toString();
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
        .map(p -> p.directions.reverse());
  }

  @VisibleForTesting
  List<Path> findPathsToDestination(final Coordinate destination) {
    final HashSet<Path> initialFromSourcePaths = HashSet.of(this);
    final HashSet<Path> initialFromDestinationPaths = HashSet.of(
        Path
            .onGrid(grid)
            .startAt(destination)
            .withDirectionLimits(HashMap.ofEntries(List.of(Direction.values())
                .map(d -> Tuple.of(
                    d.opposite(),
                    directionLimits.getOrElse(d, 0)))
                .filter(t2 -> t2._2 > 0))));

    final Tuple3<Set<Path>, Map<Coordinate, Set<Path>>, Map<Coordinate, Set<Path>>> paths = explorePaths(Tuple.of(
        HashSet.empty(),
        HashMap.of(path.head(), initialFromSourcePaths),
        HashMap.of(destination, initialFromDestinationPaths)));

    return paths._1.toList();
  }

  /**
   * Explore from two different directions and meet in the middle if possbile.
   *
   * @param current Current solutions, paths from source, and paths from destination
   * @return Solutions if any
   */
  private Tuple3<Set<Path>, Map<Coordinate, Set<Path>>, Map<Coordinate, Set<Path>>> explorePaths(
      final Tuple3<Set<Path>, Map<Coordinate, Set<Path>>, Map<Coordinate, Set<Path>>> current) {
    final Set<Path> solutions = current._1;
    final Map<Coordinate, Set<Path>> currentFromSource = current._2;
    final Map<Coordinate, Set<Path>> currentFromDestination = current._3;

    if (!solutions.isEmpty() || currentFromSource.isEmpty() || currentFromDestination.isEmpty()) {
      return current;
    }

    final Map<Coordinate, Set<Path>> nextFromSource = nextPaths(currentFromSource);
    final Map<Coordinate, Set<Path>> combinedFromSource = currentFromSource // ensure fromSource and fromDestination don't walk passed each other
        .merge(nextFromSource, Set::union);

    final Map<Coordinate, Set<Path>> nextFromDestination = nextPaths(currentFromDestination);
    final Map<Coordinate, Set<Path>> combinedFromDestination = currentFromDestination // ensure fromSource and fromDestination don't walk passed each other
        .merge(nextFromDestination, Set::union);

    // find any of the fromSource and fromDestination paths that meet
    final Set<Path> nextSolutions = combinedFromSource.keySet()
        .intersect(combinedFromDestination.keySet())
        .flatMap(c -> {
          final List<Path> fromSourcePaths = combinedFromSource.get(c).get().toList();
          final List<Path> fromDestinationPaths = combinedFromDestination.get(c).get().toList();

          return fromSourcePaths
              .crossProduct(fromDestinationPaths)
              .flatMap(t2 -> {
                final Path fromSourcePath = t2._1;
                final Map<Direction, Integer> sourceDirectionLimit = fromSourcePath.directionLimits;

                final Path fromDestinationPath = t2._2;
                final Map<Direction, Integer> destinationDirectionTaken = HashMap.ofEntries(List.of(Direction.values())
                    .map(direction -> Tuple.of(direction, fromDestinationPath.directions.count(d -> d == direction))));

                if (sourceDirectionLimit.getOrElse(Direction.UP, 0) < destinationDirectionTaken.getOrElse(Direction.DOWN, 0)
                    || sourceDirectionLimit.getOrElse(Direction.DOWN, 0) < destinationDirectionTaken.getOrElse(Direction.UP, 0)
                    || sourceDirectionLimit.getOrElse(Direction.LEFT, 0) < destinationDirectionTaken.getOrElse(Direction.RIGHT, 0)
                    || sourceDirectionLimit.getOrElse(Direction.RIGHT, 0) < destinationDirectionTaken.getOrElse(Direction.LEFT, 0)) {
                  return Option.none();
                } else {
                  final List<Coordinate> solutionPath = fromSourcePath.path.prependAll(fromDestinationPath.path.tail().reverse());
                  final Map<Direction, Integer> solutionDirectionLimits = fromSourcePath.directionLimits
                      .map((sd, l) -> Tuple.of(
                          sd,
                          l - fromDestinationPath.directions.count(dd -> sd == dd.opposite())
                      ))
                      .filter((d, l) -> l > 0);
                  final List<Direction> solutionDirections = fromSourcePath.directions.prependAll(
                      fromDestinationPath.directions
                          .map(Direction::opposite)
                          .reverse());

                  return Option.of(new Path(
                      grid,
                      solutionPath,
                      solutionDirectionLimits,
                      solutionDirections));
                }
              });
        });

    return explorePaths(Tuple.of(
        nextSolutions,
        nextFromSource,
        nextFromDestination));
  }

  private Map<Coordinate, Set<Path>> nextPaths(final Map<Coordinate, Set<Path>> currentFromSource) {
    return HashMap.ofEntries(
        currentFromSource.values()
            .flatMap(ps -> ps.flatMap(Path::nextPaths))
            .groupBy(p -> p.path.head())
            .map(t2 -> t2.map2(Value::toSet)));
  }

  /**
   * @return Potential paths
   */
  public Stream<Path> nextPaths() {
    return directionLimits.keySet()
        .toStream()
        .flatMap(d -> grid.followDirectionFrom(Option.of(path.head()), d)
            .flatMap(next -> {
              if (path.contains(next)) { // Don't retrace steps.
                return Option.none();
              } else { // Move onto next coordinate.
                return Option.of(new Path(
                    grid,
                    path.prepend(next),
                    directionLimits
                        .put(d, directionLimits.get(d).get() - 1) // decrement direction limit
                        .filter(t2 -> t2._2 > 0), // remove exhausted directions
                    directions.prepend(d)
                ));
              }
            }));
  }
}
