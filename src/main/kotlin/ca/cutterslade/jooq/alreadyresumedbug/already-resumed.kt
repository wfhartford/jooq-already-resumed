package ca.cutterslade.jooq.alreadyresumedbug

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.jdk9.awaitFirst
import kotlinx.coroutines.jdk9.awaitFirstOrNull
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.kotlin.coroutines.transactionCoroutine
import org.testcontainers.containers.PostgreSQLContainer
import java.util.UUID

private val table = DSL.table("USERS")
private val idField = DSL.field("ID", SQLDataType.UUID)
private val nameField = DSL.field("NAME", SQLDataType.VARCHAR)
private val userRows = (1..100).map {
  DSL.row(UUID.randomUUID(), "user-$it")
}

suspend fun main() {
  PostgreSQLContainer("postgres:14.2-bullseye").use { postgresContainer ->
    postgresContainer.start()

    val dslContext = dslContext(postgresContainer)

    createTable(dslContext)
    countUsers(dslContext)
    insertUsers(dslContext)
    countUsers(dslContext)

    deleteUsersInTransaction(dslContext)
    countUsers(dslContext)
    deleteUsersWithoutTransaction(dslContext)
    countUsers(dslContext)
  }
}

private suspend fun deleteUsersInTransaction(dslContext: DSLContext) {
  val deleteCount = dslContext.transactionCoroutine { ctx ->
    deleteUsers(ctx.dsl())
  }
  println("Deleted $deleteCount users in transaction")
}

private suspend fun deleteUsersWithoutTransaction(dslContext: DSLContext) {
  val deleteCount = deleteUsers(dslContext)
  println("Deleted $deleteCount users without a transaction")
}

private suspend fun deleteUsers(dslContext: DSLContext) =
  dslContext.deleteFrom(table).awaitFirst()

private suspend fun createTable(dslContext: DSLContext) {
  dslContext.createTable(table)
    .columns(idField, nameField)
    .awaitFirstOrNull()
  println("Created USERS table")
}

private suspend fun insertUsers(dslContext: DSLContext) {
  val count = dslContext.insertInto(table)
    .columns(idField, nameField)
    .valuesOfRows(userRows)
    .awaitFirst()
  println("Inserted $count users")
}

private suspend fun countUsers(dslContext: DSLContext) {
  val count = dslContext.selectCount().from(table)
    .awaitFirst()
    .value1()
  println("There are currently $count users")
}

private fun dslContext(postgresContainer: PostgreSQLContainer<*>): DSLContext =
  DSL.using(
    ConnectionFactories.get(
      ConnectionFactoryOptions.builder()
        .option(ConnectionFactoryOptions.DRIVER, "postgresql")
        .option(ConnectionFactoryOptions.HOST, postgresContainer.host)
        .option(ConnectionFactoryOptions.PORT, postgresContainer.getMappedPort(5432))
        .option(ConnectionFactoryOptions.USER, postgresContainer.username)
        .option(ConnectionFactoryOptions.PASSWORD, postgresContainer.password)
        .option(ConnectionFactoryOptions.DATABASE, postgresContainer.databaseName)
        .build()
    )
  )
