package com.example.listify

import android.content.Context

const val PREFS_NAME = "listify_prefs"
const val KEY_FIRST_TIME_SWIPE = "first_time_swipe"

fun Context.isFirstTimeWithSwipe(): Boolean {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_FIRST_TIME_SWIPE, true)
}

fun Context.setFirstTimeWithSwipe(isFirstTime: Boolean) {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    with(prefs.edit()) {
        putBoolean(KEY_FIRST_TIME_SWIPE, isFirstTime)
        apply()
    }
}
