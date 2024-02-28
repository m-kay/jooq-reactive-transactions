package com.github.mkay.jooq.reactive

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.Profiles
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.lifecycle.Startables
import org.testcontainers.utility.DockerImageName

class TestcontainersInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    private val log = LoggerFactory.getLogger(TestcontainersInitializer::class.java)

    private val mysqlActiveProfiles = "dev | test"

    val network = Network.newNetwork()

    private val mysql: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:8"))
        .withDatabaseName("JOOQ_TEST")
        .withCommand("mysqld", "--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci")
        .waitingFor(HostPortWaitStrategy())
        .withReuse(true)
        .withNetwork(network)

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val env = applicationContext.environment
        if (env.acceptsProfiles(Profiles.of(mysqlActiveProfiles)) && DockerClientFactory.instance().isDockerAvailable) {
            log.info("starting mysql testcontainer for local development or test execution")
            Startables.deepStart(mysql).join()

            val properties = mapOf(
                "spring.r2dbc.url" to "r2dbc:mysql://${mysql.host}:${mysql.getMappedPort(MySQLContainer.MYSQL_PORT)}/${mysql.databaseName}",
                "spring.r2dbc.username" to mysql.username,
                "spring.r2dbc.password" to mysql.password,
                "spring.flyway.url" to mysql.jdbcUrl,
                "spring.flyway.user" to mysql.username,
                "spring.flyway.password" to mysql.password,
            )

            env.propertySources.addFirst(MapPropertySource("testcontainers-mysql", properties))
        }
    }
}