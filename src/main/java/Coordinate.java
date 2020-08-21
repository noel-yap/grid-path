import lombok.EqualsAndHashCode;

/**
 * 2D Cartesian Coordinate
 */
@EqualsAndHashCode
public class Coordinate {
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

  public int compare(final Coordinate that) {
    final int xCompare = this.x - that.x;
    final int yCompare = this.y - that.y;

    return xCompare != 0
        ? xCompare
        : yCompare;
  }
}