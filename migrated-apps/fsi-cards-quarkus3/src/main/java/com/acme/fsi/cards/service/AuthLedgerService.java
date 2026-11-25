package com.acme.fsi.cards.service;

import com.acme.fsi.cards.model.AuthEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class AuthLedgerService {
  @Inject
  EntityManager em;

  @Transactional
  public void record(String authId, AuthEntity e) {
    if (e == null) {
      return;
    }
    e.setAuthId(authId);
    e.setCreatedAt(new Date());
    if (e.getFlaggedFlag() == null) {
      e.setFlaggedFlag(Boolean.FALSE);
    }
    em.persist(e);
  }

  public List<AuthEntity> findByAuthId(String authId) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<AuthEntity> cq = cb.createQuery(AuthEntity.class);
    Root<AuthEntity> root = cq.from(AuthEntity.class);
    cq.select(root).where(cb.equal(root.get("authId"), authId));
    return em.createQuery(cq).getResultList();
  }

  public List<AuthEntity> listLatest(int limit) {
    int max = limit <= 0 ? 10 : Math.min(limit, 100);
    TypedQuery<AuthEntity> q = em.createQuery(
        "select a from AuthEntity a order by a.createdAt desc", AuthEntity.class);
    q.setMaxResults(max);
    return q.getResultList();
  }
}
