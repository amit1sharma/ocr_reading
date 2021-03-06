package com.adcb.ocr.decode;

import com.adcb.ocr.decode.types.MrzDate;
import com.adcb.ocr.decode.types.MrzDocumentCode;
import com.adcb.ocr.decode.types.MrzFormat;
import com.adcb.ocr.decode.types.MrzSex;
import java.io.Serializable;

/**
 * An abstract MRZ record, contains basic information present in all MRZ record types.
 */
public abstract class MrzRecord implements Serializable {


    /**
     * The document code.
     */
    public MrzDocumentCode code;
    /**
     * Document code, see {@link MrzDocumentCode} for details on allowed values.
     */
    public char code1;
    /**
     * For MRTD: Type, at discretion of states, but 1-2 should be IP for passport card, AC for crew member and IV is not allowed.
     * For MRP: Type (for countries that distinguish between different types of passports)
     */
    public char code2;


    public String issuingCountry;
    /**
     * Document number, e.g. passport number.
     */
    public String documentNumber;
    /**
     * The surname in uppercase.
     */
    public String surname;
    /**
     * The given names in uppercase, separated by spaces.
     */
    public String givenNames;
    /**
     * Date of birth.
     */
    public MrzDate dateOfBirth;
    /**
     * Sex
     */
    public MrzSex sex;
    /**
     * expiration date of passport
     */
    public MrzDate expirationDate;
    /**
     * An <a href="http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3">ISO 3166-1 alpha-3</a> country code of nationality.
     * See {@link #issuingCountry} for additional allowed values.
     */
    public String nationality;
    /**
     * Detected MRZ format.
     */
    public final MrzFormat format;


    /**
     * check digits, usually common in every document.
     */
    public boolean validDocumentNumber = true;
    public boolean validDateOfBirth = true;
    public boolean validExpirationDate = true;
    public boolean validComposite = true;
    public boolean validEidaNumber = true;


    protected MrzRecord(MrzFormat format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return "MrzRecord{" + "code=" + code + "[" + code1 + code2 + "], issuingCountry=" + issuingCountry + ", documentNumber=" + documentNumber
                + ", surname=" + surname + ", givenNames=" + givenNames + ", dateOfBirth=" + dateOfBirth + ", sex=" + sex + ", expirationDate="
                + expirationDate + ", nationality=" + nationality + '}';
    }

    /**
     * Parses the MRZ record.
     * @param mrz the mrz record, not null, separated by \n
     * @throws MrzParseException when a problem occurs.
     */
    public void fromMrz(String mrz) throws MrzParseException {
        if (format != MrzFormat.get(mrz)) {
            throw new MrzParseException("invalid format: " + MrzFormat.get(mrz), mrz, new MrzRange(0, 0, 0), format);
        }
        code = MrzDocumentCode.parse(mrz);
        code1 = mrz.charAt(0);
        code2 = mrz.charAt(1);
        issuingCountry = new MrzParser(mrz).parseString(new MrzRange(2, 5, 0));
    }
    
    /**
     * Helper method to set the full name. Changes both {@link #surname} and {@link #givenNames}.
     * @param name expected array of length 2, in the form of [surname, first_name]. Must not be null.
     */
    protected final void setName(String[] name) {
        surname = name[0];
        givenNames = name[1];
    }
    
    /**
     * Serializes this record to a valid MRZ record.
     * @return a valid MRZ record, not null, separated by \n
     */
    public abstract String toMrz();
}
