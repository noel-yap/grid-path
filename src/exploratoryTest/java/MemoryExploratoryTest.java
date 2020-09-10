import com.sun.management.HotSpotDiagnosticMXBean;
import io.vavr.collection.Array;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MemoryExploratoryTest {
  @Test
  @DisplayName("Explore data structure memory sizes.")
  public void exploreDataStructureMemorySizes() throws Exception {
    final var directions = new Directions(List.empty(), DirectionLimits.empty());

    Array.fill(65536, directions);
    List.fill(65536, directions);
    HashSet.fill(65536, () -> directions);
    TreeSet.fill(65536, () -> directions);

    dumpHeap();
  }

  private static void dumpHeap() throws IOException {
    final long pid = ProcessHandle.current().pid();
    final var dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    final String filePath = dateFormat.format(new Date()) + "_" + pid + ".hprof";

    final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    final HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
        server,
        "com.sun.management:type=HotSpotDiagnostic",
        HotSpotDiagnosticMXBean.class);

    mxBean.dumpHeap(filePath, false);
  }
}
