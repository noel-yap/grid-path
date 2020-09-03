import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LegsTest {
  @Test
  @DisplayName("Should calculate size.")
  public void shouldCalculateSize() {
    final Legs legs = new Legs(
        HashMap.of(
            Coordinate.of(0, 0),
            TreeSet.of(new Directions(List.of(Direction.UP), HashMap.of(Direction.UP, 1))),
            Coordinate.of(1, 1),
            TreeSet.of(
                new Directions(List.of(Direction.UP), HashMap.of(Direction.UP, 1)),
                new Directions(List.of(Direction.DOWN), HashMap.of(Direction.DOWN, 1)),
                new Directions(List.of(Direction.LEFT), HashMap.of(Direction.LEFT, 1)),
                new Directions(List.of(Direction.RIGHT), HashMap.of(Direction.RIGHT, 1)))),
        HashSet.of(Coordinate.of(0, 0)));

    assertThat(legs.legs.size())
        .isEqualTo(2);
    assertThat(legs.size())
        .isEqualTo(5L);
  }

  @Test
  @DisplayName("Should get next paths.")
  public void shouldGetNextPaths() {
    final var grid = new Grid(
        2,
        2,
        HashSet.empty());

    final var directionLimits = HashMap.of(
        Direction.UP, 1,
        Direction.DOWN, 1);
    final var legs = new Legs(Coordinate.of(0, 0), directionLimits);

    final Legs firstPassLegs = legs.nextPaths(grid);
    assertThat(firstPassLegs.legs)
        .containsExactlyInAnyOrder(
            Tuple.of(
                Coordinate.of(0, 1),
                TreeSet.of(
                    new Directions(
                        List.of(Direction.UP),
                        HashMap.of(Direction.DOWN, 1)),
                    new Directions(
                        List.of(Direction.DOWN),
                        HashMap.of(Direction.UP, 1)))));
  }
}
