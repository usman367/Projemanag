package com.example.projemanag.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.adapters.TaskListItemAdapter
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.Card
import com.example.projemanag.models.Task
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants

class TaskListActivity : BaseActivity() {

    // A global variable for Board Details.
    private lateinit var mBoardDetails: Board

    // A global variable for board document id as mBoardDocumentId
    private lateinit var mBoardDocumentId: String

    // A global variable for Assigned Members List.
    lateinit var mAssignedMembersDetailList: ArrayList<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)


        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        // Call the function to get the Board Details
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
    }



    // A function to setup action bar
    private fun setupActionBar() {
        val toolbar_task_list_activity = findViewById<Toolbar>(R.id.toolbar_task_list_activity)
        setSupportActionBar(toolbar_task_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name //We get the title from the our board
        }

        toolbar_task_list_activity.setNavigationOnClickListener { onBackPressed() }
    }


    // A function to get the result of Board Detail
    fun boardDetails(board: Board) {
        val rv_task_list = findViewById<RecyclerView>(R.id.rv_task_list)

        // Initialize and Assign the value to the global variable for Board Details
        mBoardDetails = board

        hideProgressDialog()

        // Call the function to setup action bar.
        setupActionBar()
        //setupActionBar(board.name) //Pass in the title as a parameter which will be the name of the board

        /*
        //Code removed for video no. 278, We want to show the assigned members for each card on the card itself, instead of its more details
        // We pasted it in the boardMembersDetailList() func below

        // Setup the task list view using the adapter class and task list of the board
        // Here we are appending an item view for adding a list task list for the board.
        //Create a task
        val addTaskList = Task(resources.getString(R.string.add_list))
        //Add it to the boards ArrayList
        board.taskList.add(addTaskList)

        //We set the RecyclerViews ll as this activity
        rv_task_list.layoutManager =
                LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        //It should have a fixed size
        rv_task_list.setHasFixedSize(true)


        // Create an instance of TaskListItemsAdapter and pass the task list to it.
        val adapter = TaskListItemAdapter(this@TaskListActivity, board.taskList)
        rv_task_list.adapter = adapter // Attach the adapter to the recyclerView.

         */


        // Get all the members detail list which are assigned to the board
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(
            this@TaskListActivity,
            mBoardDetails.assignedTo
        )
    }


    /**
     * A function to get the result of add or updating the task list.
     */
    fun addUpdateTaskListSuccess() {
        //We hide the dialog that was successful to load the activity
        hideProgressDialog()

        // Show the progress dialog while we're trying to get something from the firestore database
        showProgressDialog(resources.getString(R.string.please_wait))
        // Here get the updated board details.
        FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDetails.documentId)
    }

    // A function to get the task list name from the adapter class which we will be using to create a new task list in the database
    fun createTaskList(taskListName: String) {

        Log.e("Task List Name", taskListName)

        // Create and Assign the task details
        val task = Task(taskListName, FirestoreClass().getCurrentUserID())

        mBoardDetails.taskList.add(0, task) // Add task to the first position of the boards ArrayList
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1) // Remove the last position as we have added the item manually for adding the TaskList.

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        //Call the addUpdateTaskList() func in the FirestoreClass, to update the database
        //We pass in the new board
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }


    /**
     * A function to update the taskList
     */
    // E.g., when the name of the TaskList is changed, we can call this func, to show the updated version
    fun updateTaskList(position: Int, listName: String, model: Task) {
        //Create an instance of the task using the updated parameters
        val task = Task(listName, model.createdBy)
        // Add updated task to the first position of the boards ArrayList
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1) //Remove the old one

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        //Call the addUpdateTaskList() func in the FirestoreClass, to update the database
        //We pass in the new board
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    /**
     * A function to delete the task list from database.
     */
    fun deleteTaskList(position: Int){
        //Remove the list at the position
        mBoardDetails.taskList.removeAt(position)

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        //Call the addUpdateTaskList() func in the FirestoreClass, to update the database
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }


    /**
     * A function to create a card and update it in the task list.
     */
    fun addCardToTaskList(position: Int, cardName: String) {

        // Remove the last item
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        //A new list to store the new cards
        val cardAssignedUsersList: ArrayList<String> = ArrayList()

        //We get the UsrID from the firestore class, and add it to the list
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserID())

        //Create a new instance of the card
        val card = Card(cardName, FirestoreClass().getCurrentUserID(), cardAssignedUsersList)

        //Create a list at the position we are currently at (The current task)
        val cardsList = mBoardDetails.taskList[position].cards
        //Add the card to it
        cardsList.add(card)

        //Create a task
        val task = Task(
                mBoardDetails.taskList[position].title,
                mBoardDetails.taskList[position].createdBy,
                cardsList
        )

        //Add the task to the current position of the board
        mBoardDetails.taskList[position] = task

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        //Update the task list in the database
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }


    // Inflate the members action menu for TaskListScreen
    //Creating the mAdd members menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //launch the MembersActivity Screen on item selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_members -> {
                //When the actionMembers item is clicked from the menu, go to the MembersActivity
                val intent = Intent(this@TaskListActivity, MembersActivity::class.java)
                // Pass the board details through intent
                //We get the name from constants
                //Our board is parcelable so we wan can pass it through the activities
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                // Start activity for result, passing in the MEMBERS_REQUEST_CODE
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //  Add the onActivityResult function add based on the requested document get the updated board details
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && (requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE)
        ) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            //Reload the board details, so once the user has added another member, if they have made any changes to the board
            //Once you pressed back, it will reload the page
            FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    /**
     * A function for viewing and updating card details.
     */
    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this@TaskListActivity, CardDetailsActivity2::class.java)
        //Send all the required details to CardDetailsActivity through intent
        //We pass in the name (Stored in constants) and then its value
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        //Pass the Assigned members board details list to the card detail screen
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)

        //We pass in the CARD_DETAILS_REQUEST_CODE so when the user is editing the card details
        //After they have done that, when they go back they get the updated UI
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)

        //startActivity(Intent(this@TaskListActivity, CardDetailsActivity2::class.java))
    }


    /**
     * A function to get assigned members detail list.
     */
    fun boardMembersDetailList(list: ArrayList<User>) {
        val rv_task_list = findViewById<RecyclerView>(R.id.rv_task_list)


        //Gets the board members list and assigns it to our private variable
        mAssignedMembersDetailList = list

        hideProgressDialog()

        //we pasted this code here (from boardDetails(0 func from above) for video no. 278
        // We want to show the assigned members for each card on the card itself, as well as its more details
        // Here we are appending an item view for adding a list task list for the board.

        // Setup the task list view using the adapter class and task list of the board
        // Here we are appending an item view for adding a list task list for the board.
        //Create a task
        val addTaskList = Task(resources.getString(R.string.add_list))
        //Add it to the boards ArrayList
        mBoardDetails.taskList.add(addTaskList)

        //We set the RecyclerViews ll as this activity
        rv_task_list.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        //It should have a fixed size
        rv_task_list.setHasFixedSize(true)

        // Create an instance of TaskListItemsAdapter and pass the task list to it.
        val adapter = TaskListItemAdapter(this@TaskListActivity, mBoardDetails.taskList)
        rv_task_list.adapter = adapter // Attach the adapter to the recyclerView.

    }


    /**
     * A function to update the card list in the particular task list.
     */
    //For the cards drag and drop feature, used in TaskListItemAdapter
    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {

        // Remove the last item (the "Add Cards" TV)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        //We set the cards in the list to be the cards passed in as a parameter
        mBoardDetails.taskList[taskListPosition].cards = cards

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        //Update the database
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }


    /**
     * A companion object to declare the constants.
     */
    companion object {
        //A unique code for starting the activity for result
        const val MEMBERS_REQUEST_CODE: Int = 13

        //  Add a unique request code for starting the activity for result
        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }
}