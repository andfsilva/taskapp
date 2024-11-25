package com.example.taskapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "TaskDatabase"
        private const val TABLE_TASKS = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_DEADLINE_DATE_UTC = "deadline_date_utc" // Armazena data e hora em UTC
        private const val COLUMN_NOTIFY = "notify"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_TASKS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_DESCRIPTION TEXT," +
                "$COLUMN_DEADLINE_DATE_UTC TEXT," + // Data e hora em UTC
                "$COLUMN_NOTIFY INTEGER" +
                ")"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN $COLUMN_DEADLINE_DATE_UTC TEXT") // Adiciona a coluna UTC na atualização
        } else {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
            onCreate(db)
        }
    }

    fun addTask(description: String, date: String?, time: String?, notify: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_DESCRIPTION, description)

        val deadlineDateUtc = if (date != null && time != null) {
            // Converte para UTC antes de salvar para manter horário padrão em todo aplicativo.
            convertToUtc(date, time)
        } else {
            null
        }
        values.put(COLUMN_DEADLINE_DATE_UTC, deadlineDateUtc)
        values.put(COLUMN_NOTIFY, if (notify) 1 else 0)

        db.insert(TABLE_TASKS, null, values)
        db.close()
    }
    private fun convertToUtc(date: String, time: String): String? {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        return try {
            val parsedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse("$date $time")
            parsedDate?.let { format.format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_TASKS"
        val cursor = db.rawQuery(query, null)

        val utcFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
            val deadlineDateUtc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEADLINE_DATE_UTC))
            val notify = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFY)) == 1

            val localDateTime = try {
                val date = utcFormat.parse(deadlineDateUtc)
                val localFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                localFormat.timeZone = TimeZone.getDefault()
                val formattedDate = localFormat.format(date)
                val dateLocal = formattedDate.substring(0,10)
                val timeLocal = formattedDate.substring(11,16)
                Pair(dateLocal,timeLocal)
            } catch (e: java.lang.Exception){
                e.printStackTrace()
                null
            }


            localDateTime?.let {
                tasks.add(Task(id, description, it.first, it.second, notify))
            }
        }

        cursor.close()
        db.close()
        return tasks
    }

    private fun convertUtcToLocal(dateTimeUTC: String): Pair<String, String>? {
        val utcFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")

        val localFormatDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val localFormatTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        localFormatDate.timeZone = TimeZone.getDefault()
        localFormatTime.timeZone = TimeZone.getDefault()

        return try {
            val date = utcFormat.parse(dateTimeUTC)
            date?.let {
                Pair(localFormatDate.format(it), localFormatTime.format(it))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateTask(id: Int, description: String, date: String?, time: String?, notify: Boolean): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_DESCRIPTION, description)

        val deadlineDateUtc = if (date != null && time != null) {
            convertToUtc(date, time)
        } else {
            null
        }

        values.put(COLUMN_DEADLINE_DATE_UTC, deadlineDateUtc)
        values.put(COLUMN_NOTIFY, if (notify) 1 else 0)

        val success = db.update(TABLE_TASKS, values, "$COLUMN_ID = ?", arrayOf(id.toString())) > 0
        db.close()
        return success
    }

    fun deleteTask(taskId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_TASKS, "$COLUMN_ID=?", arrayOf(taskId.toString()))
        db.close()
    }

    fun updateTaskNotify(taskId: Int, notify: Boolean): Boolean {
        val db = this.writableDatabase

        val contentValues = ContentValues().apply {
            put(COLUMN_NOTIFY, if (notify) 1 else 0)
        }
        val result = db.update(TABLE_TASKS, contentValues, "$COLUMN_ID=?", arrayOf(taskId.toString()))
        db.close()

        return result != -1
    }


}
