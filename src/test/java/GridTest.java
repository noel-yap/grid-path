import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.vavr.api.VavrAssertions.assertThat;

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
    final List<Direction> directions = List.of(
        Direction.RIGHT,
        Direction.RIGHT,
        Direction.DOWN);

    final var destination = grid.followDirectionsFrom(start, directions);

    Assertions.assertThat(destination)
        .isEqualTo(Coordinate.of(0, 1));
  }
}
