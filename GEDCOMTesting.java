import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GEDCOMTesting {
	GEDCOMValidator validator = new GEDCOMValidator();
	GEDCOMreader reader = new GEDCOMreader();

	@Test
	public void BirthBeforeDeath1() {
		// death after birth
		String birthDate = "1 JAN 1940";
		String deathDate = "15 MAY 2003";

		assertTrue(validator.isDeathDateValid(birthDate, deathDate));
	}

	@Test
	public void BirthBeforeDeath2() {
		// death after birth
		String birthDate = "1 JAN 1909";
		String deathDate = "1 JAN 1999";

		assertTrue(validator.isDeathDateValid(birthDate, deathDate));
	}

	@Test
	public void BirthBeforeDeath3() {
		// death on next day
		String birthDate = "1 JAN 1999";
		String deathDate = "2 JAN 1999";

		assertTrue(validator.isDeathDateValid(birthDate, deathDate));
	}

	@Test
	public void BirthBeforeDeath4() {
		// death and birth same day
		String birthDate = "1 DEC 2000";
		String deathDate = "1 DEC 2000";

		assertFalse(validator.isDeathDateValid(birthDate, deathDate));
	}

	@Test
	public void BirthBeforeDeath5() {
		// death before birth
		String birthDate = "1 JAN 2000";
		String deathDate = "1 JAN 1990";

		assertFalse(validator.isDeathDateValid(birthDate, deathDate));
	}

	@Test
	public void BirthBeforeDeath6() {
		// death before birth
		String birthDate = "2 JAN 1990";
		String deathDate = "1 JAN 1990";

		assertFalse(validator.isDeathDateValid(birthDate, deathDate));
	}

	@Test
	public void BirthBeforeDeath7() {
		// person is alive
		String birthDate = "2 JAN 1990";
		String deathDate = null;

		assertTrue(validator.isDeathDateValid(birthDate, deathDate));
	}

	@Test
	public void isGenderValid1() {
		// Husband is male
		HashMap<String, Object> record = new HashMap<>(1);
		record.put("SEX", "M");

		assertTrue(validator.isGenderValid(record, "M"));
	}

	@Test
	public void isGenderValid2() {
		// Wife is female
		HashMap<String, Object> record = new HashMap<>(1);
		record.put("SEX", "F");

		assertTrue(validator.isGenderValid(record, "F"));
	}

	@Test
	public void isGenderValid3() {
		// Husband is female
		HashMap<String, Object> record = new HashMap<>(1);
		record.put("SEX", "F");

		assertFalse(validator.isGenderValid(record, "M"));
	}

	@Test
	public void isGenderValid4() {
		// Wife is male
		HashMap<String, Object> record = new HashMap<>(1);
		record.put("SEX", "M");

		assertFalse(validator.isGenderValid(record, "F"));
	}

	@Test
	public void isAgeValidForMarriage1() {
		// Marriage after 14
		HashMap<String, Object> husband = new HashMap<>(1);
		HashMap<String, Object> wife = new HashMap<>(1);

		husband.put("BIRT", "01 JAN 2000");
		wife.put("BIRT", "01 JAN 2001");

		assertTrue(validator.isAgeValidForMarriage(husband, wife, "01 JAN 2016"));
	}

	@Test
	public void isAgeValidForMarriage2() {
		// Marriage before 14
		HashMap<String, Object> husband = new HashMap<>(1);
		HashMap<String, Object> wife = new HashMap<>(1);

		husband.put("BIRT", "01 JAN 2000");
		wife.put("BIRT", "01 JAN 2001");

		assertFalse(validator.isAgeValidForMarriage(husband, wife, "01 JAN 2013"));
	}

	@Test
	public void isAgeOrderThan150_1() {
		// Age is less than 150
		HashMap<String, Object> ind = new HashMap<>(1);
		ind.put("BIRT", "01 JAN 2000");
		ind.put("DEAT", "01 JAN 2001");
		assertFalse(validator.isOlderThan150(GEDCOMreader.calcAge(ind)));
	}

	@Test
	public void isAgeOrderThan150_2() {
		// Age is larger than 150
		HashMap<String, Object> ind = new HashMap<>(1);
		ind.put("BIRT", "01 JAN 1850");
		assertTrue(validator.isOlderThan150(GEDCOMreader.calcAge(ind)));
	}

	@Test
	public void isAgeExist_1() {
		// Age is not available.
		HashMap<String, Object> ind = new HashMap<>(1);
		assertFalse(validator.isAgeAvailable(GEDCOMreader.calcAge(ind)));
	}

	@Test
	public void isAgeExist_2() {
		// Age is available.
		HashMap<String, Object> ind = new HashMap<>(1);
		ind.put("BIRT", "01 JAN 1850");
		ind.put("DEAT", "01 JAN 1993");
		assertTrue(validator.isAgeAvailable(GEDCOMreader.calcAge(ind)));
	}

	private static HashMap<String, Object> father = new HashMap<String, Object>();
	private static HashMap<String, Object> father2 = new HashMap<String, Object>();
	private static HashMap<String, Object> child1 = new HashMap<String, Object>();
	private static HashMap<String, Object> child2 = new HashMap<String, Object>();
	private static HashMap<String, Object> child3 = new HashMap<String, Object>();
	private static HashMap<String, Object> child4 = new HashMap<String, Object>();

	private static HashMap<String, HashMap<String, Object>> largeMap1 = new HashMap<>(5);

	ArrayList<String> sib1 = null;
	ArrayList<String> sib2 = new ArrayList<String>(5);
	ArrayList<String> sib3 = new ArrayList<String>(2);
	ArrayList<String> sib4 = new ArrayList<String>(16);
	ArrayList<String> sib5 = new ArrayList<String>(20);

	// US 42: Reject illegitimate dates
	void setupDates(ArrayList dates) {
		dates.add("30 FEB 2018");
		dates.add("-1 JAN 2018");
		dates.add("28 MAR 20018");
		dates.add("100 APR 2018");
		dates.add("7 XXX 2018");
		dates.add("2 MAY 2018");
	}

	void checkIsValidDate() {
		ArrayList<String> dates = new ArrayList<String>(6);
		setupDates(dates);
		for (int i = 0; i < 5; i++) {
			assertFalse(reader.isValidDate(dates.get(i)));
		}
		assertTrue(reader.isValidDate(dates.get(5)));
	}

	// US22: Unique IDs
	void setupLargeHashMap() {
		largeMap1.put("I1", father);
		largeMap1.put("I2", father2);
		largeMap1.put("I3", child1);
		largeMap1.put("I4", child2);
		largeMap1.put("I5", child3);
		largeMap1.put("I6", child4);
	}

	void checkIsUniqueId() {
		assertTrue(reader.isUniqueID("I", largeMap1));
		assertTrue(reader.isUniqueID("I7", largeMap1));
		assertTrue(reader.isUniqueID("xxxx", largeMap1));
		assertFalse(reader.isUniqueID("I1", largeMap1));
		assertFalse(reader.isUniqueID("I3", largeMap1));
	}

	// US15: Fewer than 15 siblings
	void setupSibs() {
		sib2.add("I3");
		sib2.add("I4");
		sib2.add("I6");

		sib3.add("I5");
		sib3.add("I3");

		for (int i = 0; i < 15; i++) {
			sib4.add("sibling");
		}

		for (int i = 0; i < 20; i++) {
			sib5.add("sibling");
		}

	}

	void checkTooManySib() {
		assertEquals(false, validator.tooManySib(sib1));
		assertEquals(false, validator.tooManySib(sib2));
		assertEquals(false, validator.tooManySib(sib3));
		assertEquals(true, validator.tooManySib(sib4));
		assertEquals(true, validator.tooManySib(sib5));
	}

	// US16: Male last names
	void setupLastName() {
		child1.put("SEX", "M");
		child2.put("SEX", "M");
		child3.put("SEX", "M");
		child4.put("SEX", "M");

		child1.put("NAME", "John /Smith/");
		child2.put("NAME", "Joe /Smith/");
		child3.put("NAME", "Jordan /Diff/");
		child4.put("NAME", "Jay /Smith/");

		father.put("NAME", "Mr /Smith/");
		father2.put("NAME", "Mr /Diff/");

		reader.ind = largeMap1;
	}


 	void checkMarrbeforeDeath() {
 		String marr1 = "5 JAN 2015";
 		String death1 = "10 JUN 2014";
 		String marr2 = "20 AUG 2016";
 		String death2 = "15 DEC 2017";
 		assertTrue(validator.isMarriageBeforeDeath(marr1, death2));
 		assertTrue(validator.isMarriageBeforeDeath(marr2, death2));
 		assertFalse(validator.isMarriageBeforeDeath(marr1, death1));
 		assertFalse(validator.isMarriageBeforeDeath(marr2, death1));		
 	}
 	
 	void checkDivbeforeDeath() {
 		String div1 = "5 JAN 2015";
 		String death1 = "10 JUN 2014";
 		String div2 = "20 AUG 2016";
 		String death2 = "15 DEC 2017";
 		assertTrue(validator.isDivorceBeforeDeath(div1, death2));
 		assertTrue(validator.isDivorceBeforeDeath(div2, death2));
 		assertFalse(validator.isDivorceBeforeDeath(div1, death1));
 		assertFalse(validator.isDivorceBeforeDeath(div2, death1));		
 	}
 	
	void checkLastName() {
		assertTrue(reader.checkLastNames(new Object(), null));
		Object id1 = "I1";
		assertTrue(reader.checkLastNames(id1, sib2));
		assertFalse(reader.checkLastNames(id1, sib3));
	}
	
	public void isChildBornAfterMarriage1() {
		// Child born after marriage; no divorce
		String childBirthDate = "01 JAN 2000";
		String marriageDate = "01 JAN 1999";
		String divorceDate = null;

		assertTrue(validator.isChildBornAfterMarriage(childBirthDate, marriageDate, divorceDate));
	}
	
	@Test
	public void isChildBornAfterMarriage2() {
		// Child born after marriage; before divorce
		String childBirthDate = "01 JAN 2000";
		String marriageDate = "01 JAN 1999";
		String divorceDate = "01 FEB 2000";

		assertTrue(validator.isChildBornAfterMarriage(childBirthDate, marriageDate, divorceDate));
	}
	
	@Test
	public void isChildBornAfterMarriage3() {
		// Child born after marriage; after 4 months of divorce
		String childBirthDate = "01 JAN 2000";
		String marriageDate = "01 JAN 1999";
		String divorceDate = "01 SEP 1999";

		assertTrue(validator.isChildBornAfterMarriage(childBirthDate, marriageDate, divorceDate));
	}
	
	@Test
	public void isChildBornAfterMarriage4() {
		// Child born after marriage; after 11 months of divorce
		String childBirthDate = "01 JAN 2000";
		String marriageDate = "01 JAN 1999";
		String divorceDate = "01 FEB 1999";

		assertFalse(validator.isChildBornAfterMarriage(childBirthDate, marriageDate, divorceDate));
	}
	
	@Test
	public void isChildBornAfterMarriage5() {
		// Child born before marriage;
		String childBirthDate = "01 JAN 2000";
		String marriageDate = "05 JAN 2000";
		String divorceDate = null;

		assertFalse(validator.isChildBornAfterMarriage(childBirthDate, marriageDate, divorceDate));
	}
	
	@Test
	public void isChildBornBeforeParentsDeath1() {
		// Child born; no death
		String childBirthDate = "01 JAN 2000";
		String motherDeathDate = null;
		String fatherDeathDate = null;

		assertTrue(validator.isChildBornBeforeParentsDeath(childBirthDate, motherDeathDate, fatherDeathDate));
	}
	
	@Test
	public void isChildBornBeforeParentsDeath2() {
		// Child born before mother death; father is alive
		String childBirthDate = "01 JAN 2000";
		String motherDeathDate = "01 MAY 2000";
		String fatherDeathDate = null;

		assertTrue(validator.isChildBornBeforeParentsDeath(childBirthDate, motherDeathDate, fatherDeathDate));
	}
	
	@Test
	public void isChildBornBeforeParentsDeath3() {
		// Child born before father death; mother is alive
		String childBirthDate = "01 JAN 2000";
		String motherDeathDate = null;
		String fatherDeathDate = "01 MAY 2000";

		assertTrue(validator.isChildBornBeforeParentsDeath(childBirthDate, motherDeathDate, fatherDeathDate));
	}
	
	@Test
	public void isChildBornBeforeParentsDeath4() {
		// Child born after 4 months of father death; mother is alive
		String childBirthDate = "01 JAN 2000";
		String motherDeathDate = null;
		String fatherDeathDate = "01 SEP 1999";

		assertTrue(validator.isChildBornBeforeParentsDeath(childBirthDate, motherDeathDate, fatherDeathDate));
	}
	
	@Test
	public void isChildBornBeforeParentsDeath5() {
		// Child born after 11 months of father death; mother is alive
		String childBirthDate = "01 JAN 2000";
		String motherDeathDate = null;
		String fatherDeathDate = "01 FEB 1999";

		assertFalse(validator.isChildBornBeforeParentsDeath(childBirthDate, motherDeathDate, fatherDeathDate));
	}
	
	@Test
	public void isChildBornBeforeParentsDeath6() {
		// Child born after mother death; father is alive
		String childBirthDate = "01 JAN 2000";
		String motherDeathDate = "01 FEB 1999";
		String fatherDeathDate = null;

		assertFalse(validator.isChildBornBeforeParentsDeath(childBirthDate, motherDeathDate, fatherDeathDate));
	}
	
	@Test
	public void isChildBornBeforeParentsDeath7() {
		// Child born after parents death
		String childBirthDate = "01 JAN 2000";
		String motherDeathDate = "01 NOV 1999";
		String fatherDeathDate = "01 NOV 1999";

		assertFalse(validator.isChildBornBeforeParentsDeath(childBirthDate, motherDeathDate, fatherDeathDate));
	}
	
	@Test
	public void isChildBornBeforeParentsDeath8() {
		// Child born before mother death; after 2 months of father death
		String childBirthDate = "01 JAN 2000";
		String motherDeathDate = "01 NOV 2000";
		String fatherDeathDate = "01 NOV 1999";

		assertTrue(validator.isChildBornBeforeParentsDeath(childBirthDate, motherDeathDate, fatherDeathDate));
	}

	@Test
	public void isSiblingsSorted() {
		// Children should be sorted from young to old.
		ArrayList<String> children = new ArrayList<>();
		ArrayList<String> children_new;
		HashMap<String, HashMap<String, Object>> individual = new HashMap<>(5); // Hashmap of information for
		HashMap<String, Object> child1 = new HashMap<>(5);
		HashMap<String, Object> child2 = new HashMap<>(5);
		HashMap<String, Object> child3 = new HashMap<>(5);
		children.add("I1");
		children.add("I2");
		children.add("I3");
		children_new = children;
		Collections.reverse(children_new);
		child1.put("BIRT", "01 JAN 2000");
		child2.put("BIRT", "01 JAN 2001");
		child3.put("BIRT", "01 JAN 2004");
		individual.put("I1",child1);
		individual.put("I2",child2);
		individual.put("I3",child3);
		assertTrue(reader.sortSiblings(children ,individual).equals(children_new));
	}

	@Test
	public void isFindAllDeadPeople() {
		// Children should be sorted from young to old.
		HashMap<String, HashMap<String, Object>> individual = new HashMap<>(5); // Hashmap of information for
		HashMap<String, Object> p1 = new HashMap<>(5);
		HashMap<String, Object> p2 = new HashMap<>(5);
		HashMap<String, Object> p3 = new HashMap<>(5);
		ArrayList<String> dead = new ArrayList<>(2);
		dead.add("I1");
		dead.add("I3");
		p1.put("DEAT", "01 JAN 2000");
		p3.put("DEAT", "01 JAN 2004");
		individual.put("I1",p1);
		individual.put("I2",p2);
		individual.put("I3",p3);
		System.out.println(reader.deadPeople(individual).keySet());
		System.out.println(dead);
		ArrayList<String> keys = new ArrayList<>(reader.deadPeople(individual).keySet());
		assertTrue(keys.equals(dead));
	}
	
	public void isBirthDateOfSiblingValid1() {
		// Same day
		String sibling1BirthDateStr = "01 JAN 2000";
		String sibling2BirthDateStr = "01 JAN 2000";

		assertTrue(validator.isBirthDateOfSiblingValid(sibling1BirthDateStr, sibling2BirthDateStr));
	}
	
	@Test
	public void isBirthDateOfSiblingValid2() {
		// next day
		String sibling1BirthDateStr = "01 JAN 2000";
		String sibling2BirthDateStr = "02 JAN 2000";

		assertTrue(validator.isBirthDateOfSiblingValid(sibling1BirthDateStr, sibling2BirthDateStr));
	}
	
	@Test
	public void isBirthDateOfSiblingValid3() {
		// after 2 days
		String sibling1BirthDateStr = "01 JAN 2000";
		String sibling2BirthDateStr = "03 JAN 2000";

		assertFalse(validator.isBirthDateOfSiblingValid(sibling1BirthDateStr, sibling2BirthDateStr));
	}
	
	@Test
	public void isBirthDateOfSiblingValid4() {
		// after 6 months
		String sibling1BirthDateStr = "01 JAN 2000";
		String sibling2BirthDateStr = "01 JUN 2000";

		assertFalse(validator.isBirthDateOfSiblingValid(sibling1BirthDateStr, sibling2BirthDateStr));
	}
	
	@Test
	public void isBirthDateOfSiblingValid5() {
		// after 10 months
		String sibling1BirthDateStr = "01 JAN 2000";
		String sibling2BirthDateStr = "06 OCT 2000";

		assertTrue(validator.isBirthDateOfSiblingValid(sibling1BirthDateStr, sibling2BirthDateStr));
	}
	
	@Test
	public void isBirthDateOfSiblingValid6() {
		// after 8 months
		String sibling1BirthDateStr = "01 JAN 2000";
		String sibling2BirthDateStr = "01 AUG 2000";

		assertTrue(validator.isBirthDateOfSiblingValid(sibling1BirthDateStr, sibling2BirthDateStr));
	}
	
	@Test
	public void isBirthDateOfSiblingValid7() {
		// after 6 months
		String sibling1BirthDateStr = "01 JAN 2000";
		String sibling2BirthDateStr = "02 JUN 2000";

		assertFalse(validator.isBirthDateOfSiblingValid(sibling1BirthDateStr, sibling2BirthDateStr));
	}
	
	@Test
	public void isBirthDateOfSiblingValid8() {
		// after 3 years
		String sibling1BirthDateStr = "01 JAN 2000";
		String sibling2BirthDateStr = "02 JUN 2003";

		assertTrue(validator.isBirthDateOfSiblingValid(sibling1BirthDateStr, sibling2BirthDateStr));
	}
	
	@Test
	public void isSpouseSibling1() {
		ArrayList<String> spouse = new ArrayList<String>();
		ArrayList<String> sibling = new ArrayList<String>();
		
		spouse.add("I1");
		sibling.add("I2");
		sibling.add("I3");
		
		assertFalse(validator.isSpouseSibling(spouse, sibling));
	}

	@Test
	public void isSpouseSibling2() {
		ArrayList<String> spouse = new ArrayList<String>();
		ArrayList<String> sibling = new ArrayList<String>();
		
		sibling.add("I2");
		sibling.add("I3");
		
		assertFalse(validator.isSpouseSibling(spouse, sibling));
	}
	
	@Test
	public void isSpouseSibling3() {
		ArrayList<String> spouse = new ArrayList<String>();
		ArrayList<String> sibling = new ArrayList<String>();
		
		spouse.add("I1");
		spouse.add("I4");
		sibling.add("I2");
		sibling.add("I3");
		
		assertFalse(validator.isSpouseSibling(spouse, sibling));
	}
	
	@Test
	public void isSpouseSibling4() {
		ArrayList<String> spouse = new ArrayList<String>();
		ArrayList<String> sibling = new ArrayList<String>();
		
		spouse.add("I2");
		sibling.add("I2");
		sibling.add("I3");
		
		assertTrue(validator.isSpouseSibling(spouse, sibling));
	}
}