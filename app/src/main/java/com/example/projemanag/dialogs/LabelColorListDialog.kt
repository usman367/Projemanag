package com.example.projemanag.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.adapters.LabelColorListItemsAdapter

//Create a class for showing the label color list dialog
//Made it abstract so you can't instantiate them (make objects of them) but yoy can inherit them
abstract class LabelColorListDialog (
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private var mSelectedColor: String = ""
) : Dialog(context) { //The class inherits from a dialog class

    //Creating an object of our adapter
    private var adapter: LabelColorListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        //We create a view which will allow us to inflate our own layout we created
        //We want use dialog_list
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)

        setContentView(view) //Set it as the view we created above
        setCanceledOnTouchOutside(true) //Once we click outside of it, it will close
        setCancelable(true)
        //Call this func to set up the RecyclerView with the colors
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        //We set the title of the dialog to the title that's passed in
        view.findViewById<TextView>(R.id.tvTitle).text = title
        //We set the RecyclerView to be a linear layout manager
        view.findViewById<RecyclerView>(R.id.rvList).layoutManager = LinearLayoutManager(context)
        //We create an instance of our adapter
        adapter = LabelColorListItemsAdapter(context, list, mSelectedColor)
        //We set it to the RecyclerView
        view.findViewById<RecyclerView>(R.id.rvList).adapter = adapter

        //Ever adapter needs an onClickListener
        //We create an object of the LabelColorListItemsAdapter and call its func OnItemClickListener
        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener {
            //When an item is clicked
            override fun onClick(position: Int, color: String) {
                //We want to close this dialog
                dismiss()
                //Call this func using the color that's passed in
                onItemSelected(color)
            }
        }
    }

    //We will override this in the CardDetailsActivity2
    protected abstract fun onItemSelected(color: String)
}