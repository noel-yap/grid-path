import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;

/**
 * Legs to or from coordinates
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Legs {
  @EqualsAndHashCode.Include
  public final Map<Coordinate, Array<Directions>> legs;

  public final Set<Coordinate> priorEnds;

  public Legs(final Coordinate coordinate, final DirectionLimits directionLimits) {
    this(
        HashMap.of(
            coordinate,
            Array.of(new Directions(List.empty(), directionLimits))));
  }

  public Legs(final Map<Coordinate, Array<Directions>> legs) {
    this(legs, legs.keySet());
  }

  public Legs(
      final Map<Coordinate, Array<Directions>> legs,
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

  public Legs nextPaths(final Grid grid) {
    final Map<Coordinate, Array<Directions>> nextLegs = HashMap.ofEntries(
        legs
            .flatMap(legsEntry -> legsEntry._2
                .flatMap(directions -> directions
                    .directionLimits
                    .getAvailable()
                    .filter(direction -> directions.isEmpty() || !direction.equals(directions.last().opposite())) // don't double back
                    .flatMap(direction -> grid.followDirectionFrom(Option.of(legsEntry._1), direction)
                        .filterNot(priorEnds::contains) // don't retrace steps
                        .map(nextCoordinate -> Tuple.of(
                            nextCoordinate,
                            directions.append(direction)))))
                .groupBy(Tuple2::_1)
                .mapValues(t2 -> t2.map(Tuple2::_2))));

    return new Legs(
        nextLegs,
        legs.keySet());
  }
}
