import java.text.ParseException;
import java.text.SimpleDateFormat;  
import java.util.Date;  

public class GEDCOMValidator {
	/**
	 * Birth should occur before death of an individual
	 * 
	 * @param input
	 *            Birth date
	 * @param input
	 *            Death date
	 */
	public boolean isDeathDateValid(String birthDateStr, String deathDateStr) {
		try {
			if(deathDateStr == null || deathDateStr == "")
				return true;
			
		    Date birthDate = new SimpleDateFormat("dd MMM yyyy").parse(birthDateStr);
		    Date deathDate = new SimpleDateFormat("dd MMM yyyy").parse(deathDateStr);  
		    
		    if(deathDate.compareTo(birthDate) >= 0)
		    	return true;
		    
			return false;
		} catch (ParseException e) {
			e.printStackTrace();
			
			return false;
		}  
	}
	
	/**
	 * Marriage should occur before divorce of spouses, and divorce can only occur after marriage
	 * 
	 * @param input
	 *            Marriage date
	 * @param input
	 *            Divorce date
	 */
	public boolean isDivorceAfterMarriage(String marriageDateStr, String divorceDateStr) {
		try {
			if(divorceDateStr == null || divorceDateStr == "")
				return true;
			
		    Date marriageDate = new SimpleDateFormat("dd MMM yyyy").parse(marriageDateStr);
		    Date divorceDate = new SimpleDateFormat("dd MMM yyyy").parse(divorceDateStr);  
		    
		    if(divorceDate.compareTo(marriageDate) > 0)
		    	return true;
		    
			return false;
		} catch (ParseException e) {
			e.printStackTrace();
			
			return false;
		}  
	}	
}
