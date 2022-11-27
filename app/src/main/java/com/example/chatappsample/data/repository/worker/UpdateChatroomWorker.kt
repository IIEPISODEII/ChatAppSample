package com.example.chatappsample.data.repository.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import javax.inject.Inject

class UpdateChatroomWorker @Inject constructor(
    context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return if (mWork == null) {
            Result.failure()
        } else {
            mWork!!.doWork()
            Result.success()
        }
    }

    interface Work {
        fun doWork()
    }

    companion object {
        private var mWork: Work? = null

        fun setWork(yWork: Work) {
            this.mWork = yWork
        }
    }
}