package net.tospay.transaction.repositories;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
 interface BaseRepositoryInterface<T, ID extends Serializable> extends JpaRepository<T, ID>
{
    @Transactional
    void refresh(T t);
}

public  class BaseRepository<T, ID extends Serializable> extends
        SimpleJpaRepository<T, ID> implements BaseRepositoryInterface<T, ID>
{
    private final EntityManager em;

    public BaseRepository(JpaEntityInformation<T, ID> entityInformation,
            EntityManager entityManager)
    {

        super(entityInformation, entityManager);
        this.em = entityManager;
    }

    @Override
    @Transactional
    public void refresh(T t)
    {
//        if (!em.contains(t)) {
//            em.persist(t);
//        } else {
//            em.merge(t);
//        }
        em.refresh(t);
    }
}
