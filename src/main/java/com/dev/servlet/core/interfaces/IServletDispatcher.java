package com.dev.servlet.core.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This contract is used to dispatch the request
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface IServletDispatcher {

    /**
     * This contract is used to dispatch the request
     *
     * @param httpServletRequest  {@linkplain HttpServletRequest}
     * @param httpServletResponse {@linkplain HttpServletResponse}
     */
    void dispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}
