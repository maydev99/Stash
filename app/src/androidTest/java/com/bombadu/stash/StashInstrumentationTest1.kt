package com.bombadu.stash


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class StashInstrumentationTest1 {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun stashInstrumentationTest1() {
        val je = onView(
            allOf(
                withText("Sign in"),
                childAtPosition(
                    allOf(
                        withId(R.id.gSignInButton),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            1
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        je.perform(click())

        val actionMenuItemView = onView(
            allOf(
                withId(R.id.add_link), withContentDescription("Add Link"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.action_bar),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView.perform(click())

        val actionMenuItemView2 = onView(
            allOf(
                withId(R.id.add_link), withContentDescription("Add Link"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.action_bar),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView2.perform(click())

        val actionMenuItemView3 = onView(
            allOf(
                withId(R.id.date_range), withContentDescription("Date Range"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.action_bar),
                        1
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        actionMenuItemView3.perform(click())

        val appCompatRadioButton = onView(
            allOf(
                withId(R.id.rbAll), withText("All"),
                childAtPosition(
                    allOf(
                        withId(R.id.radio_group),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            1
                        )
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        appCompatRadioButton.perform(click())

        val linearLayout = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.recycler_view),
                        childAtPosition(
                            withId(R.id.constraint),
                            2
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        linearLayout.perform(click())
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
