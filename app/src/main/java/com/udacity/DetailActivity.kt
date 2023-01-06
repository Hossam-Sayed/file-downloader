package com.udacity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        filename_field_text.text = intent.getStringExtra("fileName")
        val status = intent.getStringExtra("status")
        status_field_text.text = status

        if (status == "Fail") status_field_text.setTextColor(Color.RED)

        ok_button.setOnClickListener { onBackPressed() }
    }

}
