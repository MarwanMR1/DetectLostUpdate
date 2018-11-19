import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

public class DetectingThread implements Runnable {

	@Override
	public void run() {
		String key = DetectLostUpdateMain.getKey();
		while (key != null) {
			ArrayList<HashSet<LogRecord>> result = detectLostUpdate(key,
					DetectLostUpdateMain.experimentLogs.logs.get(key));
			if (!result.isEmpty()) {
				String toPrint = "";
				for (HashSet<LogRecord> hs : result) {
					toPrint += "#" + hs;
				}
				System.out.println(key  + toPrint);
			}
			key = DetectLostUpdateMain.getKey();
		}
	}

	private ArrayList<HashSet<LogRecord>> detectLostUpdate(String cid, ArrayList<String> logs) {
		OrderedLogs orderedLogs = new OrderedLogs(cid, logs);
		if (!orderedLogs.get(orderedLogs.size() - 1).tName.equals(Util.ORDERSTATUS_ACTION)) {
			System.err.println("ERROR: Last transaction for cutomer " + cid + " is not order status.");
			System.exit(0);
		}
		BigDecimal actualBal = orderedLogs.get(orderedLogs.size() - 1).BalanceRead;

		BigDecimal bal = actualBal;
		LogRecord ti = null, tj = orderedLogs.get(orderedLogs.size() - 1);
		ti = findTi(orderedLogs, tj);
		ArrayList<HashSet<LogRecord>> T = new ArrayList<HashSet<LogRecord>>();
//		HashSet<LogRecord> TC = new HashSet<>();
//		HashSet<LogRecord> TD = new HashSet<>();

		while (ti != null) {
			BigDecimal bStart = new BigDecimal(ti.BalanceRead.toString());
			HashSet<LogRecord> TC = new HashSet<>();
			HashSet<LogRecord> TD = new HashSet<>();
			int indexOfTj = orderedLogs.indexOf(tj);
			int indexOfTi = orderedLogs.indexOf(ti);
			for (int p = indexOfTi; p < indexOfTj; p++) {
				LogRecord tp = orderedLogs.get(p);
				if (tp.tName.equals(Util.ORDERSTATUS_ACTION)) {
					continue;
				}
				for (int q = p + 1; q < indexOfTj; q++) {
					LogRecord tq = orderedLogs.get(q);
					if (tq.tName.equals(Util.ORDERSTATUS_ACTION)) {
						continue;
					}
					if (tp.tName.equals(Util.PAYMENT_ACTION) && tp.overlap(tq)
							&& tp.BalanceRead.compareTo(tq.BalanceRead) == 0) {
						TD.add(tp);
						TD.add(tq);
					}
					if ((!tp.tName.equals(Util.ORDERSTATUS_ACTION)) && tp.overlap(tq)
							&& (!tq.tName.equals(Util.ORDERSTATUS_ACTION))) {
						TC.add(tp);
						TC.add(tq);
					}
				}
				bStart = bStart.add(tp.BalanceIncrement);
			}
			if (bStart.compareTo(bal) != 0) {
				TD.addAll(TC);
			}
			if (!TD.isEmpty()) {
				T.add(TD);
			}
			bal = ti.BalanceRead;
			tj = ti;
			ti = findTi(orderedLogs, tj);
		}
		return T;
	}

	private LogRecord findTi(OrderedLogs orderedLogs, LogRecord tj) {
		int indexOfTj = orderedLogs.indexOf(tj);
		int indexOfTi = indexOfTj - 1;
		long nextTxnStartTime = Long.MAX_VALUE;
		LogRecord ti = orderedLogs.get(indexOfTj - 1);
		changedTi: while (ti != null) {
			if (ti.tName.equals(Util.DELIVERY_ACTION) || (nextTxnStartTime < ti.end)) {
				if (!ti.tName.equals(Util.ORDERSTATUS_ACTION))
					nextTxnStartTime = ti.start;
				indexOfTi--;
				ti = orderedLogs.get(indexOfTi);
				continue;
			}
			int curr = indexOfTi - 1;
			if (curr < 0) {
				return ti;
			}
			long diff = ti.start - orderedLogs.get(curr).start;
			while (diff < orderedLogs.longestTransaction) {
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
