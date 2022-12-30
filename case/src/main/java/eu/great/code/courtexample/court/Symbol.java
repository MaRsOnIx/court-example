package eu.great.code.courtexample.court;

import java.util.Arrays;
import java.util.Optional;

public enum Symbol {
    C("C"), CO("Co"), K("K"), KO("Ko");

    private final String text;
    Symbol(String text) {
        this.text = text;
    }

    public static Optional<Symbol> fromText(String text) {
        return Arrays.stream(Symbol.values())
                .filter(v -> v.text.equals(text))
                .findAny();
    }

    public String getText() {
        return text;
    }
}
