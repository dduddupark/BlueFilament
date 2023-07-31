package com.example.bluefilament

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bluefilament.ui.CustomViewer
import com.example.bluefilament.ui.theme.BlueFilamentTheme
import com.example.bluefilament.ui.theme.Pink80
import com.example.bluefilament.ui.theme.Purple80

const val yoda = "yoda"
const val knight = "knight"
const val robot = "robot"

class MainActivity : ComponentActivity() {

    val customViewer = CustomViewer()
    var mainViewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BlueFilamentTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(
                        customViewer,
                        mainViewModel
                    )
                }
            }
        }
        Log.d("MainActivity", "onCreate")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
        customViewer.onResume()
    }

    override fun onPause() {
        super.onPause()
        customViewer.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        customViewer.onDestroy()
    }
}

@Preview
@Composable
fun previewTest() {
    BlueFilamentTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
                AndroidView(
                    factory = { context ->
                    SurfaceView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            800,
                            800
                        )
                        Log.d("MainActivity", "initCustomViewer")
                        setBackgroundColor(Color.GREEN)
                    }
                })

                Text(text = "sksk")
            }
        }
    }
}

@Composable
fun Greeting(
    customViewer: CustomViewer,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        AndroidView(factory = { context ->
            SurfaceView(context).apply {
                Log.d("MainActivity", "initCustomViewer")
                layoutParams = ViewGroup.LayoutParams(
                    800,
                    800
                )
                initCustomViewer(context, customViewer, this)
                setBackgroundColor(Color.GREEN)
            }
        })
        RecyclerViewImageContent(viewModel) { info ->
            changeAnimation(context, customViewer, info)
            viewModel.changeAnimationList(customViewer.getAnimatorList())
        }
        RecyclerViewAnimationContent(viewModel) { info ->
            customViewer.setAnimationInfo(info)
        }
    }
}

private fun changeAnimation(
    context: Context,
    customViewer: CustomViewer,
    info: CustomViewer.AnimationInfo
) {
    customViewer.resetView()

    if (info.name == yoda) {
        customViewer.loadGlb(context, "grogu", "grogu")
    } else if (info.name == knight) {
        customViewer.loadGltf(context, "warcraft", "scene")
    } else if (info.name == robot) {
        customViewer.loadGlb(context, "RobotExpressive")
    }
}

fun initCustomViewer(context: Context, customViewer: CustomViewer, surfaceView: SurfaceView) {
    customViewer.run {
        loadEntity()
        setSurfaceView(surfaceView)

        //directory and model each as param
        //요다
        loadGlb(context, "grogu", "grogu")

        //기사
        //loadGltf(requireActivity(), "warcraft","scene")

        //로봇
        //loadGlb(requireActivity(), "RobotExpressive")

        //Enviroments and Lightning (OPTIONAL)
        //loadIndirectLight(requireActivity(), "venetian_crossroads_2k")
        // indirectLight()

        //background
        //loadEnviroment(requireActivity(), "venetian_crossroads_2k")
        //loadIndirectLight(requireActivity(), "pillars_2k")
        loadIndirectLight(context, "default_env")

        onResume()
    }
}

@Composable
fun RecyclerViewImageContent(
    viewModel: MainViewModel,
    itemClickedCallback: (info: CustomViewer.AnimationInfo) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp, 8.dp)) {
        items(
            items = viewModel.imageList,
            itemContent = { ThreeDItem(Pink80, it, itemClickedCallback) }
        )
    }
}

@Composable
fun RecyclerViewAnimationContent(
    viewModel: MainViewModel,
    itemClickedCallback: (info: CustomViewer.AnimationInfo) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp, 8.dp)) {
        items(
            items = viewModel.animationList,
            itemContent = { ThreeDItem(Purple80, it, itemClickedCallback) }
        )
    }
}

@Composable
fun ThreeDItem(
    color: androidx.compose.ui.graphics.Color,
    item: CustomViewer.AnimationInfo,
    itemClickedCallback: (info: CustomViewer.AnimationInfo) -> Unit
) {
    Column{
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = color),
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .wrapContentHeight()
            ,
            onClick = {
            itemClickedCallback(item)
        }) {
            Text(text = item.name)
        }
    }
}