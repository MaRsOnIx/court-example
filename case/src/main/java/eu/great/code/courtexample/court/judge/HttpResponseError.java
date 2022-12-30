package eu.great.code.courtexample.court.judge;

import org.springframework.http.HttpStatus;

public class HttpResponseError extends Exception {

    private final HttpStatus status;
    private final String message;

    public HttpResponseError(HttpStatus status) {
        this(status, null);
    }

    public HttpResponseError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
