import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;

public class GEDCOMValidator {
	GEDCOMHelper helper = new GEDCOMHelper();

	/**
	 * Birth should occur before death of an individual
	 * 
	 * @param birthDateStr - Birth date
	 * @param deathDateStr - Death date
	 */
	public boolean isDeathDateValid(String birthDateStr, String deathDateStr) {
		return helper.isDate1AfterDate2(birthDateStr, deathDateStr);
	}

	/**
	 * Marriage should occur before divorce of spouses, and divorce can only occur
	 * after marriage
	 * 
	 * @param marriageDateStr - Marriage date
	 * @param divorceDateStr - Divorce date
	 */
	public boolean isDivorceAfterMarriage(String marriageDateStr, String divorceDateStr) {
		return helper.isDate1AfterDate2(marriageDateStr, divorceDateStr);
	}

	/**
	 * Birth should occur before marriage of an individual
	 *
	 * @param birthDateStr - Birth date
	 * @param marriageDateStr - Marriage date
	 */
	
	public boolean isBirthDateBeforeMarriageDate(String birthDateStr, String marriageDateStr) {
		return helper.isDate1AfterDate2(birthDateStr, marriageDateStr);
	}

	
	public boolean isMarriageBeforeDeath(String marriageDateStr, String deathDateStr) {
		return helper.isDate1AfterDate2(marriageDateStr, deathDateStr);
	}
	
	public boolean isDivorceBeforeDeath(String divorceDateStr, String deathDateStr) {
		return helper.isDate1AfterDate2(divorceDateStr, deathDateStr);
	}
	
	
	/**
	 * Date should be before today's date
	 *
	 * @param dateStr - date
	 */
	public boolean isDateBeforeCurrentDate(String dateStr) {
		try {
			if (dateStr == null || dateStr == "" || dateStr.equals("invalid"))
				return true;

			Date date = new SimpleDateFormat("dd MMM yyyy").parse(dateStr);
			Date todayDate = new Date();
			if (todayDate.compareTo(date) > 0)
				return true;
			return false;
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Person's age should available.
	 *
	 * @param age - age of individual
	 */
	public boolean isAgeAvailable(String age) {
		return !((age.equals("invalid") || age.equals("NA")));
	}

	/**
	 * Person's age should be less than 150 years old
	 * 
	 * @param age - age of individual
	 */
	public boolean isOlderThan150(String age) {
		if (age.equals("invalid") || age.equals("NA"))
			return false;
		return Integer.parseInt(age) > 150;
	}

	
	/**
	 * Husband in family should be male and wife in family should be female
	 * 
	 * @param input
	 *            Husband record
	 * @param input
	 *            Wife record
	*/
	public boolean isGenderValid(HashMap<String, Object> record, String expectedGender) {
		if(record.get("SEX").equals(expectedGender))
			return true;
		
		return false;
	}
	
	/**
	 * Marriage should be at least 14 years after birth of both spouses (parents must be at least 14 years old)
	*/
	public boolean isAgeValidForMarriage(HashMap<String, Object> husband, HashMap<String, Object> wife, String marriageDateStr) {
		try {
			GEDCOMHelper helper = new GEDCOMHelper();
			
			Date marriageDate = new SimpleDateFormat("dd MMM yyyy").parse(marriageDateStr);
			
			if(helper.calcAgeAtDate(husband, marriageDate) < 14 || helper.calcAgeAtDate(wife, marriageDate) < 14)
				return false;
			
			return true;
		} catch (ParseException e) {
			e.printStackTrace();
			
			return false;
		}  
	}


	/**
	 * Return if an individual has more than 15 siblings
	 * 
	 * @param arr - array of siblings
	 */
	public boolean tooManySib(ArrayList arr) {
		if (arr == null)
			return false;
		return arr.size() >= 15;
	}


}
