package com.example.demo.service;

import com.example.demo.common.CoindeskApiResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CoindeskMod {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // 未知欄位忽略
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS) // Bean為空則報錯
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

  public CoindeskApiResponse call() {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
        .url("https://api.coindesk.com/v1/bpi/currentprice.json")
        .get().build();
    CoindeskApiResponse coindeskApiResponse;
    try (Response response = client.newCall(request).execute()) {
      coindeskApiResponse = objectMapper.readValue(response.body().string(), CoindeskApiResponse.class);
    } catch (Exception e) {
      log.error("", e);
      throw new RuntimeException("coindesk api 呼叫失敗");
    }
    return coindeskApiResponse;
  }

}
