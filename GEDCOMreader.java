import java.io.*;
import java.util.*;
import java.util.Calendar;

public class GEDCOMreader {

	/* Arrays with supported tags Separated by level */
	private static String[] lvlZero = { "HEAD", "TRLR", "NOTE" };
	private static String[] lvlOne = { "NAME", "SEX", "BIRT", "DEAT", "FAMC", "FAMS", "MARR", "HUSB", "WIFE", "CHIL",
			"DIV" };
	private static String[] lvlTwo = { "DATE" };
	private static String[] isDate = { "BIRT", "DEAT", "MARR", "DIV" }; // Array with tag's related to dates

	private static ArrayList<String> dateList = new ArrayList<>(Arrays.asList(isDate)); // Array list of date tags
	private static ArrayList<Integer> indArr = new ArrayList<>(); // Array list of individuals
	private static ArrayList<Integer> famArr = new ArrayList<>(); // Array list of families

	public static HashMap<String, Integer> months = new HashMap<>(12); // Hashmap of month and number association
	private static HashMap<String, HashMap<String, Object>> ind = new HashMap<>(5000); // Hashmap of information for
																						// each individual
	private static HashMap<String, HashMap<String, Object>> fam = new HashMap<>(1000); // Hashmap of information for
																						// each family

	/**
	 * Returns true if entered ID is unique
	 */
	private static boolean isUniqueID(String id, HashMap<String, HashMap<String, Object>> map) {
		return map.get(id) == null;
	}

	/**
	 * Finds tag within the input string
	 * 
	 * @param input - GEDCOM line that is being analyzed
	 * @return tag of current line
	 */
	private static String findTag(String input) {
		int n = isExceptionLineToo(input); // Identify if INDI or FAM line
		if (n == 0)
			return "FAM";
		else if (n == 1)
			return "INDI";
		else { // Get tag in string form
			String res = "";
			for (int i = 2; i < input.length(); i++) {
				if (input.charAt(i) != ' ')
					res = res + input.valueOf(input.charAt(i));
				else
					break;
			}
			return res;
		}
	}

	/**
	 * Checks if line is in the special format Applies to INDI and FAM lines only
	 * 
	 * @param input - GEDCOM line that is being analyzed
	 * @return 0 or 1 if it a special format 2 otherwise
	 */
	private static int isExceptionLineToo(String input) {
		if (input.substring(input.length() - 3).equals("FAM"))
			return 0;
		if (input.substring(input.length() - 4).equals("INDI"))
			return 1;
		return 2;
	}

	/**
	 * Find any arguments within the current input line
	 * 
	 * @param input - GEDCOM line that is being analyzed
	 * @param tag - tag of line being analyzed
	 * @return extra line arguments null if error
	 */
	private static String findArgs(String input, String tag) {
		if (isExceptionLineToo(input) > 1) { // Check if line is in special format
			String s = input.substring(tag.length() + 2); // Return anything after tag
			if (s.length() > 1) {
				if (s.charAt(0) == ' ') // If there are arguments
					return s.substring(1); // Remove the beginning space before returning
			}
			return s;
		} else if (tag.equals("FAM")) // If special format
			return input.substring(2, input.length() - 4); // Take the middle of the input line
		else if (tag.equals("INDI"))
			return input.substring(2, input.length() - 5);
		else
			return null;
	}

	/**
	 * Check if tag is supported
	 * 
	 * @param lvl - line level
	 * @param tag - tag to check
	 * @return "Y" if tag is supported "N" if tag is not supported
	 */
	private static String isSupportedTag(int lvl, String tag) {
		String[] toScan;
		int n = 0;
		if (lvl == 0)
			toScan = lvlZero; // Check level 0 tags
		else if (lvl == 1)
			toScan = lvlOne; // Check level 1 tags
		else if (lvl == 2)
			toScan = lvlTwo; // Check level 2 tags
		else
			return "N"; // Line number/level is invalid
		for (int i = 0; i < toScan.length; i++) { // Scan through supported tags of that level
			if (tag.equals(toScan[i]))
				return "Y";
		}
		return "N";
	}

