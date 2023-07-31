package com.example.bluefilament

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.bluefilament.ui.CustomViewer

class MainViewModel: ViewModel() {

    val imageList = makeResource()

    private fun makeResource() = arrayListOf(
        CustomViewer.AnimationInfo(0, yoda),
        CustomViewer.AnimationInfo(1, knight),
        CustomViewer.AnimationInfo(2, robot),
    )

    val animationList = mutableStateListOf<CustomViewer.AnimationInfo>() // Main (central) list

    fun changeAnimationList(list: List<CustomViewer.AnimationInfo>) {
        animationList.clear()
        animationList.addAll(list)
    }

}