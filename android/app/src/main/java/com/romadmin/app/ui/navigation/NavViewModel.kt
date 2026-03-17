package com.romadmin.app.ui.navigation

import androidx.lifecycle.ViewModel
import com.romadmin.app.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    val prefs: AppPreferences,
) : ViewModel()
