import io.vavr.collection.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Comparator;

@EqualsAndHashCode
@ToString
public class Directions implements Comparable<Directions> {
  private final List<Direction> directions; // in reverse for both cpu and memory performance

  public final DirectionLimits directionLimits;

  public Directions(final List<Direction> directions, final DirectionLimits directionLimits) {
    this.directions = directions;
    this.directionLimits = directionLimits;
  }

  @Override
  public int compareTo(final Directions that) {
    final var comparator = Comparator
        .comparing((Directions d) -> directionCompare(d, Direction.UP))
        .thenComparing(d -> directionCompare(d, Direction.DOWN))
        .thenComparing(d -> directionCompare(d, Direction.LEFT))
        .thenComparing(d -> directionCompare(d, Direction.RIGHT));

    return comparator.compare(this, that);
  }

  public boolean isEmpty() {
    return directions.isEmpty();
  }

  public Direction last() {
    return directions.head();
  }

  public Direction secondToLast() {
    return directions.get(1);
  }

  public int size() {
    return directions.size();
  }

  public List<Direction> getDirections() {
    return directions.reverse();
  }

  public Directions append(final Direction direction) {
    return new Directions(
        directions.prepend(direction),
        directionLimits.decrementDirectionLimit(direction));
  }

  public Directions appendAll(final Directions that) {
    return new Directions(
        this.directions.prependAll(that.directions),
        this.directionLimits.decrementDirectionLimitsBy(that.directions));
  }

  public Directions reverse() {
    return new Directions(
        directions
            .map(Direction::opposite)
            .reverse(),
        directionLimits.opposite());
  }

  private int directionCompare(final Directions directions, final Direction direction) {
    return directions.directions.count(direction::equals);
  }
}
