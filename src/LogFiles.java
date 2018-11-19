
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LogFiles {

	int numOfLogs;
	HashMap<String, ArrayList<String>> logs;

	public LogFiles(String dirPath) {
		numOfLogs = 0;
		File dir = new File(dirPath);
		logs = new HashMap<String, ArrayList<String>>();
		fetchLogs(dir.listFiles(), logs);
	}

	private void fetchLogs(File[] files, HashMap<String, ArrayList<String>> logList) {
		for (File f : files) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = br.readLine()) != null) {
					numOfLogs++;
					String id = Util.getEntityIDFromLogString(line);
					if (id != null) {
						if (!logList.containsKey(id))
							logList.put(id, new ArrayList<String>());
						logList.get(id).add(line);
					} else {
						ArrayList<String> ids = Util.getEntityIDArrayFromLogString(line);
						for (String i : ids) {
							if (!logList.containsKey(i))
								logList.put(i, new ArrayList<String>());
							logList.get(i).add(line);
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace(System.out);
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace(System.out);
				System.exit(0);
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
					System.exit(0);
				}
			}
		}
	}
}
