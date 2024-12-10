package com.dev.servlet.pojo.records;

import com.dev.servlet.interfaces.IHttpResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * This class is used to represent the HTTP response.
 *
 * @param <T>
 * @author marcelo.feliciano
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class HttpResponse<T> implements IHttpResponse<T> {

    private int status;
    private T response;
    private String next;
    private Set<String> errors;

    public static HttpResponse<Integer> of(int status) {
        return HttpResponse.<Integer>builder().status(status).build();
    }

    /**
     * Create a response with the response.
     *
     * @param response {@link HttpResponse}
     * @param <U>      type of response
     * @return {@link HttpResponse}
     */
    public static <U> HttpResponse<U> of(U response) {
        return HttpResponse.<U>builder()
                .status(HttpServletResponse.SC_OK)
                .response(response)
                .build();
    }

    /**
     * Create a response with errors.
     *
     * @param status
     * @param errors
     * @param <U>    type of response
     * @return {@link HttpResponse}
     * @author marcelo.feliciano
     */
    public static <U> HttpResponse<U> ofError(int status, Set<String> errors) {
        return HttpResponse.<U>builder()
                .status(status)
                .errors(errors)
                .build();
    }

    /**
     * @see HttpResponse#ofError(int, Set)
     */
    public static <U> HttpResponse<U> ofError(int status, String error) {
        return ofError(status, Set.of(error));
    }

    /**
     * Create a response with next path.
     *
     * @param next
     * @param <U>  type of response
     * @return {@link HttpResponse}
     * @author marcelo.feliciano
     */
    public static <U> HttpResponse<U> ofNext(String next) {
        return HttpResponse.<U>builder()
                .status(HttpServletResponse.SC_OK)
                .next(next)
                .build();
    }

    @Override
    public Set<String> getErrors() {
        return errors;
    }
}