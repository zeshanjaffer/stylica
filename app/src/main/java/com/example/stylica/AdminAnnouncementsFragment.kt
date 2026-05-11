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

class AdminAnnouncementsFragment : Fragment() {

    private data class Ann(val id: Int, val title: String, val type: String?, val created: String?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_admin_announcements, container, false)
        val db = DatabaseHelper(requireContext())
        val lv = view.findViewById<ListView>(R.id.lvAnnouncements)
        val etType = view.findViewById<EditText>(R.id.etAnnType)
        val etDate = view.findViewById<EditText>(R.id.etAnnDate)
        val list = ArrayList<Ann>()

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
                val a = list[p]
                tv.text = "${a.title}\nType: ${a.type ?: "-"} | ${a.created ?: ""}"
                return tv
            }
        }
        lv.adapter = adapter

        fun load() {
            list.clear()
            val c = db.searchAnnouncements(etType.text.toString(), etDate.text.toString())
            if (c.moveToFirst()) {
                do {
                    val id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ANN_ID))
                    val title = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ANN_TITLE))
                    val ti = c.getColumnIndex(DatabaseHelper.COL_ANN_TYPE)
                    val typ = if (ti != -1 && !c.isNull(ti)) c.getString(ti) else null
                    val ci = c.getColumnIndex(DatabaseHelper.COL_ANN_CREATED_AT)
                    val cr = if (ci != -1 && !c.isNull(ci)) c.getString(ci) else null
                    list.add(Ann(id, title, typ, cr))
                } while (c.moveToNext())
            }
            c.close()
            adapter.notifyDataSetChanged()
        }

        fun dialogEdit(ann: Ann?) {
            val container = android.widget.LinearLayout(requireContext()).apply { orientation = android.widget.LinearLayout.VERTICAL; setPadding(48, 32, 48, 32) }
            val tTitle = EditText(requireContext()).apply { hint = "Title"; setText(ann?.title ?: "") }
            val tBody = EditText(requireContext()).apply { hint = "Body"; setLines(3); setText("") }
            val tType = EditText(requireContext()).apply { hint = "Type"; setText(ann?.type ?: "") }
            if (ann != null) {
                val c2 = db.readableDatabase.rawQuery(
                    "SELECT ${DatabaseHelper.COL_ANN_BODY} FROM ${DatabaseHelper.TABLE_ANNOUNCEMENTS} WHERE ${DatabaseHelper.COL_ANN_ID}=?",
                    arrayOf(ann.id.toString())
                )
                if (c2.moveToFirst()) {
                    val bi = c2.getColumnIndex(DatabaseHelper.COL_ANN_BODY)
                    if (bi >= 0 && !c2.isNull(bi)) tBody.setText(c2.getString(bi))
                }
                c2.close()
            }
            container.addView(tTitle)
            container.addView(tBody)
            container.addView(tType)
            AlertDialog.Builder(requireContext())
                .setTitle(if (ann == null) "Add announcement" else "Edit announcement")
                .setView(container)
                .setPositiveButton("Save") { _, _ ->
                    val title = tTitle.text.toString().trim()
                    val body = tBody.text.toString().trim()
                    val type = tType.text.toString().trim()
                    if (title.isEmpty()) {
                        Toast.makeText(context, "Title required", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (ann == null) {
                        db.insertAnnouncement(title, body, type)
                    } else {
                        db.updateAnnouncement(ann.id, title, body, type)
                    }
                    load()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        lv.setOnItemClickListener { _, _, pos, _ ->
            val a = list[pos]
            AlertDialog.Builder(requireContext())
                .setItems(arrayOf("Edit", "Delete")) { _, which ->
                    if (which == 0) dialogEdit(a)
                    else {
                        db.deleteAnnouncement(a.id)
                        load()
                    }
                }
                .show()
        }

        view.findViewById<Button>(R.id.btnAnnSearch).setOnClickListener { load() }
        view.findViewById<Button>(R.id.btnAnnAdd).setOnClickListener { dialogEdit(null) }
        load()
        return view
    }
}
