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

class AdminEmployeesFragment : Fragment() {

    private data class E(val rowId: Int, val code: String, val first: String, val last: String)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_admin_employees, container, false)
        val db = DatabaseHelper(requireContext())
        val lv = view.findViewById<ListView>(R.id.lvEmployees)
        val etCode = view.findViewById<EditText>(R.id.etEmpCode)
        val etFirst = view.findViewById<EditText>(R.id.etEmpFirst)
        val etLast = view.findViewById<EditText>(R.id.etEmpLast)
        val list = ArrayList<E>()

        val adapter = object : BaseAdapter() {
            override fun getCount() = list.size
            override fun getItem(p: Int) = list[p]
            override fun getItemId(p: Int) = list[p].rowId.toLong()
            override fun getView(p: Int, cv: View?, parent: ViewGroup?): View {
                val tv = cv as? android.widget.TextView ?: android.widget.TextView(parent!!.context).apply {
                    setPadding(32, 24, 32, 24)
                    setTextColor(resources.getColor(R.color.black, null))
                    textSize = 14f
                }
                val e = list[p]
                tv.text = "ID: ${e.code}\n${e.first} ${e.last}"
                return tv
            }
        }
        lv.adapter = adapter

        fun load() {
            list.clear()
            val c = db.searchEmployees(etCode.text.toString(), etFirst.text.toString(), etLast.text.toString())
            if (c.moveToFirst()) {
                do {
                    val rowId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_ID))
                    val code = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_CODE))
                    val fn = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_FIRST))
                    val ln = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_EMP_LAST))
                    list.add(E(rowId, code, fn, ln))
                } while (c.moveToNext())
            }
            c.close()
            adapter.notifyDataSetChanged()
        }

        fun dialog(emp: E?) {
            val box = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(48, 32, 48, 32)
            }
            val etC = EditText(requireContext()).apply { hint = "Employee id"; setText(emp?.code ?: "") }
            val etF = EditText(requireContext()).apply { hint = "First name"; setText(emp?.first ?: "") }
            val etL = EditText(requireContext()).apply { hint = "Last name"; setText(emp?.last ?: "") }
            box.addView(etC)
            box.addView(etF)
            box.addView(etL)
            AlertDialog.Builder(requireContext())
                .setTitle(if (emp == null) "Add employee" else "Edit employee")
                .setView(box)
                .setPositiveButton("Save") { _, _ ->
                    val code = etC.text.toString().trim()
                    val fn = etF.text.toString().trim()
                    val ln = etL.text.toString().trim()
                    if (code.isEmpty() || fn.isEmpty() || ln.isEmpty()) {
                        Toast.makeText(context, "All fields required", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (emp == null) {
                        val id = db.insertEmployee(code, fn, ln)
                        if (id == -1L) Toast.makeText(context, "Duplicate employee id", Toast.LENGTH_SHORT).show()
                    } else {
                        val n = db.updateEmployee(emp.rowId, code, fn, ln)
                        if (n == 0) Toast.makeText(context, "Update failed (duplicate id?)", Toast.LENGTH_SHORT).show()
                    }
                    load()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        lv.setOnItemClickListener { _, _, pos, _ ->
            val e = list[pos]
            AlertDialog.Builder(requireContext())
                .setItems(arrayOf("Edit", "Delete")) { _, which ->
                    if (which == 0) dialog(e)
                    else {
                        db.deleteEmployee(e.rowId)
                        load()
                    }
                }
                .show()
        }

        view.findViewById<Button>(R.id.btnEmpSearch).setOnClickListener { load() }
        view.findViewById<Button>(R.id.btnEmpAdd).setOnClickListener { dialog(null) }
        load()
        return view
    }
}
