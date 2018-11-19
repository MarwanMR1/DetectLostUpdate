import java.math.BigDecimal;

public class LogRecord implements Comparable<LogRecord> {
	public String line;
	public String tId;
	public String tName;
	public long start;
	public long end;

	public String eId;
	public String eName;

	public BigDecimal BalanceRead;
	public BigDecimal BalanceUpdate;
	public BigDecimal BalanceIncrement;

	public LogRecord(String custid, String line) {
		this.line = line;
		String[] tokens = line.split(Util.RECORD_ATTRIBUTE_SEPERATOR);
		tId = tokens[Util.LogRecordToken.TransactionID.Index];
		start = Long.parseLong(tokens[Util.LogRecordToken.StartTime.Index]);
		end = Long.parseLong(tokens[Util.LogRecordToken.StartTime.Index]);
		tName = tokens[Util.LogRecordToken.Name.Index];
		String entitesString = tokens[Util.LogRecordToken.Entities.Index];
		String[] listOfEntities = entitesString.split(Util.ENTITY_SEPERATOR);
		String entity = null;
		for(String e : listOfEntities) {
			String[] eTokens = e.split(Util.ENTITY_ATTRIBUTE_SEPERATOR);
			String eId = eTokens[Util.EntityToken.EntityID.Index];
			if(eId.equals(custid)) {
				entity = e;
				break;
			}
		}
		String[] entitesTokens = entity.split(Util.ENTITY_ATTRIBUTE_SEPERATOR);
		eName = entitesTokens[Util.EntityToken.EntityName.Index];
		eId = entitesTokens[Util.EntityToken.EntityID.Index];
		String propsString = entitesTokens[Util.EntityToken.EntityProperties.Index];
		String[] props = propsString.split(Util.PROPERY_SEPERATOR);
		BalanceIncrement = BigDecimal.ZERO;
		BalanceRead = BigDecimal.ZERO;
		BalanceUpdate = BigDecimal.ZERO;
		for (int i = 0; i < props.length; i++) {
			String[] propToken = props[i].split(Util.PROPERY_ATTRIBUTE_SEPERATOR);
			if (propToken[Util.PropertyToken.PropertyName.Index].equals(Util.CUSTOMER_BALANCE)) {
				if (propToken[Util.PropertyToken.PropertyAction.Index].equals(Util.VALUE_READ)) {
					BalanceRead = new BigDecimal(propToken[Util.PropertyToken.PropertyValue.Index]);
				} else if (propToken[Util.PropertyToken.PropertyAction.Index].equals(Util.NEW_VALUE_UPDATE)) {
					BalanceUpdate = new BigDecimal(propToken[Util.PropertyToken.PropertyValue.Index]);
				} else if (propToken[Util.PropertyToken.PropertyAction.Index].equals(Util.INCREMENT_UPDATE)) {
					BalanceIncrement = new BigDecimal(propToken[Util.PropertyToken.PropertyValue.Index]);
				} else {
					System.err.println("ERROR: Unkown action on property ("
							+ propToken[Util.PropertyToken.PropertyAction.Index] + ")\nLine: " + line);
				}
			}
		}
	}

	public boolean overlap(LogRecord i) {
		if (start <= i.start && end >= i.end)
			return true;
		if (start >= i.start && end <= i.end)
			return true;
		if (start >= i.start && start <= i.end)
			return true;
		if (end >= i.start && end <= i.end)
			return true;
		if (end == i.start || start == i.end)
			return true;
		return false;
	}

	@Override
	public int compareTo(LogRecord o) {
		return Long.compare(start, o.start);
	}

	@Override
	public String toString() {
		if (tName.equals(Util.ORDERSTATUS_ACTION))
			return tName + "[R:" + BalanceRead.toString() + "]";
		else if (tName.equals(Util.PAYMENT_ACTION))
			return tName + "[R:" + BalanceRead.toString() + ", Dec:" + BalanceIncrement.toString() + "]";
		else if (tName.equals(Util.DELIVERY_ACTION))
			return tName + "[Inc:" + BalanceIncrement.toString() + "]";
		else
			return tName;
	}
}
