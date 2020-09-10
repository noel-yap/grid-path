import io.vavr.collection.Array;
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
//  public static int HEIGHT = 5;
//  public static int HEIGHT = 17;
//  public static int HEIGHT = 269;
//  public static int HEIGHT = 4099;
  public static int HEIGHT = 16451;
//  public static int HEIGHT = 65579;
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
  public void shouldFindDirectionsToDestination() throws Exception {
    final Coordinate start = randomCoordinate();
    final Coordinate destination = Coordinate.of(
        (start.x + WIDTH / 2 + horizontalPrng.produce(3) - 1) % WIDTH,
        (start.y + HEIGHT / 2 + verticalPrng.produce(3) - 1) % HEIGHT);

    assertThat(start)
        .isNotEqualTo(destination);

    final var directionLimits = DirectionLimits.of(
        (int) (horizontalPrng.produce() * verticalPrng.produce() / Math.log(WIDTH * HEIGHT) + WIDTH + HEIGHT),
        (int) (horizontalPrng.produce() * verticalPrng.produce() / Math.log(WIDTH * HEIGHT) + WIDTH + HEIGHT),
        (int) (horizontalPrng.produce() * verticalPrng.produce() / Math.log(WIDTH * HEIGHT) + WIDTH + HEIGHT),
        (int) (horizontalPrng.produce() * verticalPrng.produce() / Math.log(WIDTH * HEIGHT) + WIDTH + HEIGHT));

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

    final List<Direction> directions = grid.findDirections(start, destination, directionLimits).getOrElse(List.empty());
    System.out.println("one solution = " + directions);
    System.out.println("number of steps = " + directions.size());
    Array.of(Direction.values())
        .forEach(d -> System.out.println("\t" + d + ": " + directions.count(d::equals)));

    ExploratoryTestUtils.dumpHeap();

    final Option<Coordinate> endOption = grid.followDirectionsFrom(Option.of(start), directions);
    assertThat(endOption)
        .isNotEmpty();

    final Coordinate end = endOption.get();
    assertThat(end)
        .isIn(start, destination);
  }

  private Coordinate randomCoordinate() {
    return randomCoordinate(WIDTH, HEIGHT);
  }

  private Coordinate randomCoordinate(final int width, final int height) {
    final int x = horizontalPrng.produce(width);
    final int y = verticalPrng.produce(height);

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
