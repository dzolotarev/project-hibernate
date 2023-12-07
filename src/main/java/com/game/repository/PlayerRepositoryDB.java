package com.game.repository;

import com.game.entity.Player;
import com.game.utils.FactoryConfiguration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private static final String GET_ALL_USERS = "SELECT * FROM rpg.player LIMIT :limit OFFSET :offset";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";
    private static final String ID = "ID";

    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        sessionFactory = FactoryConfiguration.getInstance().getSessionFactory();
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