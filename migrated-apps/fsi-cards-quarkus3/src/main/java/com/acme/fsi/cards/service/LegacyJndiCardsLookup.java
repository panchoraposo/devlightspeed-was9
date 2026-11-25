package com.acme.fsi.cards.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;

/**
 * Legacy class name kept for demo continuity. Quarkus injects the datasource
 * instead of doing an InitialContext lookup of jdbc/CardsDS.
 */
@ApplicationScoped
public class LegacyJndiCardsLookup {
  @Inject
  DataSource dataSource;

  public DataSource lookupCardsDs() {
    return dataSource;
  }
}
