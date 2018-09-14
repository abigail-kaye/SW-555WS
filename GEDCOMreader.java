import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class GEDCOMreader {

	/**
	 * Arrays with supported tags
	 * Separated by level
	 */
	public static String[] lvlZero = { "HEAD", "TRLR", "NOTE" };
	public static String[] lvlOne = { "NAME", "SEX", "BIRT", "DEAT", "FAMC", "FAMS", "MARR", "HUSB", "WIFE", "CHIL",
			"DIV" };
	public static String[] lvlTwo = { "DATE" };
	public static String[] isDate = { "BIRT", "DEAT", "MARR", "DIV"};
	public static ArrayList<String> dateList = new ArrayList<>(Arrays.asList(isDate));
	private static HashMap<String, HashMap<String, Object>> ind = new HashMap<>(5000);
	private static HashMap<String, HashMap<String, Object>> fam = new HashMap<>(1000);
	/**
	 * Finds tag within the input string
	 * @param input GEDCOM line that is being analyzed
	 * @return tag of current line
	 */
	public static String findTag(String input) {
		int n = isExceptionLineToo(input);
		if (n == 0)
			return "FAM";
		else if (n == 1)
			return "INDI";
		else { //Creates tag string until a space " " or the end of the line is reached
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
	 * Checks if line is in the special format by analyzing the last 3 or 4 characters of the string
	 * 		0 <id> FAM
	 * 		0 <id> INDI
	 * 
	 * @param input GEDCOM line that is being analyzed
	 * @return
	 * 		0 or 1 if it a special format
	 * 		2 otherwise 
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
	 * @param input GEDCOM line that is being analyzed
	 * @param tag tag of line being analyzed
	 * @return extra line arguments
	 * 		null if error
	 */
	public static String findArgs(String input, String tag) {
		if (isExceptionLineToo(input) > 1) { //If line is not in special format
			String s = input.substring(tag.length() + 2); //arguments are anything after tag
			if (s.length() > 1) {
				if (s.charAt(0) == ' ') //If there are arguments
					return s.substring(1); //Remove the beginning space before returning
			}
			return s;
		} else if (tag.equals("FAM")) //If special format
			return input.substring(2, input.length() - 4); //Take the middle of the input line
		else if (tag.equals("INDI"))
			return input.substring(2, input.length() - 5);
		else
			return null;
	}

	/**
	 * Check if tag is supported
	 * @param lvl line level
	 * @param tag tag to check
	 * @return "Y" if tag is supported
	 * 			"N" if tag is not supported
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

	private static String calcAge(HashMap<String, Object> temp) {
		String deathDate = (String) temp.get("DEAT");
		int birthYear = Integer.parseInt(((String) temp.get("BIRT")).split(" ")[2]);
		if (deathDate == null) {
			return String.valueOf((2018 - birthYear));
		} else {
			int deathYear = Integer.parseInt(deathDate.split(" ")[2]);
			return String.valueOf(deathYear - birthYear);
		}
	}

	private static boolean isAlive(HashMap<String, Object> temp) {
		String deathDate = (String) temp.get("DEAT");
		return deathDate == null;
	}

	private static String getChildren(Object temp) {
		if (temp == null)
			return "";

		ArrayList famList = (ArrayList) temp;
		ArrayList children = new ArrayList();
		String s = "";
		for (Object famNum : famList) {
			ArrayList childrenGot = (ArrayList) fam.get(famNum).get("CHIL");
			children.addAll(childrenGot);
		}
		for (Object child : children) {
			s += child + " ";
		}
		return s;
	}

	private static String getSpouse(Object temp, Object sex) {
		if (temp == null)
			return "";
		ArrayList famList = (ArrayList) temp;
		String s = "";
		String wifeOrHus;
		if (sex.equals("M"))
			wifeOrHus = "WIFE";
		else
			wifeOrHus = "HUSB";

		for (Object famNum : famList) {
			String spouseGot = (String) fam.get(famNum).get(wifeOrHus);
			if (spouseGot != null)
				s += spouseGot + " ";
		}

		return s;
	}

	private static String getName(Object ID) {
		return (String)ind.get(ID).get("NAME");
	}


	private static void printfTable(HashMap<String, HashMap<String, Object>> table, String type) {
		int num = table.size();
		HashMap<String, Object> temp;
		String tag;
		if (type.equals("INDI")) {
			System.out.println(String.format("%5s %25s %6s %15s %3s %5s %15s %20s %20s", "ID", "NAME", "Gender", "Birthday", "Age", "Alive", "Death", "Child", "Spouse"));
			for (int i = 1; i <= num; i++) {
				tag = "I" + i;
				temp = table.get(tag);
				System.out.println(String.format("%5s %25s %6s %15s %3s %5s %15s %20s %20s", tag, temp.get("NAME"), temp.get("SEX"), temp.get("BIRT"), calcAge(temp), isAlive(temp), temp.get("DEAT"),getChildren(temp.get("FAMS")), getSpouse(temp.get("FAMS"),temp.get("SEX"))));
			}
		} else if (type.equals("FAM")) {
			System.out.println(String.format("%5s %20s %20s %10s %20s %10s %20s %20s", "ID", "Married", "Divorced", "Husband ID", "Husband Name", "Wife ID", "Wife Name", "Children"));
			for (int i = 1; i <= num; i++) {
				tag = "F" + i;
				temp = table.get(tag);
				System.out.println(String.format("%5s %20s %20s %10s %20s %10s %20s %20s", tag, temp.get("MARR"), temp.get("DIV"), temp.get("HUSB"), getName(temp.get("HUSB")), temp.get("WIFE"), getName(temp.get("WIFE")), temp.get("CHIL")));
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
			Scanner s = new Scanner(fileName); //Scan file
			while (s.hasNextLine()) { //Repeat until end of file
				String input = s.nextLine();
				int lvl = Character.getNumericValue(input.charAt(0)); //Store level number
				String tag = findTag(input);
				String argu = findArgs(input, tag);
				String supported = isSupportedTag(lvl, tag);
				if (tag.equals("FAM") || tag.equals("INDI")) { //Modify supported if in special format
					if (isExceptionLineToo(input) < 2) {
						supported = "Y";
					}
				}

				if (supported.equals("Y")) {
					if (dateList.contains(tag)) {
						dateType = tag;
					} else {
						if(lvl == 0) {
							if (tag.equals("INDI")) {
								ind_key = argu.replaceAll("@", "");
								ind.put(ind_key, new HashMap<>());
							} else if (tag.equals("FAM")) {
								fam_key = argu.replaceAll("@", "");
								fam.put(fam_key, new HashMap<>());
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
									temp_fam.put(dateType, argu);
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
									temp_ind.put(dateType, argu);
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

		printfTable(ind, "INDI");
		System.out.println("\n\n");
		printfTable(fam, "FAM");
	}

}
