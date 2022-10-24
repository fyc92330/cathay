package com.example.demo;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
   * 呼叫coindesk - 重新爬取or從DB撈取
   *
   * @param isSearch
   * @return
   */
  @GetMapping("/query/{isSearch}")
  @ResponseBody
  public Object init(@PathVariable String isSearch) {
    return switch (isSearch) {
      case "Y" -> coindeskMod.call();
      case "N" -> currencyMod.listCurrencies();
      default -> throw new RuntimeException("參數錯誤");
    };
  }

  /**
   * 資料維護api
   *
   * @param bean
   * @return
   */
  @PutMapping("/refresh")
  @ResponseBody
  public Object update(@RequestBody CoinMaintainBean bean) {
    String isAutoSearch = bean.getIsAutoSearch();
    if (!StringUtils.containsAny(isAutoSearch, 'Y', 'N')) {
      throw new RuntimeException("參數錯誤");
    }

    List<CoindeskCurrency> codes = "N".equals(isAutoSearch)
        ? bean.getCodes()
        : this.getCoinList(coindeskMod.call().getBpi());

    codes.forEach(code -> {
      log.warn("{}", code);
      CoindeskCurrency.TypeEnum typeEnum = code.getTypeEnum();
      if (typeEnum == null) {
        typeEnum = CoindeskCurrency.TypeEnum.INSERT;
      }
      switch (typeEnum) {
        case INSERT -> currencyMod.insertCurrency(code);
        case DELETE -> currencyMod.removeCurrency(code);
        default -> currencyMod.updateCurrency(code);
      }
    });

    return ResponseEntity.ok().build();
  }

  private List<CoindeskCurrency> getCoinList(CoindeskApiResponse.Bpi bpi) {
    return List.of(
        new CoindeskCurrency(bpi.getUSD()),
        new CoindeskCurrency(bpi.getGBP()),
        new CoindeskCurrency(bpi.getEUR())
    );
  }
}
