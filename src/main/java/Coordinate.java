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

  private Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return "(" + this.x + ", " + this.y + ")";
  }
}
