package com.example.demo.config;

import com.example.demo.common.CoindeskApiResponse;
import com.example.demo.common.CoindeskCurrency;
import com.example.demo.service.CoindeskMod;
import com.example.demo.service.CurrencyMod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class H2TableConfig {

  private static final Table<Record> CURRENCY = DSL.table("currency");

  private final CoindeskMod coindeskMod;
  private final CurrencyMod currencyMod;
  private final DSLContext dslContext;

  @Bean
  public void initH2Table() {

    // 如果第一次使用，建立table
    int createTable = dslContext.createTableIfNotExists(CURRENCY)
        .column("COIN_NUM", DefaultDataType.getDataType(SQLDialect.DEFAULT, Long.class).identity(true).nullable(false))
        .column("COIN_CODE", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(false).length(32))
        .column("COIN_SYMBOL", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(false).length(16))
        .column("COIN_RATE", DefaultDataType.getDataType(SQLDialect.DEFAULT, BigDecimal.class).identity(false).nullable(false))
        .column("COIN_DESC", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(true))
        .column("IS_REMOVED", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(false).defaultValue("N"))
        .constraint(DSL.primaryKey("COIN_NUM"))
        .execute();

    // 第一次建立table
    if (dslContext.selectFrom(CURRENCY).fetchAny() == null) {
      this.getCoinList(coindeskMod.call().getBpi()).stream()
          .map(c -> {
            c.setTypeEnum(CoindeskCurrency.TypeEnum.INSERT);
            return c;
          })
          .forEach(currencyMod::insertCurrency);
      log.info("資料寫入完成");
    }
  }

  private List<CoindeskCurrency> getCoinList(CoindeskApiResponse.Bpi bpi) {
    return List.of(
        new CoindeskCurrency(bpi.getUSD()),
        new CoindeskCurrency(bpi.getGBP()),
        new CoindeskCurrency(bpi.getEUR())
    );
  }
}
