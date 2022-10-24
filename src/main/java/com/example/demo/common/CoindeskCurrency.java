package com.example.demo.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CoindeskCurrency {

  /** 貨幣流水號 */
  private Long coinNum;
  /** 貨幣名稱 */
  private String coinName;
  /** 貨幣代碼 */
  private String coinCode;
  /** 貨幣符號 */
  private String coinSymbol;
  /** 貨幣匯率 */
  private BigDecimal coinRate;
  /** 貨幣描述 */
  private String coinDesc;

  public CoindeskCurrency(CoindeskApiResponse.Currency currency) {
    this.coinCode = currency.getCode();
    this.coinSymbol = currency.getSymbol();
    this.coinRate = currency.getRateFloat();
    this.coinDesc = currency.getDescription();
  }

  /** 是否移除 */
  private String isRemoved;
  /** 操作 */
  private TypeEnum typeEnum;

  public enum TypeEnum {
    INSERT,
    UPDATE,
    DELETE
  }
}
