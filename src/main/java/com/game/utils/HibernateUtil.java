package com.game.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {
    private static SessionFactory SESSION_FACTORY;

    public static void buildSessionFactory() {
        if (SESSION_FACTORY != null) {
            return;
        }

        Configuration configuration = new Configuration();
        configuration.configure();
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();
        SESSION_FACTORY = configuration.buildSessionFactory(serviceRegistry);
    }

    public static Session getCurrentSession() {
        return SESSION_FACTORY.getCurrentSession();
    }

    public static void killSessionFactory() {
        if (SESSION_FACTORY != null) {
            SESSION_FACTORY.close();
        }
    }
}
