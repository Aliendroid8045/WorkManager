package com.example.workmanager.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

class BlurImageWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        return try {

            //access stored birds.png file when the downloadImageWorker is finished
            //cacheDir is new specific to app and it will be access only by your app
            val cacheFile = File(applicationContext.cacheDir, "birds.png")
            val picture = BitmapFactory.decodeFile(cacheFile.path)
            val output = blurDownloadImage(picture, applicationContext)
            if (cacheFile.exists()) {
                //delete old birds.png so next time we can download new file
                cacheFile.delete()
            }
            //use extension function to write blur image at app cache folder as birdsblur.png
            File(applicationContext.cacheDir, "birdsblur.png").writeBitmap(
                output,
                Bitmap.CompressFormat.PNG,
                100
            )
            Result.success()
        } catch (throwable: Throwable) {
            Log.e(throwable.message, "Error applying blur")
            Result.failure()
        }
    }
}

private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

@WorkerThread
fun blurDownloadImage(bitmap: Bitmap, applicationContext: Context): Bitmap {
    lateinit var rsContext: RenderScript
    try {

        // Create the output bitmap
        val output = Bitmap.createBitmap(
            bitmap.width, bitmap.height, bitmap.config
        )

        // Blur the image
        rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
        val inAlloc = Allocation.createFromBitmap(rsContext, bitmap)
        val outAlloc = Allocation.createTyped(rsContext, inAlloc.type)
        val theIntrinsic = ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext))
        theIntrinsic.apply {
            setRadius(25f)
            theIntrinsic.setInput(inAlloc)
            theIntrinsic.forEach(outAlloc)
        }
        outAlloc.copyTo(output)

        return output
    } finally {
        rsContext.finish()
    }
}