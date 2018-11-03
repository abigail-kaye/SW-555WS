import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

public class GEDCOMValidator {
	GEDCOMHelper helper = new GEDCOMHelper();

	/**
	 * Birth should occur before death of an individual
	 * 
	 * @param birthDateStr - Birth date
	 * @param deathDateStr - Death date
	 */
	public boolean isDeathDateValid(String birthDateStr, String deathDateStr) {
		return helper.isDate2AfterDate1(birthDateStr, deathDateStr);
	}

	/**
	 * Marriage should occur before divorce of spouses, and divorce can only occur
	 * after marriage
	 * 
	 * @param marriageDateStr - Marriage date
	 * @param divorceDateStr - Divorce date
	 */
	public boolean isDivorceAfterMarriage(String marriageDateStr, String divorceDateStr) {
		return helper.isDate2AfterDate1(marriageDateStr, divorceDateStr);
	}

	/**
	 * Birth should occur before marriage of an individual
	 *
	 * @param birthDateStr - Birth date
	 * @param marriageDateStr - Marriage date
	 */
	
	public boolean isBirthDateBeforeMarriageDate(String birthDateStr, String marriageDateStr) {
		return helper.isDate2AfterDate1(birthDateStr, marriageDateStr);
	}

	
	public boolean isMarriageBeforeDeath(String marriageDateStr, String deathDateStr) {
		return helper.isDate2AfterDate1(marriageDateStr, deathDateStr);
	}
	
	public boolean isDivorceBeforeDeath(String divorceDateStr, String deathDateStr) {
		return helper.isDate2AfterDate1(divorceDateStr, deathDateStr);
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

	/**
	 * Children should be born after marriage of parents (and not more than 9 months
	 * after their divorce)
	 * 
	 * @param childBirthDateStr
	 *            - Child Birth date
	 * @param marriageDateStr
	 *            - Marriage date
	 * @param divorceDateStr
	 *            - Divorce date
	 */
	public boolean isChildBornAfterMarriage(String childBirthDateStr, String marriageDateStr, String divorceDateStr) {
		if (divorceDateStr == null || divorceDateStr == "")
			return helper.isDate2AfterDate1(marriageDateStr, childBirthDateStr);
		else {
			try {
				Date divorceDate = new SimpleDateFormat("dd MMM yyyy").parse(divorceDateStr);

				Calendar c = Calendar.getInstance();
				c.setTime(divorceDate);
				c.add(Calendar.MONTH, 9);

				return (helper.isDate2AfterDate1(marriageDateStr, childBirthDateStr) && helper
						.isDate2AfterDate1(childBirthDateStr, new SimpleDateFormat("dd MMM yyyy").format(c.getTime())));
			} catch (ParseException e) {
				return false;
			}
		}
	}

	/**
	 * Child should be born before death of mother and before 9 months after death
	 * of father
	 * 
	 * @param childBirthDateStr
	 *            - Child Birth date
	 * @param motherDeathDateStr
	 *            - Mother Death date
	 * @param fatherDeathDateStr
	 *            - Father Death date
	 */
	public boolean isChildBornBeforeParentsDeath(String childBirthDateStr, String motherDeathDateStr,
			String fatherDeathDateStr) {
		boolean isChildBornBeforeParentsDeath = true;

		if ((motherDeathDateStr == null || motherDeathDateStr == "")
				&& (fatherDeathDateStr == null || fatherDeathDateStr == ""))
			return true;
		else {
			try {
				if (motherDeathDateStr != null && motherDeathDateStr != "")
					isChildBornBeforeParentsDeath = helper.isDate2AfterDate1(childBirthDateStr, motherDeathDateStr);

				if (fatherDeathDateStr != null && fatherDeathDateStr != "") {
					Date fatherDeathDate;

					fatherDeathDate = new SimpleDateFormat("dd MMM yyyy").parse(fatherDeathDateStr);

					Calendar c = Calendar.getInstance();
					c.setTime(fatherDeathDate);
					c.add(Calendar.MONTH, 9);

					isChildBornBeforeParentsDeath = isChildBornBeforeParentsDeath && helper.isDate2AfterDate1(
							childBirthDateStr, new SimpleDateFormat("dd MMM yyyy").format(c.getTime()));
				}
			} catch (ParseException e) {
				return false;
			}
		}

		return isChildBornBeforeParentsDeath;
	}

	/**
	 * Birth dates of siblings should be more than 8 months apart or less than 2
	 * days apart (twins may be born one day apart, e.g. 11:59 PM and 12:02 AM the
	 * following calendar day)
	 * 
	 * @param sibling1BirthDateStr
	 *            - Sibling 1 Birth date
	 * @param sibling2BirthDateStr
	 *            - Sibling 2 Death date
	 */
	public boolean isBirthDateOfSiblingValid(String sibling1BirthDateStr, String sibling2BirthDateStr) {
		try {
			Date sibling1BirthDate = new SimpleDateFormat("dd MMM yyyy").parse(sibling1BirthDateStr);
			Date sibling2BirthDate = new SimpleDateFormat("dd MMM yyyy").parse(sibling2BirthDateStr);

			if(sibling1BirthDate.after(sibling2BirthDate)) {
				Date temp = sibling1BirthDate;
				sibling2BirthDate = sibling1BirthDate;
				sibling1BirthDate = temp;
			}
			
			LocalDate sibling1BirthLocalDate = LocalDate
					.parse(new SimpleDateFormat("yyyy-MM-dd").format(sibling1BirthDate));
			LocalDate sibling2BirthLocalDate = LocalDate
					.parse(new SimpleDateFormat("yyyy-MM-dd").format(sibling2BirthDate));

			Period diff = Period.between(sibling1BirthLocalDate, sibling2BirthLocalDate);

			if (diff.getYears() > 0)
				return true;
			else if (diff.getMonths() >= 7)
				return true;
			else if (diff.getDays() < 2 && diff.getMonths() == 0 && diff.getYears() == 0)
				return true;
			else
				return false;
		} catch (ParseException e) {
			return false;
		}

		/*
		 * boolean isBirthDateOfSiblingValid = true;
		 * 
		 * if (sibling == null || sibling.size() < 2) return isBirthDateOfSiblingValid;
		 * else { for(int i = 0; i < sibling.size() - 1; i++) { Date sibling1Date = new
		 * SimpleDateFormat("dd MMM yyyy").parse(sibling[i].get("BIRT"));
		 * 
		 * Calendar c = Calendar.getInstance(); c.setTime(fatherDeathDate);
		 * c.add(Calendar.MONTH, 9); }
		 */
	}

	/**
	 * Siblings should not marry one another
	 * 
	 * @param personId
	 *            - Person ID
	 * @param siblingList
	 *            - List of Sibling
	 */
	public boolean isSpouseSibling(ArrayList spouseList, ArrayList siblingList) {
		if (spouseList != null) {
			for (Object spouse : spouseList) {
				if (siblingList.contains(spouse))
					return true;
			}
		}
		return false;
	}

	public boolean isNameBirthUniq(HashMap<String, Object> individual, Set<String> set) {
		return !set.contains(individual.get("NAME").toString() + individual.get("BIRT").toString());
	}
}
