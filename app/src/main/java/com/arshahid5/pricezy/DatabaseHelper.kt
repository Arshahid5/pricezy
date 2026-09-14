package com.arshahid5.pricezy

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "pricezy.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE stores (id INTEGER PRIMARY KEY AUTOINCREMENT, nama_toko TEXT NOT NULL)")
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, nama_produk TEXT NOT NULL)")
        db.execSQL("CREATE TABLE prices (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER, store_id INTEGER, nominal_harga REAL, FOREIGN KEY(product_id) REFERENCES products(id), FOREIGN KEY(store_id) REFERENCES stores(id))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS prices")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS stores")
        onCreate(db)
    }

    fun addStore(name: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("nama_toko", name) }
        return db.insert("stores", null, values)
    }

    fun addProduct(name: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("nama_produk", name) }
        return db.insert("products", null, values)
    }

    fun addPrice(productId: Long, storeId: Long, price: Double): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("product_id", productId)
            put("store_id", storeId)
            put("nominal_harga", price)
        }
        return db.insert("prices", null, values)
    }

    fun getComparedPrices(): String {
        val db = this.readableDatabase
        val query = """
            SELECT p.nama_produk, s.nama_toko, pr.nominal_harga 
            FROM prices pr
            JOIN products p ON pr.product_id = p.id
            JOIN stores s ON pr.store_id = s.id
            ORDER BY p.nama_produk ASC, pr.nominal_harga ASC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, null)
        val result = StringBuilder()
        
        if (cursor.moveToFirst()) {
            do {
                val product = cursor.getString(0)
                val store = cursor.getString(1)
                val price = cursor.getDouble(2)
                result.append("🏷️ $product\n🏪 $store | Rp $price\n\n")
            } while (cursor.moveToNext())
        } else {
            result.append("Belum ada data perbandingan harga.")
        }
        cursor.close()
        return result.toString()
    }

    // --- FUNGSI BARU UNTUK MENGHAPUS SEMUA DATA ---
    fun clearAllData() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM prices")
        db.execSQL("DELETE FROM products")
        db.execSQL("DELETE FROM stores")
    }
}