package com.github.mkay.jooq.reactive

import io.r2dbc.spi.ConnectionFactory
import org.jooq.impl.DSL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class JooqConfig {

    @Bean
    fun jooq(connectionFactory: ConnectionFactory) = DSL.using(TransactionAwareConnectionFactoryProxy(connectionFactory))
}