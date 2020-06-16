package com.adcb.ocr.decode.records;

import com.adcb.ocr.decode.MrzParser;
import com.adcb.ocr.decode.MrzRange;
import com.adcb.ocr.decode.MrzRecord;
import com.adcb.ocr.decode.types.MrzDocumentCode;
import com.adcb.ocr.decode.types.MrzFormat;

/**
 * MRV type-B format: A two lines long, 36 characters per line format
 */
public class MrvB extends MrzRecord {

    private static final long serialVersionUID = 1L;

    public MrvB() {
        super(MrzFormat.MRV_VISA_B);
        code1 = 'V';
        code2 = '<';
        code = MrzDocumentCode.TypeV;
    }
    /**
     * Optional data at the discretion of the issuing State
     */
    public String optional;

    @Override
    public void fromMrz(String mrz) {
        super.fromMrz(mrz);
        final MrzParser parser = new MrzParser(mrz);
        setName(parser.parseName(new MrzRange(5, 36, 0)));
        documentNumber = parser.parseString(new MrzRange(0, 9, 1));
        validDocumentNumber = parser.checkDigit(9, 1, new MrzRange(0, 9, 1), "passport number");
        nationality = parser.parseString(new MrzRange(10, 13, 1));
        dateOfBirth = parser.parseDate(new MrzRange(13, 19, 1));
        validDateOfBirth = parser.checkDigit(19, 1, new MrzRange(13, 19, 1), "date of birth") && dateOfBirth.isDateValid();
        sex = parser.parseSex(20, 1);
        expirationDate = parser.parseDate(new MrzRange(21, 27, 1));
        validExpirationDate = parser.checkDigit(27, 1, new MrzRange(21, 27, 1), "expiration date") && expirationDate.isDateValid();
        optional = parser.parseString(new MrzRange(28, 36, 1));
        // TODO validComposite missing? (full MRZ line)
    }

    @Override
    public String toString() {
        return "MRV-B{" + super.toString() + ", optional=" + optional + '}';
    }

    @Override
    public String toMrz() {
        final StringBuilder sb = new StringBuilder("V<");
        sb.append(MrzParser.toMrz(issuingCountry, 3));
        sb.append(MrzParser.nameToMrz(surname, givenNames, 31));
        sb.append('\n');
        // second line
        sb.append(MrzParser.toMrz(documentNumber, 9));
        sb.append(MrzParser.computeCheckDigitChar(MrzParser.toMrz(documentNumber, 9)));
        sb.append(MrzParser.toMrz(nationality, 3));
        sb.append(dateOfBirth.toMrz());
        sb.append(MrzParser.computeCheckDigitChar(dateOfBirth.toMrz()));
        sb.append(sex.mrz);
        sb.append(expirationDate.toMrz());
        sb.append(MrzParser.computeCheckDigitChar(expirationDate.toMrz()));
        sb.append(MrzParser.toMrz(optional, 8));
        sb.append('\n');
        return sb.toString();
    }
}
