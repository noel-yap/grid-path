import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class GridExploratoryTest {
  public static int WIDTH = 61;
  public static int HEIGHT = 59;

  public static class RandomInt {
    private final MersenneTwister prng = new MersenneTwister();

    private final int maxExclusive;

    public RandomInt(final int maxExclusive) {
      this.maxExclusive = maxExclusive;
    }

    final int produce() {
      return prng.nextInt(maxExclusive);
    }
  }

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

    final Set<Coordinate> obstacles = List.fill((int) CombinatoricsUtils.binomialCoefficient(WIDTH, HEIGHT), this::randomCoordinate)
        .toSet()
        .filter(c -> !c.equals(start) && !c.equals(destination));
    final var grid = new Grid(
        WIDTH,
        HEIGHT,
        obstacles);

    final var directionLimits = HashMap.of(
        Direction.UP, horizontalPrng.produce() * verticalPrng.produce() / 2 + horizontalPrng.produce() * verticalPrng.produce() / 4,
        Direction.DOWN, horizontalPrng.produce() * verticalPrng.produce() / 2 + horizontalPrng.produce() * verticalPrng.produce() / 4,
        Direction.LEFT, horizontalPrng.produce() * verticalPrng.produce() / 2 + horizontalPrng.produce() * verticalPrng.produce() / 4,
        Direction.RIGHT, horizontalPrng.produce() * verticalPrng.produce() / 2 + horizontalPrng.produce() * verticalPrng.produce() / 4);

    log.info("direction limits = " + directionLimits);
    log.info(grid.draw(start, destination));
    log.info("===");
    log.info("one solution = " + grid.findDirections(start, destination, directionLimits));
  }

  final Coordinate randomCoordinate() {
    final int x = verticalPrng.produce();
    final int y = horizontalPrng.produce();

    return Coordinate.of(x, y);
  }
}
