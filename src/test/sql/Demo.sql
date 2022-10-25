CREATE TABLE CURRENCY
(
    COIN_NUM    integer
        CONSTRAINT CURRENCY_PK PRIMARY KEY,
    COIN_NAME   varchar(32)    NOT NULL,
    COIN_CODE   varchar(32)    NOT NULL,
    COIN_SYMBOL varchar(16)    NOT NULL,
    COIN_RATE   numeric(12, 3) NOT NULL,
    COIN_DESC   varchar(512),
    IS_REMOVED  char           NOT NULL DEFAULT 'N',
    CREATE_DATE timestamp      NOT NULL DEFAULT NOW(),
    UPDATE_DATE timestamp
)

CREATE TABLE LOG_CURRENCY
(
    LOG_INDEX       integer
        CONSTRAINT LOG_CURRENCY_PK PRIMARY KEY,
    COIN_NUM    integer NOT NULL,
    COIN_NAME   varchar(32),
    COIN_CODE   varchar(32),
    COIN_SYMBOL varchar(16),
    COIN_RATE   numeric(12, 3),
    COIN_DESC   varchar(512),
    IS_REMOVED  char,
    CREATE_DATE timestamp DEFAULT NOW(),
)