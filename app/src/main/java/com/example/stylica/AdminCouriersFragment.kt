package com.example.stylica

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class AdminCouriersFragment : Fragment() {

    private data class C(val id: Int, val name: String, val phone: String?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_admin_couriers, container, false)
        val db = DatabaseHelper(requireContext())
        val lv = view.findViewById<ListView>(R.id.lvCouriers)
        val list = ArrayList<C>()

        val adapter = object : BaseAdapter() {
            override fun getCount() = list.size
            override fun getItem(p: Int) = list[p]
            override fun getItemId(p: Int) = list[p].id.toLong()
            override fun getView(p: Int, cv: View?, parent: ViewGroup?): View {
                val tv = cv as? android.widget.TextView ?: android.widget.TextView(parent!!.context).apply {
                    setPadding(32, 24, 32, 24)
                    setTextColor(resources.getColor(R.color.black, null))
                    textSize = 14f
                }
                val c = list[p]
                tv.text = "${c.name}\n${c.phone ?: ""}"
                return tv
            }
        }
        lv.adapter = adapter

        fun load() {
            list.clear()
            val cur = db.getAllCouriers()
            if (cur.moveToFirst()) {
                do {
                    val id = cur.getInt(cur.getColumnIndexOrThrow(DatabaseHelper.COL_COURIER_ID))
                    val name = cur.getString(cur.getColumnIndexOrThrow(DatabaseHelper.COL_COURIER_NAME))
                    val pi = cur.getColumnIndex(DatabaseHelper.COL_COURIER_PHONE)
                    val ph = if (pi != -1 && !cur.isNull(pi)) cur.getString(pi) else null
                    list.add(C(id, name, ph))
                } while (cur.moveToNext())
            }
            cur.close()
            adapter.notifyDataSetChanged()
        }

        fun dialog(courier: C?) {
            val box = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(48, 32, 48, 32)
            }
            val etName = EditText(requireContext()).apply { hint = "Name"; setText(courier?.name ?: "") }
            val etPhone = EditText(requireContext()).apply { hint = "Phone"; setText(courier?.phone ?: "") }
            box.addView(etName)
            box.addView(etPhone)
            AlertDialog.Builder(requireContext())
                .setTitle(if (courier == null) "Add courier" else "Edit courier")
                .setView(box)
                .setPositiveButton("Save") { _, _ ->
                    val n = etName.text.toString().trim()
                    val ph = etPhone.text.toString().trim()
                    if (n.isEmpty()) {
                        Toast.makeText(context, "Name required", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (courier == null) db.insertCourier(n, ph)
                    else db.updateCourier(courier.id, n, ph)
                    load()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        lv.setOnItemClickListener { _, _, pos, _ ->
            val c = list[pos]
            AlertDialog.Builder(requireContext())
                .setItems(arrayOf("Edit", "Delete")) { _, which ->
                    if (which == 0) dialog(c)
                    else {
                        db.deleteCourier(c.id)
                        load()
                    }
                }
                .show()
        }

        view.findViewById<Button>(R.id.btnAddCourier).setOnClickListener { dialog(null) }
        load()
        return view
    }
}
