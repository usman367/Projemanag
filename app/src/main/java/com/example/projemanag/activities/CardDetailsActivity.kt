package com.example.projemanag.activities

import android.os.Bundle
import com.example.projemanag.R

//Use this instead of activity_card_details, I created the class for it instead of the activity, and then had to create an xml file for it, doesn't work
class CardDetailsActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
    }
}