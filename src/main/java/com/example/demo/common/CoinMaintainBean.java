package com.example.demo.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CoinMaintainBean {
  /** 呼叫api進行更新 */
  private String isAutoSearch;
  /** 貨幣名稱 */
  private List<CoindeskCurrency> coins;
}
