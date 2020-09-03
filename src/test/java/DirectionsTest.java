import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.vavr.api.VavrAssertions.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
public class DirectionsTest {
  @Test
  @DisplayName("Should filter out zero direction limit.")
  public void shouldFilterOutZeroDirectionLimit() {
    final var directions = new Directions(
        List.empty(),
        HashMap.of(
            Direction.UP, 1,
            Direction.DOWN, 0));

    assertThat(directions.directionLimits)
        .isEqualTo(HashMap.of(
            Direction.UP, 1));
  }

  @Test
  @DisplayName("Should include negative direction limit.")
  public void shouldIncludeNegativeDirectionLimit() {
    final var directions = new Directions(
        List.empty(),
        HashMap.of(
            Direction.UP, -1,
            Direction.DOWN, 0));

    assertThat(directions.directionLimits)
        .isEqualTo(HashMap.of(
            Direction.UP, -1));
  }

  @Test
  @DisplayName("Should append direction.")
  public void shouldAppendDirection(final SoftAssertions softly) {
    final var directions = new Directions(
        List.empty(),
        HashMap.of(
            Direction.UP, 1,
            Direction.DOWN, 1));

    final Directions actual = directions.append(Direction.UP);

    softly.assertThat(actual.getDirections())
        .isEqualTo(List.of(Direction.UP));
    softly.assertThat(actual.directionLimits)
        .isEqualTo(HashMap.of(
            Direction.DOWN, 1));
  }

  @Test
  @DisplayName("Should append all directions.")
  public void shouldAppendAllDirections(final SoftAssertions softly) {
    final var directionLimits = HashMap.of(
        Direction.UP, 1,
        Direction.DOWN, 1,
        Direction.LEFT, 1,
        Direction.RIGHT, 1);
    final var directions0 = new Directions(
        List.of(Direction.UP),
        directionLimits);
    final var directions1 = new Directions(
        List.of(Direction.LEFT),
        directionLimits);

    final Directions actual = directions0.appendAll(directions1);

    softly.assertThat(actual.getDirections())
        .isEqualTo(List.of(Direction.UP, Direction.LEFT));
    softly.assertThat(actual.directionLimits)
        .isEqualTo(HashMap.of(
            Direction.UP, 1,
            Direction.DOWN, 1,
            Direction.RIGHT, 1));
  }

  @Test
  @DisplayName("Should wind up with overdraft of direction limits.")
  public void shouldWindUpWithOverdraftOfDirectionLimits(final SoftAssertions softly) {
    final var directions0 = new Directions(
        List.of(Direction.UP),
        HashMap.empty());
    final var directions1 = new Directions(
        List.of(Direction.LEFT),
        HashMap.empty());

    final Directions actual = directions0.appendAll(directions1);

    softly.assertThat(actual.getDirections())
        .isEqualTo(List.of(Direction.UP, Direction.LEFT));
    softly.assertThat(actual.directionLimits)
        .isEqualTo(HashMap.of(
            Direction.LEFT, -1));
  }

  @Test
  @DisplayName("Should reverse directions.")
  public void shouldReverseDirections(final SoftAssertions softly) {
    final var directionLimits = HashMap.of(
        Direction.UP, 1,
        Direction.DOWN, 2,
        Direction.LEFT, 3,
        Direction.RIGHT, 5);
    final var directions = new Directions(
        List.of(Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT),
        directionLimits);

    final Directions actual = directions.reverse();

    softly.assertThat(actual.getDirections())
        .isEqualTo(List.of(Direction.DOWN, Direction.RIGHT, Direction.UP, Direction.LEFT));
    softly.assertThat(actual.directionLimits)
        .isEqualTo(HashMap.of(
            Direction.UP, 2,
            Direction.DOWN, 1,
            Direction.LEFT, 5,
            Direction.RIGHT, 3));
  }
}
