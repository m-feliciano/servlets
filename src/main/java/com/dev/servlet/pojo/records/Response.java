package com.dev.servlet.pojo.records;

import com.dev.servlet.utils.CollectionUtils;
import lombok.Getter;
import lombok.ToString;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This class represents the response object.
 * It is used to hold the response data {@link Data} or errors that are returned from the server.
 *
 * @author marcelo.feliciano
 */
@ToString(
        of = {"status", "responseData", "errors"},
        includeFieldNames = false
)
@Getter
public class Response {

    private final int status;
    private Data responseData;
    private List<String> errors;

    // Ok, of, ofNext, ofError methods: Return a new Response object ready to be used.
    public static Response ok() {
        return new Response(HttpServletResponse.SC_OK); // 200
    }

    public static Response of(Data data) {
        return Response.ok().data(data);
    }

    public static Response ofNext(String path) {
        return Response.ok().next(path);
    }

    public static Response ofError(int status, String error) {
        var errors = List.of(error);
        return new Response(status, errors);
    }

    public Response(int status) {
        this.status = status;
    }

    public Response(int status, List<String> errors) {
        this.status = status;
        this.errors = errors;
    }

    public Response data(String name, Object value) {
        this.data(Data.of(name, value));
        return this;
    }

    public Response data(Data data) {
        if (this.responseData == null) {
            this.responseData = data;
        } else {
            // Join immutable sets
            Collection<KeyPair> joinSet = CollectionUtils.join(this.responseData.get(), data.get());
            this.responseData = new Data(Set.copyOf(joinSet));
        }
        return this;
    }

    public Response next(String path) {
        this.data("next", path);
        return this;
    }

    public String getNext() {
        Object next = responseData.get("next");
        return (String) next;
    }


    public String getErrorMessage() {
        if (CollectionUtils.isEmpty(errors)) {
            return null;
        }

        return String.join("\n", errors);
    }

    /**
     * Data class to hold the response data.
     * It is a list of key-value {@link KeyPair} that is used to store the response data.
     *
     * @author marcelo.feliciano
     * @since 1.4.0
     */
    @ToString(
            of = {"dataSet"},
            includeFieldNames = false
    )
    public static class Data {

        private Set<KeyPair> dataSet;

        /**
         * Create an immutable list of key-value pairs.
         *
         * @param name
         * @param value
         * @return {@link Data}
         * @see KeyPair#setOf(String, Object)
         */
        public static Data of(String name, Object value) {
            var newData = KeyPair.setOf(name, value);
            return new Data(newData);
        }

        public Data() {
        }

        public Data(Set<KeyPair> dataSet) {
            this.dataSet = dataSet;
        }

        public Set<KeyPair> get() {
            return dataSet;
        }

        public Object get(String key) {
            if (CollectionUtils.isEmpty(dataSet))
                return null;

            return this.get().stream()
                    .filter(p -> p.getKey().equals(key))
                    .findFirst()
                    .map(KeyPair::getValue)
                    .orElse(null);
        }

        public Data add(String name, Object value) {
            if (dataSet == null) {
                dataSet = KeyPair.mutableSetOf(name, value);
            } else {
                dataSet.add(KeyPair.of(name, value));
            }

            return this;
        }
    }
}