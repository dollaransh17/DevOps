package in.course.converter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ConverterTest {
    @Test void convertsKilometresToMiles() { assertEquals(3.1068559612, Converter.convert(5, "km", "miles"), 1e-9); }
    @Test void convertsMass() { assertEquals(2500, Converter.convert(2.5, "kg", "g")); }
    @Test void convertsTime() { assertEquals(90, Converter.convert(1.5, "h", "min")); }
    @Test void handlesZeroNegativeAndSameUnit() {
        assertEquals(0, Converter.convert(0, "m", "km"));
        assertEquals(-200, Converter.convert(-2, "m", "cm"));
        assertEquals(12, Converter.convert(12, "m", "m"));
    }
    @Test void rejectsInvalidConversions() {
        assertThrows(IllegalArgumentException.class, () -> Converter.convert(1, "kg", "m"));
        assertThrows(IllegalArgumentException.class, () -> Converter.convert(1, "unknown", "m"));
        assertThrows(IllegalArgumentException.class, () -> Converter.convert(Double.NaN, "m", "km"));
        assertThrows(IllegalArgumentException.class, () -> Converter.convert(Double.POSITIVE_INFINITY, "m", "km"));
        assertThrows(IllegalArgumentException.class, () -> Converter.convert(Double.MAX_VALUE, "km", "cm"));
    }
}
