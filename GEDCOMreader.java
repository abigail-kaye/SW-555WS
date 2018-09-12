import java.io.*;
import java.util.*;

import javax.print.DocFlavor.STRING;

public class GEDCOMreader {

	/**
	 * Arrays with supported tags
	 * Separated by level
	 */
	public static String[] lvlZero = { "HEAD", "TRLR", "NOTE" };
	public static String[] lvlOne = { "NAME", "SEX", "BIRT", "DEAT", "FAMC", "FAMS", "MARR", "HUSB", "WIFE", "CHIL",
			"DIV" };
	public static String[] lvlTwo = { "DATE" };

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

	public static void main(String[] args) {
		File fileName = new File("Kaye_Abigail_testFile.txt");
		try {
			Scanner s = new Scanner(fileName); //Scan file
			while (s.hasNextLine()) { //Repeat until end of file
				String input = s.nextLine();
				int lvl = Character.getNumericValue(input.charAt(0)); //Store level number
				String tag = findTag(input);
				String argu = findArgs(input, tag);
				String supported = isSupportedTag(lvl, tag);
				if (tag.equals("FAM") || tag.equals("INDI")) { //Modify supported if in special format
					if (isExceptionLineToo(input) < 2)
						supported = "Y";
				}
				String output = "<-- " + lvl + "|" + tag + "|" + supported + "|" + argu;
				String iInput = "--> " + input;
				System.out.println(iInput);
				System.out.println(output);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
