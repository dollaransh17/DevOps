package in.course.converter;

import java.util.Map;

/** Conversion factors are relative to metres, grams and seconds. */
public final class Converter {
    private record Unit(String category, double factor) {}
    private static final Map<String, Unit> UNITS = Map.of(
        "m", new Unit("length", 1), "km", new Unit("length", 1000),
        "miles", new Unit("length", 1609.344), "cm", new Unit("length", 0.01),
        "g", new Unit("mass", 1), "kg", new Unit("mass", 1000),
        "s", new Unit("time", 1), "min", new Unit("time", 60), "h", new Unit("time", 3600));
    private Converter() {}

    public static double convert(double value, String from, String to) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Enter a finite number.");
        Unit source = UNITS.get(from), target = UNITS.get(to);
        if (source == null || target == null) throw new IllegalArgumentException("Choose a supported unit.");
        if (!source.category.equals(target.category))
            throw new IllegalArgumentException("Choose units from the same category.");
        double result = value * (source.factor / target.factor);
        if (!Double.isFinite(result)) throw new IllegalArgumentException("The result is too large.");
        return result;
    }
}
