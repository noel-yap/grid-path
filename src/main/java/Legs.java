import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;

/**
 * Legs to or from coordinates
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Legs {
  @EqualsAndHashCode.Include
  public final Map<Coordinate, Array<Directions>> legs;

  public final Array<Coordinate> priorEnds;

  public Legs(final Coordinate coordinate, final DirectionLimits directionLimits) {
    this(
        HashMap.of(
            coordinate,
            Array.of(new Directions(List.empty(), directionLimits))));
  }

  public Legs(final Map<Coordinate, Array<Directions>> legs) {
    this(legs, Array.empty());
  }

  public Legs(
      final Map<Coordinate, Array<Directions>> legs,
      final Array<Coordinate> priorEnds) {
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
            .toStream() // TODO: See what happens to memory use if this is removed.
            .flatMap(legsEntry -> legsEntry._2
                .flatMap(directions -> directions
                    .directionLimits
                    .getAvailable()
                    .filter(direction -> {
                      // don't double back or go over steps covered by other legs
                      return (directions.isEmpty() || !direction.equals(directions.last().opposite()))
                          && (directions.size() <= 1 || !direction.equals(directions.secondToLast().opposite()));
                    })
                    .flatMap(direction -> grid.followDirectionFrom(Option.of(legsEntry._1), direction)
                        .filterNot(priorEnds::contains) // don't go over steps covered by other legs
                        .map(nextCoordinate -> {
                          final var nextDirections = directions.append(direction);

                          return Tuple.of(nextCoordinate, nextDirections);
                        })))
                .groupBy(Tuple2::_1)
                .mapValues(t2 -> t2.map(Tuple2::_2))));

    return new Legs(
        nextLegs,
        Array.ofAll(legs.keySet()));
  }
}
