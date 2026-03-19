package com.example.sqliteexample

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * DBHelper manages the database lifecycle (creation and updates)
 * and provides methods for CRUD operations. [cite: 138, 140]
 */
class DBHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    // 1. Define Constants for Database Configuration [cite: 143-155]
    // Using constants avoids "magic strings" and prevents typing mistakes [cite: 156-159].
    companion object {
        private const val DATABASE_NAME = "notes.db"
        private const val DATABASE_VERSION = 1

        // Table and Column Names
        private const val TABLE_NOTES = "notes"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_CONTENT = "content"
        private const val COL_DATE = "created_date"
    }

    // 2. Define the Table Creation Query [cite: 160-169]
    private val CREATE_TABLE = """
        CREATE TABLE $TABLE_NOTES (
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_TITLE TEXT,
            $COL_CONTENT TEXT,
            $COL_DATE TEXT
        )
    """.trimIndent()

    // Runs when the database is created for the first time [cite: 171-176].
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    // Runs when the DATABASE_VERSION is incremented [cite: 177-184].
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Warning: This simple implementation deletes old data on upgrade.
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        onCreate(db)
    }

    // --- DATABASE OPERATIONS (CRUD) ---

    /**
     * INSERT: Adds a new note to the table.
     * Returns the row ID of the newly inserted row, or -1 if an error occurred [cite: 185-205].
     */
    fun insertNote(title: String, content: String, date: String): Long {
        val db = this.writableDatabase // Use writableDatabase for changes [cite: 188, 190]
        val values = ContentValues()   // Stores data as Key -> Value pairs

        values.put(COL_TITLE, title)
        values.put(COL_CONTENT, content)
        values.put(COL_DATE, date)

        val result = db.insert(TABLE_NOTES, null, values)
        db.close() // Always close to free up resources [cite: 202]
        return result
    }

    /**
     * READ: Retrieves all notes from the database.
     * Returns a list of formatted strings for display [cite: 206-209].
     */
    fun getAllNotes(): MutableList<String> {
        val noteList = mutableListOf<String>()
        val db = this.readableDatabase // Use readableDatabase for queries

        // rawQuery returns a Cursor to iterate through results [cite: 212-213].
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NOTES", null)

        if (cursor.moveToFirst()) {
            do {
                // Safely access columns by name using getColumnIndexOrThrow [cite: 218-222].
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE))
                val content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))

                noteList.add("$id | $title ($date)\n$content")
            } while (cursor.moveToNext())
        }

        cursor.close() // Mandatory: Close cursor after reading [cite: 230-231]
        db.close()
        return noteList
    }

    /**
     * UPDATE: Modifies an existing note based on its ID [cite: 232-236].
     */
    fun updateNote(id: Int, title: String, content: String, date: String): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_TITLE, title)
        values.put(COL_CONTENT, content)
        values.put(COL_DATE, date)

        val rowsUpdated = db.update(
            TABLE_NOTES,
            values,
            "$COL_ID=?",
            arrayOf(id.toString())
        )
        db.close()
        return rowsUpdated
    }

    /**
     * DELETE: Removes a note from the database [cite: 237-239].
     */
    fun deleteNote(id: Int): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(
            TABLE_NOTES,
            "$COL_ID=?",
            arrayOf(id.toString())
        )
        db.close()
        return rowsDeleted
    }
}