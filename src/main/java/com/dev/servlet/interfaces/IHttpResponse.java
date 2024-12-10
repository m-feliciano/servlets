package com.dev.servlet.interfaces;

import java.util.Set;

/**
 * This class is used to represent the HTTP response.
 *
 * @param <T>
 * @author marcelo.feliciano
 */
public interface IHttpResponse<T> {

    int getStatus();

    T getResponse();

    Set<String> getErrors();

    String getNext();
}