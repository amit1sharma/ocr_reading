package com.adcb.ocr.decode;

import com.adcb.ocr.decode.types.MrzDate;
import com.adcb.ocr.decode.types.MrzFormat;
import com.adcb.ocr.decode.types.MrzSex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses the MRZ records.
 * <p/>
 * All parse methods throws {@link MrzParseException} unless stated otherwise.
 */
public class MrzParser {

	/**
	 * The MRZ record, not null.
	 */
	public final String mrz;
	/**
	 * The MRZ record separated into rows.
	 */
	public final String[] rows;
	/**
	 * MRZ record format.
	 */
	public final MrzFormat format;
	@Value("${tesseract.correction.required}")
	private String correctionRequired;
	/**
	 * Creates new parser which parses given MRZ record.
	 * @param mrz the MRZ record, not null.
	 */
	public MrzParser(String mrz) {
		this.mrz = mrz;
		this.rows = mrz.split("\n");
		this.format = MrzFormat.get(mrz);
	}

	/**
	 * Parses the MRZ name in form of SURNAME<<FIRSTNAME<
	 * @Param range the range
	 * @return array of [surname, first_name], never null, always with a length of 2.
	 */
	public String[] parseName(MrzRange range) {
		checkValidCharacters(range);
		String str = rawValue(range);
		while (str.endsWith("<")) {
			str = str.substring(0, str.length() - 1);
		}
		final String[] names = str.split("<<");
		String surname = "";
		String givenNames = "";
		surname = parseString(new MrzRange(range.column, range.column + names[0].length(), range.row));
		if(names.length==1){
			givenNames = parseString(new MrzRange(range.column, range.column + names[0].length(), range.row));
			surname = "";
		}
		else if(names.length>1){
			surname = parseString(new MrzRange(range.column, range.column + names[0].length(), range.row));
			givenNames = parseString(new MrzRange(range.column + names[0].length() + 2, range.column + str.length(), range.row));
		}
		return new String[]{surname, givenNames};
	}

	/**
	 * Returns a raw MRZ value from given range. If multiple ranges are specified, the value is concatenated.
	 * @param range the ranges, not null.
	 * @return raw value, never null, may be empty.
	 */
	public String rawValue(MrzRange... range) {
		StringBuilder sb = new StringBuilder();
		for (MrzRange r : range) {
			sb.append(rows[r.row].substring(r.column, r.columnTo));
		}
		return sb.toString();
	}
	/**
	 * Returns a modified numeric value for alpha numeric string.
	 * @param originalString , not null.
	 * @return String value, never null, may be empty.
	 */

	public static String fixTesseractReadingErrors(String number){
//		System.out.println("number before = " +number);
		number = number.replaceAll("D", "0");
		number = number.replaceAll("O", "0");
		number = number.replaceAll("Q", "0");
		number = number.replaceAll("B", "8");
		number = number.replaceAll("S", "5");
		number = number.replaceAll("I", "1");
		number = number.replaceAll("J", "7");
		number = number.replaceAll("T", "7");
		number = number.replaceAll("Z", "2");
		number = number.replaceAll("G", "6");
//		System.out.println("number after = " +number);
		return number;

	}

	/**
	 * Checks that given range contains valid characters.
	 * @param range the range to check.
	 */
	public void checkValidCharacters(MrzRange range) {
		final String str = rawValue(range);
		for (int i = 0; i < str.length(); i++) {
			final char c = str.charAt(i);
			if (c != FILLER && (c < '0' || c > '9') && (c < 'A' || c > 'Z')) {
				throw new MrzParseException("Invalid character in MRZ record: " + c, mrz, new MrzRange(range.column + i, range.column + i + 1, range.row), format);
			}
		}
	}

	/**
	 * Parses a string in given range. &lt;&lt; are replaced with ", ", &lt; is replaced by space.
	 * @param range the range
	 * @return parsed string.
	 */
	public String parseString(MrzRange range) {
		checkValidCharacters(range);
		String str = rawValue(range);
		while (str.endsWith("<")) {
			str = str.substring(0, str.length() - 1);
		}
		return str.replace("" + FILLER + FILLER, ", ").replace(FILLER, ' ');
	}

