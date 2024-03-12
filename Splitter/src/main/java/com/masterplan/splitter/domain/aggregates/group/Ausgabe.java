package com.masterplan.splitter.domain.aggregates.group;

import com.masterplan.splitter.annotations.Value;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.javamoney.moneta.Money;

@Value
public final class Ausgabe {


  private final String kreditor;
  private final Set<String> debitoren;
  private final String beschreibung;
  private final Money kosten;

  public Ausgabe(String kreditor, Set<String> debitoren, String beschreibung, Money kosten) {
    this.kreditor = kreditor;
    this.debitoren = Collections.unmodifiableSet(debitoren);
    this.beschreibung = beschreibung;
    this.kosten = Money.of(kosten.getNumber(), "EUR");
  }

  public Money getDebt() {
    return kosten.divide(debitoren.size());
  }

  public Money getCred() {
    return Money.of(kosten.getNumber(), "EUR");
  }

  public String kreditor() {
    return kreditor;
  }

  public Set<String> debitoren() {
    return debitoren;
  }

  public java.lang.String beschreibung() {
    return beschreibung;
  }

  public Money kosten() {
    return Money.of(kosten.getNumber(), "EUR");
  }

  public boolean hasUserName(String name) {
    if (kreditor.equals(name)) {
      return true;
    }
    for (String debitor : debitoren) {
      if (debitor.equals(name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (Ausgabe) obj;
    return Objects.equals(this.kreditor, that.kreditor)
        && Objects.equals(this.debitoren, that.debitoren)
        && Objects.equals(this.beschreibung, that.beschreibung)
        && Objects.equals(this.kosten, that.kosten);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kreditor, debitoren, beschreibung, kosten);
  }

  @Override
  public String toString() {
    return "Ausgabe{"
        + "kreditor=" + kreditor
        + ", debitoren=" + debitoren
        + ", beschreibung='" + beschreibung
        + ", kosten=" + kosten
        + '}';
  }

}
