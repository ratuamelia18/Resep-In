package com.ratu.resep_in.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ratu.resep_in.R
import com.ratu.resep_in.data.Recipe

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ResepIn.db"
        private const val DATABASE_VERSION = 3 // Versi dinaikkan ke 3 agar tabel otomatis ter-update di HP
        private const val TABLE_NAME = "resep"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_IMAGE = "image_res_id"
        private const val COLUMN_VIDEO = "video_path" // Kolom baru untuk menampung lokasi video lokal
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_TITLE TEXT, " +
                "$COLUMN_DESCRIPTION TEXT, " +
                "$COLUMN_IMAGE INTEGER, " +
                "$COLUMN_VIDEO TEXT)")
        db?.execSQL(createTableQuery)

        // Data bawaan awal agar aplikasi tidak kosong saat demo di depan dosen
        db?.execSQL("INSERT INTO $TABLE_NAME ($COLUMN_TITLE, $COLUMN_DESCRIPTION, $COLUMN_IMAGE, $COLUMN_VIDEO) VALUES ('Nasi Goreng', 'Bahan-bahan:\n• 1 piring nasi putih\n• 2 siung bawang putih\n\nCara Membuat:\n1. Tumis bawang hingga harum.\n2. Masukkan nasi dan bumbu.', ${R.drawable.nasigoreng}, '')")
        db?.execSQL("INSERT INTO $TABLE_NAME ($COLUMN_TITLE, $COLUMN_DESCRIPTION, $COLUMN_IMAGE, $COLUMN_VIDEO) VALUES ('Ayam Taliwang', 'Bahan-bahan:\n• 1 ekor ayam\n• Cabe rawit\n\nCara Membuat:\n1. Bakar ayam hingga setengah matang.\n2. Lumuri bumbu pedas.', ${R.drawable.ayamtaliwang}, '')")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertRecipe(title: String, description: String, imageResId: Int, videoPath: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_IMAGE, imageResId)
            put(COLUMN_VIDEO, videoPath)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result
    }

    fun getAllRecipes(): List<Recipe> {
        val recipeList = mutableListOf<Recipe>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val titleString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val descString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val imageRes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE))
                val videoPathString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIDEO)) ?: ""

                recipeList.add(Recipe(titleString, descString, imageRes, videoPathString))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return recipeList
    }
}