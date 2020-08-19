import lombok.EqualsAndHashCode;

/**
 * 2D Cartesian Coordinate
 */
@EqualsAndHashCode
public class Coordinate {
  public final byte x;
  public final byte y;

  public static Coordinate of(final int x, final int y) {
    return new Coordinate(x, y);
  }

  private Coordinate(final int x, final int y) {
    this.x = (byte) x;
    this.y = (byte) y;
  }

  @Override
  public String toString() {
    return "(" + this.x + ", " + this.y + ")";
  }
}
