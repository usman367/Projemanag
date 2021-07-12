package com.example.projemanag.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.adapters.CardMemberListItemsAdapter
import com.example.projemanag.dialogs.LabelColorListDialog
import com.example.projemanag.dialogs.MembersListDialog
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.*
import com.example.projemanag.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity2 : BaseActivity() {

    // A global variable for board details
    private lateinit var mBoardDetails: Board
    // A global variable for task item position
    private var mTaskListPosition: Int = -1
    // A global variable for card item position
    private var mCardPosition: Int = -1

    // A global variable for selected label color
    private var mSelectedColor: String = ""

    // A global variable for Assigned Members List.
    private lateinit var mMembersDetailList: ArrayList<User>

    // A global variable for selected due date
    private var mSelectedDueDateMilliSeconds: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details2)

        // Call the getIntentData function here
        getIntentData()

        // Call the setup action bar function here
        setupActionBar()


        // Set the card name in the EditText for editing
        val et_name_card_details = findViewById<EditText>(R.id.et_name_card_details)
        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        // The cursor after the string length
        //When the card is clicked, it sets the cursor to the end of the string
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)


        // A click event for update button and also call the function to update the card details
        val btn_update_card_details = findViewById<Button>(R.id.btn_update_card_details)
        btn_update_card_details.setOnClickListener {
            if(et_name_card_details.text.toString().isNotEmpty()) {
                //If the name id not empty then call the function to update the card details
                updateCardDetails()
            }else{
                Toast.makeText(this@CardDetailsActivity2, "Enter card name.", Toast.LENGTH_SHORT).show()
            }
        }

        //This sets the color of the card that is shown when you click on the card
        // Add a click event for selecting a label color and launch the dialog
        val tv_select_label_color = findViewById<TextView>(R.id.tv_select_label_color)
        tv_select_label_color.setOnClickListener {
            labelColorsListDialog()
        }


        //Get the already selected label color and set it to the TextView background.
        //Get the selected color
        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        //If its not empty (if the user has chosen a color)
        if (mSelectedColor.isNotEmpty()) {
            //Then call this func to set the cards selected color
            setColor()
        }


        // Add the click event to launch the members list dialog
        val tv_select_members = findViewById<TextView>(R.id.tv_select_members)
        tv_select_members.setOnClickListener {
            //Call this func from below
            membersListDialog()
        }

        setupSelectedMembersList()



        // Add click event for selecting the due date
        val tv_select_due_date = findViewById<TextView>(R.id.tv_select_due_date)
        tv_select_due_date.setOnClickListener {
            //Call the date picker dialog function
            showDataPicker()
        }

        // Set the due to if it is already selected before
        //Get the cards due date
        mSelectedDueDateMilliSeconds = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate
        //If there is a due date
        if (mSelectedDueDateMilliSeconds > 0) {
            //get the right format
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            //Set the format to the date
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            //Show it in the TV
            tv_select_due_date.text = selectedDate
        }


    }


    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {
        val toolbar_card_details_activity = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_card_details_activity)
        setSupportActionBar(toolbar_card_details_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            // Set the title of action bar
            //Which will be the boards task at the current position, its cards at the current position's name
            actionBar.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        }

        toolbar_card_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    // A function to get all the data that is sent through intent of TaskListActivity
    private fun getIntentData() {

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            //It was giving an error so I added <Board>
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL) as Board
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            //We added a default value of -1
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        //Get the members detail list here through intent
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }


    // Inflate the menu file here
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        //Adding the menu_delete_car
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            //When the delete icon is pressed
            R.id.action_delete_card -> {
                //Call the function for showing an alert dialog for deleting the card
                //We pass in the name of the card
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * A function to get the result of add or updating the task list.
     */
    fun addUpdateTaskListSuccess() {

        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

    /**
     * A function to update card details.
     */
    private fun updateCardDetails() {
        val et_name_card_details = findViewById<EditText>(R.id.et_name_card_details)

        // Here we have updated the card name using the data model class.
        //We pass in its name, createdBy, assignedTo, and the color of the card
        val card = Card(
            et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        //Update the TaskList
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        //Removes the "Add Card" TV (We still add it somewhere else so the user will still see it)
        taskList.removeAt(taskList.size - 1)

        // Here we have assigned the update card details to the task list using the card position.
        //We override the card at the current position with the updated card we hace created above
        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        //Update the data in the database
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity2, mBoardDetails)
    }


    /**
     * A function to delete the card from the task list.
     */
    //We cant specifically just remove a card, we will have to edit the whole TaskList
    //Func is called in the alertDialogForDeleteCard() func below
    private fun deleteCard() {

        // Here we have got the cards list from the task item list using the task list position.
        //(cards is the name of the list)
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        // Here we will remove the item from cards list using the card position.
        //We remove the card at the current position
        cardsList.removeAt(mCardPosition)

        //Creating an ArrayList of TaskList
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        //Removing the  "Add Card" TextView from the card)
        taskList.removeAt(taskList.size - 1)

        //Setting the ths list in Task to the current updated list we made
        taskList[mTaskListPosition].cards = cardsList

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        //Updating the list in the database
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity2, mBoardDetails)
    }


    // A function to show an alert dialog for the confirmation to delete the card
    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.alert))
        //set message for alert dialog
        //We pass in the cardName so the user knows what card they're about to delete
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            //When the user presses "Yes" call the function to delete the card
            deleteCard()
        }

        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()

        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }


    /**
     * A function to add some static label colors in the list.
     */
    private fun colorsList(): ArrayList<String> {

        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    // A function to remove the text and set the label color to the TextView
    private fun setColor() {
        val tv_select_label_color = findViewById<TextView>(R.id.tv_select_label_color)

        //Set the TV that says "Select Color" to the name of the color
        tv_select_label_color.text = ""
        //Set it's background color tot the color
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    // A function to launch the label color list dialog
    private fun labelColorsListDialog() {
        //Creating an ArrayList of colors, which will be the colorsList() func
        val colorsList: ArrayList<String> = colorsList()

        //Creating an instance of the dialog
        //Pass the selected color to show it as already selected with tick icon in the list.
        val listDialog = object : LabelColorListDialog(
            this@CardDetailsActivity2,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor
        ) {

            //We override this func from LabelListColorDialog
            override fun onItemSelected(color: String) {
                //We get the color
                //We set the color to be the selected color
                mSelectedColor = color
                //Call this func from above which will set the color as the new background color of the label
                setColor()
            }
        }
        listDialog.show()
    }


    /**
     * A function to launch and setup assigned members detail list into recyclerview.
     */
    private fun membersListDialog() {

        // Here we get the updated assigned members list
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        //If there are any members assigned to the card
        if (cardAssignedMembersList.size > 0) {
            // Here we got the details of assigned members list from the global members list which is passed from the Task List screen.
            //Go through the members in the list
            for (i in mMembersDetailList.indices) {
                //For every single member in the list check its ID
                for (j in cardAssignedMembersList) {
                    //If its the same as the member that's assigned to the card
                    if (mMembersDetailList[i].id == j) {
                        //Set its selected to true
                        mMembersDetailList[i].selected = true
                    }
                }
            }

        } else {
            //For every member in th eindices, set its selected to false
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }

        //Create an object of MembersListDialog
        //We want it to display in this activity, using its list, and a strings from strings for its title
        val listDialog = object : MembersListDialog(
            this@CardDetailsActivity2,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ) {
            //Override its onItemSelected() func
            //For when the user clicks on a member on the membersListDialog()
            override fun onItemSelected(user: User, action: String) {

                // Here based on the action in the members list dialog update the list
                if (action == Constants.SELECT) {
                    //If the user ID passed to us from the parameter is NOT in the assigned to
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(
                            user.id)
                    ) {
                        //Then add that user yo assignedTo
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(
                            user.id
                        )
                    }

                }
                else {
                    //Otherwise remove it from assignedTo
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(
                        user.id
                    )
                    //Go through the members
                    for (i in mMembersDetailList.indices) {
                        //Find the user that's just been removed
                        if (mMembersDetailList[i].id == user.id) {
                            //Set their selected to false
                            mMembersDetailList[i].selected = false
                        }
                    }
                }

                //Recall this method to update the selected members list
                setupSelectedMembersList()

            }
        }

        listDialog.show()

    }


    /**
     * A function to setup the recyclerView for card assigned members.
     */
    private fun setupSelectedMembersList() {
        val tv_select_members = findViewById<TextView>(R.id.tv_select_members)
        val rv_selected_members_list = findViewById<RecyclerView>(R.id.rv_selected_members_list)

        // Assigned members of the Card.
        //Get the cards assignedTo members
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        // A instance of selected members list.
        //A new ArrayList of SelectedMembers
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        // Here we got the detail list of members and add it to the selected members list as required.
        //Go through the members in the list
        for (i in mMembersDetailList.indices) {
            //For every single member in the list check its ID
            for (j in cardAssignedMembersList) {
                //If its the same as the member that's assigned to the card
                if (mMembersDetailList[i].id == j) {
                    //Create an instance of the SelectedMembers data class
                    val selectedMember = SelectedMembers(
                        //Set its id and image to SelectedMembers
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )

                    //Add the SelectedMember to this list
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            //If there are any members in the list

            // This is for the last item to show.
            //It will show the plus icon TV
            selectedMembersList.add(SelectedMembers("", ""))

            //Hide the "plus" icon and show the RecyclerView
            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility = View.VISIBLE

            //Give the RV a GridLayoutManager
            //SpanCount: 6, We want to be able to display 6 members next to each other
            rv_selected_members_list.layoutManager = GridLayoutManager(this@CardDetailsActivity2, 6)
            //Create an instance of the adapter
            val adapter = CardMemberListItemsAdapter(this@CardDetailsActivity2, selectedMembersList, true)
            //Assign it to the RV
            rv_selected_members_list.adapter = adapter

            //Create an instance of the CardMemberListItemsAdapter
            adapter.setOnClickListener(object :
                CardMemberListItemsAdapter.OnClickListener {
                //Override its onClick() func to setup the recyclerView for card assigned members.
                override fun onClick() {
                    //Call our membersListDialog() func from above
                    membersListDialog()
                }

            })

        } else {
            //Otherwise, show the "plus" TV and hide the RV
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility = View.GONE
        }

    }


    /**
     * The function to show the DatePicker Dialog and select the due date.
     */
    private fun showDataPicker() {
        /**
         * This Gets a calendar using the default time zone and locale.
         * The calender returned is based on the current time
         * in the default time zone with the default.
         */
        //Calendar instance
        val c = Calendar.getInstance()
        //Get the year, month and day from it
        val year =
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day

        /**
         * Creates a new date picker dialog for the specified date using the parent
         * context's default date picker dialog theme.
         */
        //Created a date picker dialog
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                /*
                  The listener used to indicate the user has finished selecting a date.

                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.*/

                // Here we have appended 0 if the selected day is smaller than 10 to make it double digit value.
                //If the dayOfMonth is less than 10, then append 0 at the start i.e.,  7 -> 07, to make it double figures
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                //If the monthOfYear is less than 10, then append 0 at the start i.e.,  7 -> 07, to make it double figures
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                //Create a variable of selected date which consists of the variables from above
                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"

                // Selected date it set to the TextView to make it visible to user.
                val tv_select_due_date = findViewById<TextView>(R.id.tv_select_due_date)
                tv_select_due_date.text = selectedDate


                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                // The formatter will parse the selected date in to Date object
                // so we can simply get date in to milliseconds.
                //Put the selectedDate variable on the format from above
                val theDate = sdf.parse(selectedDate)


                /** Here we have get the time in milliSeconds from Date object
                 */
                //Set the date to our variable we created at the top
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )

        dpd.show() // It is used to show the datePicker Dialog.
    }

}