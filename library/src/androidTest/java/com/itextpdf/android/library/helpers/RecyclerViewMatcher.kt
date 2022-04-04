package com.itextpdf.android.library.helpers

import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
    return RecyclerViewMatcher(recyclerViewId)
}

class RecyclerViewMatcher(val mRecyclerViewId: Int) {

    fun atPosition(position: Int, targetViewId: Int): Matcher<View> {
        return atPositionOnView(position, targetViewId)
    }

    private fun atPositionOnView(position: Int, targetViewId: Int = -1): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            var resources: Resources? = null
            var childView: View? = null
            override fun describeTo(description: Description) {
                val id = if (targetViewId == -1) mRecyclerViewId else targetViewId
                var idDescription = id.toString()
                if (resources != null) {
                    idDescription = try {
                        resources!!.getResourceName(id)
                    } catch (var4: NotFoundException) {
                        String.format("%s (resource name not found)", id)
                    }
                }
                description.appendText("with id: $idDescription")
            }

            override fun matchesSafely(view: View): Boolean {
                resources = view.resources
                if (childView == null) {
                    val recyclerView = view.rootView.findViewById<View>(mRecyclerViewId) as RecyclerView?
                    childView = if (recyclerView != null) {
                        recyclerView.findViewHolderForAdapterPosition(position)!!.itemView
                    } else {
                        return false
                    }
                }
                return if (targetViewId == -1) {
                    view === childView
                } else {
                    val targetView = childView!!.findViewById<View>(targetViewId)
                    view === targetView
                }
            }
        }
    }
}