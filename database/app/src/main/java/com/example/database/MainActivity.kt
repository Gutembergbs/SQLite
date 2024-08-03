package com.example.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.os.Bundle
import android.view.View

// FeedReaderContract is not used in this code. If needed, you can remove it.
object FeedReaderContract {
    // Table contents are grouped together in an anonymous object.
    object FeedEntry : BaseColumns {
        const val TABLE_NAME = "entry"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_SUBTITLE = "subtitle"
    }
}

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Criação da tabela
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME_COL + " TEXT UNIQUE," +
                AGE_COL + " TEXT" + ")")
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Atualização da tabela
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun addName(name: String, age: String) {
        val values = ContentValues().apply {
            put(NAME_COL, name)
            put(AGE_COL, age)
        }
        val db = this.writableDatabase
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        db.close()
    }

    fun getName(query: String? = null): Cursor? {
        val db = this.readableDatabase
        return if (query.isNullOrEmpty()) {
            db.rawQuery("SELECT DISTINCT * FROM $TABLE_NAME", null)
        } else {
            db.rawQuery("SELECT DISTINCT * FROM $TABLE_NAME WHERE $NAME_COL LIKE ?", arrayOf("%$query%"))
        }
    }

    fun updateName(oldName: String, newName: String, newAge: String) {
        val values = ContentValues().apply {
            put(NAME_COL, newName)
            put(AGE_COL, newAge)
        }
        val db = this.writableDatabase
        db.update(TABLE_NAME, values, "$NAME_COL = ?", arrayOf(oldName))
        db.close()
    }

    companion object {
        const val DATABASE_NAME = "GEEKS_FOR_GEEKS"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "gfg_table"
        const val ID_COL = "id"
        const val NAME_COL = "name"
        const val AGE_COL = "age"
    }
}


class MainActivity : ComponentActivity() {
    private lateinit var edtName: EditText
    private lateinit var edtAge: EditText
    private lateinit var txtName: TextView
    private lateinit var txtAge: TextView
    private lateinit var searchName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        edtName = findViewById(R.id.enterName)
        edtAge = findViewById(R.id.enterAge)
        txtName = findViewById(R.id.Name)
        txtAge = findViewById(R.id.Age)
        searchName = findViewById(R.id.searchName)
    }

    fun addName(view: View) {
        val name = edtName.text.toString().trim()
        val age = edtAge.text.toString().trim()

        if (name.isEmpty() || age.isEmpty()) {
            Toast.makeText(this, "Nome e idade não podem estar vazios", Toast.LENGTH_LONG).show()
            return
        }

        val db = DBHelper(this)
        db.addName(name, age)
        Toast.makeText(this, "$name adicionado no banco de dados", Toast.LENGTH_LONG).show()
        clearField(edtName)
        clearField(edtAge)
    }


    fun printName(view: View) {
        val db = DBHelper(this)
        val cursor = db.getName()
        cursor?.use {
            txtName.text = ""
            txtAge.text = ""
            if (it.moveToFirst()) {
                do {
                    txtName.append("\n" + it.getString(it.getColumnIndexOrThrow(DBHelper.NAME_COL)))
                    txtAge.append("\n" + it.getString(it.getColumnIndexOrThrow(DBHelper.AGE_COL)))
                } while (it.moveToNext())
            }
        }
    }

    fun searchName(view: View) {
        val db = DBHelper(this)
        val query = searchName.text.toString()
        val cursor = db.getName(query)
        cursor?.use {
            txtName.text = ""
            txtAge.text = ""
            if (it.moveToFirst()) {
                do {
                    txtName.append("\n" + it.getString(it.getColumnIndexOrThrow(DBHelper.NAME_COL)))
                    txtAge.append("\n" + it.getString(it.getColumnIndexOrThrow(DBHelper.AGE_COL)))
                } while (it.moveToNext())
            }
        }
    }

    private fun clearField(editText: EditText) {
        editText.text.clear()
    }
}
