package com.example.storage

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.sql.rowset.CachedRowSet
import javax.sql.rowset.RowSetProvider

class Database(hostname: String = "localhost", port: Int = 3306, database: String, username: String, password: String) {
    private var source: HikariDataSource

    init {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl =
            "jdbc:mysql://${hostname}:${port}/${database}"
        hikariConfig.username = username
        hikariConfig.password = password
        source = HikariDataSource(hikariConfig)
    }


    suspend fun asyncQuery(q: String, vararg replacements: Any = emptyArray()): CachedRowSet {
        return withContext(Dispatchers.IO) {
            query(q, *replacements)
        }
    }

    fun query(q: String, vararg replacements: Any = emptyArray()): CachedRowSet {
        source.connection.use { conn ->
            val s = conn.prepareStatement(q)

            var i = 1

            replacements.forEach { replacement -> s.setObject(i++, replacement) }

            val set = s.executeQuery()

            val cachedSet = RowSetProvider.newFactory().createCachedRowSet()
            cachedSet.populate(set)

            return cachedSet
        }
    }

    suspend fun asyncUpdateQuery(q: String, vararg replacements: Any = emptyArray()) {
        withContext(Dispatchers.IO) {
            updateQuery(q, *replacements)
        }
    }

    fun updateQuery(q: String, vararg replacements: Any = emptyArray()) {
        source.connection.use { conn ->
            val s = conn.prepareStatement(q)

            var i = 1

            replacements.forEach { replacement -> s.setObject(i++, replacement) }

            try {
                s.executeUpdate()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

}

class Replacements(vararg val replacements: Any = emptyArray())