	/**
	 * Verifies the check digit.
	 * @param col the 0-based column of the check digit.
	 * @param row the 0-based column of the check digit.
	 * @param strRange the range for which the check digit is computed.
	 * @param fieldName (optional) field name. Used only when validity check fails.
	 * @return true if check digit is valid, false if not
	 */
	public boolean checkDigit(int col, int row, MrzRange strRange, String fieldName) {
		return checkDigit(col, row, rawValue(strRange), fieldName);
	}

	/**
	 * Verifies the check digit.
	 * @param col the 0-based column of the check digit.
	 * @param row the 0-based column of the check digit.
	 * @param str the raw MRZ substring.
	 * @param fieldName (optional) field name. Used only when validity check fails.
	 * @return true if check digit is valid, false if not
	 */
	public boolean checkDigit(int col, int row, String str, String fieldName) {

		if( fieldName.equals("document number") || fieldName.equals("date of birth")){
			str = fixTesseractReadingErrors(str);
		}
		/**
		 * If the check digit validation fails, this will contain the location.
		 */
		MrzRange invalidCheckdigit = null;

		final char digit = (char) (computeCheckDigit(str) + '0');
		char checkDigit = rows[row].charAt(col);
		checkDigit = fixTesseractReadingErrors(Character.toString(checkDigit)).charAt(0);
		if (checkDigit == FILLER) {
			checkDigit = '0';
		}
		if (digit != checkDigit) {
			invalidCheckdigit = new MrzRange(col, col + 1, row);
			log.error("Check digit verification failed for " + fieldName + ": expected " + digit + " but got " + checkDigit);
		}
		return invalidCheckdigit==null;
	}

	private static Logger log = LoggerFactory.getLogger(MrzParser.class);

	/**
	 * Parses MRZ date.
	 * @param range the range containing the date, in the YYMMDD format. The range must be 6 characters long.
	 * @return parsed date
	 * @throws IllegalArgumentException if the range is not 6 characters long.
	 */
	public MrzDate parseDate(MrzRange range) {
		if (range.length() != 6) {
			throw new IllegalArgumentException("Parameter range: invalid value " + range + ": must be 6 characters long");
		}
		MrzRange r;
		r = new MrzRange(range.column, range.column + 2, range.row);
		int year;
		try {
			String tempYear = rawValue(r);
			tempYear = fixTesseractReadingErrors(tempYear);
			year = Integer.parseInt(tempYear);
		} catch (NumberFormatException ex) {
			year = -1;
			log.debug("Failed to parse MRZ date year " + rawValue(range) + ": " + ex, mrz, r);
		}
		if (year < 0 || year > 99) {
			log.debug("Invalid year value " + year + ": must be 0..99");
		}
		r = new MrzRange(range.column + 2, range.column + 4, range.row);
		int month;
		try {
			String tempMnth = rawValue(r);
			tempMnth = fixTesseractReadingErrors(tempMnth);
			month = Integer.parseInt(tempMnth);
		} catch (NumberFormatException ex) {
			month = -1;
			log.debug("Failed to parse MRZ date month " + rawValue(range) + ": " + ex, mrz, r);
		}
		if (month < 1 || month > 12) {
			log.debug("Invalid month value " + month + ": must be 1..12");
		}
		r = new MrzRange(range.column + 4, range.column + 6, range.row);
		int day;
		try {
			String tempDay = rawValue(r);
			tempDay = fixTesseractReadingErrors(tempDay);
			day = Integer.parseInt(tempDay);
		} catch (NumberFormatException ex) {
			day = -1;
			log.debug("Failed to parse MRZ date month " + rawValue(range) + ": " + ex, mrz, r);
		}
		if (day < 1 || day > 31) {
			log.debug("Invalid day value " + day + ": must be 1..31");
		}
		String tempDate = rawValue(range);
		tempDate = fixTesseractReadingErrors(tempDate);
		return new MrzDate(year, month, day, tempDate);

	}

	/**
	 * Parses the "sex" value from given column/row.
	 * @param col the 0-based column
	 * @param row the 0-based row
	 * @return sex, never null.
	 */
	public MrzSex parseSex(int col, int row) {
		return MrzSex.fromMrz(rows[row].charAt(col));
	}
	private static final int[] MRZ_WEIGHTS = new int[]{7, 3, 1};

