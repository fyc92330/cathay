package com.example.demo.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Arrays;

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
  /** 是否移除 */
  private String isRemoved;

  public CoindeskCurrency(CoindeskApiResponse.Currency currency) {
    final String code = currency.getCode();
    this.coinCode = code;
    this.coinSymbol = currency.getSymbol();
    this.coinRate = currency.getRateFloat();
    this.coinDesc = currency.getDescription();
    this.coinName = Coin.getEnum(code).getNameStr();
  }


  /** 操作 */
  private TypeEnum typeEnum;
  private String type;

  public enum TypeEnum {
    INSERT,
    UPDATE,
    DELETE;

    public static TypeEnum getEnum(String type) {
      return Arrays.stream(values())
          .filter(e -> e.name().equals(type))
          .findAny()
          .orElseThrow(() -> new EnumConstantNotPresentException(TypeEnum.class, type));
    }
  }

  @Getter
  @AllArgsConstructor
  enum Coin {
    USD("美金"),
    GBP("英鎊"),
    EUR("歐元");

    private final String nameStr;

    public static Coin getEnum(String code) {
      return Arrays.stream(values())
          .filter(e -> e.name().equals(code))
          .findAny()
          .orElseThrow(() -> new EnumConstantNotPresentException(Coin.class, code));
    }
  }
}
