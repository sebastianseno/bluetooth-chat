package com.example.myapplication.extension

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

fun NavBackStackEntry.lifecycleIsResumed() =
    this.getLifecycle().currentState == Lifecycle.State.RESUMED

fun NavController.navigateTo(
    toRoute: String,
    navBackStackEntry: NavBackStackEntry
) {
    if (navBackStackEntry.lifecycleIsResumed()) {
        navigate(toRoute)
    }
}