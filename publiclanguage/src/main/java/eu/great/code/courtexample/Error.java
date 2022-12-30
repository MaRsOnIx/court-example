package eu.great.code.courtexample;

public record Error(String message) {
    public static Error of(String message) {
        return new Error(message);
    }
}
