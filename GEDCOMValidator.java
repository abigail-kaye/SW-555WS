import java.text.ParseException;
import java.text.SimpleDateFormat;  
import java.util.Date;  

public class GEDCOMValidator {

	/**
	 * noDate should return true if date is empty or null
	 *
	 * @param dateStr -  date
	 */
	private boolean noDate(String dateStr) {
		return dateStr == null || dateStr.equals("");
	}

	/**
	 * EarlyDate should occur before LateDate
	 *
	 * @param earlyDateStr - early date
	 * @param lateDateStr - late date
	 */
	private boolean compareDate(String earlyDateStr, String lateDateStr) {
		try {
			Date earlyDate = new SimpleDateFormat("dd MMM yyyy").parse(earlyDateStr);
			Date lateDate = new SimpleDateFormat("dd MMM yyyy").parse(lateDateStr);
			return (lateDate.compareTo(earlyDate) >= 0);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Birth should occur before death of an individual
	 * 
	 * @param birthDateStr - Birth date
	 * @param deathDateStr - Death date
	 */
	public boolean isDeathDateValid(String birthDateStr, String deathDateStr) {
		if (noDate(deathDateStr))
			return true;
		return compareDate(birthDateStr, deathDateStr);
	}
	
	/**
	 * Marriage should occur before divorce of spouses, and divorce can only occur after marriage
	 * 
	 * @param marriageDateStr - Marriage date
	 * @param divorceDateStr - Divorce date
	 */
	public boolean isDivorceAfterMarriage(String marriageDateStr, String divorceDateStr) {
		if (noDate(divorceDateStr))
			return true;
		return compareDate(marriageDateStr, divorceDateStr);
	}

	/**
	 * Birth should occur before marriage of an individual
	 *
	 * @param birthDateStr - Birth date
	 * @param marriageDateStr - Marriage date
	 */
	public boolean isBirthDateBeforeMarriageDate(String birthDateStr, String marriageDateStr) {
		if (noDate(marriageDateStr))
			return true;
		return compareDate(birthDateStr, marriageDateStr);
	}

	/**
	 * Date should be before today's date
	 *
	 * @param dateStr - date
	 */
	public boolean isDateBeforeCurrentDate(String dateStr) {
		try {
			if (noDate(dateStr))
				return true;

			Date date = new SimpleDateFormat("dd MMM yyyy").parse(dateStr);
			Date todayDate = new Date();
			if(todayDate.compareTo(date) > 0)
				return true;

			return false;
		} catch (ParseException e) {
			e.printStackTrace();

			return false;
		}
	}
}
