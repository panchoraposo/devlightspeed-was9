package com.acme.fsi.cards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "card_auths")
public class AuthEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false, length = 64)
  private String authId;

  @Column(nullable = false, length = 64)
  private String merchantId;

  @Column(nullable = false, length = 4)
  private String panLast4;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(nullable = false)
  private Date createdAt;

  @Convert(converter = YesNoConverter.class)
  @Column
  private Boolean flaggedFlag;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAuthId() {
    return authId;
  }

  public void setAuthId(String authId) {
    this.authId = authId;
  }

  public String getMerchantId() {
    return merchantId;
  }

  public void setMerchantId(String merchantId) {
    this.merchantId = merchantId;
  }

  public String getPanLast4() {
    return panLast4;
  }

  public void setPanLast4(String panLast4) {
    this.panLast4 = panLast4;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Boolean getFlaggedFlag() {
    return flaggedFlag;
  }

  public void setFlaggedFlag(Boolean flaggedFlag) {
    this.flaggedFlag = flaggedFlag;
  }
}
