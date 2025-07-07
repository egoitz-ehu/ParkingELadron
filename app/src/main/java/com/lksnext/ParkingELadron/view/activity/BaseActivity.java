// BaseActivity.java
package com.lksnext.ParkingELadron.view.activity;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE);
        String lang = prefs.getString("lang", "es");
        super.attachBaseContext(WelcomeActivity.setLocale(newBase, lang));
    }
}