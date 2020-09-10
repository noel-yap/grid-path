import com.sun.management.HotSpotDiagnosticMXBean;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExploratoryTestUtils {
  public static void dumpHeap() throws IOException {
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
