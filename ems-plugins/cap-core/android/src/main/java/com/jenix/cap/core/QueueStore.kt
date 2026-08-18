package com.jenix.cap.core

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class QueueRow(
    val id: Long,
    val payload: String,
    val dedupeKey: String?,
    val attempts: Int,
)

class QueueStore(context: Context) : SQLiteOpenHelper(context, "jenix_cap_core.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE ${CoreKeys.TABLE} (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "namespace TEXT NOT NULL," +
                "dedupeKey TEXT," +
                "payload TEXT NOT NULL," +
                "attempts INTEGER NOT NULL DEFAULT 0," +
                "createdAt INTEGER NOT NULL," +
                "lastError TEXT)"
        )
        db.execSQL("CREATE UNIQUE INDEX queue_namespace_dedupe ON ${CoreKeys.TABLE}(namespace, dedupeKey)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun enqueue(namespace: String, payload: String, dedupeKey: String? = null) {
        val values = ContentValues().apply {
            put("namespace", namespace)
            put("dedupeKey", dedupeKey)
            put("payload", payload)
            put("createdAt", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(CoreKeys.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun readBatch(namespace: String, limit: Int): List<QueueRow> {
        val rows = mutableListOf<QueueRow>()
        readableDatabase.query(
            CoreKeys.TABLE,
            arrayOf("id", "payload", "dedupeKey", "attempts"),
            "namespace = ?",
            arrayOf(namespace),
            null,
            null,
            "id ASC",
            limit.toString(),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                rows += QueueRow(
                    id = cursor.getLong(0),
                    payload = cursor.getString(1),
                    dedupeKey = cursor.getString(2),
                    attempts = cursor.getInt(3),
                )
            }
        }
        return rows
    }

    fun delete(ids: List<Long>) {
        if (ids.isEmpty()) return
        val placeholders = ids.joinToString(",") { "?" }
        writableDatabase.delete(CoreKeys.TABLE, "id IN ($placeholders)", ids.map(Long::toString).toTypedArray())
    }

    fun incrementAttempts(id: Long, message: String) {
        writableDatabase.execSQL(
            "UPDATE ${CoreKeys.TABLE} SET attempts = attempts + 1, lastError = ? WHERE id = ?",
            arrayOf(message, id),
        )
    }

    fun count(namespace: String): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ${CoreKeys.TABLE} WHERE namespace = ?",
            arrayOf(namespace),
        ).use { cursor ->
            cursor.moveToFirst()
            return cursor.getInt(0)
        }
    }
}
