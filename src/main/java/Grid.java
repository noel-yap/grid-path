import io.vavr.Function2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.control.Option;

/**
 * A 5×5 grid with obstacles
 */
public class Grid {
  private final int width;
  private final int height;
  private final Set<Coordinate> obstacles;

  private static final Map<Direction, Function2<Grid, Coordinate, Coordinate>> directionNextCoordinateMap = HashMap.of(
      Direction.UP, (g, c) -> Coordinate.of((c.x + g.height - 1) % g.height, c.y),
      Direction.DOWN, (g, c) -> Coordinate.of((c.x + 1) % g.height, c.y),
      Direction.LEFT, (g, c) -> Coordinate.of(c.x, (c.y + g.width - 1) % g.width),
      Direction.RIGHT, (g, c) -> Coordinate.of(c.x, (c.y + 1) % g.width));

  public Grid(
      final int width,
      final int height,
      final Set<Coordinate> obstacles) {
    this.width = width;
    this.height = height;
    this.obstacles = obstacles;
  }

  public String draw(final Coordinate start, final Coordinate destination) {
    final var result = new StringBuffer();

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
   * Follows list of {@link Direction}s
   *
   * @param directions {@link Direction}s to follow
   * @return Last Coordinate after following directions or just prior to hitting an obstacle
   */
  public Coordinate followDirectionsFrom(final Coordinate from, final List<Direction> directions) {
    return directions
        .scanLeft(Option.of(from), this::followDirectionFrom)
        .takeWhile(o -> !o.isEmpty())
        .last()
        .get();
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
          final Coordinate to = directionNextCoordinateMap.get(direction).get().apply(this, f);

          return obstacles.contains(to)
              ? Option.none()
              : Option.of(to);
        });
  }
}
