import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class GEDCOMTesting {
	GEDCOMValidator validator = new GEDCOMValidator();
	
	@Test
	public void BirthBeforeDeath1()  {
		//death after birth
		String birthDate = "1 JAN 1940";
		String deathDate = "15 MAY 2003";
		
		assertTrue(validator.isDeathDateValid(birthDate, deathDate));
	}
	
	@Test
	public void BirthBeforeDeath2()  {
		//death after birth
		String birthDate = "1 JAN 1909";
		String deathDate = "1 JAN 1999";
		
		assertTrue(validator.isDeathDateValid(birthDate, deathDate));
	}
	
	@Test
	public void BirthBeforeDeath3()  {
		//death on next day
		String birthDate = "1 JAN 1999";
		String deathDate = "2 JAN 1999";
		
		assertTrue(validator.isDeathDateValid(birthDate, deathDate));
	}
	
	@Test
	public void BirthBeforeDeath4()  {
		//death and birth same day
		String birthDate = "1 DEC 2000";
		String deathDate = "1 DEC 2000";
		
		assertTrue(validator.isDeathDateValid(birthDate, deathDate));
	}
	
	@Test
	public void BirthBeforeDeath5()  {
		//death before birth
		String birthDate = "1 JAN 2000";
		String deathDate = "1 JAN 1990";
		
		assertFalse(validator.isDeathDateValid(birthDate, deathDate));
	}
	
	@Test
	public void BirthBeforeDeath6()  {
		//death before birth
		String birthDate = "2 JAN 1990";
		String deathDate = "1 JAN 1990";
		
		assertFalse(validator.isDeathDateValid(birthDate, deathDate));
	}
	
	@Test
	public void BirthBeforeDeath7()  {
		//person is alive
		String birthDate = "2 JAN 1990";
		String deathDate = null;
		
		assertTrue(validator.isDeathDateValid(birthDate, deathDate));
	}
	
	@Test
	public void isGenderValid1()  {
		//Husband is male
		HashMap<String, Object> record = new HashMap<>(1);
		record.put("SEX", "M");
		
		assertTrue(validator.isGenderValid(record, "M"));
	}
	
	@Test
	public void isGenderValid2()  {
		//Wife is female
		HashMap<String, Object> record = new HashMap<>(1);
		record.put("SEX", "F");
		
		assertTrue(validator.isGenderValid(record, "F"));
	}
	
	@Test
	public void isGenderValid3()  {
		//Husband is female
		HashMap<String, Object> record = new HashMap<>(1);
		record.put("SEX", "F");
		
		assertFalse(validator.isGenderValid(record, "M"));
	}
	
	@Test
	public void isGenderValid4()  {
		//Wife is male
		HashMap<String, Object> record = new HashMap<>(1);
		record.put("SEX", "M");
		
		assertFalse(validator.isGenderValid(record, "F"));
	}
	
	@Test
	public void isAgeValidForMarriage1()  {
		//Marriage after 14
		HashMap<String, Object> husband = new HashMap<>(1);
		HashMap<String, Object> wife = new HashMap<>(1);
		
		husband.put("BIRT", "01 JAN 2000");
		wife.put("BIRT", "01 JAN 2001");
		
		assertTrue(validator.isAgeValidForMarriage(husband, wife, "01 JAN 2016"));
	}
	
	@Test
	public void isAgeValidForMarriage2()  {
		//Marriage before 14
		HashMap<String, Object> husband = new HashMap<>(1);
		HashMap<String, Object> wife = new HashMap<>(1);
		
		husband.put("BIRT", "01 JAN 2000");
		wife.put("BIRT", "01 JAN 2001");
		
		assertFalse(validator.isAgeValidForMarriage(husband, wife, "01 JAN 2013"));
	}
}