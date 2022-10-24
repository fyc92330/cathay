package com.example.demo;

import com.example.demo.common.CoinMaintainBean;
import com.example.demo.common.CoindeskCurrency;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.example.demo.common.DemoConst.CURRENCY;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class DemoApplicationTests {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private DSLContext dslContext;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("測試呼叫資料轉換的API,並顯示其內容。")
  public void apiResultConvert() throws Exception {
    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .get("/api/v1/coin/coindesk/Y")
        .contentType(MediaType.APPLICATION_JSON);
    MvcResult result = mockMvc.perform(requestBuilder)
        .andExpect(status().isOk())
        .andReturn();
    log.info("api 資料轉換: {}", result.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("測試呼叫coindeskAPI,並顯示其內容。")
  public void apiResultNonConvert() throws Exception {
    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .get("/api/v1/coin/coindesk/N")
        .contentType(MediaType.APPLICATION_JSON);
    MvcResult result = mockMvc.perform(requestBuilder)
        .andExpect(status().isOk())
        .andReturn();
    log.info("api 原始檔案: {}", result.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("測試呼叫查詢幣別對應表資料 API,並顯示其內容。")
  public void queryCurrency() throws Exception {
    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .get("/api/v1/coin/query")
        .contentType(MediaType.APPLICATION_JSON);
    MvcResult result = mockMvc.perform(requestBuilder)
        .andExpect(status().isOk())
        .andReturn();
    log.info("查詢資料: {}", result.getResponse().getContentAsString());
  }

  @Test
  @DisplayName("測試呼叫新增幣別對應表資料 API。")
  public void insertCurrency() throws Exception {
    final String code = CoindeskCurrency.Coin.NTD.name();
    // 新增幣別
    CoindeskCurrency currency = new CoindeskCurrency();
    currency.setCoinName("新台幣");
    currency.setCoinCode(code);
    currency.setCoinSymbol("$");
    currency.setCoinRate(BigDecimal.ONE);
    currency.setCoinDesc("台灣使用的新台幣");

    CoinMaintainBean bean = new CoinMaintainBean();
    bean.setIsAutoSearch("N");
    bean.setCoins(List.of(currency));
    String json = objectMapper.writeValueAsString(bean);


    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .post("/api/v1/coin/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json);
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());

    // 檢核
    this.validDatabase(code);
  }

  @Test
  @DisplayName("測試呼叫更新幣別對應表資料 API,並顯示其內容。")
  public void updateCurrency() throws Exception {
    final CoindeskCurrency.Coin coin = CoindeskCurrency.Coin.USD;
    // 新增幣別
    CoindeskCurrency currency = new CoindeskCurrency();
    currency.setCoinName(coin.getNameStr());
    currency.setCoinCode(coin.name());
    currency.setCoinDesc("美金，是美金!");
    currency.setType("UPDATE");

    CoinMaintainBean bean = new CoinMaintainBean();
    bean.setIsAutoSearch("N");
    bean.setCoins(List.of(currency));
    String json = objectMapper.writeValueAsString(bean);

    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .put("/api/v1/coin/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json);
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());

    // 檢核
    this.validDatabase(coin.name());
  }

  @Test
  @DisplayName("測試呼叫刪除幣別對應表資料 API。")
  public void deleteCurrency() throws Exception {
    final CoindeskCurrency.Coin coin = CoindeskCurrency.Coin.GBP;
    // 新增幣別
    CoindeskCurrency currency = new CoindeskCurrency();
    currency.setCoinCode(coin.name());
    currency.setType("DELETE");

    CoinMaintainBean bean = new CoinMaintainBean();
    bean.setCoins(List.of(currency));
    String json = objectMapper.writeValueAsString(bean);

    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .delete("/api/v1/coin/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json);
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());

    // 檢核
    this.validDatabase(coin.name());
  }

  /**
   * 檢核資料庫是否成功改動
   *
   * @param coinCode
   */
  private void validDatabase(String coinCode){
    List<Field<?>> columns = Arrays.asList(
        DSL.field(DSL.name("COIN_NUM")),
        DSL.field(DSL.name("COIN_NAME")),
        DSL.field(DSL.name("COIN_CODE")),
        DSL.field(DSL.name("COIN_SYMBOL")),
        DSL.field(DSL.name("COIN_RATE")),
        DSL.field(DSL.name("COIN_DESC")),
        DSL.field(DSL.name("IS_REMOVED"))
    );

    CoindeskCurrency currency = dslContext.select(columns)
        .from(CURRENCY)
        .where(DSL.field(DSL.name("COIN_CODE")).eq(coinCode))
        .fetchAny(r -> r.into(CoindeskCurrency.class));

    log.info("[檢核] 幣別:{}, 內容:{}", coinCode, currency);
  }
}
