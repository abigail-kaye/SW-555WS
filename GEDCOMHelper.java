import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class GEDCOMHelper {
	/**
	 * Returns age of individual based on provided date
	 * 
	 * @param temp
	 *            - individual to check
	 * @param dateToCal
	 *            - date on which you want to find out age
	 */
	public int calcAgeAtDate(HashMap<String, Object> temp, Date date) {
		if(GEDCOMreader.months.isEmpty())
			GEDCOMreader.fillMonthHashMap();
		
		/* Accounts for incorrect date format */
		if ((String) temp.get("BIRT") == "invalid")
			return 0;

		/* Split date into year, month, day string */
		int birthYear = Integer.parseInt(((String) temp.get("BIRT")).split(" ")[2]);
		String monthString = (((String) temp.get("BIRT")).split(" ")[1]);
		int birthDay = Integer.parseInt(((String) temp.get("BIRT")).split(" ")[0]);

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		int monthNum = GEDCOMreader.months.get(monthString); // Get current month
		if (cal.get(Calendar.MONTH) < monthNum) // Check if birth month has already passed
			return (cal.get(Calendar.YEAR) - 1 - birthYear); // Return age
		else if (cal.get(Calendar.MONTH) == monthNum) { // Check if birth month is this month
			int day = cal.get(Calendar.DAY_OF_MONTH); // Get current day
			if (day < birthDay) // Check if birth day has not passed yet
				return (cal.get(Calendar.YEAR) - 1 - birthYear); // Return age
		}
		return (cal.get(Calendar.YEAR) - birthYear); // Return age
	}
	
	public boolean isDate1AfterDate2(String date1Str, String date2Str) {
		try {
			if(date2Str == null || date2Str == "")
				return true;
			if(date1Str == null || date1Str == "")
				return true;
			
		    Date date1 = new SimpleDateFormat("dd MMM yyyy").parse(date1Str);
		    Date date2 = new SimpleDateFormat("dd MMM yyyy").parse(date2Str);  
		    
		    if(date2.compareTo(date1) >= 0)
		    	return true;
		    
			return false;
		} catch (ParseException e) {
			return false;
		}
	}
}
