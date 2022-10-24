package com.example.demo.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoindeskApiResponse {

  private static final long serialVersionUID = 1L;

  @JsonProperty("time")
  private CoindeskTime time;
  @JsonProperty("disclaimer")
  private String disclaimer;
  @JsonProperty("chartName")
  private String chartName;
  @JsonProperty("bpi")
  private Bpi bpi;

  @Data
  static class CoindeskTime {

    @JsonProperty("updated")
    private String updated;
    @JsonProperty("updatedISO")
    private String updatedISO;
    @JsonProperty("updateduk")
    private String updateduk;
  }

  @Data
  public static class Bpi {
    @JsonProperty("USD")
    private Currency USD;
    @JsonProperty("GBP")
    private Currency GBP;
    @JsonProperty("EUR")
    private Currency EUR;
  }

  @Data
  public static class Currency {
    /** 貨幣名稱 */
    @JsonProperty("code")
    private String code;
    /** 貨幣符號 */
    @JsonProperty("symbol")
    private String symbol;
    /** 貨幣匯率 */
    @JsonProperty("rate_float")
    private BigDecimal rateFloat;
    @JsonProperty("rate")
    private String rate;
    /** 貨幣描述 */
    @JsonProperty("description")
    private String description;

  }
}
