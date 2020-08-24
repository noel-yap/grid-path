import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GridExploratoryTest {
  public static int HEIGHT = 8219;
  public static int WIDTH = HEIGHT + 2;

  final RandomInt horizontalPrng = new RandomInt(WIDTH);
  final RandomInt verticalPrng = new RandomInt(HEIGHT);

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
        Direction.UP, (int) Math.sqrt(horizontalPrng.produce() * verticalPrng.produce()),
        Direction.DOWN, (int) Math.sqrt(horizontalPrng.produce() * verticalPrng.produce()),
        Direction.LEFT, (int) Math.sqrt(horizontalPrng.produce() * verticalPrng.produce()),
        Direction.RIGHT, (int) Math.sqrt(horizontalPrng.produce() * verticalPrng.produce()));

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

    assertThat(grid.followDirectionsFrom(Option.of(start), directions))
        .isEqualTo(Option.of(destination));
  }

  final Coordinate randomCoordinate() {
    final int x = verticalPrng.produce();
    final int y = horizontalPrng.produce();

    return Coordinate.of(x, y);
  }

  private static class RandomInt {
    private final MersenneTwister prng = new MersenneTwister();

    private final int maxExclusive;

    public RandomInt(final int maxExclusive) {
      this.maxExclusive = maxExclusive;
    }

    final int produce() {
      return prng.nextInt(maxExclusive);
    }
  }
}
