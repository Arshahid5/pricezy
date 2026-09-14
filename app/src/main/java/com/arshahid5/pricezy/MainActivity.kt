package com.arshahid5.pricezy

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)

        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Banding Harga (Pricezy)"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }

        val storeInput = EditText(this).apply { hint = "Nama Toko (Contoh: Indomaret)" }
        val productInput = EditText(this).apply { hint = "Nama Produk (Contoh: Kopi)" }
        val priceInput = EditText(this).apply { 
            hint = "Nominal Harga" 
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val saveBtn = Button(this).apply { 
            text = "SIMPAN DATA" 
            setBackgroundColor(Color.parseColor("#007BFF"))
            setTextColor(Color.WHITE)
        }
        
        val compareBtn = Button(this).apply { 
            text = "LIHAT PERBANDINGAN HARGA" 
            setBackgroundColor(Color.parseColor("#28A745"))
            setTextColor(Color.WHITE)
        }

        // --- TOMBOL BARU HAPUS DATA ---
        val clearBtn = Button(this).apply {
            text = "HAPUS SEMUA DATA"
            setBackgroundColor(Color.parseColor("#DC3545")) // Merah
            setTextColor(Color.WHITE)
        }
        
        val resultText = TextView(this).apply {
            text = "Hasil dari yang termurah akan muncul di sini."
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 48, 0, 0)
        }

        saveBtn.setOnClickListener {
            val storeName = storeInput.text.toString()
            val productName = productInput.text.toString()
            val priceStr = priceInput.text.toString()

            if (storeName.isNotBlank() && productName.isNotBlank() && priceStr.isNotBlank()) {
                val storeId = dbHelper.addStore(storeName)
                val productId = dbHelper.addProduct(productName)
                val price = priceStr.toDoubleOrNull() ?: 0.0
                
                if (storeId > 0 && productId > 0) {
                    dbHelper.addPrice(productId, storeId, price)
                    Toast.makeText(this, "Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                    storeInput.text.clear()
                    productInput.text.clear()
                    priceInput.text.clear()
                }
            } else {
                Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
            }
        }

        compareBtn.setOnClickListener {
            resultText.text = dbHelper.getComparedPrices()
        }

        // --- AKSI TOMBOL HAPUS DATA ---
        clearBtn.setOnClickListener {
            dbHelper.clearAllData()
            resultText.text = "Data berhasil dihapus."
            Toast.makeText(this, "Semua data telah dihapus!", Toast.LENGTH_SHORT).show()
        }

        mainLayout.addView(title)
        mainLayout.addView(storeInput)
        mainLayout.addView(productInput)
        mainLayout.addView(priceInput)
        
        val space = Space(this).apply { minimumHeight = 32 }
        mainLayout.addView(space)
        
        mainLayout.addView(saveBtn)
        mainLayout.addView(compareBtn)
        mainLayout.addView(clearBtn) // Menyisipkan tombol hapus ke tampilan
        mainLayout.addView(resultText)

        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
}