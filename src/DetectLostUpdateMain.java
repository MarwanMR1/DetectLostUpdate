import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DetectLostUpdateMain {
	public static LogFiles experimentLogs;
	private static Queue<String> keys;

	private static int numOfWarehouses;
	private static int numOfThreads;
	

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("ERROR: 3 arguments are required and 1 is optional." + System.lineSeparator()
					+ "Number of TPC-C warehouses, Logs directory, [number of threads]");
			System.exit(0);
		}
		numOfWarehouses = Integer.parseInt(args[0]);
		String logsDir = args[1];
		numOfThreads = 1;
		if (args.length >= 3) {
			try {
				numOfThreads = Integer.parseInt(args[2]);
			} catch (Exception e) {
				System.out.println(
						"ERROR: Incorrect number of threads (" + args[2] + "). Changing number of threads to 1.");
				numOfThreads = 1;
			}
		}

		System.out.println("Running Detect lost updates\nVersion 0.2");
		System.out.println("Number of TPC-C warehouses: " + numOfWarehouses + ", Number of threads: " + numOfThreads
				+ ", Log directory: " + logsDir);

		long start = System.currentTimeMillis();
		experimentLogs = new LogFiles(logsDir);
		keys = new LinkedList<>();
		System.out.println("Number of logs: " + experimentLogs.numOfLogs);

		runThreads();
		long end = System.currentTimeMillis();
		double time = ((end - start) / 1000.0);
		System.out.println("Duration: " + time + " seconds");
//		System.out.println("Lost updates: " + result.lostUpdate + ", (" + result.lu_1 + ", " + result.lu_2 + ", "
//				+ result.lu_3 + ", " + result.lu_4 + ")");
//		System.out.println("Read skew: " + result.readSkew);
//		System.out.println("Write skew: " + result.writeSkew);
//		System.out.println("Observed updates: " + result.observedUpdate);
//		System.out.println("Dirty reads: " + result.dirtyRead);
//		System.out.println("Dirty writes: " + result.dirtyWrite);
//		System.out.println("Unknown anomaly: " + result.unknown);
//		System.out.println("Total Anomalies: " + result.totalAnomalies);
//		System.out.println("AvgOverlap: " + result.getAvgOverlap());
//		System.out.println("AvgLogs: " + result.getAvgLogs());
//		System.out
//				.println("Total,Logs,Time,Lost Update,Read Skew,Write Skew,Observed Update,Dirty Read,AOT,ARL,Unknown");
//		System.out.println(result.totalAnomalies + "," + experimentLogs.logs.size() + "," + time + ","
//				+ result.lostUpdate + "," + result.readSkew + "," + result.writeSkew + "," + result.observedUpdate + ","
//				+ result.dirtyRead + "," + result.getAvgOverlap() + "," + result.getAvgLogs() + "," + result.unknown);
	}

	private static void runThreads() {
		for (int w = 1; w <= numOfWarehouses; w++) {
			for (int d = 1; d <= 10; d++) {
				for (int c = 1; c <= 3000; c++) {
					keys.add(getCustomerKey(w, d, c));
				}
			}
		}
		ExecutorService exe = Executors.newFixedThreadPool(numOfThreads);
		@SuppressWarnings("rawtypes")
		ArrayList<Future> futures = new ArrayList<>();
		for (int i = 0; i < numOfThreads; i++) {
			futures.add(exe.submit(new DetectingThread()));
		}
		for (@SuppressWarnings("rawtypes")
		Future f : futures) {
			try {
				f.get();
			} catch (InterruptedException e) {
				e.printStackTrace(System.out);
				System.exit(0);
			} catch (ExecutionException e) {
				e.printStackTrace(System.out);
				System.exit(0);
			}
		}
		exe.shutdown();
	}

	private static String getCustomerKey(int w, int d, int c) {
		return w + "-" + d + "-" + c;
	}

	public synchronized static String getKey() {
		if(keys.isEmpty())
			return null;
		return keys.remove();
	}

}
