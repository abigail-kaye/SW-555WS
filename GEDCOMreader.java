import java.io.*;
import java.util.*;
import java.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class GEDCOMreader {

	/* Arrays with supported tags Separated by level */
	private static String[] lvlZero = { "HEAD", "TRLR", "NOTE" };
	private static String[] lvlOne = { "NAME", "SEX", "BIRT", "DEAT", "FAMC", "FAMS", "MARR", "HUSB", "WIFE", "CHIL",
			"DIV" };
	private static String[] lvlTwo = { "DATE" };
	private static String[] isDate = { "BIRT", "DEAT", "MARR", "DIV" }; // Array with tag's related to dates
	public static String[] mon = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

	private static ArrayList<String> dateList = new ArrayList<>(Arrays.asList(isDate)); // Array list of date tags
	private static ArrayList<Integer> indArr = new ArrayList<>(); // Array list of individuals
	private static ArrayList<Integer> famArr = new ArrayList<>(); // Array list of families

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
	 * Return true if date is valid
	 */
	public static boolean isValidDate(String date) {
		int dateArr[] = createDateArr(date); 
		
		//Get original information
		String Origmonth = date.split(" ")[1];
		int Origday = Integer.parseInt(date.split(" ")[0]);
		int Origyear = Integer.parseInt(date.split(" ")[2]);
		
		if (dateArr == null) //Check dateArr was created sucessfully
			return false;
		if (dateArr[2] > 9999) //Check year is within bounds
			return false;
		if (dateArr[1] == Origday && mon[dateArr[0]].equals(Origmonth) && dateArr[2] == Origyear) //Check original information matches new date
			return true;
		return false;
	}
	
	/**
	 * Return (string) age of individual
	 */
	public static String calcAge(HashMap<String, Object> temp) {
		/* Accounts for incorrect date format */
		if ((String) temp.get("DEAT") == "invalid")
			return "NA";
		if ((String) temp.get("BIRT") == "invalid")
			return "NA";

		int[] birthArr = createDateArr((String) temp.get("BIRT"));
		int[] end = new int[3];
		int age;
		
		if (!isAlive(temp)) //Check if user is dead
			end = createDateArr((String) temp.get("DEAT"));
		else {
			//End date is today
			Calendar c = Calendar.getInstance();
			end[0] = c.get(Calendar.MONTH);
			end[1] = c.get(Calendar.DAY_OF_MONTH);
			end[2] = c.get(Calendar.YEAR);
		}
		
		if (datePassed(birthArr,end)) //Check if birthday has passed this year
			age = end[2] - birthArr[2];
		else
			age = end[2] - birthArr[2] - 1;
		
		return String.valueOf(age);
	}

	/**
	 * Returns an integer array corresponding to the string passed to the function
	 * in the form [day, month number, year]
	 */
	public static int[] createDateArr(String dateString) {
		try {
			Date date = new SimpleDateFormat("dd MMM yyyy").parse(dateString); //Parse string for information
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			
			//Add elements to the array
			int dateArr[] = { c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.YEAR) };
			return dateArr;
		} catch (ParseException e) {
			return null; //Return null if problem parsing date
		}
	}

	/**
	 * Return true if date 1 has occured before date 2
	 */
	public static boolean datePassed(int[] date1, int[] date2) {
		if (date1[0] < date2[0]) //Compare months
			return true;
		else if (date1[0] == date2[0]) {
			if (date1[1] >= date2[1]) //Compare days if the months are the same
				return true;
			return false;
		} else
			return false;
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
		HashMap<String, Object> temp, ind_temp;
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

		}
	}


	public static void main(String[] args) {
		File fileName = new File("Kaye_Abigail_testFile.txt");
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