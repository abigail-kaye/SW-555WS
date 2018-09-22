import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class GEDCOMreaderTest {

    @Test
    public void WhenDateYearIsEarly() {
        assertEquals(true, GEDCOMreader.dateValid("4 SEP 1993"));
    }

    @Test
    public void WhenDateYearIsLater() {
        assertEquals(false, GEDCOMreader.dateValid("4 SEP 2019"));
    }

    @Test
    public void WhenDateMonthIsEarly() {
        assertEquals(true, GEDCOMreader.dateValid("4 JAN 2018"));
    }

    @Test
    public void WhenDateMonthIsLater() {
        assertEquals(false, GEDCOMreader.dateValid("4 DEC 2018"));
    }

    @Test
    public void WhenDateDayIsEarly() {
        assertEquals(true, GEDCOMreader.dateValid("1 SEP 2018"));
    }


    @Test
    public void testMarriedDateBeforeBirthdayYear() {
        assertEquals(false, GEDCOMreader.marriedDateValid("4 DEC 1992", "4 DEC 1993"));
    }

    @Test
    public void testMarriedDateAfterBirthdayYear() {
        assertEquals(true, GEDCOMreader.marriedDateValid("4 DEC 1994", "4 DEC 1993"));
    }

    @Test
    public void testMarriedDateBeforeBirthdayMonth() {
        assertEquals(false, GEDCOMreader.marriedDateValid("4 JAN 1992", "4 DEC 1992"));
    }

    @Test
    public void testMarriedDateAfterBirthdayMonth() {
        assertEquals(true, GEDCOMreader.marriedDateValid("4 DEC 1992", "4 JAN 1992"));
    }

    @Test
    public void testMarriedDateBeforeBirthdayDay() {
        assertEquals(false, GEDCOMreader.marriedDateValid("1 DEC 1992", "24 DEC 1992"));
    }
}
