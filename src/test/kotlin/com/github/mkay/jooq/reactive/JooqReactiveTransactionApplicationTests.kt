package com.github.mkay.jooq.reactive

import com.github.mkay.jooq.reactive.infrastructure.TransactionalService
import com.github.mkay.jooq.tables.references.PERSONS
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import reactor.kotlin.core.publisher.toMono

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersInitializer::class])
class JooqReactiveTransactionApplicationTests {

    @Autowired
    private lateinit var transactionalService: TransactionalService

    @Autowired
    private lateinit var jooq: DSLContext

    @Autowired
    private lateinit var r2dbc: R2dbcEntityTemplate

    @Test
    fun `test transaction`() {
        assertThrows<DataAccessException> {
            transactionalService.transactionalInsertsWithJooq(
                "21314", "Hans Muster",
                "123" to "Haupstrasse 10, 9000 St. Gallen",
                "124" to "Strasse",
            ).block()
        }

        val (count) = jooq.selectCount().from(PERSONS).toMono().block()!!
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `test transaction r2dbc`() {
        assertThrows<DataAccessResourceFailureException> {
            transactionalService.transactionalInsertsWithR2dbc(
                "213148", "Hans Muster",
                "1234" to "Haupstrasse 10, 9000 St. Gallen",
                "1244" to "Strasse",
            ).block()
        }

        val count = r2dbc.databaseClient
            .sql("select count(*) as count from PERSONS WHERE id = ?")
            .bind(0, "213148")
            .map { row -> row.get("count", Long::class.java) }
            .first().block()!!

        assertThat(count).isEqualTo(0)
    }

}
