import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.assertj.core.util.VisibleForTesting;

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

    result.append("○: Start\n");
    result.append("●: Destination\n");
    result.append("□: Available\n");
    result.append("■: Blocked\n");
    result.append("\n");

    for (int x = 0; x < height; ++x) {
      for (int y = 0; y < width; ++y) {
        final char c = x == start.x && y == start.y
            ? '○'
            : x == destination.x && y == destination.y
            ? '●'
            : obstacles.contains(Coordinate.of(x, y))
            ? '■'
            : '□';

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

  @VisibleForTesting
  Stream<Path> findPath(
      final Coordinate source,
      final Coordinate destination,
      final Map<Direction, Integer> directionLimits) {
    final Stream<Path> initialFromSourcePaths = Stream.of(
        new Path(source, directionLimits));
    final Stream<Path> initialFromDestinationPaths = Stream.of(
        new Path(
            destination,
            HashMap.ofEntries(List.of(Direction.values())
                .map(d -> Tuple.of(
                    d.opposite(),
                    directionLimits.getOrElse(d, 0))))));

    return explorePaths(
        HashMap.of(source, initialFromSourcePaths),
        HashMap.of(destination, initialFromDestinationPaths));
  }

  /**
   * Explore from two different directions and meet in the middle if possible.
   *
   * @param currentFromSource Current paths from source
   * @param currentFromDestination Current paths from destination
   * @return Solutions if any
   */
  private Stream<Path> explorePaths(
      final Map<Coordinate, Stream<Path>> currentFromSource,
      final Map<Coordinate, Stream<Path>> currentFromDestination) {
    final Map<Coordinate, Stream<Path>> nextFromSource = nextPaths(currentFromSource);
    final Map<Coordinate, Stream<Path>> combinedFromSource = currentFromSource // ensure fromSource and fromDestination don't walk passed each other
        .merge(nextFromSource, Stream::appendAll);

    final Map<Coordinate, Stream<Path>> nextFromDestination = nextPaths(currentFromDestination);
    final Map<Coordinate, Stream<Path>> combinedFromDestination = currentFromDestination // ensure fromSource and fromDestination don't walk passed each other
        .merge(nextFromDestination, Stream::appendAll);

    // find any of the fromSource and fromDestination paths that meet
    final Stream<Path> nextSolutions = combinedFromSource.keySet()
        .intersect(combinedFromDestination.keySet())
        .toStream()
        .flatMap(c -> {
          final Stream<Path> fromSourcePaths = combinedFromSource.getOrElse(c, Stream.empty());
          final Stream<Path> fromDestinationPaths = combinedFromDestination.getOrElse(c, Stream.empty());

          return fromSourcePaths
              .crossProduct(fromDestinationPaths)
              .flatMap(t2 -> {
                final Path fromSourcePath = t2._1;
                final Path fromDestinationPath = t2._2;

                return fromSourcePath.join(fromDestinationPath.reverse());
              });
        });

    return !nextSolutions.isEmpty() || nextFromSource.isEmpty() || nextFromDestination.isEmpty()
        ? nextSolutions
        : explorePaths(nextFromSource, nextFromDestination);
  }

  private Map<Coordinate, Stream<Path>> nextPaths(final Map<Coordinate, Stream<Path>> currentPaths) {
    return HashMap.ofEntries(
        currentPaths.values()
            .reduce(Stream::appendAll)
            .flatMap(p -> p.nextPaths(this))
            .groupBy(Path::last));
  }
}
