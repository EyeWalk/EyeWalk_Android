package com.insane.eyewalk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.insane.eyewalk.R
import com.insane.eyewalk.fragment.ContactFragment
import com.insane.eyewalk.model.Contact

class ContactViewAdapter(private val mList: List<Contact>, private val context: ContactFragment): RecyclerView.Adapter<ContactViewAdapter.ViewHolder>() {

//    val db = AppDataBase.getDataBase(context.requireContext())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_contact_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = mList[position]

        holder.tvUserName.text = contact.name

        if (contact.emergency)
            holder.tvUserEmergency.text = context.resources.getString(R.string.label_contact)
        else
            holder.tvUserEmergency.visibility = View.GONE

        // CLICK LISTENERS
        holder.llContactDetail.setOnClickListener { context.openContactDetail(contact) }

    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserEmergency: TextView = itemView.findViewById(R.id.tvUserEmergency)
        val llContactDetail: LinearLayout = itemView.findViewById(R.id.llContactDetail)
    }

}