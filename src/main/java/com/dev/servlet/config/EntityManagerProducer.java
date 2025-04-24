package com.dev.servlet.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Slf4j
@ApplicationScoped
public class EntityManagerProducer {
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
            log.error("EntityManagerFactory is null. Check persistence.xml configuration.");
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
