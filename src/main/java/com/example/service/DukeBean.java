package com.example.service;

import com.example.entity.Duke;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class DukeBean {

    @PersistenceContext(unitName = "dukesPU")
    private EntityManager em;

    public void saveDuke(String name, int age) {
        Duke duke = new Duke(name, age);
        em.persist(duke);
    }

    public List<Duke> getAllDukes() {
        return em.createQuery("SELECT d FROM Duke d", Duke.class).getResultList();
    }
}
