package com.example.demo.service;

import com.example.demo.common.Bpi;
import com.example.demo.common.CoindeskApiResponse;
import com.example.demo.common.CoindeskCurrency;
import com.example.demo.common.CoindeskManageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.example.demo.common.DemoConst.CURRENCY;

@Slf4j
@RequiredArgsConstructor
@Service
public class CurrencyMod {
  private final DSLContext dslContext;

  /**
   * 取得列表
   *
   * @return
   */
  public synchronized List<CoindeskCurrency> listCurrencies() {

    List<Field<?>> columns = Arrays.asList(
        DSL.field(DSL.name("COIN_NUM")),
        DSL.field(DSL.name("COIN_NAME")),
        DSL.field(DSL.name("COIN_CODE")),
        DSL.field(DSL.name("COIN_SYMBOL")),
        DSL.field(DSL.name("COIN_RATE")),
        DSL.field(DSL.name("COIN_DESC")),
        DSL.field(DSL.name("IS_REMOVED"))
    );

    return dslContext.select(columns)
        .from(CURRENCY)
        .fetch()
        .into(CoindeskCurrency.class);
  }

  /**
   * 更新
   *
   * @param coindeskCurrency
   */
  @Transactional
  public void updateCurrency(CoindeskCurrency coindeskCurrency) {
    log.info("開始更新 coin:{}", coindeskCurrency);
    dslContext.update(CURRENCY)
        .set(DSL.field(DSL.name("COIN_SYMBOL")), coindeskCurrency.getCoinSymbol())
        .set(DSL.field(DSL.name("COIN_RATE")), coindeskCurrency.getCoinRate())
        .set(DSL.field(DSL.name("COIN_DESC")), coindeskCurrency.getCoinDesc())
        .set(DSL.field(DSL.name("UPDATE_DATE")), LocalDateTime.now())
        .where(DSL.field(DSL.name("COIN_CODE")).eq(coindeskCurrency.getCoinCode()))
        .execute();
  }

  /**
   * 軟刪除
   *
   * @param coindeskCurrency
   */
  @Transactional
  public void removeCurrency(CoindeskCurrency coindeskCurrency) {
    log.info("開始移除 coin:{}", coindeskCurrency);
    dslContext.update(CURRENCY)
        .set(DSL.field(DSL.name("IS_REMOVED")), "Y")
        .set(DSL.field(DSL.name("UPDATE_DATE")), LocalDateTime.now())
        .where(DSL.field(DSL.name("COIN_CODE")).eq(coindeskCurrency.getCoinCode()))
        .execute();
  }

  /**
   * 新增
   *
   * @param coindeskCurrency
   */
  @Transactional
  public void insertCurrency(CoindeskCurrency coindeskCurrency) {
    log.info("開始建立新的幣別 coin:{}", coindeskCurrency);
    final String code = coindeskCurrency.getCoinCode();
    final CoindeskCurrency currency = this.listCurrencies().stream()
        .filter(c -> code.equals(c.getCoinCode()))
        .findAny()
        .orElseGet(CoindeskCurrency::new);
    // 建立幣值
    if (currency.getCoinNum() == null) {
      dslContext.insertInto(CURRENCY)
          .set(DSL.field(DSL.name("COIN_NAME")), coindeskCurrency.getCoinName())
          .set(DSL.field(DSL.name("COIN_CODE")), code)
          .set(DSL.field(DSL.name("COIN_SYMBOL")), coindeskCurrency.getCoinSymbol())
          .set(DSL.field(DSL.name("COIN_RATE")), coindeskCurrency.getCoinRate())
          .set(DSL.field(DSL.name("COIN_DESC")), coindeskCurrency.getCoinDesc())
          .execute();
    } else if ("Y".equals(currency.getIsRemoved())) {
      // 如果是被軟刪除的, 重新啟用
      dslContext.update(CURRENCY)
          .set(DSL.field(DSL.name("IS_REMOVED")), "N")
          .where(DSL.field(DSL.name("COIN_CODE")).eq(code))
          .execute();
    }
  }

  /**
   * 轉換資料格式
   *
   * @param response
   * @param coins
   * @return
   */
  public CoindeskManageResponse convertResponse(CoindeskApiResponse response, List<CoindeskCurrency> coins) {
    Bpi bpi = new Bpi();
    this.listCurrencies().stream()
        .filter(c -> coins.stream().map(CoindeskCurrency::getCoinCode).anyMatch(c.getCoinCode()::equals))
        .forEach(c -> {
          final String coinCode = c.getCoinCode();
          Bpi.Currency currency = new Bpi.Currency();
          currency.setName(c.getCoinName());
          currency.setCode(coinCode);
          currency.setRateFloat(c.getCoinRate());

          switch (CoindeskCurrency.Coin.getEnum(coinCode)) {
            case USD -> bpi.setUSD(currency);
            case EUR -> bpi.setEUR(currency);
            case GBP -> bpi.setGBP(currency);
          }
        });

    return new CoindeskManageResponse(response, bpi);
  }
}
