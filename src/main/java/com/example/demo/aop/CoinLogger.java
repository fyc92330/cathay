package com.example.demo.aop;

import com.example.demo.common.CoindeskCurrency;
import com.example.demo.common.DemoConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@RequiredArgsConstructor
@Component
public class CoinLogger {

  private final DSLContext dslContext;

  @Pointcut("execution(* com.example.demo.service.*.*(..))")
  public void modifyPointcut() {

  }

  /**
   * 非查詢的行為寫入log table
   *
   * @param joinPoint
   */
  @After(value = "modifyPointcut()")
  protected void afterAdvice(JoinPoint joinPoint) {
    Object[] objs = joinPoint.getArgs();
    if (objs.length != 0 && objs[0] instanceof CoindeskCurrency currency) {
      log.info("同步寫入logTable, coin:{}", currency);
      dslContext.insertInto(DemoConst.LOG_CURRENCY)
          .set(DSL.field(DSL.name("COIN_CODE")), currency.getCoinCode())
          .set(DSL.field(DSL.name("COIN_SYMBOL")), currency.getCoinSymbol())
          .set(DSL.field(DSL.name("COIN_RATE")), currency.getCoinRate())
          .set(DSL.field(DSL.name("COIN_DESC")), currency.getCoinDesc())
          .set(DSL.field(DSL.name("IS_REMOVED")), currency.getIsRemoved())
          .execute();
    }
  }
}
