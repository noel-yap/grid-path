import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GridExploratoryTest {
  public static int HEIGHT = 16451;
  public static int WIDTH = HEIGHT + 2;

  final RandomInt horizontalPrng = new RandomInt(WIDTH);
  final RandomInt verticalPrng = new RandomInt(HEIGHT);

  @Disabled
  @Test
  @DisplayName("Should draw grid.")
  public void shouldDrawGrid() {
    final var grid = new Grid(5, 5, HashSet.empty());

    final var start = randomCoordinate(5, 5);
    final var destination = randomCoordinate(5, 5);

    System.out.println(grid.draw(start, destination));
  }

  /*
   - WIDTH×HEIGHT Grid
   -   Random obstacles
   - Random starting coordinate
   -   Random available steps
   - Random destination coordinate
   */
  @Test
  @DisplayName("Should find directions to destination.")
  public void shouldFindDirectionsToDestination() {
    final Coordinate start = randomCoordinate();
    final Coordinate destination = randomCoordinate();

    assertThat(start)
        .isNotEqualTo(destination);

    final var directionLimits = HashMap.of(
        Direction.UP, horizontalPrng.produce() * verticalPrng.produce() / 16,
        Direction.DOWN, horizontalPrng.produce() * verticalPrng.produce() / 16,
        Direction.LEFT, horizontalPrng.produce() * verticalPrng.produce() / 16,
        Direction.RIGHT, horizontalPrng.produce() * verticalPrng.produce() / 16);

    System.out.println("direction limits = " + directionLimits);

    final Set<Coordinate> obstacles = HashSet
        .fill((int) CombinatoricsUtils.binomialCoefficient(WIDTH, HEIGHT), this::randomCoordinate)
        .filterNot(c -> c.equals(start) || c.equals(destination));
    final var grid = new Grid(
        WIDTH,
        HEIGHT,
        obstacles);

    System.out.println(grid.draw(start, destination));

    System.out.println("===");

    final List<Direction> directions = grid.findDirections(start, destination, directionLimits);
    System.out.println("one solution = " + directions);
    System.out.println("number of steps = " + directions.size());

    final Option<Coordinate> endOption = grid.followDirectionsFrom(Option.of(start), directions);
    assertThat(endOption)
        .isNotEmpty();

    final Coordinate end = endOption.get();
    assertThat(end)
        .as(
            "Obstacles near end are:\n"
                + obstacles
                .filter(o -> o.x - end.x < 2 && o.y - end.y < 2)
                .toList()
                .map(Coordinate::toString)
                .intersperse(", ")
                + "\n"
                + "Direction limits are: "
                + directionLimits)
        .isIn(start, destination);
  }

  private Coordinate randomCoordinate(final int width, final int height) {
    final int x = verticalPrng.produce(height);
    final int y = horizontalPrng.produce(width);

    return Coordinate.of(x, y);
  }

  private Coordinate randomCoordinate() {
    final int x = verticalPrng.produce(HEIGHT);
    final int y = horizontalPrng.produce(WIDTH);

    return Coordinate.of(x, y);
  }

  private static class RandomInt {
    private final MersenneTwister prng = new MersenneTwister();

    private final int maxExclusive;

    public RandomInt(final int maxExclusive) {
      this.maxExclusive = maxExclusive;
    }

    public int produce() {
      return produce(this.maxExclusive);
    }

    public int produce(final int maxExclusive) {
      return prng.nextInt(maxExclusive);
    }
  }
}
