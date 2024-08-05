package com.example.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.activity.ComponentActivity
import android.os.Bundle
import android.view.View

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Criação da tabela
        val query = ("CREATE TABLE $TABLE_NAME ("
                + "$ID_COL INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$NAME_COL TEXT UNIQUE, "
                + "$AGE_COL TEXT)")
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Atualização da tabela
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
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

    // Função para atualizar nome e idade
    fun updateName(oldName: String, oldAge: String, newName: String, newAge: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(NAME_COL, newName)
            put(AGE_COL, newAge)
        }

        // Atualiza a tabela onde o nome e a idade correspondem aos antigos
        val result = db.update(TABLE_NAME, contentValues, "$NAME_COL=? AND $AGE_COL=?", arrayOf(oldName, oldAge))
        db.close()
        return result > 0 // Retorna true se uma linha foi atualizada, caso contrário, false
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
    private lateinit var button3: Button
    private var oldName: String? = null
    private var oldAge: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        edtName = findViewById(R.id.enterName)
        edtAge = findViewById(R.id.enterAge)
        txtName = findViewById(R.id.Name)
        txtAge = findViewById(R.id.Age)
        searchName = findViewById(R.id.searchName)
        button3 = findViewById(R.id.button3)

        // Inicializa button3 como invisível
        button3.visibility = View.INVISIBLE
    }

    fun addName(view: View) {
        val name = edtName.text.toString().trim()
        val age = edtAge.text.toString().trim()

        if (name.isEmpty() || age.isEmpty()) {
            Toast.makeText(this, "Nome e idade não podem estar vazios", Toast.LENGTH_LONG).show()
            return
        }

        val db = DBHelper(this)
        val cursor = db.getName(name)

        if (cursor != null && cursor.moveToFirst()) {
            Toast.makeText(this, "Nome já existe no banco de dados", Toast.LENGTH_LONG).show()
            cursor.close()
        } else {
            db.addName(name, age)
            Toast.makeText(this, "$name adicionado no banco de dados", Toast.LENGTH_LONG).show()
            clearField(edtName)
            clearField(edtAge)
            clearField(searchName)
            button3.visibility = View.INVISIBLE
        }
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
        clearField(edtName)
        clearField(edtAge)
        clearField(searchName)
        button3.visibility = View.INVISIBLE
    }

    fun searchName(view: View) {
        val db = DBHelper(this)
        val query = searchName.text.toString().trim()
        // Verifica se o campo de pesquisa está vazio
        if (query.isEmpty()) {
            Toast.makeText(this, "Por favor, insira um nome para pesquisar", Toast.LENGTH_LONG).show()
            return
        }
        val cursor = db.getName(query)
        cursor?.use {
            txtName.text = ""
            txtAge.text = ""
            if (it.moveToFirst()) {
                oldName = it.getString(it.getColumnIndexOrThrow(DBHelper.NAME_COL))
                oldAge = it.getString(it.getColumnIndexOrThrow(DBHelper.AGE_COL))
                edtName.setText(oldName)
                edtAge.setText(oldAge)
                do {
                    txtName.append("\n" + it.getString(it.getColumnIndexOrThrow(DBHelper.NAME_COL)))
                    txtAge.append("\n" + it.getString(it.getColumnIndexOrThrow(DBHelper.AGE_COL)))
                } while (it.moveToNext())

                // Verificar se ambos os campos estão preenchidos
                if (edtName.text.toString().isNotEmpty() && edtAge.text.toString().isNotEmpty()) {
                    button3.visibility = View.VISIBLE
                }

            } else {
                Toast.makeText(this, "Nenhum resultado encontrado", Toast.LENGTH_LONG).show()
            }
        }
        clearField(searchName)
    }

    fun editName(view: View) {
        val newName = edtName.text.toString().trim()
        val newAge = edtAge.text.toString().trim()

        if (newName.isEmpty() || newAge.isEmpty() || oldName == null || oldAge == null) {
            Toast.makeText(this, "Nome ou idade não podem estar vazios", Toast.LENGTH_LONG).show()
            return
        }

        val db = DBHelper(this)
        val updated = db.updateName(oldName!!, oldAge!!, newName, newAge)

        if (updated) {
            Toast.makeText(this, "Contato atualizado de $oldName ($oldAge) para $newName ($newAge)", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Falha ao atualizar $oldName.", Toast.LENGTH_LONG).show()
        }

        // Após a edição, torne o botão invisível novamente
        button3.visibility = View.INVISIBLE

        // Imprimir todos os nomes para verificar a atualização
        printName(view)
    }

    private fun clearField(editText: EditText) {
        editText.text.clear()
    }
}
