package dev.sorn.orc.types;


import io.vavr.control.Option;
import jakarta.validation.ValidationException;
import static io.vavr.control.Option.none;

public record LineNumberRange(
    Option<LineNumber> from,
    Option<LineNumber> to
) {

    public LineNumberRange {
        if (!from.isEmpty() && !to.isEmpty()) {
            if (from.get().value() >= to.get().value()) {
                throw new ValidationException("invalid line number range" +
                    ": " + from.get().value() +
                    ", " + to.get().value());
            }
        }
    }

    public static LineNumberRange of(int fromLineNumberInclusive, int toLineNumberExclusive) {
        return of(LineNumber.of(fromLineNumberInclusive), LineNumber.of(toLineNumberExclusive));
    }

    public static LineNumberRange of(LineNumber fromLineNumberInclusive, LineNumber toLineNumberExclusive) {
        return new LineNumberRange(Option.of(fromLineNumberInclusive), Option.of(toLineNumberExclusive));
    }

    public static LineNumberRange empty() {
        return new LineNumberRange(none(), none());
    }

    public static LineNumberRange from(LineNumber fromLineNumberInclusive) {
        return new LineNumberRange(Option.of(fromLineNumberInclusive), none());
    }

    public static LineNumberRange to(LineNumber toLineNumberExclusive) {
        return new LineNumberRange(none(), Option.of(toLineNumberExclusive));
    }

}
