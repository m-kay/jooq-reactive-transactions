package com.github.mkay.jooq.reactive

import com.github.mkay.jooq.tables.records.AddressesRecord
import com.github.mkay.jooq.tables.records.PersonsRecord
import com.github.mkay.jooq.tables.references.ADDRESSES
import com.github.mkay.jooq.tables.references.PERSONS
import org.jooq.DSLContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class TransactionalService(
    private val jooq: DSLContext,
    private val r2dbc: R2dbcEntityTemplate
) {

    @Transactional
    fun transactionalInsertsWithJooq(
        personId: String,
        personName: String,
        vararg adresses: Pair<String, String>
    ): Mono<Void> {
        return jooq.insertInto(PERSONS, PERSONS.ID, PERSONS.NAME)
            .valuesOfRecords(PersonsRecord(personId, personName))
            .toMono()
            .then(
                jooq.insertInto(ADDRESSES, ADDRESSES.ID, ADDRESSES.PERSONID, ADDRESSES.ADDRESS)
                    .valuesOfRecords(
                        adresses.map { AddressesRecord(it.first, personId, it.second) }
                    )
                    .toMono()
                    .then()
            )
    }

    @Transactional
    fun transactionalInsertsWithR2dbc(
        personId: String,
        personName: String,
        vararg addresses: Pair<String, String>
    ): Mono<Void> {
        return r2dbc.databaseClient.sql("INSERT INTO PERSONS(id, name) values (?, ?)")
            .bind(0, personId).bind(1, personName)
            .fetch()
            .rowsUpdated()
            .then(
                insertAddresses(personId, listOf(*addresses))
            ).then()
    }

    private fun insertAddresses(personId: String, addresses: List<Pair<String, String>>): Mono<Long> {
        var sql = r2dbc.databaseClient.sql(
            "INSERT INTO ADDRESSES(id, personId, address) values ${addresses.joinToString(", ") { "(?, ?, ?)" }}")
        addresses.forEachIndexed { i, address ->
            sql = sql.bind(i*3, address.first)
            sql = sql.bind(i*3+1, personId)
            sql = sql.bind(i*3+2, address.second)
        }
        return sql.fetch().rowsUpdated()
    }


}