package com.masterplan.splitter.domain.aggregates.group;

import com.masterplan.splitter.annotations.Value;
import java.util.Objects;
import org.javamoney.moneta.Money;

@Value
public final class Transaktion {

  private final String name;
  private final Money sendet;
  private final String an;

  public Transaktion(String name, Money sendet, String an) {
    this.name = name;
    this.sendet = Money.of(sendet.getNumber(), "EUR");
    this.an = an;
  }

  public String name() {
    return name;
  }

  public Money sendet() {
    return Money.of(sendet.getNumber(), "EUR");
  }

  public String an() {
    return an;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (Transaktion) obj;
    return Objects.equals(this.name, that.name)
        && Objects.equals(this.sendet, that.sendet)
        && Objects.equals(this.an, that.an);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, sendet, an);
  }

  @Override
  public String toString() {
    return "Transaktion["
        + "name=" + name + ", "
        + "sendet=" + sendet + ", "
        + "an=" + an + ']';
  }


}
