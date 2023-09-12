package com.studyolle.studyolle.infra;

import org.testcontainers.containers.*;
public abstract class AbstractContainerBaseTest {


    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer();
        POSTGRE_SQL_CONTAINER.start();
    }

}
