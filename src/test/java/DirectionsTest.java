import io.vavr.collection.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
public class DirectionsTest {
  @Test
  @DisplayName("Should compare directions.")
  public void shouldCompareDirections() {
    final var directionsLhs = new Directions(
        List.of(Direction.UP, Direction.LEFT),
        DirectionLimits.of(0, 1, 0, 0));
    final var directionsRhs = new Directions(
        List.of(Direction.LEFT, Direction.UP),
        DirectionLimits.of(0, 1, 0, 0));

    Assertions.assertThat(directionsLhs)
        .isEqualByComparingTo(directionsRhs);
  }

  @Test
  @DisplayName("Should append direction.")
  public void shouldAppendDirection(final SoftAssertions softly) {
    final var directions = new Directions(
        List.empty(),
        DirectionLimits.of(1, 1, 0, 0));

    final Directions actual = directions.append(Direction.UP);

    softly.assertThat(actual.getDirections())
        .isEqualTo(List.of(Direction.UP));
    softly.assertThat(actual.directionLimits)
        .isEqualTo(DirectionLimits.of(0, 1, 0, 0));
  }

  @Test
  @DisplayName("Should append all directions.")
  public void shouldAppendAllDirections(final SoftAssertions softly) {
    final var directionLimits = DirectionLimits.of(1, 1, 1, 1);
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
        .isEqualTo(DirectionLimits.of(1, 1, 0, 1));
  }

  @Test
  @DisplayName("Should wind up with overdraft of direction limits.")
  public void shouldWindUpWithOverdraftOfDirectionLimits(final SoftAssertions softly) {
    final var directions0 = new Directions(
        List.of(Direction.UP),
        DirectionLimits.empty());
    final var directions1 = new Directions(
        List.of(Direction.LEFT),
        DirectionLimits.empty());

    final Directions actual = directions0.appendAll(directions1);

    softly.assertThat(actual.getDirections())
        .isEqualTo(List.of(Direction.UP, Direction.LEFT));
    softly.assertThat(actual.directionLimits)
        .isEqualTo(DirectionLimits.of(0, 0, -1, 0));
  }

  @Test
  @DisplayName("Should reverse directions.")
  public void shouldReverseDirections(final SoftAssertions softly) {
    final var directionLimits = DirectionLimits.of(1, 2, 3, 5);
    final var directions = new Directions(
        List.of(Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT),
        directionLimits);

    final Directions actual = directions.reverse();

    softly.assertThat(actual.getDirections())
        .isEqualTo(List.of(Direction.DOWN, Direction.RIGHT, Direction.UP, Direction.LEFT));
    softly.assertThat(actual.directionLimits)
        .isEqualTo(DirectionLimits.of(2, 1, 5, 3));
  }
}
