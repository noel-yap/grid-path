import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class DirectionLimits {
  public int upLimit;
  public int downLimit;
  public int leftLimit;
  public int rightLimit;

  public static DirectionLimits empty() {
    return DirectionLimits.of(0, 0, 0, 0);
  }

  public static DirectionLimits of(
      final int upLimit,
      final int downLimit,
      final int leftLimit,
      final int rightLimit) {
    return new DirectionLimits(upLimit, downLimit, leftLimit, rightLimit);
  }

  public DirectionLimits(
      final int upLimit,
      final int downLimit,
      final int leftLimit,
      final int rightLimit) {
    this.upLimit = upLimit;
    this.downLimit = downLimit;
    this.leftLimit = leftLimit;
    this.rightLimit = rightLimit;
  }

  public boolean isValid() {
    return upLimit > -1
        && downLimit > -1
        && leftLimit > -1
        && rightLimit > -1;
  }

  public int get(final Direction direction) {
    return switch (direction) {
      case UP -> upLimit;
      case DOWN -> downLimit;
      case LEFT -> leftLimit;
      case RIGHT -> rightLimit;
    };
  }

  public Stream<Direction> getAvailable() {
    return Stream.of(Direction.values())
            .filter(d -> get(d) > 0);
  }

  public DirectionLimits decrementUpLimit() {
    return new DirectionLimits(upLimit - 1, downLimit, leftLimit, rightLimit);
  }

  public DirectionLimits decrementDownLimit() {
    return new DirectionLimits(upLimit, downLimit - 1, leftLimit, rightLimit);
  }

  public DirectionLimits decrementLeftLimit() {
    return new DirectionLimits(upLimit, downLimit, leftLimit - 1, rightLimit);
  }

  public DirectionLimits decrementRightLimit() {
    return new DirectionLimits(upLimit, downLimit, leftLimit, rightLimit - 1);
  }

  public DirectionLimits decrementDirectionLimit(final Direction direction) {
    return switch (direction) {
      case UP -> decrementUpLimit();
      case DOWN -> decrementDownLimit();
      case LEFT -> decrementLeftLimit();
      case RIGHT -> decrementRightLimit();
    };
  }

  public DirectionLimits decrementDirectionLimitsBy(final List<Direction> directions) {
    return new DirectionLimits(
        upLimit - directions.count(Direction.UP::equals),
        downLimit - directions.count(Direction.DOWN::equals),
        leftLimit - directions.count(Direction.LEFT::equals),
        rightLimit - directions.count(Direction.RIGHT::equals));
  }

  public DirectionLimits opposite() {
    return new DirectionLimits(downLimit, upLimit, rightLimit, leftLimit);
  }
}
