package com.example.demo.common;

import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class DemoConst {
  public static final Table<Record> CURRENCY = DSL.table("currency");
  public static final Table<Record> LOG_CURRENCY = DSL.table("log_currency");
}
