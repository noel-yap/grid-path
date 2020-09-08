import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
public class LegsTest {
  @Test
  @DisplayName("Should calculate size.")
  public void shouldCalculateSize() {
    final Legs legs = new Legs(
        HashMap.of(
            Coordinate.of(0, 0),
            Array.of(new Directions(List.of(Direction.UP), DirectionLimits.of(1, 0, 0, 0))),
            Coordinate.of(1, 1),
            Array.of(
                new Directions(List.of(Direction.UP), DirectionLimits.of(1, 0, 0, 0)),
                new Directions(List.of(Direction.DOWN), DirectionLimits.of(0, 1, 0, 0)),
                new Directions(List.of(Direction.LEFT), DirectionLimits.of(0, 0, 1, 0)),
                new Directions(List.of(Direction.RIGHT), DirectionLimits.of(0, 0, 0, 1)))));

    assertThat(legs.legs.size())
        .isEqualTo(2);
  }

  @Test
  @DisplayName("Should get next paths.")
  public void shouldGetNextPaths(final SoftAssertions softly) {
    final var grid = new Grid(
        2,
        2,
        HashSet.empty());

    final var directionLimits = DirectionLimits.of(1, 1, 0, 0);
    final var legs = new Legs(Coordinate.of(0, 0), directionLimits);

    final Legs firstPassLegs = legs.nextPaths(grid);

    softly.assertThat(firstPassLegs.legs.keySet())
        .containsExactly(Coordinate.of(0, 1));
    softly.assertThat(firstPassLegs.legs.values().head())
        .containsExactlyInAnyOrder(
            new Directions(
                List.of(Direction.UP),
                DirectionLimits.of(0, 1, 0, 0)),
            new Directions(
                List.of(Direction.DOWN),
                DirectionLimits.of(1, 0, 0, 0)));
  }

  @Test
  @DisplayName("Should not double-back.")
  public void shouldNotDoubleBack(final SoftAssertions softly) {
    final var grid = new Grid(
        2,
        2,
        HashSet.empty());

    final var directionLimits = DirectionLimits.of(1, 1, 1, 0);
    final var legs = new Legs(Coordinate.of(0, 0), directionLimits);

    final Legs firstPassLegs = legs.nextPaths(grid);
    final Legs secondPassLegs = firstPassLegs.nextPaths(grid);

    softly.assertThat(secondPassLegs.legs.keySet())
        .containsExactly(Coordinate.of(1, 1));
    softly.assertThat(secondPassLegs.legs.values().head())
        .containsExactlyInAnyOrder(
            new Directions(
                List.of(Direction.LEFT, Direction.UP),
                DirectionLimits.of(0, 1, 0, 0)),
            new Directions(
                List.of(Direction.LEFT, Direction.DOWN),
                DirectionLimits.of(1, 0, 0, 0)));
  }
}
