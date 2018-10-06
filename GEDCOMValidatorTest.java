import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class GEDCOMValidatorTest {
    @Test
    public void testIsBirthDateBeforeMarriageDate() {
        GEDCOMValidator validator = new GEDCOMValidator();
        assertEquals( true, validator.isBirthDateBeforeMarriageDate("8 AUG 1985", ""));
        assertEquals( true, validator.isBirthDateBeforeMarriageDate("8 AUG 1985", null));
    }

    @Test
    public void testIsDeathDateValid() {
        GEDCOMValidator validator = new GEDCOMValidator();
        assertEquals( true, validator.isDeathDateValid("8 AUG 1985", ""));
        assertEquals( true, validator.isDeathDateValid("8 AUG 1985", null));
    }

    @Test
    public void testIsDivorceAfterMarriage() {
        GEDCOMValidator validator = new GEDCOMValidator();
        assertEquals(true, validator.isDivorceAfterMarriage("1 AUG 1955",""));
        assertEquals(true, validator.isDivorceAfterMarriage("8 AUG 2011", null));
    }

    @Test
    public void testIsDateBeforeCurrentDate() {
        GEDCOMValidator validator = new GEDCOMValidator();
        assertEquals(true, validator.isDateBeforeCurrentDate(""));
        assertEquals(true, validator.isDateBeforeCurrentDate(null));
    }

}
