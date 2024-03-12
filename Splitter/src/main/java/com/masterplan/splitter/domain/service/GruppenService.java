package com.masterplan.splitter.domain.service;

import com.masterplan.splitter.domain.aggregates.group.Ausgabe;
import com.masterplan.splitter.domain.aggregates.group.Gruppe;
import com.masterplan.splitter.domain.aggregates.group.Transaktion;
import com.masterplan.splitter.exceptions.GruppeGeschlossenException;
import com.masterplan.splitter.exceptions.PersonKeinMitgliedException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.javamoney.moneta.Money;

public class GruppenService {

  public Set<Transaktion> calculate(Gruppe gruppe) {
    Map<String, Money> debts = getDebts(gruppe);
    //Sort Keys (Usernames) after Value Order and into a List
    List<String> nameList = mapToListInOrderKeys(debts);
    //Sort Money values after Usernames and return in a List
    List<Money> moneyList = mapToListInOrderValues(debts);
    //calculate negativ values onto positives
    Set<Transaktion>  ret = easyKnapsack(nameList, moneyList);
    return ret;
  }

  Set<Transaktion> easyKnapsack(List<String> nameList, List<Money> moneyList) {
    Set<Transaktion> ret = new HashSet<>();
    //find first index with Negativ number in moneylist
    int firstNeg = getFirstNegativeIndex(moneyList);
    for (int posIndex = firstNeg - 1; posIndex >= 0; posIndex--) {
      while (moneyList.get(posIndex).isGreaterThan(Money.of(0.01, "EUR"))) {
        int negIndex = moneyList.size() - 1;
        while (
            !(moneyList.get(negIndex).abs().remainder(moneyList.get(posIndex).getNumber()).isZero())
                && negIndex > firstNeg) {
          negIndex--;
        }
        if (moneyList.get(negIndex).abs()
            .isLessThanOrEqualTo(moneyList.get(posIndex))) { //negative abs fits into positive
          ret.add(new Transaktion(nameList.get(negIndex),
              moneyStripper(moneyList.get(negIndex).abs()),
              nameList.get(posIndex)));
          moneyList.set(posIndex, moneyList.get(posIndex).subtract(moneyList.get(negIndex).abs()));
          moneyList.remove(negIndex);
          nameList.remove(negIndex);
        } else { //negativ abs does not fit into positive
          ret.add(new Transaktion(nameList.get(negIndex),
              moneyStripper(moneyList.get(posIndex)),
              nameList.get(posIndex)));
          moneyList.set(posIndex, moneyList.get(posIndex).subtract(moneyList.get(posIndex)));
          moneyList.set(negIndex, moneyList.get(negIndex).add(moneyList.get(posIndex)));
        }
      }
    }
    return ret;
  }


  Map<String, Money> getDebts(Gruppe gruppe) {
    Map<String, Money> debts = new HashMap<>();
    gruppe.getMitglieder().forEach(
        member -> debts.putIfAbsent(member, Money.of(0, "EUR"))
    );
    gruppe.getAusgaben().forEach(
        expense -> {
          Money creditorMoney = debts.get(expense.kreditor()).add(expense.getCred());
          debts.replace(expense.kreditor(), creditorMoney);
          expense.debitoren().forEach(
              deb -> {
                Money debitorMoney = debts.get(deb).subtract(expense.getDebt());
                debts.replace(deb, debitorMoney);
              }
          );
        }
    );
    return debts;
  }

  private Money moneyStripper(Money money) {
    BigDecimal ret = money.getNumberStripped().setScale(2, RoundingMode.HALF_UP);
    return Money.of(ret, "EUR");
  }

  private int getFirstNegativeIndex(List<Money> moneyList) {
    int firstNegativeIndex = 0;
    while (firstNegativeIndex < moneyList.size()) {
      if (moneyList.get(firstNegativeIndex).isNegative()) {
        break;
      } else {
        firstNegativeIndex++;
      }
    }
    return firstNegativeIndex;
  }

  ArrayList<Money> mapToListInOrderValues(Map<String, Money> debts) {
    return new ArrayList<>(
        debts.values().stream().sorted(Comparator.reverseOrder()).toList());
  }

  ArrayList<String> mapToListInOrderKeys(Map<String, Money> debts) {
    return new ArrayList<>(debts.entrySet().stream()
        .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
        .map(Entry::getKey)
        .toList());
  }

  public void addMitglied(Gruppe gruppe, String githubHandle) {
    if (!gruppe.isOffenFuerPersonen() || gruppe.isGeschlossen()) {
      return;
    }
    if (!gruppe.hasMitglied(githubHandle)) {
      gruppe.addMitglied(githubHandle);
    }
  }

  public void addGruppenAusgabe(Gruppe gruppe, String kreditor, Set<String> debitor,
      String kommentar,
      Money amount) throws GruppeGeschlossenException, PersonKeinMitgliedException {
    if (!(gruppe.hasMitglied(kreditor))) {
      throw new PersonKeinMitgliedException("Keine Person mit dem Namen " + kreditor + " gefunden");
    }
    if (debitor.isEmpty()) {
      throw new IllegalArgumentException("keine Debitoren vorhanden");
    }
    for (String deb : debitor) {
      if (!(gruppe.hasMitglied(deb))) {
        throw new PersonKeinMitgliedException("Keine Person mit dem Namen " + deb + " gefunden");
      }
    }
    if (gruppe.isGeschlossen()) {
      throw new GruppeGeschlossenException("Gruppe ist bereits geschlossen");
    }
    gruppe.addAusgabe(kreditor, debitor, kommentar, amount);
    gruppe.setTransaktionen(calculate(gruppe));
    gruppeSchliessenFuerPersonen(gruppe);
  }

  public Gruppe createGruppe(String groupName, String creatorName) {
    return new Gruppe(groupName, creatorName);
  }

  public void gruppeSchliessenFuerPersonen(Gruppe gruppe) {
    gruppe.schliessenFuerPersonen();
  }

  public void gruppeSchliessen(Gruppe gruppe) {
    gruppe.schliessen();
  }

  List<Ausgabe> getAusgabe(Gruppe gruppe) {
    return gruppe.getAusgaben();
  }

  public boolean hasMitglied(Gruppe gruppe, String name) {
    return gruppe.hasMitglied(name);
  }

  public Set<Transaktion> getTransaktionen(Gruppe gruppe) {
    return gruppe.getTransaktionen();
  }
}
