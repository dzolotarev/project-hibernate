package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static java.util.Objects.isNull;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private static final String GET_ALL_USERS = "SELECT * FROM rpg.player LIMIT :limit OFFSET :offset";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";
    private static final String ID = "ID";

    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = getProperties();
        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
//        properties.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
//        properties.put(Environment.URL, "jdbc:mysql://localhost:3306/rpg");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.FORMAT_SQL, "true");
        return properties;
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        List<Player> result = null;
        try (Session session = sessionFactory.openSession()) {
            NativeQuery<Player> query = session.createNativeQuery(GET_ALL_USERS, Player.class);
            query.setParameter(LIMIT, pageSize);
            query.setParameter(OFFSET, pageNumber * pageSize);
            result = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isNull(result) ? new ArrayList<>() : result;
    }

    @Override
    public int getAllCount() {
        long result = 0;
        try (Session session = sessionFactory.openSession()) {
            result = session.createNamedQuery("Players_GetAllCount", Long.class).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) result;
    }

    @Override
    public Player save(Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(player);
            transaction.commit();
        } catch (Exception e) {
            if (!isNull(transaction)) {
                transaction.rollback();
                e.printStackTrace();
            }
        }
        return player;
    }

    @Override
    public Player update(Player player) {
        Transaction transaction = null;
        Player result = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            result = (Player) session.merge(player);
            transaction.commit();
        } catch (Exception e) {
            if (!isNull(transaction)) {
                transaction.rollback();
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public Optional<Player> findById(long id) {
        Player player = null;
        try (Session session = sessionFactory.openSession()) {
            Query<Player> query = session.createNamedQuery("Players_FindById", Player.class);
            query.setParameter(ID, id);
            player = query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(player);
    }

    @Override
    public void delete(Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        } catch (Exception e) {
            if (!isNull(transaction)) {
                transaction.rollback();
                e.printStackTrace();
            }
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}