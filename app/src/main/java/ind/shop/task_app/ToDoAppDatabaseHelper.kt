package ind.shop.task_app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ToDoAppDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TODOAPP_DB"
        private const val DATABASE_VERSION = 3 // bump to 3 for completed_at
        private const val TABLE_NAME = "Tasks_Table"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title_of_task"
        private const val COL_DESC = "description_of_task"
        private const val COL_IS_COMPLETED = "is_completed"
        private const val COL_COMPLETED_AT = "completed_at"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
        CREATE TABLE $TABLE_NAME (
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_TITLE TEXT,
            $COL_DESC TEXT,
            $COL_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
            $COL_COMPLETED_AT INTEGER
        )
    """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COL_IS_COMPLETED INTEGER NOT NULL DEFAULT 0")
        }
        if (oldVersion < 3) {
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COL_COMPLETED_AT INTEGER")
        }
    }

    fun insertTask(task: Tasks_detail) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, task.title)
            put(COL_DESC, task.text_description)
            put(COL_IS_COMPLETED, task.isCompleted)
            task.completedAt?.let { put(COL_COMPLETED_AT, it) }
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllPendingTasks(): List<Tasks_detail> {
        val list = mutableListOf<Tasks_detail>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID, $COL_TITLE, $COL_DESC, $COL_IS_COMPLETED, $COL_COMPLETED_AT FROM $TABLE_NAME WHERE $COL_IS_COMPLETED = 0 ORDER BY $COL_ID DESC",
            null
        )
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESC))
            val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_COMPLETED))
            val completedAt = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COL_COMPLETED_AT)))
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_COMPLETED_AT)) else null
            list.add(Tasks_detail(id, title, desc, isCompleted, completedAt))
        }
        cursor.close()
        db.close()
        return list
    }

    fun getAllCompletedTasks(): List<Tasks_detail> {
        val list = mutableListOf<Tasks_detail>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID, $COL_TITLE, $COL_DESC, $COL_IS_COMPLETED, $COL_COMPLETED_AT FROM $TABLE_NAME WHERE $COL_IS_COMPLETED = 1 ORDER BY $COL_COMPLETED_AT DESC, $COL_ID DESC",
            null
        )
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESC))
            val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_COMPLETED))
            val completedAt = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COL_COMPLETED_AT)))
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_COMPLETED_AT)) else null
            list.add(Tasks_detail(id, title, desc, isCompleted, completedAt))
        }
        cursor.close()
        db.close()
        return list
    }

    fun getTaskById(taskId: Int): Tasks_detail {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID, $COL_TITLE, $COL_DESC, $COL_IS_COMPLETED, $COL_COMPLETED_AT FROM $TABLE_NAME WHERE $COL_ID = ?",
            arrayOf(taskId.toString())
        )
        cursor.moveToFirst()
        val completedAt = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COL_COMPLETED_AT)))
            cursor.getLong(cursor.getColumnIndexOrThrow(COL_COMPLETED_AT)) else null
        val task = Tasks_detail(
            cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_DESC)),
            cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_COMPLETED)),
            completedAt
        )
        cursor.close()
        db.close()
        return task
    }

    fun updateTask(task: Tasks_detail) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, task.title)
            put(COL_DESC, task.text_description)
        }
        db.update(TABLE_NAME, values, "$COL_ID=?", arrayOf(task.id.toString()))
        db.close()
    }

    fun markTaskCompleted(taskId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_COMPLETED, 1)
            put(COL_COMPLETED_AT, System.currentTimeMillis())
        }
        db.update(TABLE_NAME, values, "$COL_ID=?", arrayOf(taskId.toString()))
        db.close()
    }

    fun markTaskIncomplete(taskId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_COMPLETED, 0)
            putNull(COL_COMPLETED_AT)
        }
        db.update(TABLE_NAME, values, "$COL_ID=?", arrayOf(taskId.toString()))
        db.close()
    }

    fun deleteTask(taskId: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COL_ID=?", arrayOf(taskId.toString()))
        db.close()
    }


}
