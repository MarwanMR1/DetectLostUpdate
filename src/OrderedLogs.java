import java.util.ArrayList;
import java.util.Collections;

public class OrderedLogs {
	long longestTransaction;
	private ArrayList<LogRecord> oLogs;
	public OrderedLogs(ArrayList<String> logs) {
		longestTransaction = -1;
		oLogs = new ArrayList<LogRecord>();
		for(String l : logs) {
			LogRecord r = new LogRecord(l);
			long duration = r.end - r.start;
			if(duration > longestTransaction)
				longestTransaction = duration;
			oLogs.add(r);
		}
		Collections.sort(oLogs);
	}
	
	public LogRecord get(int index) {
		return oLogs.get(index);
	}
	
	public int indexOf(LogRecord r) {
		return oLogs.indexOf(r);
	}
	
	public int size() {
		return oLogs.size();
	}

}
