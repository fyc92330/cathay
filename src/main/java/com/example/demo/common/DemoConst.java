package com.example.demo.common;

import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.time.format.DateTimeFormatter;

public class DemoConst {
  public static final Table<Record> CURRENCY = DSL.table("currency");
  public static final Table<Record> LOG_CURRENCY = DSL.table("log_currency");
  public static final DateTimeFormatter DATE_TIME_PARSE = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
  public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
}
