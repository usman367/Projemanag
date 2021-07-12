package com.example.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.models.Card
import com.example.projemanag.models.SelectedMembers

// Create an adapter class for cards list so we can display the cards in the Recycler View
class CardListItemsAdapter (
private val context: Context,
private var list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //item_card We want to use this for the individual cards
        return MyViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_card,
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
        val model = list[position]

        if (holder is MyViewHolder) {
            //This sets the color of the actual card (Not the color you see when you click on the card, that's in CardDetailsActivity)
            // As we have already have a View Item for label color so make it visible and set the selected label color
            if (model.labelColor.isNotEmpty()) {
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.VISIBLE
                holder.itemView.findViewById<View>(R.id.view_label_color).setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.GONE
            }

            //Put the name of the card in it
            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name

            // Adding an on click listener to the card item view
            holder.itemView.setOnClickListener{
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }


            //For displaying the cards members on the card (not the more details of card)
            // Now with use of public list of Assigned members detail List populate the recyclerView for Assigned Members
            //If we're interacting with the TassListActivity and its lists size is greater than 0
            if ((context as TaskListActivity).mAssignedMembersDetailList.size > 0) {
                // A instance of selected members list.
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                // Here we got the detail list of members and add it to the selected members list as required.
                //Go through the members in the list
                for (i in context.mAssignedMembersDetailList.indices) {
                    //For every single member in assignedTo
                    for (j in model.assignedTo) {
                        //If its the member is the same as the one that's assigned to the card
                        if (context.mAssignedMembersDetailList[i].id == j) {
                            //Create an instance of the SelectedMembers data class
                            val selectedMember = SelectedMembers(
                                context.mAssignedMembersDetailList[i].id,
                                context.mAssignedMembersDetailList[i].image
                            )

                            //Add it to the list
                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                //If the list we created above is empty
                if (selectedMembersList.size > 0) {

                    //If the lists size is 1 and the member is in the list is the person who created the card
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        //Then we don't want to show the members RecyclerView
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.GONE
                    } else {
                        //Otherwise, display it
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.VISIBLE

                        // Use a GridLayout for the RV
                        //spanCount = 4, we want to display up to 4 members next to each other
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).layoutManager = GridLayoutManager(context, 4)
                        //Create an instance of the Adapter
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                        //Assign it to the RV
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).adapter = adapter
                        //Create an object of CardMemberListItemsAdapter
                        adapter.setOnClickListener(object :
                            CardMemberListItemsAdapter.OnClickListener {
                            //Override ist onClick() func
                            override fun onClick() {
                                //If the there is a onClickListener, use it on this item
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }

                } else {
                    //If the lists size is not greater than 0, then don't show the RV
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.GONE
                }

            }


        }


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
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    /**
     * A function for OnClickListener where the Interface is the expected parameter..
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {
        fun onClick(position: Int)
    }

}