	/**
	 * Check if date is in correct format and follows date rules
	 * 
	 * @param date - date to check (in string form)
	 * @return true if date is correct; false if date is incorrect
	 */
	private static boolean isValidDate(String date) {
		/* Break string into respective parts */
		int year = Integer.parseInt(date.split(" ")[2]);
		String month = date.split(" ")[1];
		int day = Integer.parseInt(date.split(" ")[0]);

		if (year > 9999 || year < 0) // Check year is not too large or too small
			return false;
		boolean flag = false;
		for (String s : months.keySet()) { // Check if valid month
			if (s.equals(month))
				flag = true;
		}
		if (flag == false)
			return false;
		if (day > 31 || day < 1) // Check if day is within normal bounds
			return false;
		if (month.equals("FEB") && day > 28) // Check February end date
			return false;
		if ((month.equals("APR") || month.equals("JUN") || month.equals("SEP") || month.equals("NOV")) && day > 30)
			return false; // C
		return true;
	}

	/**
	 * Returns age (in string form) of individual
	 * 
	 * @param temp - individual to check
	 */
	private static String calcAge(HashMap<String, Object> temp) {

		/* Accounts for incorrect date format */
		if ((String) temp.get("DEAT") == "invalid")
			return "NA";
		if ((String) temp.get("BIRT") == "invalid")
			return "NA";

		/* Split date into year, month, day string */
		int birthYear = Integer.parseInt(((String) temp.get("BIRT")).split(" ")[2]);
		String monthString = (((String) temp.get("BIRT")).split(" ")[1]);
		int birthDay = Integer.parseInt(((String) temp.get("BIRT")).split(" ")[0]);

		Calendar now = Calendar.getInstance(); // Get current date
		String deathDate = (String) temp.get("DEAT"); // Get death date

		if (deathDate != null) { // Check if person has already died
			int deathYear = Integer.parseInt(deathDate.split(" ")[2]); // Get death year
			return String.valueOf(deathYear - birthYear); // Return age
		}

		int monthNum = months.get(monthString); // Get current month
		if (now.get(Calendar.MONTH) < monthNum) // Check if birth month has already passed
			return String.valueOf((Calendar.getInstance().get(Calendar.YEAR) - 1 - birthYear)); // Return age
		else if (now.get(Calendar.MONTH) == monthNum) { // Check if birth month is this month
			int currDay = now.get(Calendar.DAY_OF_MONTH); // Get current day
			if (currDay < birthDay) // Check if birth day has not passed yet
				return String.valueOf((Calendar.getInstance().get(Calendar.YEAR) - 1 - birthYear)); // Return age
		}
		return String.valueOf((Calendar.getInstance().get(Calendar.YEAR) - birthYear)); // Return age
	}

	/**
	 * Check if individual is alive
	 * 
	 * @param temp- individual to check
	 * @return true if individual is alive; false if individual is deceased
	 */
	private static boolean isAlive(HashMap<String, Object> temp) {
		String deathDate = (String) temp.get("DEAT");
		return deathDate == null;
	}

	/**
	 * Return string of children
	 * 
	 * @param temp - family to check
	 */
	private static String getChildren(Object temp) {
		if (temp == null) // Return NA if no children
			return "NA";

		ArrayList famList = (ArrayList) temp; // Store all families in an arraylist
		ArrayList children = new ArrayList(); // Create arraylist for children
		String s = "["; // String list to return

		for (Object famNum : famList) { // Loop through each family
			ArrayList childrenGot = (ArrayList) fam.get(famNum).get("CHIL"); // Get all children in 1 family
			children.addAll(childrenGot); // Add children to children array list
		}

		for (Object child : children) { // Loop through children array list
			s += child + ", "; // Add child ID tag to return string
		}

		s += "]";
		s = s.replace(", ]", "]");
		return s;
	}

