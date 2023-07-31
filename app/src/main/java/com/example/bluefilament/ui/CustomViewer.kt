package com.example.bluefilament.ui

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.Log
import android.view.Choreographer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceView
import com.google.android.filament.ColorGrading
import com.google.android.filament.Colors
import com.google.android.filament.EntityManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Skybox
import com.google.android.filament.SwapChain
import com.google.android.filament.ToneMapper
import com.google.android.filament.View
import com.google.android.filament.utils.KTX1Loader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer

class CustomViewer {
    companion object {
        init {
            Utils.init()
        }
    }

    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer

    private var isAnimationSelected = false
    private var animationIndex = 0

    fun loadEntity() {
        choreographer = Choreographer.getInstance()
    }

    fun setSurfaceView(mSurfaceView: SurfaceView) {
        modelViewer = ModelViewer(mSurfaceView)
        mSurfaceView.setOnTouchListener(modelViewer)

        modelViewer.view.colorGrading = ColorGrading.Builder()
            .toneMapper(ToneMapper.Linear()).build(modelViewer.engine)

        modelViewer.view.renderQuality.apply {
            hdrColorBuffer = View.QualityLevel.LOW
        }
        // dynamic resolution often helps a lot
        modelViewer.view.dynamicResolutionOptions.apply {
            enabled = false
            quality = View.QualityLevel.LOW
        }

        // MSAA is needed with dynamic resolution MEDIUM
        modelViewer.view.multiSampleAntiAliasingOptions.apply {
            enabled = false
        }

        // FXAA is pretty cheap and helps a lot
        modelViewer.view.antiAliasing = View.AntiAliasing.FXAA

        // ambient occlusion is the cheapest effect that adds a lot of quality
        modelViewer.view.ambientOcclusionOptions.apply {
            enabled = false
        }

        // bloom is pretty expensive but adds a fair amount of realism
        modelViewer.view.bloomOptions.apply { enabled = false }

        //Skybox and background color
        //without this part the scene'll appear broken
        //modelViewer.engine.createSwapChain(modelViewer.view, SwapChain.CONFIG_TRANSPARENT)
        modelViewer.scene.skybox = Skybox.Builder().build(modelViewer.engine)
        modelViewer.scene.skybox?.setColor(0.663f, 0.302f, 0.749f, 0.149f) //White color

        makeTransparentBackground(mSurfaceView)
    }

    fun resetView() {
        //modelViewer.animator?.resetBoneMatrices()
        modelViewer.clearRootTransform()
    }

    data class AnimationInfo(
        val index: Int,
        val name: String
    )

    fun getAnimatorList(): ArrayList<AnimationInfo> {

        val list = ArrayList<AnimationInfo>()

        modelViewer.animator?.apply {
           for(i in 0 until this.animationCount) {
               Log.d("CustomViewer", "i = ${getAnimationName(i)}")
               list.add(AnimationInfo(i, getAnimationName(i)))
           }
        }

        return list
    }

    fun setAnimationInfo(info: AnimationInfo) {
        isAnimationSelected = true
        animationIndex = info.index
    }

    fun startAnimation(time: Float) {
        modelViewer.animator?.apply {
            Log.d("CustomViewer", "time = ${time}, animationIndex = $animationIndex}")
            applyAnimation(animationIndex, time)
            updateBoneMatrices()
        }
    }

    fun loadGlb(context: Context, name: String) {
        val buffer = readAsset(context, "threed/models/${name}.glb")
        modelViewer.apply {
            loadModelGlb(buffer)
            transformToUnitCube()
        }
    }

    fun loadGlb(context: Context, dirName: String, name: String) {
        val buffer = readAsset(context, "threed/models/${dirName}/${name}.glb")
        modelViewer.apply {
            loadModelGlb(buffer)
            transformToUnitCube()
        }
    }

    fun loadGltf(context: Context, name: String) {
        val buffer = context.assets.open("threed/models/${name}.gltf").use { input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            ByteBuffer.wrap(bytes)
        }
        modelViewer.apply {
            loadModelGltf(buffer) { uri -> readAsset(context, "threed/models/$uri") }
            transformToUnitCube()
        }
    }

    fun loadGltf(context: Context, dirName: String, name: String) {
        val buffer = context.assets.open("threed/models/${dirName}/${name}.gltf").use { input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            ByteBuffer.wrap(bytes)
        }
        modelViewer.apply {
            loadModelGltf(buffer) { uri -> readAsset(context, "threed/models/${dirName}/$uri") }
            transformToUnitCube()
        }
    }

    fun loadIndirectLight(context: Context, ibl: String) {
        // Create the indirect light source and add it to the scene.
        val buffer = readAsset(context, "threed/environments/venetian_crossroads_2k/${ibl}_ibl.ktx")
        KTX1Loader.createIndirectLight(modelViewer.engine, buffer).apply {
            intensity = 20_000f
            modelViewer.scene.indirectLight = this
        }
    }

    private fun makeTransparentBackground(surfaceView: SurfaceView) {
        surfaceView.setZOrderOnTop(true)
        surfaceView.setBackgroundColor(Color.TRANSPARENT)
        surfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)

        modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT
        modelViewer.scene.skybox = null

        val options = modelViewer.renderer.clearOptions
        options.clear = true
        modelViewer.renderer.clearOptions = options
    }

    fun indirectLight() {

        val light = EntityManager.get().create()
        val (r, g, b) = Colors.cct(5_500.0f)

        LightManager.Builder(LightManager.Type.POINT)
            .intensity(400_000.0f)
            .direction(-1.0f, -0.5f, -1.0f)
            .build(modelViewer.engine, light)

        modelViewer.scene.addEntity(light)

        //modelViewer.scene.indirectLight = IndirectLight(20_000)
    }

    fun loadEnviroment(context: Context, ibl: String) {
        // Create the sky box and add it to the scene.
        val buffer = readAsset(context, "threed/environments/venetian_crossroads_2k/${ibl}_skybox.ktx")
        KTX1Loader.createSkybox(modelViewer.engine, buffer).apply {
            modelViewer.scene.skybox = this
        }
    }

    private fun readAsset(context: Context, assetName: String): ByteBuffer {
        val input = context.assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        private val startTime = System.nanoTime()
        override fun doFrame(currentTime: Long) {
            val seconds = (currentTime - startTime).toDouble() / 1_000_000_000
            choreographer.postFrameCallback(this)
            modelViewer.animator?.apply {
                if (isAnimationSelected) {
                    startAnimation(seconds.toFloat())
                } else {
                    if (animationCount > 0) {
                        applyAnimation(0, seconds.toFloat())
                    }
                    updateBoneMatrices()
                }
            }
            modelViewer.render(currentTime)
        }
    }

    fun onResume() {
        if (this::choreographer.isInitialized) {
            choreographer.postFrameCallback(frameCallback)
        }
    }

    fun onPause() {
        if (this::choreographer.isInitialized) {
            choreographer.removeFrameCallback(frameCallback)
        }
    }

    fun onDestroy() {
        if (this::choreographer.isInitialized) {
            choreographer.removeFrameCallback(frameCallback)
        }
    }

}