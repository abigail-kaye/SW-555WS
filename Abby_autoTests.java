import static org.junit.jupiter.api.Assertions.*;

import java.util.*;


import org.junit.jupiter.api.Test;

class Abby_autoTests {
	private GEDCOMreader reader;
	private GEDCOMValidator validator;
	private static HashMap<String, Object> father = new HashMap<String, Object>();
	private static HashMap<String, Object> father2 = new HashMap<String, Object>();
	private static HashMap<String, Object> child1 = new HashMap<String, Object>();
	private static HashMap<String, Object> child2 = new HashMap<String, Object>();
	private static HashMap<String, Object> child3 = new HashMap<String, Object>();
	private static HashMap<String, Object> child4 = new HashMap<String, Object>();
	
	private static HashMap<String, HashMap<String, Object>> largeMap1 = new HashMap<>(5);
	
	ArrayList<String> dates = new ArrayList<String>(6);
	
	ArrayList<String> sib1 = null;
	ArrayList<String> sib2 = new ArrayList<String>(5);
	ArrayList<String> sib3 = new ArrayList<String>(2);
	ArrayList<String> sib4 = new ArrayList<String>(16);
	ArrayList<String> sib5 = new ArrayList<String>(20);
	
	
	//US 42: Reject illegitimate dates
	void setupDates() {
		dates.add("30 FEB 2018");
		dates.add("-1 JAN 2018");
		dates.add("28 MAR 20018");
		dates.add("100 APR 2018");
		dates.add("7 XXX 2018");
		dates.add("2 MAY 2018");
	}
	void checkIsValidDate () {
		for (int i = 0; i < 5; i++) {
			assertFalse(reader.isValidDate(dates.get(i)));
		}
		assertTrue(reader.isValidDate(dates.get(5)));
	}
	
 	//US22: Unique IDs
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
	
	//US15: Fewer than 15 siblings
 	void setupSibs() {
 		sib2.add("I3");
 		sib2.add("I4");
 		sib2.add("I6");
 		
 		sib3.add("I5");
 		sib3.add("I3");
 		
 		for (int i = 0; i< 15; i++) {
 			sib4.add("sibling");
 		}
 		
 		for (int i = 0; i< 20; i++) {
 			sib5.add("sibling");
 		}
 		
 	}
 	void checkTooManySib() {
		assertEquals(false,validator.tooManySib(sib1));
		assertEquals(false,validator.tooManySib(sib2));
		assertEquals(false,validator.tooManySib(sib3));
		assertEquals(true,validator.tooManySib(sib4));
		assertEquals(true,validator.tooManySib(sib5));
	}
	
 	//US16: Male last names
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
 	void checkLastName() {
 		assertTrue(reader.checkLastNames(new Object(), null));
 		Object id1 = "I1";
 		assertTrue(reader.checkLastNames(id1, sib2));
 		assertFalse(reader.checkLastNames(id1, sib3));
 	}
	
 	@Test
	void test() {
		reader = new GEDCOMreader();
		validator = new GEDCOMValidator();
		
		setupDates();
		checkIsValidDate();
		
		setupLargeHashMap();
		checkIsUniqueId();
		
		setupSibs();
		checkTooManySib();
		
		setupLastName();
		checkLastName();
		
		
	}
}