	/**
	 * Checks if given character is valid in MRZ.
	 * @param c the character.
	 * @return true if the character is valid, false otherwise.
	 */
	private static boolean isValid(char c) {
		return ((c == FILLER) || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z'));
	}

	private static int getCharacterValue(char c) {
		if (c == FILLER) {
			return 0;
		}
		if (c >= '0' && c <= '9') {
			return c - '0';
		}
		if (c >= 'A' && c <= 'Z') {
			return c - 'A' + 10;
		}
		throw new RuntimeException("Invalid character in MRZ record: " + c);
	}

	/**
	 * Computes MRZ check digit for given string of characters.
	 * @param str the string
	 * @return check digit in range of 0..9
	 */
	public static int computeCheckDigit(String str) {
		int result = 0;
		for (int i = 0; i < str.length(); i++) {
			result += getCharacterValue(str.charAt(i)) * MRZ_WEIGHTS[i % MRZ_WEIGHTS.length];
		}
		return result % 10;
	}

	/**
	 * Computes MRZ check digit for given string of characters.
	 * @param str the string
	 * @return check digit in range of 0..9
	 */
	public static char computeCheckDigitChar(String str) {
		return (char) ('0' + computeCheckDigit(str));
	}

	/**
	 * Factory method, which parses the MRZ and returns appropriate record class.
	 * @param mrz MRZ to parse.
	 * @return record class.
	 */
	public static MrzRecord parse(String mrz) {
		final MrzRecord result = MrzFormat.get(mrz).newRecord();
		result.fromMrz(mrz);
		return result;
	}


	private static final Map<String, String> EXPAND_CHARACTERS = new HashMap<String, String>();

	static {
		EXPAND_CHARACTERS.put("\u00C4", "AE"); // Ä
		EXPAND_CHARACTERS.put("\u00E4", "AE"); // ä
		EXPAND_CHARACTERS.put("\u00C5", "AA"); // Å
		EXPAND_CHARACTERS.put("\u00E5", "AA"); // å
		EXPAND_CHARACTERS.put("\u00C6", "AE"); // Æ
		EXPAND_CHARACTERS.put("\u00E6", "AE"); // æ
		EXPAND_CHARACTERS.put("\u0132", "IJ"); // Ĳ
		EXPAND_CHARACTERS.put("\u0133", "IJ"); // ĳ
		EXPAND_CHARACTERS.put("\u00D6", "OE"); // Ö
		EXPAND_CHARACTERS.put("\u00F6", "OE"); // ö
		EXPAND_CHARACTERS.put("\u00D8", "OE"); // Ø
		EXPAND_CHARACTERS.put("\u00F8", "OE"); // ø
		EXPAND_CHARACTERS.put("\u00DC", "UE"); // Ü
		EXPAND_CHARACTERS.put("\u00FC", "UE"); // ü
		EXPAND_CHARACTERS.put("\u00DF", "SS"); // ß
	}

	/**
	 * Converts given string to a MRZ string: removes all accents, converts the string to upper-case and replaces all spaces and invalid characters with '&lt;'.
	 * @param string the string to convert. Passing null is the same as passing in an empty string.
	 * @param length required length of the string. If given string is longer, it is truncated. If given string is shorter than given length, '&lt;' characters are appended at the end. If -1, the string is neither truncated nor enlarged.
	 * @return MRZ-valid string.
	 */
	public static String toMrz(String string, int length) {
		if (string == null) {
			string = "";
		}
		for (final Map.Entry<String, String> e : EXPAND_CHARACTERS.entrySet()) {
			string = string.replace(e.getKey(), e.getValue());
		}
		string = string.replace("’", "");
		string = string.replace("'", "");
		string = deaccent(string).toUpperCase();
		if (length >= 0 && string.length() > length) {
			string = string.substring(0, length);
		}
		final StringBuilder sb = new StringBuilder(string);
		for (int i = 0; i < sb.length(); i++) {
			if (!isValid(sb.charAt(i))) {
				sb.setCharAt(i, FILLER);
			}
		}
		while (sb.length() < length) {
			sb.append(FILLER);
		}
		return sb.toString();
	}

	private static boolean isBlank(String str) {
		return str == null || str.trim().length() == 0;
	}

	/**
	 * Converts a surname and given names to a MRZ string, shortening them as per Doc 9303 Part 3 Vol 1 Section 6.7 of the MRZ specification when necessary.
	 * @param surname the surname, not blank.
	 * @param givenNames given names, not blank.
	 * @param length required length of the string. If given string is longer, it is shortened. If given string is shorter than given length, '&lt;' characters are appended at the end.
	 * @return name, properly converted to MRZ format of SURNAME&lt;&lt;GIVENNAMES&lt;..., with the exact length of given length.
	 */
	public static String nameToMrz(String surname, String givenNames, int length) {
		if (isBlank(surname)) {
			throw new IllegalArgumentException("Parameter surname: invalid value " + surname + ": blank");
		}
		if (isBlank(givenNames)) {
			throw new IllegalArgumentException("Parameter givenNames: invalid value " + givenNames + ": blank");
		}
		if (length <= 0) {
			throw new IllegalArgumentException("Parameter length: invalid value " + length + ": not positive");
		}
		surname = surname.replace(", ", " ");
		givenNames = givenNames.replace(", ", " ");
		final String[] surnames = surname.trim().split("[ \n\t\f\r]+");
		final String[] given = givenNames.trim().split("[ \n\t\f\r]+");
		for (int i = 0; i < surnames.length; i++) {
			surnames[i] = toMrz(surnames[i], -1);
		}
		for (int i = 0; i < given.length; i++) {
			given[i] = toMrz(given[i], -1);
		}
		// truncate
		int nameSize = getNameSize(surnames, given);
		String[] currentlyTruncating = given;
		int currentlyTruncatingIndex = given.length - 1;
		while (nameSize > length) {
			final String ct = currentlyTruncating[currentlyTruncatingIndex];
			final int ctsize = ct.length();
			if (nameSize - ctsize + 1 <= length) {
				currentlyTruncating[currentlyTruncatingIndex] = ct.substring(0, ctsize - (nameSize - length));
			} else {
				currentlyTruncating[currentlyTruncatingIndex] = ct.substring(0, 1);
				currentlyTruncatingIndex--;
				if (currentlyTruncatingIndex < 0) {
					if (currentlyTruncating == surnames) {
						throw new IllegalArgumentException("Cannot truncate name " + surname + " " + givenNames + ": length too small: " + length + "; truncated to " + toName(surnames, given));
					}
					currentlyTruncating = surnames;
					currentlyTruncatingIndex = currentlyTruncating.length - 1;
				}
			}
			nameSize = getNameSize(surnames, given);
		}
		return toMrz(toName(surnames, given), length);
	}
	/**
	 * The filler character, '&lt;'.
	 */
	public static final char FILLER = '<';

	private static String toName(String[] surnames, String[] given) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : surnames) {
			if (first) {
				first = false;
			} else {
				sb.append(FILLER);
			}
			sb.append(s);
		}
		sb.append(FILLER);
		for (String s : given) {
			sb.append(FILLER);
			sb.append(s);
		}
		return sb.toString();
	}

	private static int getNameSize(final String[] surnames, final String[] given) {
		int result = 0;
		for (String s : surnames) {
			result += s.length() + 1;
		}
		for (String s : given) {
			result += s.length() + 1;
		}
		return result;
	}

	private static String deaccent(String str) {
		String n = Normalizer.normalize(str, Normalizer.Form.NFD);
		return n.replaceAll("[^\\p{ASCII}]", "").toLowerCase();
	}

	public static boolean checkDigitEida(String emiratesId){
		boolean odd = true; 
		long oddSum = 0, evenSum = 0;
		long checkElement  = Long.parseUnsignedLong(emiratesId) % 10;
		long n = Long.parseUnsignedLong(emiratesId) / 10;
		while(n!=0){
			if(odd){
				char [] charSplit = Long.toString((n % 10) * 2).toCharArray();
				for (int i = 0; i < charSplit.length; i++)
				{
					oddSum +=Long.parseUnsignedLong(Character.toString(charSplit[i]));
				}

			}
			else
				evenSum += n % 10;
			n /= 10;
			odd = !odd;
		}
		long total = oddSum + evenSum;
		long nextDecimal = (total % 10 == 0) ? total : ((total / 10) + 1) * 10;
		if ((nextDecimal - total) == checkElement)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
