package com.example.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import java.util.ArrayList

//Create an adapter class for selection of card label color using the "item_label_color"
class LabelColorListItemsAdapter (
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //We want to use v as individual items in the RecyclerView
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_label_color,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        //Get the individual item from the list at the current position
        val item = list[position]

        if (holder is MyViewHolder) {
            //We set the background color of the view to the color of the item
            //parseColor If a color is passed as a String e.e., #FFFFFF, it will make an actual color of it
            holder.itemView.findViewById<View>(R.id.view_main).setBackgroundColor(Color.parseColor(item))

            if (item == mSelectedColor) {
                //If this item is selected, then make the tick on the right Visible
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_color).visibility = View.VISIBLE
            } else {
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_color).visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                //Making the item clickable
                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick(position, item)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    //Adding this interface because we want to be able to click on the individual elements
    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }
}