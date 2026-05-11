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

class AdminPaymentCompaniesFragment : Fragment() {

    private data class Pc(val id: Int, val name: String, val kind: String?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_admin_payment_companies, container, false)
        val db = DatabaseHelper(requireContext())
        val lv = view.findViewById<ListView>(R.id.lvPaymentCompanies)
        val list = ArrayList<Pc>()

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
                val x = list[p]
                tv.text = "${x.name}\nKind: ${x.kind ?: "-"}"
                return tv
            }
        }
        lv.adapter = adapter

        fun load() {
            list.clear()
            val c = db.getAllPaymentCompanies()
            if (c.moveToFirst()) {
                do {
                    val id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PC_ID))
                    val name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PC_NAME))
                    val ki = c.getColumnIndex(DatabaseHelper.COL_PC_KIND)
                    val k = if (ki != -1 && !c.isNull(ki)) c.getString(ki) else null
                    list.add(Pc(id, name, k))
                } while (c.moveToNext())
            }
            c.close()
            adapter.notifyDataSetChanged()
        }

        fun dialog(pc: Pc?) {
            val box = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(48, 32, 48, 32)
            }
            val etName = EditText(requireContext()).apply { hint = "Company name"; setText(pc?.name ?: "") }
            val etKind = EditText(requireContext()).apply {
                hint = "Kind: insurance | payment_receiver"
                setText(pc?.kind ?: "")
            }
            box.addView(etName)
            box.addView(etKind)
            AlertDialog.Builder(requireContext())
                .setTitle(if (pc == null) "Add company" else "Edit company")
                .setView(box)
                .setPositiveButton("Save") { _, _ ->
                    val n = etName.text.toString().trim()
                    val k = etKind.text.toString().trim()
                    if (n.isEmpty()) {
                        Toast.makeText(context, "Name required", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (pc == null) db.insertPaymentCompany(n, k)
                    else db.updatePaymentCompany(pc.id, n, k)
                    load()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        lv.setOnItemClickListener { _, _, pos, _ ->
            val x = list[pos]
            AlertDialog.Builder(requireContext())
                .setItems(arrayOf("Edit", "Delete")) { _, which ->
                    if (which == 0) dialog(x)
                    else {
                        db.deletePaymentCompany(x.id)
                        load()
                    }
                }
                .show()
        }

        view.findViewById<Button>(R.id.btnAddPc).setOnClickListener { dialog(null) }
        load()
        return view
    }
}
