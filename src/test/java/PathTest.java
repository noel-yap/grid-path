import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.vavr.api.VavrAssertions.assertThat;

public class PathTest {
  @Test
  @DisplayName("Should have exactly one next path.")
  public void shouldHaveExactlyOneNextPath() {
    final var grid = new Grid(
        5,
        5,
        HashSet.of(
            Coordinate.of(0, 0),
            Coordinate.of(0, 2),
            Coordinate.of(1, 1)));

    final var expected = List.of(new Path(
        grid,
        List.of(
            Coordinate.of(0, 1),
            Coordinate.of(4, 1))
            .reverse(),
        HashMap.of(
            Direction.DOWN, 1,
            Direction.LEFT, 1,
            Direction.RIGHT, 1),
        List.of(Direction.UP).reverse()
    ));

    final Path path = Path
        .onGrid(grid)
        .startAt(Coordinate.of(0, 1))
        .withDirectionLimits(HashMap.of(
            Direction.UP, 1,
            Direction.DOWN, 1,
            Direction.LEFT, 1,
            Direction.RIGHT, 1));

    assertThat(path.nextPaths())
        .isEqualTo(expected);
  }

  @Test
  @DisplayName("Should have no next paths.")
  public void shouldHaveNoNextPaths() {
    final var grid = new Grid(
        5,
        5,
        HashSet.of(
            Coordinate.of(0, 0),
            Coordinate.of(0, 2),
            Coordinate.of(1, 1),
            Coordinate.of(4, 1)));

    final Path path = Path
        .onGrid(grid)
        .startAt(Coordinate.of(0, 1));

    assertThat(path.nextPaths())
        .isEmpty();
  }

  @Test
  @DisplayName("Should find paths to destination.")
  public void shouldFindPathToDestination() {
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

    final Path expected = new Path(
        grid,
        List.of(
            start,
            Coordinate.of(4, 1),
            Coordinate.of(3, 1),
            Coordinate.of(3, 2),
            Coordinate.of(3, 3),
            destination)
            .reverse(),
        HashMap.of(
            Direction.RIGHT, 1),
        List.of(
            Direction.UP,
            Direction.UP,
            Direction.RIGHT,
            Direction.RIGHT,
            Direction.DOWN)
            .reverse()
    );

    final Path path = Path
        .onGrid(grid)
        .startAt(start)
        .withDirectionLimits(HashMap.of(
            Direction.UP, 2,
            Direction.DOWN, 1,
            Direction.RIGHT, 3));

    assertThat(path.findPathsToDestination(destination))
        .contains(expected);
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

    final Path path = Path
        .onGrid(grid)
        .startAt(start)
        .withDirectionLimits(HashMap.of(
            Direction.UP, 2,
            Direction.DOWN, 2,
            Direction.LEFT, 3,
            Direction.RIGHT, 2));

    assertThat(path.findDirectionsToDestination(destination))
        .containsAnyOf(
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