	/**
	 * Return string of
	 * 
	 * @param temp - individual
	 * @param sex - sex of individual
	 */
	private static String getSpouse(Object temp, Object sex) {
		if (temp == null) // Return NA if no object
			return "NA";

		ArrayList famList = (ArrayList) temp; // Store all families in an arrayList
		String s = "[";
		String wifeOrHus; // Status of spouse
		if (sex.equals("M")) // Identify sex of current individual (and therefore sex of partner)
			wifeOrHus = "WIFE";
		else
			wifeOrHus = "HUSB";

		for (Object famNum : famList) { // Loop through each family
			String spouseGot = (String) fam.get(famNum).get(wifeOrHus); // Find spouse of individual
			if (spouseGot != null)
				s += spouseGot + ", "; // Add ID of spouse to returning string
		}

		s += "]";
		s = s.replace(", ]", "]");
		return s;
	}

	/**
	 * Return name of individual
	 */
	private static String getName(Object ID) {
		return (String) ind.get(ID).get("NAME");
	}

	/**
	 * Print table regarding individuals and families
	 * 
	 * @param table - (individual or family) table to print out
	 * @param type - tag of table to print out out (INDI or FAM)
	 */
	private static void printfTable(HashMap<String, HashMap<String, Object>> table, String type) {
		HashMap<String, Object> temp;
		String tag;

		/* Print individual table */
		if (type.equals("INDI")) {
			Collections.sort(indArr); // Organize (sort) individuals
			System.out.println(String.format("%5s %25s %6s %15s %3s %5s %15s %20s %20s", "ID", "NAME", "Gender",
					"Birthday", "Age", "Alive", "Death", "Child", "Spouse")); // Print table headers

			/* Get information for each individual */
			for (Integer i : indArr) {
				tag = "I" + i; // Get ID of individual
				temp = table.get(tag); // Get information of individual
				System.out.println(String.format("%5s %25s %6s %15s %3s %5s %15s %20s %20s", tag, temp.get("NAME"), // Print
																													// information
						temp.get("SEX"), temp.get("BIRT"), calcAge(temp), isAlive(temp),
						temp.get("DEAT") != null ? temp.get("DEAT") : "NA", // Check if individual has died and write
																			// correct death date
						getChildren(temp.get("FAMS")), getSpouse(temp.get("FAMS"), temp.get("SEX"))));
			}
		}

		/* Print family table */
		else if (type.equals("FAM")) {
			Collections.sort(famArr); // Organize (sort) families
			System.out.println(String.format("%5s %20s %20s %10s %20s %10s %20s %20s", "ID", "Married", "Divorced",
					"Husband ID", "Husband Name", "Wife ID", "Wife Name", "Children")); // Print table headers

			/* Get information for each family */
			for (Integer i : famArr) {
				tag = "F" + i; // Get family ID
				temp = table.get(tag); // Get information of family
				System.out.println(String.format("%5s %20s %20s %10s %20s %10s %20s %20s", tag, temp.get("MARR"),
						temp.get("DIV") != null ? temp.get("DIV") : "NA", temp.get("HUSB"), getName(temp.get("HUSB")),
						temp.get("WIFE"), getName(temp.get("WIFE")), temp.get("CHIL"))); // Print information
			}
		}
	}

