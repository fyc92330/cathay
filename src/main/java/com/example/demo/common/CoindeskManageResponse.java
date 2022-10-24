package com.example.demo.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@ToString
public class CoindeskManageResponse {

  private String updatedTime;
  private String disclaimer;
  private String chartName;
  private Bpi bpi;

  public CoindeskManageResponse(CoindeskApiResponse response, Bpi bpi){
    this.bpi = bpi;
    this.chartName = response.getChartName();
    this.disclaimer = response.getDisclaimer();
    this.updatedTime = LocalDateTime.parse(response.getTime().getUpdatedISO(), DemoConst.DATE_TIME_PARSE).format(DemoConst.DATE_TIME_FORMAT);
  }
}
