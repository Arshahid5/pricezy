package com.arshahid5.pricezy

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Data Class untuk menyimpan struktur data harga per item
data class PriceRecord(val priceId: Long, val productName: String, val storeName: String, val price: Double)

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

    // Fungsi pintar: Cek jika toko/produk sudah ada, gunakan ID lama. Jika belum, buat baru.
    fun addStore(name: String): Long {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT id FROM stores WHERE nama_toko = ? COLLATE NOCASE", arrayOf(name))
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            cursor.close()
            return id
        }
        cursor.close()
        val values = ContentValues().apply { put("nama_toko", name) }
        return db.insert("stores", null, values)
    }

    fun addProduct(name: String): Long {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT id FROM products WHERE nama_produk = ? COLLATE NOCASE", arrayOf(name))
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            cursor.close()
            return id
        }
        cursor.close()
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

    // Fungsi Query dengan Fitur Pencarian (Search)
    fun getPrices(queryStr: String = ""): List<PriceRecord> {
        val db = this.readableDatabase
        val list = mutableListOf<PriceRecord>()
        
        val query = if (queryStr.isEmpty()) {
            """
            SELECT pr.id, p.nama_produk, s.nama_toko, pr.nominal_harga 
            FROM prices pr
            JOIN products p ON pr.product_id = p.id
            JOIN stores s ON pr.store_id = s.id
            ORDER BY p.nama_produk ASC, pr.nominal_harga ASC
            """.trimIndent()
        } else {
            """
            SELECT pr.id, p.nama_produk, s.nama_toko, pr.nominal_harga 
            FROM prices pr
            JOIN products p ON pr.product_id = p.id
            JOIN stores s ON pr.store_id = s.id
            WHERE p.nama_produk LIKE '%$queryStr%' OR s.nama_toko LIKE '%$queryStr%'
            ORDER BY p.nama_produk ASC, pr.nominal_harga ASC
            """.trimIndent()
        }
        
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(PriceRecord(
                    priceId = cursor.getLong(0),
                    productName = cursor.getString(1),
                    storeName = cursor.getString(2),
                    price = cursor.getDouble(3)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updatePrice(priceId: Long, newPrice: Double) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("nominal_harga", newPrice) }
        db.update("prices", values, "id=?", arrayOf(priceId.toString()))
    }

    fun deletePrice(priceId: Long) {
        val db = this.writableDatabase
        db.delete("prices", "id=?", arrayOf(priceId.toString()))
    }
}