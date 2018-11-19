public class Util {

	public static final String RECORD_ATTRIBUTE_SEPERATOR = ",";
	
	public static final String ENTITY_SEPERATOR = "&";
	public static final String ENTITY_ATTRIBUTE_SEPERATOR = ";";
	
	public static final String PROPERY_SEPERATOR = "#";
	public static final String PROPERY_ATTRIBUTE_SEPERATOR = ":";
	
	public static final String NEW_VALUE_UPDATE = "N";
	public static final String INCREMENT_UPDATE = "I";
	public static final String VALUE_READ = "R";

	public static final String CUSTOMER_BALANCE = "BALANCE";
	
	public static final String ORDERSTATUS_ACTION = "OS";
	public static final String PAYMENT_ACTION = "PA";
	public static final String DELIVERY_ACTION = "DE";

	public enum LogRecordToken {
		Type(0), Name(1), TransactionID(2), StartTime(3), EndTime(4), Entities(5);
		public final int Index;

		LogRecordToken(int index) {
			Index = index;
		}
	}

	public enum EntityToken {
		EntityName(0), EntityID(1), EntityProperties(2);
		public final int Index;

		EntityToken(int index) {
			Index = index;
		}
	}

	public enum PropertyToken {
		PropertyName(0), PropertyValue(1), PropertyAction(2);
		public final int Index;

		PropertyToken(int index) {
			Index = index;
		}
	}

	public static String getIdFromLogString(String log) {
		String id = null;
		try {
			id = log.split(RECORD_ATTRIBUTE_SEPERATOR)[LogRecordToken.TransactionID.Index];
		} catch (NumberFormatException e) {
			e.printStackTrace(System.out);
			System.exit(0);
		}
		return id;
	}

	public static String getEntityIDFromLogString(String log) {
		String id = null;
		try {
			String entities = log.split(RECORD_ATTRIBUTE_SEPERATOR)[LogRecordToken.Entities.Index];
			if(entities.split(ENTITY_SEPERATOR).length > 1) {
				System.err.println("ERROR: log record contain multiple entities\n"+log);
				System.exit(0);
			}
			id = entities.split(ENTITY_ATTRIBUTE_SEPERATOR)[EntityToken.EntityID.Index];
		} catch (NumberFormatException e) {
			e.printStackTrace(System.out);
			System.exit(0);
		}
		return id;
	}
}
