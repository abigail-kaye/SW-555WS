import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

	public static HashMap<String, Integer> months = new HashMap<>(12); // Hashmap of month and number association
	public static HashMap<String, HashMap<String, Object>> ind = new HashMap<>(5000); // Information for each individual
	private static HashMap<String, HashMap<String, Object>> fam = new HashMap<>(1000); // Information for each family

	/* Returns true if entered ID is unique */
	public static boolean isUniqueID(String id, HashMap<String, HashMap<String, Object>> map) {
		return map.get(id) == null;
	}

	/**
	 * Find tag within the input string
	 * 
	 * @param input - file line
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
	 * Checks if line is in the special format
	 * Applies to INDI and FAM lines only
	 * 
	 * @param input - file line
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
	 * @param input - file input line
	 * @param tag - tag of line
	 * @return extra line arguments null if error
	 */
	private static String findArgs(String input, String tag) {
		if (isExceptionLineToo(input) > 1) { // Check if line is in special format
			String s = input.substring(tag.length() + 2); // Return anything after tag
			if (s.length() > 1) {
				if (s.charAt(0) == ' ')
					return s.substring(1);
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
		if (lvl == 0) // Check various level tags
			toScan = lvlZero;
		else if (lvl == 1)
			toScan = lvlOne;
		else if (lvl == 2)
			toScan = lvlTwo;
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
	public static boolean isValidDate(String date) {
		int dateArr[] = createDateArr(date);

		// Get original information
		String Origmonth = date.split(" ")[1];
		int Origday = Integer.parseInt(date.split(" ")[0]);
		int Origyear = Integer.parseInt(date.split(" ")[2]);

		if (dateArr == null) // Check dateArr was created sucessfully
			return false;
		if (dateArr[2] > 9999) // Check year is within bounds
			return false;
		// Check original information matches new date
		if (dateArr[1] == Origday && mon[dateArr[0]].equals(Origmonth) && dateArr[2] == Origyear) 
			return true;
		return false;
	}

	/**
	 * Returns age (in string form) of individual
	 * 
	 * @param temp - individual to check
	 */
	public static String calcAge(HashMap<String, Object> temp) {
		/* Accounts for incorrect date format */
		if ((String) temp.get("DEAT") == "invalid")
			return "NA";
		if ((String) temp.get("BIRT") == "invalid" || temp.get("BIRT") == null)
			return "NA";

		int[] birthArr = createDateArr((String) temp.get("BIRT"));
		int[] end = new int[3];
		int age;

		if (!isAlive(temp)) // Identify end (death/current) date
			end = createDateArr((String) temp.get("DEAT"));
		else {
			Calendar c = Calendar.getInstance();
			end[0] = c.get(Calendar.MONTH);
			end[1] = c.get(Calendar.DAY_OF_MONTH);
			end[2] = c.get(Calendar.YEAR);
		}

		if (datePassed(birthArr, end)) // Check if birthday has passed this year
			age = end[2] - birthArr[2];
		else
			age = end[2] - birthArr[2] - 1;

		return String.valueOf(age);
	}

	public static Calendar getRecentDate() {
		Calendar c = Calendar.getInstance();
		Date d = new Date();

		int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH);
		int year = c.get(Calendar.YEAR);
		day -= 30;
		c.set(year, month, day);
		return c;
	}

	/*
	 * Return an integer array corresponding to the date passed to the function Form
	 * [day, month number, year] JAN = 0
	 */
	public static int[] createDateArr(String dateString) {
		try {
			if (dateString == null)
				return null;
			Date date = new SimpleDateFormat("dd MMM yyyy").parse(dateString); // Parse string for information
			Calendar c = Calendar.getInstance();
			c.setTime(date);

			// Add elements to the array
			int dateArr[] = { c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.YEAR) };
			return dateArr;
		} catch (ParseException e) {
			return null; // Return null if problem parsing date
		}
	}

	/* Return true if date 1 has occurred before date 2 */
	public static boolean datePassed(int[] date1, int[] date2) {
		if (date1[2] < date2[2]) {
			return true;
		} else {
			if (date1[0] < date2[0]) // Compare months
				return true;
			else if (date1[0] == date2[0]) {
				if (date1[1] >= date2[1]) // Compare days if the months are the same
					return true;
				return false;
			} else
				return false;
		}
	}

	/**
	 * Check if individual is alive
	 * 
	 * @param temp - individual to check
	 * @return true if individual is alive; false if individual is deceased
	 */
	private static boolean isAlive(HashMap<String, Object> temp) {
		String deathDate = (String) temp.get("DEAT");
		return deathDate == null;
	}

	/* Print array of individuals */
	private static String printArr(ArrayList arr) {
		if (arr == null)
			return "NA";
		else
			return arr.toString();
	}

	/**
	 * Return string of children
	 * 
	 * @param temp - family to check
	 */
	private static ArrayList getChildren(Object temp) {
		if (temp == null) // Return NA if no children
			return null;

		ArrayList famList = (ArrayList) temp; // Store all families in an arraylist
		ArrayList children = new ArrayList(); // Create arraylist for children

		for (Object famNum : famList) {
			ArrayList childrenGot = (ArrayList) fam.get(famNum).get("CHIL"); // Get all children in 1 family
			if (childrenGot != null)
				children.addAll(childrenGot); // Add children to children array list
		}
		return children;
	}

	/**
	 * Return spouse array
	 * NOTE: USE ARR.TOSTRING() INSTEAD!!
	 * @param temp - individual
	 * @param sex - sex of individual
	 */
	private static ArrayList getSpouse(Object temp, Object sex) {
		if (temp == null) // Return NA if no object
			return null;

		ArrayList famList = (ArrayList) temp; // Store all families in an arrayList
		ArrayList spouse = new ArrayList();
		String wifeOrHus;
		if (sex.equals("M")) // Identify sex of partner based on current individual
			wifeOrHus = "WIFE";
		else
			wifeOrHus = "HUSB";

		for (Object famNum : famList) { // Loop through each family
			spouse.add(fam.get(famNum).get(wifeOrHus));
		}
		return spouse;
	}

	/**
	 * Return name of individual
	 */
	private static String getName(Object ID) {
		return (String) ind.get(ID).get("NAME");
	}

	/* Return (string) last name of individual */
	private static String getLastName(Object ID) {
		String fullName = getName(ID);
		int slash = fullName.indexOf("/");
		String lastName = fullName.substring(slash + 1, fullName.length() - 1); // Identify last name based on /'s
		return lastName;
	}

	/* Return true if all males have same last name */
	public static boolean checkLastNames(Object ID, ArrayList children) {
		if (children == null) // Return true if no children
			return true;
		String fatherName = getLastName(ID); // Father's last name to compare
		for (int i = 0; i < children.size(); i++) {
			String tag = (String) children.get(i);
			if (ind.get(tag).get("SEX").equals("M")) { // Check only son's
				if (!getLastName(tag).equals(fatherName)) {
					return false;
				}
			}
		}
		return true;
	}

	/* Prints out date errors only DOES NOT CHECK IF VALID DATE */
	private static void printDateErrors(HashMap<String, Object> temp, Object ID) {
		for (int i = 0; i < isDate.length; i++) {
			if (temp.get(isDate[i]) != null && temp.get(isDate[i]).equals("invalid")) {
				System.out.println("ERROR: INDIVIDUAL: US42 " + ID + " " + isDate[i] + " is in the wrong format");
			}
		}
	}

	/**
	 * Print table regarding individuals and families
	 * 
	 * @param table - table to print out
	 * @param type - tag of table to print out out (INDI or FAM)
	 */
	private static void printfTable(HashMap<String, HashMap<String, Object>> table, String type) {
		HashMap<String, Object> temp;
		String tag;

		/* Print individual table */
		if (type.equals("INDI")) {
			Collections.sort(indArr);
			System.out.println(String.format("%5s %25s %6s %15s %3s %5s %15s %20s %20s", "ID", "NAME", "Gender",
					"Birthday", "Age", "Alive", "Death", "Child", "Spouse")); // Print table headers

			/* Get information for each individual */
			for (Integer i : indArr) {
				tag = "I" + i; // Get ID of individual
				temp = table.get(tag); // Get information of individual
				if (temp == null) {
					continue;
				}
				String children = printArr(sortSiblings(getChildren(temp.get("FAMS")), ind));
				String spouse = printArr(getSpouse(temp.get("FAMS"), temp.get("SEX")));
				System.out.println(String.format("%5s %25s %6s %15s %3s %5s %15s %20s %20s", tag, temp.get("NAME"),
						temp.get("SEX"), temp.get("BIRT"), calcAge(temp), isAlive(temp),
						temp.get("DEAT") != null ? temp.get("DEAT") : "NA", children, spouse)); // Print information
			}
		}

		/* Print family table */
		else if (type.equals("FAM")) {
			Collections.sort(famArr);
			System.out.println(String.format("%5s %20s %20s %10s %20s %10s %20s %20s", "ID", "Married", "Divorced",
					"Husband ID", "Husband Name", "Wife ID", "Wife Name", "Children")); // Print table headers

			/* Get information for each family */
			for (Integer i : famArr) {
				tag = "F" + i; // Get family ID
				temp = table.get(tag); // Get information of family
				System.out.println(String.format("%5s %20s %20s %10s %20s %10s %20s %20s", tag, temp.get("MARR"),
						temp.get("DIV") != null ? temp.get("DIV") : "NA", temp.get("HUSB"), getName(temp.get("HUSB")),
						temp.get("WIFE"), getName(temp.get("WIFE")),
						sortSiblings((ArrayList<String>) temp.get("CHIL"), ind))); // Print information
			}
		}
	}

	/* Print errors of file */
	private static void printfErrors(HashMap<String, HashMap<String, Object>> indiTable,
			HashMap<String, HashMap<String, Object>> famTable) {
		HashMap<String, Object> temp;
		String tag;
		GEDCOMValidator validator = new GEDCOMValidator();

		Collections.sort(indArr);
		Set<String> setOfInd = new HashSet<>();
		/* Loop through individual tables */
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

			if (!validator.isAgeAvailable(calcAge(temp))) {
				System.out.println("ERROR: INDIVIDUAL: US27: " + tag + ":  Age is not available.");
			}

			if (validator.isOlderThan150(calcAge(temp))) {
				if (temp.get("DEAT") == null) // Choose which error to display based on living status
					System.out.println("ERROR: INDIVIDUAL: US07: " + tag + ":  More than 150 years old - Birth "
							+ (String) temp.get("BIRT"));
				else
					System.out
							.println("ERROR: INDIVIDUAL: US07: " + tag + ":  More than 150 years old at death - Birth "
									+ (String) temp.get("BIRT") + ": Death " + temp.get("DEAT"));
			}

			if (temp.get("SEX").equals("M")) {
				if (!checkLastNames(tag, getChildren(temp.get("FAMS")))) {
					System.out.println("ERROR: INDIVIDUAL: US16:" + tag + "'s son does not have the same last name");
				}
			}

			if (!validator.isNameBirthUniq(temp, setOfInd)) {
				System.out.println("ERROR: INDIVIDUAL: US23:" + tag + "'s name and birthday union is not unique");
			}
			setOfInd.add(temp.get("NAME").toString() + temp.get("BIRT").toString());


			printDateErrors(temp, tag);
		}

		/* Loop through family array */
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

			String wiDeat = (String) indiTable.get(temp.get("WIFE")).get("DEAT");
			String husDeat = (String) indiTable.get(temp.get("HUSB")).get("DEAT");
			if (!validator.isMarriageBeforeDeath((String) temp.get("MARR"), wiDeat)) {
				System.out.println(
						"ERROR: INDIVIDUAL: US05: " + tag + ": Died " + wiDeat + " before married " + temp.get("MARR"));
			}
			if (!validator.isMarriageBeforeDeath((String) temp.get("MARR"), husDeat)) {
				System.out.println("ERROR: INDIVIDUAL: US05: " + tag + ": Died " + husDeat + " before married "
						+ temp.get("MARR"));
			}

			if (!validator.isMarriageBeforeDeath((String) temp.get("DIV"), wiDeat)) {
				System.out.println(
						"ERROR: INDIVIDUAL: US06: " + tag + ": Died " + wiDeat + " before divorced " + temp.get("DIV"));
			}
			if (!validator.isMarriageBeforeDeath((String) temp.get("DIV"), husDeat)) {
				System.out.println("ERROR: INDIVIDUAL: US06: " + tag + ": Died " + husDeat + " before divorced "
						+ temp.get("DIV"));
			}

			if (validator.tooManySib((ArrayList) temp.get("CHIL")))
				System.out.println("ERROR: FAMILY: US15: " + tag + " Has more than 15 siblings");

			if (temp.get("MARR").equals("invalid"))
				System.out.println("ERROR: FAMILY: US42: " + tag + " Marriage date in wrong format");

			if (temp.get("DIV") != null) {
				if (temp.get("DIV").equals("invalid"))
					System.out.println("ERROR: FAMILY: US42: " + tag + " Divorce date in wrong format");
			}

			if (!validator.isGenderValid(indiTable.get(temp.get("HUSB")), "M")) {
				System.out.println("ERROR: FAMILY: US21: " + tag + ": Husband in family should be male");
			}

			if (!validator.isGenderValid(indiTable.get(temp.get("WIFE")), "F")) {
				System.out.println("ERROR: FAMILY: US21: " + tag + ": Wife in family should be female");
			}

			if (!validator.isAgeValidForMarriage(indiTable.get(temp.get("HUSB")), indiTable.get(temp.get("WIFE")),
					(String) temp.get("MARR"))) {
				System.out.println("ERROR: FAMILY: US10: " + tag
						+ ": Marriage should be at least 14 years after birth of both spouses");
			}

			ArrayList children = (ArrayList) temp.get("CHIL");
			if (children != null) {
				for (Object childID : children) {
					if (!validator.isChildBornAfterMarriage((String) indiTable.get(childID).get("BIRT"),
							(String) temp.get("MARR"), (String) temp.get("DIV"))) {
						System.out.println("ERROR: FAMILY: US08: " + tag + ": Child - " + (String) childID
								+ " born before marriage of parents or after 9 months of their divorce");
					}
				}
			}

			if (children != null) {
				for (Object childID : children) {
					if (!validator.isChildBornBeforeParentsDeath((String) indiTable.get(childID).get("BIRT"), wiDeat,
							husDeat)) {
						System.out.println("ERROR: FAMILY: US09: " + tag + ": Child - " + (String) childID
								+ " born after death of mother or after 9 months of father death");
					}
				}
			}
		}
	}

	/* Fill hashmap with month data */
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

	/**
	 * Print table regarding individuals and families
	 *
	 * @param children - children array
	 * @param indTable - individual table to lookup information
	 * @return sorted children array
	 */

	public static ArrayList<String> sortSiblings(ArrayList<String> children,
			HashMap<String, HashMap<String, Object>> indTable) {
		if (children == null || children.isEmpty())
			return children;
		Collections.sort(children, Comparator.comparing(o -> calcAge(indTable.get(o))));
		return children;
	}

	/**
	 * Print table regarding individuals and families
	 *
	 * @param individual - all individuals table
	 * @return dead people
	 */

	public static HashMap<String, HashMap<String, Object>> deadPeople(
			HashMap<String, HashMap<String, Object>> individual) {
		HashMap<String, HashMap<String, Object>> dead = new HashMap<>(5000);
		for (String key : individual.keySet()) {
			if (!isAlive(individual.get(key))) {
				dead.put(key, individual.get(key));
			}
		}
		return dead;
	}

	public static HashMap<String, HashMap<String, Object>> recentBirthDeath(
			HashMap<String, HashMap<String, Object>> individual, String type) {

		HashMap<String, HashMap<String, Object>> map = new HashMap<>(5000);
		Calendar recentDate = getRecentDate();
		for (String key : individual.keySet()) {
			HashMap<String, Object> person = individual.get(key);
			String dateString = (String) person.get(type);
			if (dateString != null) {
				int[] dateArr = createDateArr(dateString);
				Calendar dateCal = Calendar.getInstance();
				dateCal.set(dateArr[2], dateArr[0], dateArr[1]);
				if (recentDate.before(dateCal))
					map.put(key, individual.get(key));
			}
		}
		return map;

	}


	/**
	 * Find out all living married people
	 *
	 * @param individual - all individuals table
	 * @param family - all family table
	 * @return living married people
	 */

	public static HashMap<String, HashMap<String, Object>> livingMarried(HashMap<String, HashMap<String, Object>> individual, HashMap<String, HashMap<String, Object>> family) {
		HashMap<String, HashMap<String, Object>> livingMarriedPeople = new HashMap<>(5000);
		Set<Object> hash_Set = new HashSet<>();
		for (String key : family.keySet()) {
			if (family.get(key).get("DIV") == null) {
				hash_Set.add(family.get(key).get("HUSB"));
				hash_Set.add(family.get(key).get("WIFE"));
			}
		}
		Iterator<Object> itr = hash_Set.iterator();
		while(itr.hasNext()){
			String key = (String) itr.next();
			HashMap<String, Object> temp = individual.get(key);
			if (isAlive(temp)) {
				livingMarriedPeople.put(key, temp);
			}
		}
		return livingMarriedPeople;
	}

	public static void main(String[] args) {
		File fileName = new File("Kaye_Abigail_testFile.txt");
		String dateType = "";
		String ind_key = "";
		String fam_key = "";
		String type = "";
		try {
			Scanner s = new Scanner(fileName); // Scan file
			while (s.hasNextLine()) {
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
		
		 System.out.println("Deceased Individuals");
		 printfTable(deadPeople(ind), "INDI");
		 System.out.println("\n");
		
		 System.out.println("Recent Births");
		 printfTable(recentBirthDeath(ind, "BIRT"), "INDI");
		 System.out.println("\n");

		System.out.println("Recent Deaths");
		printfTable(recentBirthDeath(ind, "DEAT"), "INDI");
		System.out.println("\n");

		System.out.println("All living married people");
		printfTable(livingMarried(ind, fam),"INDI");
		System.out.println("\n");

		printfErrors(ind, fam);
	}
}