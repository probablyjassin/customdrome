package com.jassin.customdrome.playback

import android.util.Log
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private companion object {
        const val TAG = "PlaybackService"
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        super.onCreate()
        PlaybackEngine.initialize(this)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d(TAG, "onGetSession() from controller=${controllerInfo.packageName}")
        return PlaybackEngine.currentSession().also {
            Log.d(TAG, "onGetSession() returning session=${it != null}")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        PlaybackEngine.release()
        super.onDestroy()
    }
}


