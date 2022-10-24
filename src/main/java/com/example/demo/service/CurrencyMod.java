package com.example.demo.service;

import com.example.demo.common.CoindeskCurrency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CurrencyMod {

  private static final Table<Record> CURRENCY = DSL.table("currency");

  private final DSLContext dslContext;

  public synchronized List<CoindeskCurrency> listCurrencies() {
//    String sql = """
//          SELECT *
//          FROM CURRENCY
//          WHERE IS_REMOVE = 'N'
//        """;
//    return dslContext.resultQuery(sql).fetchInto(CoindeskCurrency.class);

    List<Field<?>> columns = Arrays.asList(
        DSL.field(DSL.name("COIN_NUM")),
        DSL.field(DSL.name("COIN_CODE")),
        DSL.field(DSL.name("COIN_SYMBOL")),
        DSL.field(DSL.name("COIN_RATE")),
        DSL.field(DSL.name("COIN_DESC")),
        DSL.field(DSL.name("IS_REMOVED"))
    );

    return dslContext.selectFrom(CURRENCY)
        .where(DSL.field(DSL.name("IS_REMOVED")).eq("N"))
        .fetch(r -> {
          log.warn("{}", r);
          return r.into(CoindeskCurrency.class);
        });
  }

  public void updateCurrency(CoindeskCurrency coindeskCurrency) {
    dslContext.update(CURRENCY)
        .set(DSL.field(DSL.name("COIN_SYMBOL")), coindeskCurrency.getCoinSymbol())
        .set(DSL.field(DSL.name("COIN_RATE")), coindeskCurrency.getCoinRate())
        .set(DSL.field(DSL.name("COIN_DESC")), coindeskCurrency.getCoinDesc())
        .where(DSL.field(DSL.name("COIN_CODE")).eq(coindeskCurrency.getCoinCode()))
        .execute();
  }

  public void removeCurrency(CoindeskCurrency coindeskCurrency) {
    dslContext.update(CURRENCY)
        .set(DSL.field(DSL.name("IS_REMOVED")), "Y")
        .where(DSL.field(DSL.name("COIN_CODE")).eq(coindeskCurrency.getCoinCode()))
        .execute();
  }

  public void insertCurrency(CoindeskCurrency coindeskCurrency) {
    final String code = coindeskCurrency.getCoinCode();
    boolean isExists = this.listCurrencies().stream()
        .anyMatch(currency -> code.equals(currency.getCoinCode()) && "Y".equals(currency.getIsRemoved()));
    System.out.println(isExists);
    if (isExists) {
      this.reopen(code);
    }

    dslContext.insertInto(CURRENCY)
        .set(DSL.field(DSL.name("COIN_CODE")), code)
        .set(DSL.field(DSL.name("COIN_SYMBOL")), coindeskCurrency.getCoinSymbol())
        .set(DSL.field(DSL.name("COIN_RATE")), coindeskCurrency.getCoinRate())
        .set(DSL.field(DSL.name("COIN_DESC")), coindeskCurrency.getCoinDesc())
        .execute();
  }

  private void reopen(String code) {
    dslContext.update(CURRENCY)
        .set(DSL.field(DSL.name("IS_REMOVED")), "N")
        .where(DSL.field(DSL.name("COIN_CODE")).eq(code))
        .execute();
  }
}
