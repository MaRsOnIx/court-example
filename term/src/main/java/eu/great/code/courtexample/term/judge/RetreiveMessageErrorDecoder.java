package eu.great.code.courtexample.term.judge;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class RetreiveMessageErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        if(response.body() == null) {
            return new HttpResponseError(HttpStatus.valueOf(response.status()));
        }

        try (var inputStream = response.body().asInputStream();
             var bis = new BufferedInputStream(inputStream);
             var buf = new ByteArrayOutputStream()) {

            for (int result = bis.read(); result != -1; result = bis.read()) {
                buf.write((byte) result);
            }
            String message = buf.toString(StandardCharsets.UTF_8);
            return new HttpResponseError(HttpStatus.valueOf(response.status()), message);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

