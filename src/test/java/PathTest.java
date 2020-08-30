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
        Coordinate.of(0, 1),
        Coordinate.of(4, 1),
        HashMap.of(
            Direction.DOWN, 1,
            Direction.LEFT, 1,
            Direction.RIGHT, 1),
        List.of(Direction.UP).reverse()
    ));

    final Path path = new Path(
        Coordinate.of(0, 1),
        Coordinate.of(0, 1),
        HashMap.of(
            Direction.UP, 1,
            Direction.DOWN, 1,
            Direction.LEFT, 1,
            Direction.RIGHT, 1));

    assertThat(path.nextPaths(grid).toList())
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

    final Path path = new Path(
        Coordinate.of(0, 1),
        Coordinate.of(0, 1),
        HashMap.empty());

    assertThat(path.nextPaths(grid).toList())
        .isEmpty();
  }
}
