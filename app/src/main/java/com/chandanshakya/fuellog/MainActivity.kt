package com.chandanshakya.fuellog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chandanshakya.fuellog.ui.fragments.VehiclesFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, VehiclesFragment())
                .commit()
        }
    }
}
