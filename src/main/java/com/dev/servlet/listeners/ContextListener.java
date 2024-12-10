package com.dev.servlet.listeners;

import com.dev.servlet.utils.BeanUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * The listener interface for receiving context events.
 *
 * @since 1.3.0
 */
@WebListener
public class ContextListener implements ServletContextListener {

    /**
     * This method is called when the servlet context is destroyed
     *
     * @param arg0 {@link ServletContextEvent}
     */
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // Empty method
    }

    /**
     * This method is called when the servlet context is initialized
     *
     * @param arg0 {@link ServletContextEvent}
     */
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        BeanUtil.DependencyResolver resolver = BeanUtil.getResolver();
        resolver.resolveAll();
    }
}