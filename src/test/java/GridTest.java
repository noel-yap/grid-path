import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.vavr.api.VavrAssertions.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
public class GridTest {
  /*
     - Starting coordinate (0, 1)
     - Obstacle at (0, 2)
     - Steps: [R, R, D]
     ┌───┬───┬───┬───┬───┐
     │   │ S → X │   │   │
     ├───┼───┼───┼───┼───┤
     │   │   │   │   │   │
     ├───┼───┼───┼───┼───┤
     │   │   │   │   │   │
     ├───┼───┼───┼───┼───┤
     │   │   │   │   │   │
     ├───┼───┼───┼───┼───┤
     │   │   │   │   │   │
     └───┴───┴───┴───┴───┘
     Answer: (0, 1)
   */
  @Test
  @DisplayName("Should be blocked by obstacles.")
  public void shouldBeBlockedByObstacles() {
    final Grid grid = new Grid(
        5,
        5,
        HashSet.of(
            Coordinate.of(0, 2)
        ));

    final var start = Coordinate.of(0, 1);

    final var destination = grid.followDirectionFrom(Option.of(start), Direction.RIGHT);

    Assertions.assertThat(destination)
        .isEmpty();
  }

  @Test
  @DisplayName("Should find path.")
  public void shouldFindPath(final SoftAssertions softly) {
    final Grid grid = new Grid(
        5,
        5,
        HashSet.of(
            Coordinate.of(0, 0),
            Coordinate.of(0, 2),
            Coordinate.of(1, 1),
            Coordinate.of(4, 0),
            Coordinate.of(4, 2)));

    final Coordinate start = Coordinate.of(0, 1);
    final Coordinate destination = Coordinate.of(4, 3);

    final HashMap<Direction, Integer> directionLimits = HashMap.of(
        Direction.UP, 2,
        Direction.DOWN, 1,
        Direction.RIGHT, 3);

    final Path actual = grid.findPath(start, destination, directionLimits).get();

    softly.assertThat(actual.head().equals(start));
    softly.assertThat(actual.last().equals(destination));
  }

  /*
   - 5x5 Grid
   - Starting coordinate (0, 1)
   - Destination coordinate (4, 3)
   - Available Steps: [U, D, U, R, R, D, L, L, L]
   ┌───┬─↑─┬───┬───┬───┐
   │ X │ S │ X │   │   │
   ├───┼───┼───┼───┼───┤
   │ X │ X │ X │   │   │
   ├───┼───┼───┼───┼───┤
   │   │   │   │   │   │
   ├───┼───┼───┼───┼───┤
   │   │ . → . → . │   │
   ├───┼─↑─┼───┼─↓─┼───┤
   │ X │ . │ X │ D │   │
   └───┴───┴───┴───┴───┘
   Possible answer: [U, U, R, R, D]
   Possible answer: [U, U, L, L, L, D]
   */
  @Test
  @DisplayName("Should find directions to destination.")
  public void shouldFindDirectionsToDestination() {
    final var start = Coordinate.of(0, 1);
    final var destination = Coordinate.of(4, 3);

    final Grid grid = new Grid(
        5,
        5,
        HashSet.of(
            Coordinate.of(0, 0),
            Coordinate.of(0, 2),
            Coordinate.of(1, 0),
            Coordinate.of(1, 1),
            Coordinate.of(1, 2),
            Coordinate.of(4, 0),
            Coordinate.of(4, 2)));

    final HashMap<Direction, Integer> directionLimits = HashMap.of(
        Direction.UP, 2,
        Direction.DOWN, 2,
        Direction.LEFT, 3,
        Direction.RIGHT, 2);

    final List<Direction> actual = grid.findDirections(start, destination, directionLimits);

    assertThat(actual)
        .isIn(
            List.of(
                Direction.UP,
                Direction.UP,
                Direction.RIGHT,
                Direction.RIGHT,
                Direction.DOWN
            ),
            List.of(
                Direction.UP,
                Direction.UP,
                Direction.LEFT,
                Direction.LEFT,
                Direction.LEFT,
                Direction.DOWN),
            List.of(
                Direction.UP,
                Direction.UP,
                Direction.LEFT,
                Direction.LEFT,
                Direction.DOWN,
                Direction.LEFT));
  }
}
