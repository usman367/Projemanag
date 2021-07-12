package com.example.projemanag.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.adapters.MemberListItemsAdapter
import com.example.projemanag.models.User

//Create a members list dialog class to show the list of members in a dialog
abstract class MembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
) : Dialog(context) {

    //Createas an object of the MemberListItemsAdapter
    private var adapter: MemberListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        //We want to use the dialog_list as our layout
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        //Set the title of the view
        view.findViewById<TextView>(R.id.tvTitle).text = title

        //If the list is not empty
        if (list.size > 0) {

            //We want to use a linearLayout for the RecyclerView
            view.findViewById<RecyclerView>(R.id.rvList).layoutManager = LinearLayoutManager(context)
            //Initialize the adapter
            adapter = MemberListItemsAdapter(context, list)
            //Assign it to our RecyclerView
            view.findViewById<RecyclerView>(R.id.rvList).adapter = adapter


            adapter!!.setOnClickListener(object :
            //Create an object of MemberListItemsAdapter
                MemberListItemsAdapter.OnClickListener {
                //Override its onClick() func
                override fun onClick(position: Int, user: User, action:String) {
                    //Close the dialog
                    dismiss()
                    //Call the onItemSelected() func from below
                    onItemSelected(user, action)
                }
            })
        }
    }


    protected abstract fun onItemSelected(user: User, action:String)
}