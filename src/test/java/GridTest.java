import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.vavr.api.VavrAssertions.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
public class GridTest {
  @Test
  @DisplayName("Should follow up.")
  public void shouldFollowUp() {
    final var grid = new Grid(3, 3, HashSet.empty());

    final var from = Coordinate.of(1, 1);

    final Coordinate to = grid.followDirectionFrom(from, Direction.UP);

    Assertions.assertThat(to)
        .isEqualTo(Coordinate.of(1, 0));
  }

  @Test
  @DisplayName("Should wrap up.")
  public void shouldWrapUp() {
    final var grid = new Grid(3, 3, HashSet.empty());

    final var from = Coordinate.of(1, 0);

    final Coordinate to = grid.followDirectionFrom(from, Direction.UP);

    Assertions.assertThat(to)
        .isEqualTo(Coordinate.of(1, 2));
  }

  @Test
  @DisplayName("Should follow down.")
  public void shouldFollowDown() {
    final var grid = new Grid(3, 3, HashSet.empty());

    final var from = Coordinate.of(1, 1);

    final Coordinate to = grid.followDirectionFrom(from, Direction.DOWN);

    Assertions.assertThat(to)
        .isEqualTo(Coordinate.of(1, 2));
  }

  @Test
  @DisplayName("Should wrap down.")
  public void shouldWrapDown() {
    final var grid = new Grid(3, 3, HashSet.empty());

    final var from = Coordinate.of(1, 2);

    final Coordinate to = grid.followDirectionFrom(from, Direction.DOWN);

    Assertions.assertThat(to)
        .isEqualTo(Coordinate.of(1, 0));
  }

  @Test
  @DisplayName("Should follow left.")
  public void shouldFollowLeft() {
    final var grid = new Grid(3, 3, HashSet.empty());

    final var from = Coordinate.of(1, 1);

    final Coordinate to = grid.followDirectionFrom(from, Direction.LEFT);

    Assertions.assertThat(to)
        .isEqualTo(Coordinate.of(0, 1));
  }

  @Test
  @DisplayName("Should wrap left.")
  public void shouldWrapLeft() {
    final var grid = new Grid(3, 3, HashSet.empty());

    final var from = Coordinate.of(0, 1);

    final Coordinate to = grid.followDirectionFrom(from, Direction.LEFT);

    Assertions.assertThat(to)
        .isEqualTo(Coordinate.of(2, 1));
  }

  @Test
  @DisplayName("Should follow right.")
  public void shouldFollowRight() {
    final var grid = new Grid(3, 3, HashSet.empty());

    final var from = Coordinate.of(1, 1);

    final Coordinate to = grid.followDirectionFrom(from, Direction.RIGHT);

    Assertions.assertThat(to)
        .isEqualTo(Coordinate.of(2, 1));
  }

  @Test
  @DisplayName("Should wrap right.")
  public void shouldWrapRight() {
    final var grid = new Grid(3, 3, HashSet.empty());

    final var from = Coordinate.of(2, 1);

    final Coordinate to = grid.followDirectionFrom(from, Direction.RIGHT);

    Assertions.assertThat(to)
        .isEqualTo(Coordinate.of(0, 1));
  }

  @Test
  @DisplayName("Should meet legs.")
  public void shouldMeetLegs() {
    final var expected = HashMap.of(
        Coordinate.of(1, 0), Array.of(
            new Directions(
                List.of(Direction.RIGHT, Direction.UP),
                DirectionLimits.of(1, 1, 1, 0))));

    final var grid = new Grid(2, 2, HashSet.empty());

    final DirectionLimits directionLimits = DirectionLimits.of(1, 1, 1, 1);
    final Map<Coordinate, Array<Directions>> legs0 = HashMap.of(
        Coordinate.of(0, 0), Array.of(new Directions(List.of(Direction.UP), directionLimits)),
        Coordinate.of(1, 1), Array.of(new Directions(List.of(Direction.DOWN), DirectionLimits.empty())));
    final Map<Coordinate, Array<Directions>> legs1 = HashMap.of(
        Coordinate.of(0, 0), Array.of(new Directions(List.of(Direction.LEFT), directionLimits)),
        Coordinate.of(1, 1), Array.of(new Directions(List.of(Direction.RIGHT), DirectionLimits.empty())));

    final Map<Coordinate, Array<Directions>> actual = grid.meet(legs0, legs1);

    Assertions.assertThat(actual)
        .isEqualTo(expected);
  }

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
            Coordinate.of(2, 0)
        ));

    final var start = Coordinate.of(1, 0);

    final var destination = grid.followDirectionsFrom(Option.of(start), List.of(Direction.RIGHT, Direction.RIGHT));

    Assertions.assertThat(destination)
        .isEmpty();
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
    final var start = Coordinate.of(1, 0);
    final var destination = Coordinate.of(3, 4);

    final Grid grid = new Grid(
        5,
        5,
        HashSet.of(
            Coordinate.of(0, 0),
            Coordinate.of(2, 0),
            Coordinate.of(0, 1),
            Coordinate.of(1, 1),
            Coordinate.of(2, 1),
            Coordinate.of(0, 4),
            Coordinate.of(2, 4)));

    final DirectionLimits directionLimits = DirectionLimits.of(2, 2, 3, 2);

    final Option<List<Direction>> actual = grid.findDirections(start, destination, directionLimits);
    assertThat(grid.followDirectionsFrom(Option.of(start), actual.get()))
        .isEqualTo(Option.of(destination));
    assertThat(actual.get())
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
