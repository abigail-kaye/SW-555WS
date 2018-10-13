import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GEDCOMTesting {
	GEDCOMValidator validator = new GEDCOMValidator();
	GEDCOMreader reader = new GEDCOMreader();

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

	@Test
	public void isAgeOrderThan150_1()  {
		//Age is less than 150
		HashMap<String, Object> ind = new HashMap<>(1);
		ind.put("BIRT", "01 JAN 2000");
		ind.put("DEAT", "01 JAN 2001");
		assertFalse(validator.isOlderThan150(GEDCOMreader.calcAge(ind)));
	}

	@Test
	public void isAgeOrderThan150_2()  {
		//Age is larger than 150
		HashMap<String, Object> ind = new HashMap<>(1);
		ind.put("BIRT", "01 JAN 1850");
		assertTrue(validator.isOlderThan150(GEDCOMreader.calcAge(ind)));
	}

	@Test
	public void isAgeExist_1()  {
		//Age is not available.
		HashMap<String, Object> ind = new HashMap<>(1);
		assertFalse(validator.isAgeAvailable(GEDCOMreader.calcAge(ind)));
	}

	@Test
	public void isAgeExist_2()  {
		//Age is available.
		HashMap<String, Object> ind = new HashMap<>(1);
		ind.put("BIRT", "01 JAN 1850");
		ind.put("DEAT", "01 JAN 1993");
		assertTrue(validator.isAgeAvailable(GEDCOMreader.calcAge(ind)));
	}
}