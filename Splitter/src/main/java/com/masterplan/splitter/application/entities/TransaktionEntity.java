package com.masterplan.splitter.application.entities;

import com.masterplan.splitter.domain.aggregates.group.Transaktion;

public record TransaktionEntity(String von, String an, Long cents) {

  public TransaktionEntity(Transaktion transaktion) {
    this(
        transaktion.name(),
        transaktion.an(),
        transaktion.sendet().scaleByPowerOfTen(2).getNumber().longValueExact()
    );
  }

}
