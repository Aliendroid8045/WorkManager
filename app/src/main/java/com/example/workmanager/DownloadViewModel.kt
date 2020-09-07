package com.example.workmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.workmanager.worker.BlurImageWorker
import com.example.workmanager.worker.DownloadImageWorker

class DownloadViewModel(application: Application) :
    AndroidViewModel(application) {
    val isDownloadImageFinished = MediatorLiveData<WorkInfo>()
    val isBlurringImageDone = MediatorLiveData<WorkInfo>()

    fun doImageDownload() {
        //First task to download image from server
        //This is onetime request
        val downloadImage =
            OneTimeWorkRequest.Builder(DownloadImageWorker::class.java)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag("imageDownload")
                .build()

        //Second task to blur the image which downloaded from internet
        val blurImage = OneTimeWorkRequest.Builder(BlurImageWorker::class.java)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresStorageNotLow(true)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag("blurImage")
            .build()

        WorkManager.getInstance(getApplication())
            .beginWith(downloadImage)//Start downloading image-> once it finished, go for blur work
            .then(blurImage) // this task will wait until image is downloaded from server
            .enqueue()
        //update the status of jobs using MediatorLieData.
        downloadImageStatus(downloadImage)
        blurWorkingStatus(blurImage)

    }


    private fun downloadImageStatus(downloadImage: OneTimeWorkRequest) {
        val downloadImageStatus = WorkManager.getInstance(getApplication())
            .getWorkInfoByIdLiveData(downloadImage.id)

        isDownloadImageFinished.addSource(downloadImageStatus) { workStatus ->
            isDownloadImageFinished.value = workStatus

            if (workStatus.state.isFinished) {
                isDownloadImageFinished.removeSource(downloadImageStatus)
            }
        }
    }

    private fun blurWorkingStatus(blurImage: OneTimeWorkRequest) {
        val blurImageStatus = WorkManager.getInstance(getApplication())
            .getWorkInfoByIdLiveData(blurImage.id)

        isBlurringImageDone.addSource(blurImageStatus) { blurWorkStatus ->
            isBlurringImageDone.value = blurWorkStatus

            if (blurWorkStatus.state.isFinished) {
                isBlurringImageDone.removeSource(blurImageStatus)
            }
        }
    }
}
