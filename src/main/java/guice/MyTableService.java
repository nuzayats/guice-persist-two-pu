package guice;

import entity.MyTable;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class MyTableService {

    @Inject
    private EntityManager em;

    public String fetch() {
        return em.createQuery("SELECT t FROM MyTable t", MyTable.class).getSingleResult().getMycol();
    }
}
