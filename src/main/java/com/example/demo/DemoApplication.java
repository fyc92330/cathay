package com.example.demo;

import com.example.demo.common.Bpi;
import com.example.demo.common.CoinMaintainBean;
import com.example.demo.common.CoindeskApiResponse;
import com.example.demo.common.CoindeskCurrency;
import com.example.demo.service.CoindeskMod;
import com.example.demo.service.CurrencyMod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.tools.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/coin")
@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  private final CoindeskMod coindeskMod;
  private final CurrencyMod currencyMod;


  /**
   * 呼叫coindesk - 是否重新整理
   *
   * @param isConvert
   * @return
   */
  @GetMapping("/coindesk/{isConvert}")
  @ResponseBody
  public Object convert(@PathVariable String isConvert) {
    CoindeskApiResponse response = coindeskMod.call();
    this.validParams(isConvert);
    return "Y".equals(isConvert)
        ? currencyMod.convertResponse(response, this.getCoinList(response.getBpi()))
        : response;
  }

  /**
   * 呼叫db幣別資料
   *
   * @return
   */
  @GetMapping("/query")
  @ResponseBody
  public Object init() {
    return currencyMod.listCurrencies().stream()
        .filter(c -> "N".equals(c.getIsRemoved()))
        .collect(Collectors.toList());
  }

  /**
   * 增加幣別
   *
   * @param bean
   * @return
   */
  @PostMapping("/refresh")
  @ResponseBody
  public Object create(@RequestBody CoinMaintainBean bean) {
    String isAutoSearch = bean.getIsAutoSearch();
    this.validParams(isAutoSearch);
    List<CoindeskCurrency> coins = "N".equals(isAutoSearch)
        ? bean.getCoins()
        : this.getCoinList(coindeskMod.call().getBpi());

    log.info("開始進行");
    coins.stream()
        .filter(coin -> {
          final String type = coin.getType();
          return type == null || type.equals("INSERT");
        })
        .forEach(currencyMod::insertCurrency);

    return ResponseEntity.ok().build();
  }

  /**
   * 編輯幣別
   *
   * @param bean
   * @return
   */
  @PutMapping("/refresh")
  @ResponseBody
  public Object edit(@RequestBody CoinMaintainBean bean) {
    String isAutoSearch = bean.getIsAutoSearch();
    this.validParams(isAutoSearch);
    List<CoindeskCurrency> coins = "N".equals(isAutoSearch)
        ? bean.getCoins()
        : this.getCoinList(coindeskMod.call().getBpi());

    log.info("開始進行");
    coins.stream()
        .filter(coin -> "UPDATE".equals(coin.getType()))
        .forEach(currencyMod::updateCurrency);

    return ResponseEntity.ok().build();
  }

  /**
   * 移除幣別
   *
   * @param bean
   * @return
   */
  @DeleteMapping("/refresh")
  @ResponseBody
  public Object remove(@RequestBody CoinMaintainBean bean) {
    List<CoindeskCurrency> coins = bean.getCoins();
    coins.forEach(currencyMod::removeCurrency);
    return ResponseEntity.ok().build();
  }

  /** =================================================== private ================================================== */

  /**
   * 幣別集合
   *
   * @param bpi
   * @return
   */
  private List<CoindeskCurrency> getCoinList(Bpi bpi) {
    return List.of(
        new CoindeskCurrency(bpi.getUSD()),
        new CoindeskCurrency(bpi.getGBP()),
        new CoindeskCurrency(bpi.getEUR())
    );
  }

  /**
   * 檢核參數是否正確
   *
   * @param invalidStr
   */
  private void validParams(String invalidStr) {
    if (!StringUtils.containsAny(invalidStr, 'Y', 'N')) {
      throw new RuntimeException("參數錯誤");
    }
  }
}
