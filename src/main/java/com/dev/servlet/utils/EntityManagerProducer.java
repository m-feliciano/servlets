package com.dev.servlet.utils;

import org.hibernate.Session;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.logging.Logger;

@ApplicationScoped
public class EntityManagerProducer {

    private static final Logger LOGGER = Logger.getLogger(EntityManagerProducer.class.getName());

    private EntityManagerFactory factory;

    public EntityManagerProducer() {
        factory = Persistence.createEntityManagerFactory("servletpu");
    }

    public void setEntityManagerFactory(EntityManagerFactory factory) {
        this.factory = factory;
    }

    @Produces
    @RequestScoped
    public Session getEntityManager() {
        if (factory == null) {
            LOGGER.severe("EntityManagerFactory is null. Check persistence.xml configuration.");
            throw new IllegalStateException("EntityManagerFactory is not initialized.");
        }
        return (Session) factory.createEntityManager();
    }

    @PreDestroy
    public void closeEntityManagerFactory() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }

    public void close(@Disposes EntityManager em) {
        if (em.isOpen()) {
            em.close();
        }
    }
}