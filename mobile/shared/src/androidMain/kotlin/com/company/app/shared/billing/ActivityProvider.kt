package com.company.app.shared.billing

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityProvider {
    private var ref: WeakReference<Activity>? = null

    fun set(activity: Activity) {
        ref = WeakReference(activity)
    }

    fun get(): Activity? = ref?.get()
}
