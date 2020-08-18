import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.vavr.api.VavrAssertions.assertThat;

public class PathExploratoryTest {
  public static int WIDTH = 13;
  public static int HEIGHT = 11;

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
   - 11x13 Grid
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

    final Set<Coordinate> obstacles = List.fill((int) CombinatoricsUtils.binomialCoefficient(WIDTH, HEIGHT), () -> randomCoordinate())
        .toSet()
        .filter(c -> !c.equals(start) && !c.equals(destination));
    final var grid = new Grid(
        WIDTH,
        HEIGHT,
        obstacles);

    final var directionLimits = HashMap.of(
        Direction.UP, horizontalPrng.produce() * verticalPrng.produce() / 4,
        Direction.DOWN, horizontalPrng.produce() * verticalPrng.produce() / 4,
        Direction.LEFT, horizontalPrng.produce() * verticalPrng.produce() / 4,
        Direction.RIGHT, horizontalPrng.produce() * verticalPrng.produce() / 4);
    final var path = Path
        .onGrid(grid)
        .startAt(start)
        .withDirectionLimits(directionLimits);

    System.out.println("direction limits = " + directionLimits);
    System.out.println(grid.draw(start, destination));
    System.out.println("===");
    System.out.println("solutions = " + path.findDirectionsToDestination(destination));
  }

  final Coordinate randomCoordinate() {
    return Coordinate.of(verticalPrng.produce(), horizontalPrng.produce());
  }
}
