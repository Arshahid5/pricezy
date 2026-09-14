package com.arshahid5.pricezy

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var contentContainer: FrameLayout
    
    // Tampilan Tab
    private lateinit var viewInput: ScrollView
    private lateinit var viewList: LinearLayout

    // Komponen List & Pencarian
    private lateinit var listContainer: LinearLayout
    private lateinit var searchInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F3F4F6")) // Warna background aplikasi (abu-abu muda modern)
        }

        // --- Custom Header ---
        val header = TextView(this).apply {
            text = "Pricezy"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2563EB")) // Biru Modern
            setPadding(48, 48, 48, 48)
            gravity = Gravity.CENTER
        }
        rootLayout.addView(header)

        // --- Area Konten Utama ---
        contentContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f // Akan memanjang mengisi ruang kosong
            )
        }
        rootLayout.addView(contentContainer)

        setupInputView()
        setupListView()

        contentContainer.addView(viewInput)
        contentContainer.addView(viewList)

        // --- Bottom Navigation (Menu Bawah) ---
        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.WHITE)
        }

        val btnTabInput = Button(this).apply {
            text = "📝 INPUT DATA"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#2563EB"))
            setTypeface(null, Typeface.BOLD)
        }
        
        val btnTabList = Button(this).apply {
            text = "🔍 DAFTAR HARGA"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.GRAY)
            setTypeface(null, Typeface.BOLD)
        }

        btnTabInput.setOnClickListener {
            viewInput.visibility = View.VISIBLE
            viewList.visibility = View.GONE
            btnTabInput.setTextColor(Color.parseColor("#2563EB"))
            btnTabList.setTextColor(Color.GRAY)
        }

        btnTabList.setOnClickListener {
            viewInput.visibility = View.GONE
            viewList.visibility = View.VISIBLE
            btnTabList.setTextColor(Color.parseColor("#2563EB"))
            btnTabInput.setTextColor(Color.GRAY)
            loadListData() // Refresh list otomatis saat tab dibuka
        }

        bottomNav.addView(btnTabInput)
        bottomNav.addView(btnTabList)
        rootLayout.addView(bottomNav)

        btnTabInput.performClick() // Set tab default ke Input saat pertama kali buka
        setContentView(rootLayout)
    }

    private fun setupInputView() {
        viewInput = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Tambah Catatan Harga"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 48)
        }

        val storeInput = EditText(this).apply { hint = "Nama Toko (Contoh: Indomaret)" }
        val productInput = EditText(this).apply { hint = "Nama Produk (Contoh: Beras 1L)" }
        val priceInput = EditText(this).apply {
            hint = "Nominal Harga (Contoh: 15000)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val saveBtn = Button(this).apply {
            text = "SIMPAN DATA"
            setBackgroundColor(Color.parseColor("#10B981")) // Hijau Modern
            setTextColor(Color.WHITE)
            setPadding(0, 32, 0, 32)
        }
        
        val space = Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, 48) }

        saveBtn.setOnClickListener {
            val storeName = storeInput.text.toString().trim()
            val productName = productInput.text.toString().trim()
            val priceStr = priceInput.text.toString().trim()

            if (storeName.isNotEmpty() && productName.isNotEmpty() && priceStr.isNotEmpty()) {
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

        layout.addView(title)
        layout.addView(storeInput)
        layout.addView(productInput)
        layout.addView(priceInput)
        layout.addView(space)
        layout.addView(saveBtn)
        viewInput.addView(layout)
    }

    private fun setupListView() {
        viewList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }

        val searchContainer = LinearLayout(this).apply { setPadding(48, 48, 48, 16) }
        
        val searchBackground = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 24f
        }
        
        searchInput = EditText(this).apply {
            hint = "🔍 Ketik nama produk/toko..."
            background = searchBackground
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 32, 32, 32)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    loadListData(s.toString()) // Pencarian otomatis setiap ketikan
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        searchContainer.addView(searchInput)
        viewList.addView(searchContainer)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 48)
        }
        scrollView.addView(listContainer)
        viewList.addView(scrollView)
    }

    private fun loadListData(query: String = "") {
        listContainer.removeAllViews()
        val prices = dbHelper.getPrices(query)

        if (prices.isEmpty()) {
            val emptyTxt = TextView(this).apply {
                text = "Belum ada data / Tidak ditemukan."
                gravity = Gravity.CENTER
                textSize = 16f
                setPadding(0, 64, 0, 0)
            }
            listContainer.addView(emptyTxt)
            return
        }

        val cardBackground = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 24f
        }

        for (item in prices) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = cardBackground
                setPadding(48, 48, 48, 48)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 32) }
            }

            val titleTxt = TextView(this).apply {
                text = "🏷️ ${item.productName}"
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.BLACK)
            }
            
            val descTxt = TextView(this).apply {
                text = "🏪 ${item.storeName}\n💰 Rp ${item.price}"
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(0, 16, 0, 32)
            }

            val btnLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            
            val editBtn = Button(this).apply {
                text = "EDIT"
                setBackgroundColor(Color.parseColor("#F59E0B")) // Kuning
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            
            val space = Space(this).apply { layoutParams = LinearLayout.LayoutParams(24, 1) }

            val delBtn = Button(this).apply {
                text = "HAPUS"
                setBackgroundColor(Color.parseColor("#EF4444")) // Merah
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            editBtn.setOnClickListener { showEditDialog(item) }
            delBtn.setOnClickListener { 
                dbHelper.deletePrice(item.priceId)
                loadListData(searchInput.text.toString())
                Toast.makeText(this@MainActivity, "Data dihapus", Toast.LENGTH_SHORT).show()
            }

            btnLayout.addView(editBtn)
            btnLayout.addView(space)
            btnLayout.addView(delBtn)

            card.addView(titleTxt)
            card.addView(descTxt)
            card.addView(btnLayout)
            listContainer.addView(card)
        }
    }

    private fun showEditDialog(item: PriceRecord) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(item.price.toString())
        }
        
        AlertDialog.Builder(this)
            .setTitle("Edit Harga: ${item.productName}")
            .setMessage("Ganti harga di toko: ${item.storeName}")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newPrice = input.text.toString().toDoubleOrNull()
                if (newPrice != null) {
                    dbHelper.updatePrice(item.priceId, newPrice)
                    loadListData(searchInput.text.toString()) // Refresh list
                    Toast.makeText(this, "Harga berhasil diupdate!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}