	private static void printfErrors(HashMap<String, HashMap<String, Object>> indiTable,
			HashMap<String, HashMap<String, Object>> famTable) {
		HashMap<String, Object> temp;
		String tag;
		GEDCOMValidator validator = new GEDCOMValidator();

		Collections.sort(indArr);

		for (Integer i : indArr) {
			tag = "I" + i;
			temp = indiTable.get(tag);

			if (!validator.isDateBeforeCurrentDate((String) temp.get("BIRT")))
				System.out.println("ERROR: INDIVIDUAL: US01: " + tag + ": Born after today");

			if (!validator.isDateBeforeCurrentDate((String) temp.get("DEAT")))
				System.out.println("ERROR: INDIVIDUAL: US01: " + tag + ": Died after today");

			if (!validator.isDeathDateValid((String) temp.get("BIRT"), (String) temp.get("DEAT")))
				System.out.println("ERROR: INDIVIDUAL: US03: " + tag + ": Died " + temp.get("DEAT") + " before born "
						+ temp.get("BIRT"));

			if (validator.isOlderThan150(calcAge(temp))) { // Check if individual is older than 150 years old (dead or
															// alive)
				if (temp.get("DEAT") == null) // Choose which error to display based on living status
					System.out.println("ERROR: INDIVIDUAL: US07: " + tag + ":  More than 150 years old - Birth "
							+ (String) temp.get("BIRT"));
				else
					System.out
							.println("ERROR: INDIVIDUAL: US07: " + tag + ":  More than 150 years old at death - Birth "
									+ (String) temp.get("BIRT") + ": Death " + temp.get("DEAT"));
			}

			if (temp.get("BIRT").equals("invalid"))
				System.out.println("ERROR: INDIVIDUAL: US42: " + tag + " Birth date in wrong format");
			if (temp.get("DEAT") != null) {
				if (temp.get("DEAT").equals("invalid"))
					System.out.println("ERROR: INDIVIDUAL: US42: " + tag + " Death date in wrong format");
			}

		}

		Collections.sort(famArr);

		for (Integer i : famArr) {
			tag = "F" + i;
			temp = famTable.get(tag);

			if (!validator.isDateBeforeCurrentDate((String) temp.get("MARR")))
				System.out.println("ERROR: FAMILY: US01: " + tag + ": Married after today");

			if (!validator.isDateBeforeCurrentDate((String) temp.get("DIV")))
				System.out.println("ERROR: FAMILY: US01: " + tag + ": Divorced after today");

			String[] result = { (String) temp.get("HUSB"), (String) temp.get("WIFE") };
			for (String ind : result) {
				if (!validator.isBirthDateBeforeMarriageDate((String) indiTable.get(ind).get("BIRT"),
						(String) temp.get("MARR")))
					System.out.println("ERROR: INDIVIDUAL: US02: " + ind + ": Married " + temp.get("MARR")
							+ " before birthday " + indiTable.get(ind).get("BIRT"));
			}
			if (!validator.isDivorceAfterMarriage((String) temp.get("MARR"), (String) temp.get("DIV")))
				System.out.println("ERROR: FAMILY: US04: " + tag + ": Divorced " + temp.get("DIV") + " before marriage "
						+ temp.get("MARR"));

			if (temp.get("MARR").equals("invalid"))
				System.out.println("ERROR: FAMILY: US42: " + tag + " Marriage date in wrong format");

			if (temp.get("DIV") != null) {
				if (temp.get("DIV").equals("invalid"))
					System.out.println("ERROR: FAMILY: US42: " + tag + " Divorce date in wrong format");
			}
			
			if(!validator.isGenderValid(indiTable.get(temp.get("HUSB")), "M")){
				System.out.println("ERROR: FAMILY: US21: " + tag + ": Husband in family should be male");
			}
			
			if(!validator.isGenderValid(indiTable.get(temp.get("WIFE")), "F")){
				System.out.println("ERROR: FAMILY: US21: " + tag + ": Wife in family should be female");
			}

			if(!validator.isAgeValidForMarriage(indiTable.get(temp.get("HUSB")), indiTable.get(temp.get("WIFE")), (String)temp.get("MARR"))){
				System.out.println("ERROR: FAMILY: US10: " + tag + ": Marriage should be at least 14 years after birth of both spouses");
			}
		}
	}

	/**
	 * Fill hashmap with month data
	 */
	public static void fillMonthHashMap() {
		months.put("JAN", 0);
		months.put("FEB", 1);
		months.put("MAR", 2);
		months.put("APR", 3);
		months.put("MAY", 4);
		months.put("JUN", 5);
		months.put("JUL", 6);
		months.put("AUG", 7);
		months.put("SEP", 8);
		months.put("OCT", 9);
		months.put("NOV", 10);
		months.put("DEC", 11);
	}

