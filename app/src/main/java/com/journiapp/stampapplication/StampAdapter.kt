package com.journiapp.stampapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.journiapp.stampapplication.model.Stamp
import com.journiapp.stampapplication.utils.StempClickListener
import com.squareup.picasso.Picasso


class StampAdapter(
    val stempClickListener: StempClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var stamps: ArrayList<Stamp> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_stamp, parent, false)
        return StampViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return stamps.count()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as StampViewHolder).bind(stamps[position])

        holder.itemView.setOnLongClickListener {
            stempClickListener.onStempClickListener(position)
            return@setOnLongClickListener true
        }
    }

    class StampViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var BASE_API_URL = "https://www.journiapp.com/picture/"

        fun bind(stamp: Stamp) {
            val ivStamp = itemView.findViewById<ImageView>(R.id.iv_stamp)

            Picasso.get()
                .load(BASE_API_URL + stamp.pictureGuid + "_stamp.png")
                .into(ivStamp)
        }
    }
}