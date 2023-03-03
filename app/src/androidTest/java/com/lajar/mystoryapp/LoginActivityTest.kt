package com.lajar.mystoryapp

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.lajar.mystoryapp.Helper.EspressoIdlingResource
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest{
    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp(){
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun login_and_logout(){
        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_login_password)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
        Intents.init()
        onView(withId(R.id.ed_login_email)).perform(typeText("fajariyandi36@gmail.com"))
        onView(withId(R.id.ed_login_password)).perform(typeText("fajar123")).perform(
            ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.btn_login)).perform(click())
        intended(hasComponent(ListActivity::class.java.name))
        onView(withId(R.id.logout)).perform(click())
    }
    @Test
    fun login_Failed(){
        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_login_password)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_login_email)).perform(typeText("fajariyandi36@gmail.com"))
        onView(withId(R.id.ed_login_password)).perform(typeText("wrongPassword")).perform(
            ViewActions.closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click())
        intended(hasComponent(LoginActivity::class.java.name))
    }

}