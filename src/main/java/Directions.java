import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Comparator;

@EqualsAndHashCode
@ToString
public class Directions implements Comparable<Directions> {
  @EqualsAndHashCode.Include
  private final List<Direction> directions; // in reverse for both cpu and memory performance

  public final Map<Direction, Integer> directionLimits;

  public Directions(final List<Direction> directions, final Map<Direction, Integer> directionLimits) {
    this.directions = directions;
    this.directionLimits = directionLimits
        .filterValues(l -> l != 0);
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

  public int size() {
    return directions.size();
  }

  public List<Direction> getDirections() {
    return directions.reverse();
  }

  public Directions append(final Direction direction) {
    return new Directions(
        directions.prepend(direction),
        directionLimits
            .put(direction, directionLimits.get(direction).get() - 1));
  }

  public Directions appendAll(final Directions that) {
    return new Directions(
        this.directions.prependAll(that.directions),
        HashMap.ofEntries(List.of(Direction.values())
            .map(d ->
                Tuple.of(
                    d,
                    this.directionLimits.getOrElse(d, 0) - that.directions.count(d::equals)))));
  }

  public Directions reverse() {
    return new Directions(
        directions
            .map(Direction::opposite)
            .reverse(),
        directionLimits
            .mapKeys(Direction::opposite));
  }

  private int directionCompare(final Directions directions, final Direction direction) {
    return directions.directions.count(direction::equals);
  }
}