	public static void main(String[] args) {
		File fileName = new File("ErrorFile.txt");
		fillMonthHashMap();
		String dateType = "";
		String ind_key = "";
		String fam_key = "";
		String type = "";
		try {
			Scanner s = new Scanner(fileName); // Scan file
			while (s.hasNextLine()) { // Repeat until end of file
				String input = s.nextLine();
				int lvl = Character.getNumericValue(input.charAt(0)); // Store level number
				String tag = findTag(input); // Get tag of line
				String argu = findArgs(input, tag); // Get arguments of line
				String supported = isSupportedTag(lvl, tag); // Check if tag is supported
				if (tag.equals("FAM") || tag.equals("INDI")) { // Modify supported if in special format
					if (isExceptionLineToo(input) < 2)
						supported = "Y";
				}
				if (supported.equals("Y")) {
					if (dateList.contains(tag)) // Check if tag is date related
						dateType = tag;
					else {
						if (lvl == 0) { // Store level 0 information
							if (tag.equals("INDI")) { // Get individual information
								ind_key = argu.replaceAll("@", ""); // Remove all @ characters in the line
								if (isUniqueID(ind_key, ind)) { // Check if ID is unique
									indArr.add(Integer.parseInt(ind_key.substring(1))); // Add individual ID to indArr
																						// list
									ind.put(ind_key, new HashMap<>()); // Create new line item for individual
								} else {
									// Print error if ID is not unique
									System.out.println("ERROR: US22: Individual ID " + ind_key + " is not unique");
									return;
								}
							} else if (tag.equals("FAM")) { // Get family information
								fam_key = argu.replaceAll("@", "");
								if (isUniqueID(fam_key, fam)) {
									fam.put(fam_key, new HashMap<>());
									famArr.add(Integer.parseInt(fam_key.substring(1)));
								} else {
									System.out.println("Family ID " + fam_key + " is not unique");
									return;
								}
							}
							type = tag;
						} else { // Store level 1 and 2 information

							/* Get family information */
							if (type.equals("FAM")) {
								HashMap<String, Object> temp_fam = fam.get(fam_key); // Create hashmap for family
																						// information
								if (tag.equals("HUSB") || tag.equals("WIFE")) // Check if line is regarding husband or
																				// wife
									temp_fam.put(tag, argu.replace("@", "")); // Remove @ character in husband or wife
																				// line

								else if (tag.equals("CHIL")) { // Check if line is regarding a child
									ArrayList arr = (ArrayList) temp_fam.get(tag); // Get other children (of that
																					// family)
									if (arr == null)
										arr = new ArrayList(); // Create new arraylist of children if first child
									arr.add(argu.replace("@", "")); // Add child to arraylist
									temp_fam.put(tag, arr); // Assign child to the family (under child tag)

								} else if (tag.equals("DATE")) { // Check if line is regarding a date
									if (!isValidDate(argu)) // Check if date is not valid
										temp_fam.put(dateType, "invalid"); // Mark date as invaild
									else
										temp_fam.put(dateType, argu); // Add date to families record
									dateType = "";
								}
								fam.put(fam_key, temp_fam); // Add family information to family hashmap

								/* Get individual information */
							} else if (type.equals("INDI")) {
								HashMap<String, Object> temp_ind = ind.get(ind_key); // Create hashmap for individual
																						// information
								if (tag.contains("FAM")) { // Check if line is regarding what family they are apart of
									ArrayList arr = (ArrayList) temp_ind.get(tag); // Add family information to
																					// arraylist
									if (arr == null) // Create new array list if none already exists
										arr = new ArrayList();
									arr.add(argu.replace("@", ""));
									temp_ind.put(tag, arr); // Add family information to hashmap

								} else if (tag.equals("DATE")) { // Check if line is regarding a date
									if (!isValidDate(argu)) // Check if date is not valid
										temp_ind.put(dateType, "invalid"); // Mark date as invalid
									else
										temp_ind.put(dateType, argu); // Add date to family records
									dateType = "";

								} else // Check if tag is extranious
									temp_ind.put(tag, argu); // Add information and corresponding tag to hashmap

								ind.put(ind_key, temp_ind); // Add individual to hashmap
							}

						}
					}
				}
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("Individuals");
		printfTable(ind, "INDI");
		System.out.println("\n");
		System.out.println("Families");
		printfTable(fam, "FAM");
		System.out.println("\n");

		printfErrors(ind, fam);
	}
}