package com.lksnext.ParkingELadron.view.activity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lksnext.ParkingELadron.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.test.core.app.ActivityScenario;

@RunWith(AndroidJUnit4.class)
public class WelcomeActivityTest {
    @Rule
    public ActivityScenarioRule<WelcomeActivity> rule = new ActivityScenarioRule<>(WelcomeActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void openRegisterTest() {
        onView(withId(R.id.btnCreate)).perform(click());

        intended(hasComponent(RegisterActivity.class.getName()));
    }

    @Test
    public void openLoginTest() {
        onView(withId(R.id.btnEnter)).perform(click());

        intended(hasComponent(LoginActivity.class.getName()));
    }

    @Test
    public void languageChangeTest() {
        onView(withId(R.id.tvTitle)).check(matches(withText(R.string.welcome_title)));

        onView(withId(R.id.spinnerLanguage)).perform(click());

        onView(withText("English")).perform(click());

        ActivityScenario<WelcomeActivity> scenario = rule.getScenario();
        scenario.onActivity(activity -> {
            SharedPreferences prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE);
            String currentLang = prefs.getString("lang", "es");
            assertEquals("en", currentLang);

            // Verificar que los recursos están usando el idioma inglés
            Resources resources = activity.getResources();
            Configuration config = resources.getConfiguration();
            assertEquals("en", config.getLocales().get(0).getLanguage());
        });
    }
}
