package com.example.stylica

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class AdminProductSearchFragment : Fragment() {

    private data class Row(
        val id: Int,
        val name: String,
        val price: Double,
        val status: String,
        val imageUri: String?
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_admin_product_search, container, false)
        val dbHelper = DatabaseHelper(requireContext())
        val grid = view.findViewById<GridView>(R.id.gridProductSearch)
        val empty = view.findViewById<TextView>(R.id.tvEmptyProductSearch)
        val etModFirst = view.findViewById<EditText>(R.id.etModFirst)
        val etModLast = view.findViewById<EditText>(R.id.etModLast)
        val etModReg = view.findViewById<EditText>(R.id.etModReg)
        val etCat = view.findViewById<EditText>(R.id.etCat)
        val etSub = view.findViewById<EditText>(R.id.etSub)

        val list = ArrayList<Row>()

        val adapter: BaseAdapter = object : BaseAdapter() {
            override fun getCount() = list.size
            override fun getItem(position: Int) = list[position]
            override fun getItemId(position: Int) = list[position].id.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val row = convertView ?: layoutInflater.inflate(R.layout.item_pending_product, parent, false)
                val p = list[position]
                row.findViewById<TextView>(R.id.productNameTv).text = p.name + "\n(" + p.status + ")"
                row.findViewById<TextView>(R.id.productPriceTv).text = "Rs. ${p.price}"
                row.findViewById<Button>(R.id.approveBtn).visibility = View.GONE
                row.findViewById<ImageView>(R.id.editBtn).visibility = View.GONE
                val del = row.findViewById<ImageView>(R.id.deleteBtn)
                del.visibility = View.VISIBLE
                val iv = row.findViewById<ImageView>(R.id.productImageView)
                if (!p.imageUri.isNullOrEmpty()) {
                    try {
                        val uriString = p.imageUri.split(",").firstOrNull() ?: ""
                        if (uriString.isNotEmpty()) {
                            val inputStream = row.context.contentResolver.openInputStream(Uri.parse(uriString))
                            if (inputStream != null) {
                                iv.setImageBitmap(android.graphics.BitmapFactory.decodeStream(inputStream))
                                inputStream.close()
                            }
                        }
                    } catch (_: Exception) { }
                }
                del.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete product")
                        .setMessage("Delete ${p.name}?")
                        .setPositiveButton("Delete") { _, _ ->
                            if (dbHelper.deleteProduct(p.id)) {
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                reload()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                return row
            }
        }
        grid.adapter = adapter

        fun reload() {
            list.clear()
            val c = dbHelper.searchModeratorProductsForAdmin(
                etModFirst.text.toString(),
                etModLast.text.toString(),
                etModReg.text.toString(),
                etCat.text.toString(),
                etSub.text.toString()
            )
            if (c.moveToFirst()) {
                do {
                    val id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_ID))
                    val name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_NAME))
                    val price = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE))
                    val stIdx = c.getColumnIndex(DatabaseHelper.COL_STATUS)
                    val status = if (stIdx != -1 && !c.isNull(stIdx)) c.getString(stIdx) else ""
                    val imgIdx = c.getColumnIndex(DatabaseHelper.COL_PRODUCT_IMAGE)
                    val img = if (imgIdx != -1 && !c.isNull(imgIdx)) c.getString(imgIdx) else null
                    list.add(Row(id, name, price, status, img))
                } while (c.moveToNext())
            }
            c.close()
            adapter.notifyDataSetChanged()
            empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            grid.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }

        view.findViewById<Button>(R.id.btnRunProductSearch).setOnClickListener { reload() }
        reload()
        return view
    }
}
