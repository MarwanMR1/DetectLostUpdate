import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

public class DetectLostUpdateMain {
	public static LogFiles experimentLogs;

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("ERROR: 3 arguments are required and 1 is optional." + System.lineSeparator()
					+ "Number of TPC-C warehouses, Logs directory, [number of threads]");
			System.exit(0);
		}
		int numOfWarehouses = Integer.parseInt(args[0]);
		String logsDir = args[1];
		int numOfThreads = 1;
		if (args.length >= 3) {
			try {
				numOfThreads = Integer.parseInt(args[2]);
			} catch (Exception e) {
				System.out.println(
						"ERROR: Incorrect number of threads (" + args[2] + "). Changing number of threads to 1.");
				numOfThreads = 1;
			}
		}

		System.out.println("Running Detect lost updates\nVersion 0.1");
		System.out.println("Number of TPC-C warehouses: " + numOfWarehouses + ", Number of threads: " + numOfThreads
				+ ", Log directory: " + logsDir);

		long start = System.currentTimeMillis();
		experimentLogs = new LogFiles(logsDir);
		System.out.println("Number of logs: " + experimentLogs.numOfLogs);

		for (int w = 1; w <= numOfWarehouses; w++) {
			for (int d = 1; d <= 10; d++) {
				for (int c = 1; c <= 3000; c++) {
					String key = getCustomerKey(w, d, c);
					HashSet<LogRecord> result = detectLostUpdate(key, experimentLogs.logs.get(key));
					if (!result.isEmpty()) {
						System.out.println(key + ": " + result);
					}
				}
			}
		}
		long end = System.currentTimeMillis();
//		exe.shutdown();
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

	private static String getCustomerKey(int w, int d, int c) {
		return w + "-" + d + "-" + c;
	}

	private static HashSet<LogRecord> detectLostUpdate(String cid, ArrayList<String> logs) {
		OrderedLogs orderedLogs = new OrderedLogs(cid, logs);
		if (!orderedLogs.get(orderedLogs.size() - 1).tName.equals(Util.ORDERSTATUS_ACTION)) {
			System.err.println("ERROR: Last transaction for cutomer " + cid + " is not order status.");
			System.exit(0);
		}
		BigDecimal actualBal = orderedLogs.get(orderedLogs.size() - 1).BalanceRead;

		BigDecimal bal = actualBal;
		LogRecord ti = null, tj = orderedLogs.get(orderedLogs.size() - 1);
		ti = findTi(orderedLogs, tj);
		HashSet<LogRecord> T = new HashSet<>();
		HashSet<LogRecord> TC = new HashSet<>();

		while (ti != null) {
			BigDecimal bStart = new BigDecimal(ti.BalanceRead.doubleValue());
			TC.clear();
			int indexOfTj = orderedLogs.indexOf(tj);
			int indexOfTi = orderedLogs.indexOf(ti);
			for (int p = indexOfTi; p <= indexOfTj; p++) {
				LogRecord tp = orderedLogs.get(p);
				for (int q = p + 1; q <= indexOfTj; q++) {
					LogRecord tq = orderedLogs.get(q);
					if (tp.tName.equals(Util.PAYMENT_ACTION) && tp.overlap(tq)
							&& tp.BalanceRead.compareTo(tq.BalanceRead) == 0) {
						T.add(tp);
						T.add(tq);
					}
					if ((!tp.tName.equals(Util.ORDERSTATUS_ACTION)) && tp.overlap(tq)
							&& (!tq.tName.equals(Util.ORDERSTATUS_ACTION))) {
						TC.add(tp);
						TC.add(tq);
					}
					bStart = bStart.add(tp.BalanceIncrement);
				}
				if (bStart.compareTo(bal) != 0) {
					T.addAll(TC);
				}
				bal = ti.BalanceRead;
				tj = ti;
				ti = findTi(orderedLogs, tj);
			}
		}
		return T;
	}

	private static LogRecord findTi(OrderedLogs orderedLogs, LogRecord tj) {
		int indexOfTj = orderedLogs.indexOf(tj);
		int indexOfTi = indexOfTj - 1;
		long nextTxnStartTime = Long.MAX_VALUE;
		LogRecord ti = orderedLogs.get(indexOfTj - 1);
		changedTi: while (ti != null) {
			if (ti.tName.equals(Util.DELIVERY_ACTION) || (nextTxnStartTime < ti.end)) {
				if (!ti.tName.equals(Util.ORDERSTATUS_ACTION))
					nextTxnStartTime = ti.start;
				ti = orderedLogs.get(indexOfTi - 1);
				continue;
			}
			int curr = indexOfTi - 1;
			if (curr < 0) {
				return ti;
			}
			long diff = ti.start - orderedLogs.get(curr).start;
			while (diff > orderedLogs.longestTransaction) {
				if (!orderedLogs.get(curr).tName.equals(Util.ORDERSTATUS_ACTION)) {
					if (ti.start < orderedLogs.get(curr).end) {
						indexOfTi = curr - 1;
						ti = orderedLogs.get(indexOfTi);
						continue changedTi;
					}
				}
				curr--;
				if (curr < 0) {
					return ti;
				}
				diff = ti.start - orderedLogs.get(curr).start;
			}
			break changedTi;
		}
		return ti;
	}

}
