package com.example.demo.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
}
