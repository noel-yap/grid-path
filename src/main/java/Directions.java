import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import lombok.EqualsAndHashCode;

import java.util.function.Predicate;

@EqualsAndHashCode
public class Directions implements Comparable<Directions> {
  private final List<Direction> directions; // in reverse for both cpu and memory performance
  public final Map<Direction, Integer> directionLimits;

  public Directions(final List<Direction> directions, final Map<Direction, Integer> directionLimits) {
    this.directions = directions;
    this.directionLimits = directionLimits
        .filterValues(l -> l != 0);
  }

  @Override
  public String toString() {
    return directions.toString();
  }

  @Override
  public int compareTo(final Directions that) {
    final int sizeCompare = that.directions.size() - this.directions.size();
    final int upCompare = directionCompare(that, Direction.UP);
    final int downCompare = directionCompare(that, Direction.DOWN);
    final int leftCompare = directionCompare(that, Direction.LEFT);
    final int rightCompare = directionCompare(that, Direction.RIGHT);

    return sizeCompare != 0
        ? sizeCompare
        : upCompare != 0
        ? upCompare
        : downCompare != 0
        ? downCompare
        : leftCompare != 0
        ? leftCompare
        : rightCompare;
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
            {
              final int dCount = that.directions.count(dThat -> dThat.equals(d));

              return Tuple.of(
                  d,
                  this.directionLimits.getOrElse(d, 0) - dCount);
            })));
  }

  public Directions reverse() {
    return new Directions(
        directions
            .map(Direction::opposite)
            .reverse(),
        directionLimits
            .mapKeys(Direction::opposite));
  }

  private int directionCompare(final Directions that, final Direction d) {
    final Predicate<Direction> directionPredicate = direction -> direction == d;

    return that.directions.count(directionPredicate) - this.directions.count(directionPredicate);
  }
}
