import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeSet;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;

/**
 * Legs to or from coordinates
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Legs {
  @EqualsAndHashCode.Include
  public final Map<Coordinate, SortedSet<Directions>> legs;

  public final Set<Coordinate> priorEnds;

  public Legs(final Coordinate coordinate, final Map<Direction, Integer> directionLimits) {
    this(
        HashMap.of(
            coordinate,
            TreeSet.of(Directions::compareTo, new Directions(List.empty(), directionLimits))));
  }

  public Legs(final Map<Coordinate, SortedSet<Directions>> legs) {
    this(legs, legs.keySet());
  }

  public Legs(
      final Map<Coordinate, SortedSet<Directions>> legs,
      final Set<Coordinate> priorEnds) {
    this.legs = legs;
    this.priorEnds = priorEnds;
  }

  @Override
  public String toString() {
    return legs.toString();
  }

  public boolean isEmpty() {
    return legs.isEmpty();
  }

  /**
   * Total number of legs
   *
   * @return Total number of legs
   */
  public Number size() {
    return legs.values().map(Set::size).sum();
  }

  public Legs nextPaths(final Grid grid) {
    final Map<Coordinate, SortedSet<Directions>> nextLegs = legs
        .flatMap((fromCoordinate, ds) -> ds
            .flatMap(directions -> directions
                .directionLimits.keySet()
                .toStream()
                .filter(direction -> directions.isEmpty() || !direction.equals(directions.last().opposite()))
                .flatMap(direction -> grid.followDirectionFrom(Option.of(fromCoordinate), direction)
                    .filterNot(priorEnds::contains)
                    .map(nextCoordinate -> Tuple.of(
                        nextCoordinate,
                        directions.append(direction)))))
            .groupBy(t2 -> t2._1)
            .mapValues(s -> s.map(t2 -> t2._2)));

    return new Legs(
        nextLegs,
        legs.keySet());
  }
}
