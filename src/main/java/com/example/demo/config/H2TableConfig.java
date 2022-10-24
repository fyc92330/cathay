package com.example.demo.config;

import com.example.demo.common.Bpi;
import com.example.demo.common.CoindeskCurrency;
import com.example.demo.service.CoindeskMod;
import com.example.demo.service.CurrencyMod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.CreateTableColumnStep;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.example.demo.common.DemoConst.CURRENCY;
import static com.example.demo.common.DemoConst.LOG_CURRENCY;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class H2TableConfig {
  private static final Map<String, DataType<?>> DATA_TYPE_MAP;
  private static final Map<String, DataType<?>> LOG_DATA_TYPE_MAP;

  private final CoindeskMod coindeskMod;
  private final CurrencyMod currencyMod;
  private final DSLContext dslContext;

  static {
    DATA_TYPE_MAP = Map.of(
        "COIN_NUM", DefaultDataType.getDataType(SQLDialect.DEFAULT, Long.class).identity(true).nullable(false),
        "COIN_NAME", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(false).length(32),
        "COIN_CODE", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(false).length(32),
        "COIN_SYMBOL", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(false).length(16),
        "COIN_RATE", DefaultDataType.getDataType(SQLDialect.DEFAULT, BigDecimal.class).identity(false).nullable(false),
        "COIN_DESC", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(true),
        "IS_REMOVED", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(false).defaultValue("N"),
        "CREATE_DATE", DefaultDataType.getDataType(SQLDialect.DEFAULT, LocalDateTime.class).defaultValue(LocalDateTime.now()),
        "UPDATE_DATE", DefaultDataType.getDataType(SQLDialect.DEFAULT, LocalDateTime.class)
    );
    LOG_DATA_TYPE_MAP = Map.of(
        "INDEX", DefaultDataType.getDataType(SQLDialect.DEFAULT, Long.class).identity(true).nullable(false),
        "COIN_NUM", DefaultDataType.getDataType(SQLDialect.DEFAULT, Long.class).identity(false).nullable(true),
        "COIN_NAME", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(true).length(32),
        "COIN_CODE", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(true).length(32),
        "COIN_SYMBOL", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(true).length(16),
        "COIN_RATE", DefaultDataType.getDataType(SQLDialect.DEFAULT, BigDecimal.class).identity(false).nullable(true),
        "COIN_DESC", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(true),
        "IS_REMOVED", DefaultDataType.getDataType(SQLDialect.DEFAULT, String.class).identity(false).nullable(true),
        "CREATE_DATE", DefaultDataType.getDataType(SQLDialect.DEFAULT, LocalDateTime.class).defaultValue(LocalDateTime.now()));
  }

  @Bean
  public void init() {
    this.initH2LogTable();
    this.initH2Table();
  }

  /**
   * 初始化建立table
   */
  private void initH2Table() {
    if (this.tableExists(CURRENCY)) {
      return;
    }

    log.info("找不到已建立的表格, 開始建立...");
    CreateTableColumnStep step = dslContext.createTableIfNotExists(CURRENCY);
    DATA_TYPE_MAP.forEach(step::column);
    step.constraint(DSL.primaryKey("COIN_NUM"))
        .constraint(DSL.unique(DSL.name("COIN_NAME"), DSL.name("COIN_CODE")))
        .execute();

    // 寫入資料
    this.getCoinList(coindeskMod.call().getBpi()).forEach(currencyMod::insertCurrency);
    log.info("資料寫入完成");
  }

  /**
   * 初始化建立LogTable
   */
  private void initH2LogTable() {
    if (this.tableExists(LOG_CURRENCY)) {
      return;
    }

    log.info("找不到已建立的表格, 開始建立...");
    CreateTableColumnStep step = dslContext.createTableIfNotExists(LOG_CURRENCY);
    LOG_DATA_TYPE_MAP.forEach(step::column);

    step.constraint(DSL.primaryKey("INDEX"))
        .constraint(DSL.unique(DSL.name("COIN_NAME"), DSL.name("COIN_CODE")))
        .execute();
    log.info("LogTable 建立完成");
  }

  private boolean tableExists(Table<Record> table) {
    try {
      dslContext.selectFrom(table).fetchAny();
      log.info("table已存在: {}", table.getName());
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private List<CoindeskCurrency> getCoinList(Bpi bpi) {
    return List.of(
        new CoindeskCurrency(bpi.getUSD()),
        new CoindeskCurrency(bpi.getGBP()),
        new CoindeskCurrency(bpi.getEUR())
    );
  }
}
