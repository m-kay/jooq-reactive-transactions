package com.github.mkay.jooq.reactive

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JooqReactiveTransactionApplication

fun main(args: Array<String>) {
	runApplication<JooqReactiveTransactionApplication>(*args){
		addInitializers(TestcontainersInitializer())
	}
}
