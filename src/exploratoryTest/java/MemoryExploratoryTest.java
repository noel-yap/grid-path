import io.vavr.collection.Array;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MemoryExploratoryTest {
  @Test
  @DisplayName("Explore data structure memory sizes.")
  public void exploreDataStructureMemorySizes() throws Exception {
    final var directions = new Directions(List.empty(), DirectionLimits.empty());

    Array.fill(65536, directions);
    List.fill(65536, directions);
    HashSet.fill(65536, () -> directions);
    TreeSet.fill(65536, () -> directions);

    ExploratoryTestUtils.dumpHeap();
  }
}
