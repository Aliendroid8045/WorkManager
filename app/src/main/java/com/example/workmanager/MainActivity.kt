package com.example.workmanager

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    val viewModel: DownloadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        progress_circular.visibility = View.VISIBLE
        Toast.makeText(this, "We are downloading image using WorkManager", Toast.LENGTH_LONG).show()

        viewModel.doImageDownload()

        observeImageDownload()

        observeBlurImage()
    }

    private fun observeBlurImage() {
        viewModel.isBlurringImageDone.observe(this) { blurStatus ->
            if (blurStatus != null && blurStatus.state.isFinished) {
                getBitmapFromCacheAndDisplay(true)
                Toast.makeText(this, "blur applied", Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun observeImageDownload() {
        viewModel.isDownloadImageFinished.observe(this) { downloadStatus ->
            if (downloadStatus != null && downloadStatus.state.isFinished) {
                getBitmapFromCacheAndDisplay(false)
                Toast.makeText(this, "We are applying Blur to the downloaded image using WorkManager", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getBitmapFromCacheAndDisplay(isBlurred: Boolean) {
        val filename = if (isBlurred) "birdsblur.png" else "birds.png"
        val cacheFile = File(applicationContext.cacheDir, filename)
        val picture = BitmapFactory.decodeFile(cacheFile.path)
        progress_circular.visibility = View.GONE
        iv_download.visibility = View.VISIBLE
        iv_download.setImageBitmap(picture)
    }
}