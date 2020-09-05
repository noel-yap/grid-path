import lombok.EqualsAndHashCode;

import java.util.Comparator;

/**
 * 2D Cartesian Coordinate
 */
@EqualsAndHashCode
public class Coordinate implements Comparable<Coordinate> {
  public final int x;
  public final int y;

  public static Coordinate of(final int x, final int y) {
    return new Coordinate(x, y);
  }

  private Coordinate(final int x, final int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return "(" + this.x + ", " + this.y + ")";
  }

  @Override
  public int compareTo(final Coordinate that) {
    final var comparator = Comparator
        .comparing((Coordinate c) -> c.x)
        .thenComparing(c -> c.y);

    return comparator.compare(this, that);
  }
}
