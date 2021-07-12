package com.example.projemanag.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.models.Task
import java.util.*
import kotlin.collections.ArrayList

open class TaskListItemAdapter (
        private val context: Context,
        private var list: ArrayList<Task>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // A global variable for position dragged FROM.
    private var mPositionDraggedFrom = -1
    // A global variable for position dragged TO.
    private var mPositionDraggedTo = -1


    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        // Here we have done some additional changes to display the item of the task list item in 70% of the screen size

        //We want to use the item_task file for our View holder
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        // Here the layout params are converted dynamically according to the screen size as width is 70% and height is wrap_content.
        //parent.width * 0.7 We want to use 70% of screen width
        val layoutParams = LinearLayout.LayoutParams(
                (parent.width * 0.7).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Here the dynamic margins are applied to the view.
        //we're adding margins on th left and right (Using the toDp() and toPx() funcs we created below)
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)

        //Set out the layout parameters for our view
        view.layoutParams = layoutParams

        return MyViewHolder(view)
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
        //Get the model from whatever position we are in the list
        val model = list[position]

        if (holder is MyViewHolder) {
            //If the RecyclerView ViewHolder is the same as the MyViewHolder (Which we created below)

            //If we are the start of the list (With no cards yet)
            if (position == list.size - 1) {
                //Then display our Add List TextView
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
                // (If we are the start of the list, it will not display the LL that contains the cards and stuff)
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.GONE

                //holder.itemView.tv_add_task_list.visibility = View.VISIBLE
                //holder.itemView.ll_task_item.visibility = View.GONE

            } else {
                //Otherwise don't show the Add List TextView
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                //Show the LL where they can add tasks
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.VISIBLE
            }


            //Setting the title of the model to the Task Lists title
            holder.itemView.findViewById<TextView>(R.id.tv_task_list_title).text = model.title


            // A click event for showing the view for adding the task list name
            holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).setOnClickListener {
                //When they press add task, we want the "Add Task" TV to disappear
                //And we want the CardView where we write its name to appear
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<androidx.cardview.widget.CardView>(R.id.cv_add_task_list_name).visibility = View.VISIBLE
            }


            // A click event for hiding the view for adding the task list name
            holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name).setOnClickListener {
                //When we press the "Cross" ImageButton, we want the CardView where we write its name to disappear
                //And we want to show the Add Task Tv to appear again
                holder.itemView.findViewById<androidx.cardview.widget.CardView>(R.id.cv_add_task_list_name).visibility = View.GONE
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
            }


            // A click event for passing the task list name to the base activity function. To create a task list
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name).setOnClickListener {
                //When we press the "Tick" ImageButton is clicked
                //Get the name of the List from the EditText
                val listName = holder.itemView.findViewById<EditText>(R.id.et_task_list_name).text.toString()

                //If the list name is not empty
                if (listName.isNotEmpty()) {
                    // Here we check the context is an instance of the TaskListActivity.
                    if (context is TaskListActivity) {
                        //Call the createTaskList() func from TaskListActivity
                        context.createTaskList(listName)
                    }

                } else {
                    //Tell the user they can't create a board without a name
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }

            }



            // A click event for iv_edit_list for showing the editable view
            holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name).setOnClickListener {

                holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).setText(model.title) // Set the existing title
                holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility = View.VISIBLE //Show the CardView where we edit the name
            }

            // A click event for iv_close_editable_view for hiding the editable view
            holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view).setOnClickListener {
                //When we press the "Cross" ImageButton, we want the CardView where we edit its name to disappear
                holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility = View.GONE
            }


            // A click event for iv_edit_list for showing thr editable vie
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name).setOnClickListener {
                //When we press the "Tick" ImageButton is clicked on the Edit name CardView
                //Get the name of the List from the EditText
                val listName = holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).text.toString()

                //If the list name is not empty
                if (listName.isNotEmpty()) {
                    // Here we check the context is an instance of the TaskListActivity.
                    if (context is TaskListActivity) {
                        //Call the updateTaskList() func from TaskListActivity to show the updated version on the screen
                        context.updateTaskList(position, listName, model)
                    }

                } else {
                    //Let the user know they need to enter a name
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            // A click event for ib_delete_list for deleting the task list
            holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list).setOnClickListener {
                //Call the alert dialog func from below to make sure the user wants to delete the task
                alertDialogForDeleteList(position, model.title)
            }



            // TODO (A click event for adding a card in the task list )
            holder.itemView.findViewById<TextView>(R.id.tv_add_card).setOnClickListener {
                //When we click on "Add Card", we hide the "Add Card" TV and show the CardView where we add the add
                holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.VISIBLE

                // A click event for closing the view for card add in the task list
                holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name).setOnClickListener {
                    holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.VISIBLE
                    holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.GONE
                }


                // A click event for adding a card in the task list
                holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name).setOnClickListener {

                    val cardName = holder.itemView.findViewById<EditText>(R.id.et_card_name).text.toString()

                    if (cardName.isNotEmpty()) {
                        //If the name id not empty
                        if (context is TaskListActivity) {
                            //Call this func from the TaskListActivity to add the card
                            context.addCardToTaskList(position, cardName)
                        }

                    }else{
                        Toast.makeText(context, "Please Enter Card Detail.", Toast.LENGTH_SHORT).show()
                    }
                }
            }


            // Load the cards list in the recyclerView
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).layoutManager = LinearLayoutManager(context)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).setHasFixedSize(true)

            //Create an instance of its adapter
            val adapter = CardListItemsAdapter(context, model.cards)
            //Set the adapter to the RecyclerView
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).adapter = adapter


            // Added a click event on card items for card details
            //Creating an object of CardListItemsAdapter, so we can set an onClickListener
            adapter.setOnClickListener(object :
                CardListItemsAdapter.OnClickListener {
                //We override OnClickListener interface of the CardListItemsAdapter
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity) {
                        //We add the onClickListener to this list, on this card
                        context.cardDetails(position, cardPosition)
                    }
                }
            })



            // A feature to drag and drop the card items
            /**
             * Creates a divider {@link RecyclerView.ItemDecoration} that can be used with a
             * {@link LinearLayoutManager}.
             *
             * @param context Current context, it will be used to access resources.
             * @param orientation Divider orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}.
             */
            //DividerItemDecoration.VERTICAL So we can darg from top to bottom
            val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            //  Add it to our RV
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).addItemDecoration(dividerItemDecoration)

            //  Creates an ItemTouchHelper that will work with the given Callback.
            //ItemTouchHelper Allows you to drag and drop stuff
            //ItemTouchHelper.UP or ItemTouchHelper.DOWN We want to be able to drag them up or dwon
            val helper = ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                //We implemented this func and the func onSwiped() below for the ItemTouchHelper
                /*Called when ItemTouchHelper wants to move the dragged item from its old position to
                 the new position.*/
                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    //Variables for where it comes from, and where it goes
                    val draggedPosition = dragged.adapterPosition
                    val targetPosition = target.adapterPosition

                    // Assign the global variable with updated values
                    if (mPositionDraggedFrom == -1) {
                        mPositionDraggedFrom = draggedPosition
                    }
                    mPositionDraggedTo = targetPosition
                    // END


                    /**
                     * Swaps the elements at the specified positions in the specified list.
                     */
                    //We use the Collections list class' swap() function to move the cards in the list
                    Collections.swap(list[position].cards, draggedPosition, targetPosition)

                    // move item in `draggedPosition` to `targetPosition` in adapter.
                    //We have to notify the adapter otherwise it will not know something changed
                    adapter.notifyItemMoved(draggedPosition, targetPosition)

                    return false // true if moved, false otherwise
                }

                // Called when a ViewHolder is swiped by the user.
                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) { // remove from adapter
                }

                // Finally when the dragging is completed than call the function to update the cards in the database
                // And reset the global variables
                /*Called by the ItemTouchHelper when the user interaction with an element is over and it
                 also completed its animation.*/
                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)

                    //If the position dargged from or to is not -1, and the position dragged from to position to are not the same
                    if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {

                        //Run context as TaskListActivity, and call its func updateCardsInTaskList() so it updates the data
                        (context as TaskListActivity).updateCardsInTaskList(
                            position,
                            list[position].cards
                        )

                    }

                    // Reset the global variables
                    mPositionDraggedFrom = -1
                    mPositionDraggedTo = -1
                }

            })

            /*Attaches the ItemTouchHelper to the provided RecyclerView. If TouchHelper is already
            attached to a RecyclerView, it will first detach from the previous one.*/
            helper.attachToRecyclerView(holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list))


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


//    We're doing this because we want the view holder to take up 70% of the screen,
//    so we have space tpo scroll left and right
    /**
     * A function to get density pixel from pixel
     */
//    Resources.getSystem().displayMetrics.density).toInt() Gets the density pixels and converts them into an int
    private fun Int.toDp(): Int =
            (this / Resources.getSystem().displayMetrics.density).toInt()

    /**
     * A function to get pixel from density pixel
     */
    private fun Int.toPx(): Int =
            (this * Resources.getSystem().displayMetrics.density).toInt()


    /**
     * Method is used to show the Alert Dialog for deleting the task list.
     */
    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is TaskListActivity) {
                //Call the deleteTaskList() func from TaskListActivity
                context.deleteTaskList(position)
            }
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()

        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }


}