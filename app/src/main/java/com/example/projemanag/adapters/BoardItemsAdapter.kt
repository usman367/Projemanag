package com.example.projemanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.models.Board

//An adapter class for Board Items in the MainActivity
//We create the constructor inside the () of the class
//We inherit from RecyclerView.Adapter
open class BoardItemsAdapter (
    private val context: Context,
    private var list: ArrayList<Board>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        //We want to use the item_board file for our recycleView list
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_board,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val iv_board_image = findViewById<ImageView>(R.id.iv_board_image)
//        val tv_name = findViewById<TextView>(R.id.tv_name)
//        val iv_board_image = findViewById<ImageView>(R.id.tv_created_by)


        //We get the model from the current position in the list
        //(list is passed in as a parameter to our class, and position to our method)
        val model = list[position]

        //If the holder we get from this methods parameter equal to MyViewHolder (at the bottom of the class)
        if (holder is MyViewHolder) {

            //Use Glide library to load the image
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.findViewById<ImageView>(R.id.iv_board_image)) //We use the holders image as our board image, We want to load it into iv_board_image
                //.into(holder.itemView.iv_board_image)

            //Setting the name and the created by of the board
            holder.itemView.findViewById<TextView>(R.id.tv_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_created_by).text = "Created By : ${model.createdBy}"
            //holder.itemView.tv_name.text = model.name
            //holder.itemView.tv_created_by.text = "Created By : ${model.createdBy}"

            //Setting an onClickListener on the board
            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    //Then we want to assign the onClickListener to the current model
                    //We create the onClick() func below
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {
        //Contains a function which will know the position where it was clicked, and the model
        fun onClick(position: Int, model: Board)
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)


    /**
     * A function for OnClickListener where the Interface is the expected parameter..
     */
    //So we can click on the items in the RecyclerView to launch the TaskListActivity
    fun setOnClickListener(onClickListener: OnClickListener) {
        //Set the onClickListener of this class to the onClickListener that's passed to this function
        this.onClickListener = onClickListener
    }
}