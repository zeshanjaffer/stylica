package com.example.stylica

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class AdminModeratorsFragment : Fragment() {

    private data class M(
        val email: String,
        val first: String,
        val last: String,
        val domain: String?,
        val reg: String?
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_admin_moderators, container, false)
        val db = DatabaseHelper(requireContext())
        val lv = view.findViewById<ListView>(R.id.lvModerators)
        val etDomain = view.findViewById<EditText>(R.id.etDomainPart)
        val etReg = view.findViewById<EditText>(R.id.etRegDatePart)
        val etCat = view.findViewById<EditText>(R.id.etProdCatPart)
        val list = ArrayList<M>()

        val adapter = object : BaseAdapter() {
            override fun getCount() = list.size
            override fun getItem(position: Int) = list[position]
            override fun getItemId(position: Int) = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val tv = convertView as? TextView ?: TextView(parent!!.context).apply {
                    setPadding(32, 24, 32, 24)
                    setTextColor(resources.getColor(R.color.black, null))
                    textSize = 14f
                }
                val m = list[position]
                tv.text = "${m.first} ${m.last}\n${m.email}\nDomain: ${m.domain ?: "-"}\nRegistered: ${m.reg ?: "-"}"
                return tv
            }
        }
        lv.adapter = adapter

        fun load() {
            list.clear()
            val c = db.searchModerators(etDomain.text.toString(), etReg.text.toString(), etCat.text.toString())
            if (c.moveToFirst()) {
                do {
                    val em = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL))
                    val fn = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME))
                    val ln = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME))
                    val di = c.getColumnIndex(DatabaseHelper.COL_DOMAIN)
                    val dom = if (di != -1 && !c.isNull(di)) c.getString(di) else null
                    val ri = c.getColumnIndex(DatabaseHelper.COL_REGISTERED_AT)
                    val reg = if (ri != -1 && !c.isNull(ri)) c.getString(ri) else null
                    list.add(M(em, fn, ln, dom, reg))
                } while (c.moveToNext())
            }
            c.close()
            adapter.notifyDataSetChanged()
        }

        view.findViewById<Button>(R.id.btnSearchMods).setOnClickListener { load() }
        load()
        return view
    }
}
