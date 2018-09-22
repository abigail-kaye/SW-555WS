import java.util.*;
import java.util.Calendar;
import java.io.*;
public class GEDCOMreader {

	/**
	 * Arrays with supported tags Separated by level
	 */
	public static String[] lvlZero = { "HEAD", "TRLR", "NOTE" };
	public static String[] lvlOne = { "NAME", "SEX", "BIRT", "DEAT", "FAMC", "FAMS", "MARR", "HUSB", "WIFE", "CHIL",
			"DIV" };
	public static String[] lvlTwo = { "DATE" };
	public static String[] isDate = { "BIRT", "DEAT", "MARR", "DIV" };

	public static ArrayList<String> dateList = new ArrayList<>(Arrays.asList(isDate));
	private static ArrayList<Integer> indArr = new ArrayList<>();
	private static ArrayList<Integer> famArr = new ArrayList<>();

	public static HashMap<String, Integer> months = new HashMap<>(12);
	private static HashMap<String, HashMap<String, Object>> ind = new HashMap<>(5000);
	private static HashMap<String, HashMap<String, Object>> fam = new HashMap<>(1000);
    private static String date;
    static Calendar now = Calendar.getInstance();
    /**
	 * Finds tag within the input string
	 * 
	 * @param input
	 *            GEDCOM line that is being analyzed
	 * @return tag of current line
	 */
	public static String findTag(String input) {
		int n = isExceptionLineToo(input);
		if (n == 0)
			return "FAM";
		else if (n == 1)
			return "INDI";
		else { // Creates tag string until a space " " or the end of the line is reached
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
	 * Checks if line is in the special format by analyzing the last 3 or 4
	 * characters of the string 0 <id> FAM 0 <id> INDI
	 * 
	 * @param input
	 *            GEDCOM line that is being analyzed
	 * @return 0 or 1 if it a special format 2 otherwise
	 */
	public static int isExceptionLineToo(String input) {
		if (input.substring(input.length() - 3).equals("FAM"))
			return 0;
		if (input.substring(input.length() - 4).equals("INDI"))
			return 1;
		return 2;
	}

	/**
	 * Find any arguments within the current input line
	 * 
	 * @param input
	 *            GEDCOM line that is being analyzed
	 * @param tag
	 *            tag of line being analyzed
	 * @return extra line arguments null if error
	 */
	public static String findArgs(String input, String tag) {
		if (isExceptionLineToo(input) > 1) { // If line is not in special format
			String s = input.substring(tag.length() + 2); // arguments are anything after tag
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
	 * @param lvl
	 *            line level
	 * @param tag
	 *            tag to check
	 * @return "Y" if tag is supported "N" if tag is not supported
	 */
	public static String isSupportedTag(int lvl, String tag) {
		String[] toScan;
		int n = 0;
		if (lvl == 0)
			toScan = lvlZero;
		else if (lvl == 1)
			toScan = lvlOne;
		else if (lvl == 2)
			toScan = lvlTwo;
		else
			return "N";
		for (int i = 0; i < toScan.length; i++) {
			if (tag.equals(toScan[i]))
				return "Y";
		}
		return "N";
	}

    /**
     * Calculate the age
     * @param individual
     *          hashmap store all individual information
     * @return Age in this year if alive
     * 		   Or age when he's dead
     */
	private static String calcAge(HashMap<String, Object> individual) {
		String deathDate = (String) individual.get("DEAT");
		int birthYear = Integer.parseInt(((String) individual.get("BIRT")).split(" ")[2]);
		String monthString = (((String) individual.get("BIRT")).split(" ")[1]);
		int birthDay = Integer.parseInt(((String) individual.get("BIRT")).split(" ")[0]);

		if (deathDate != null) {
			int deathYear = Integer.parseInt(deathDate.split(" ")[2]);
			return String.valueOf(deathYear - birthYear);
		}
		int monthNum = months.get(monthString);
		if (now.get(Calendar.MONTH) < monthNum)
			return String.valueOf((2017 - birthYear));
		else if (now.get(Calendar.MONTH) == monthNum) {
			int currDay = now.get(Calendar.DAY_OF_MONTH);
			if (currDay < birthDay)
				return String.valueOf((2017 - birthYear));
		}
		return String.valueOf((2018 - birthYear));
	}

	/**
	 * Check if alive
	 * @param  individual
     *              hashmap store all individual information
	 * @return true if alive
	 * 		   or false
	 */

	private static boolean isAlive(HashMap<String, Object> individual) {
		String deathDate = (String) individual.get("DEAT");
		return deathDate == null;
	}

	/**
	 * Get all children
	 * @param  famlist
     *             store all associated fam ID
	 * @return "NA" if there is no child
	 * 		   Or a string of all children
	 */

	private static String getChildren(Object famlist) {
		if (famlist == null)
			return "NA";

		ArrayList famList = (ArrayList) famlist;
		ArrayList children = new ArrayList();
		String s = "[";

		for (Object famNum : famList) {
			ArrayList childrenGot = (ArrayList) fam.get(famNum).get("CHIL");
			children.addAll(childrenGot);
		}

		for (Object child : children) {
			s += child + ", ";
		}

		s += "]";
		s = s.replace(", ]", "]");
		return s;
	}

    /**
     * Get all spouse
     * @param  famlist
     *              Store all associated family IDs
     *         sex
     *              Store associated sex
     * @return "NA" if there is no spouse
     * 		   Or a string of all spouses
     */

	private static String getSpouse(Object famlist, Object sex) {
		if (famlist == null)
			return "NA";
		
		ArrayList famList = (ArrayList) famlist;
		String s = "[";
		String wifeOrHus;
		if (sex.equals("M"))
			wifeOrHus = "WIFE";
		else
			wifeOrHus = "HUSB";

		for (Object famNum : famList) {
			String spouseGot = (String) fam.get(famNum).get(wifeOrHus);
			if (spouseGot != null)
				s += spouseGot + ", ";
		}

		s += "]";
		s = s.replace(", ]", "]");
		return s;
	}

    /**
     * Get name through ID
     * @param  ID
     *          Individual's ID
     * @return return a string name
     * 		   Or a string of all spouses
     */
	private static String getName(Object ID) {
		return (String) ind.get(ID).get("NAME");
	}

    /**
     * Print the table
     * @param  table
     *             table needed to be printed
     *         type
     *             individual table or family table
     * @return Null
     *
     */
	private static void printfTable(HashMap<String, HashMap<String, Object>> table, String type) {
		HashMap<String, Object> temp;
		String tag;
		if (type.equals("INDI")) {

			Collections.sort(indArr);
			System.out.println(String.format("%5s %25s %6s %15s %3s %5s %15s %20s %20s", "ID", "NAME", "Gender",
					"Birthday", "Age", "Alive", "Death", "Child", "Spouse"));
			for (Integer i : indArr) {
				tag = "I" + i;
				temp = table.get(tag);
				System.out.println(String.format("%5s %25s %6s %15s %3s %5s %15s %20s %20s", tag, temp.get("NAME"),
						temp.get("SEX"), temp.get("BIRT"), calcAge(temp), isAlive(temp),
						temp.get("DEAT") != null ? temp.get("DEAT") : "NA", getChildren(temp.get("FAMS")),
						getSpouse(temp.get("FAMS"), temp.get("SEX"))));

			}
		} else if (type.equals("FAM")) {

			Collections.sort(famArr);
			System.out.println(String.format("%5s %20s %20s %10s %20s %10s %20s %20s", "ID", "Married", "Divorced",
					"Husband ID", "Husband Name", "Wife ID", "Wife Name", "Children"));
			for (Integer i : famArr) {

				tag = "F" + i;
				temp = table.get(tag);
				System.out.println(String.format("%5s %20s %20s %10s %20s %10s %20s %20s", tag, temp.get("MARR"),
						temp.get("DIV") != null ? temp.get("DIV") : "NA", temp.get("HUSB"), getName(temp.get("HUSB")),
						temp.get("WIFE"), getName(temp.get("WIFE")), temp.get("CHIL")));

			}
		}

	}

    private static void fillMonthHashMap() {
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
     * Check whether date is valid, before today's date
     * @param  date
     *           today's date
     * @return true if valid
     *          or false
     */

    public static boolean dateValid(String date) {
        fillMonthHashMap();
        int year = Integer.parseInt(date.split(" ")[2]);
        int month = months.get(date.split(" ")[1]);
        int day = Integer.parseInt(date.split(" ")[0]);
        int nowYear = now.get(Calendar.YEAR);
        int nowMonth = now.get(Calendar.MONTH);
        int nowDay = now.get(Calendar.DAY_OF_MONTH);
        return compare_date(year, month, day, nowYear, nowMonth, nowDay);
    }

    /**
     * Check whether married date is valid, before birthdate
     * @param  date
     *           today's date
     * @return true if valid
     *          or false
     */

    public static boolean marriedDateValid(String marriedDate, String birthDate) {
        fillMonthHashMap();
        int marriedYear = Integer.parseInt(marriedDate.split(" ")[2]);
        int marriedMonth = months.get(marriedDate.split(" ")[1]);
        int marriedDay = Integer.parseInt(marriedDate.split(" ")[0]);
        int birthYear = Integer.parseInt(birthDate.split(" ")[2]);
        int birthMonth = months.get(birthDate.split(" ")[1]);
        int birthDay = Integer.parseInt(birthDate.split(" ")[0]);
        return compare_date(birthYear, birthMonth, birthDay, marriedYear, marriedMonth, marriedDay);
    }

    private static boolean compare_date(int year, int month, int day, int year_later, int month_later, int day_later) {
        if (year > year_later)
            return false;
        else if (year < year_later)
            return true;
        else if (month > month_later)
            return false;
        else if (month < month_later)
            return true;
        else if (day > day_later)
            return false;
        else if (day < day_later)
            return true;
        else
            return true;
    }

    /**
     * Parse the GED file and Store associated information into ind or fam hashmap
     * Print the ind and fam table
     * @param  Null
     * @return Null
     *
     */
	public static void main(String[] args) {
		File fileName = new File("Nishant Patel_A01.ged");
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
				String tag = findTag(input);
				String argu = findArgs(input, tag);
				String supported = isSupportedTag(lvl, tag);
				if (tag.equals("FAM") || tag.equals("INDI")) { // Modify supported if in special format
					if (isExceptionLineToo(input) < 2) {
						supported = "Y";
					}
				}

				if (supported.equals("Y")) {
					if (dateList.contains(tag)) {
						dateType = tag;
					} else {
						if (lvl == 0) {
							if (tag.equals("INDI")) {
								ind_key = argu.replaceAll("@", "");
								indArr.add(Integer.parseInt(ind_key.substring(1)));
								ind.put(ind_key, new HashMap<>());
							} else if (tag.equals("FAM")) {
								fam_key = argu.replaceAll("@", "");
								fam.put(fam_key, new HashMap<>());
								famArr.add(Integer.parseInt(fam_key.substring(1)));
							}
							type = tag;
						} else {
							if (type.equals("FAM")) {
								HashMap<String, Object> temp_fam = fam.get(fam_key);
								if (tag.equals("HUSB") || tag.equals("WIFE")) {
									temp_fam.put(tag, argu.replace("@", ""));
								} else if (tag.equals("CHIL")) {
									ArrayList arr = (ArrayList) temp_fam.get(tag);
									if (arr == null) {
										arr = new ArrayList();
									}
									arr.add(argu.replace("@", ""));
									temp_fam.put(tag, arr);
								} else if (tag.equals("DATE")) {
                                    if (dateValid(argu))
									    temp_fam.put(dateType, argu);
                                    else
                                        temp_fam.put(dateType, "Invalid Date");
									dateType = "";
								}
								fam.put(fam_key, temp_fam);
							} else if (type.equals("INDI")) {
								HashMap<String, Object> temp_ind = ind.get(ind_key);
								if (tag.contains("FAM")) {
									ArrayList arr = (ArrayList) temp_ind.get(tag);
									if (arr == null) {
										arr = new ArrayList();
									}
									arr.add(argu.replace("@", ""));
									temp_ind.put(tag, arr);
								} else if (tag.equals("DATE")) {
								    if (dateValid(argu))
									    temp_ind.put(dateType, argu);
								    else
                                        temp_ind.put(dateType, "Invalid Date");
									dateType = "";
								} else {
									temp_ind.put(tag, argu);
								}
								ind.put(ind_key, temp_ind);
							}

						}
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("Individuals");
		printfTable(ind, "INDI");
		System.out.println("\n\n");
		System.out.println("Families");
		printfTable(fam, "FAM");
	}
}