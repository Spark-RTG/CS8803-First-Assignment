package com.example.todoperfect

import android.util.Log

object LogUtil {
    private const val VERBOSE = 1
    private const val DEBUG = 2
    private const val INFO = 3
    private const val WARN = 4
    private const val ERROR = 4
    private const val NO_LOG = 5
    private const val TAG = "TodoperfectLogUtil"
    private const val LEVEL = NO_LOG

    fun v(msg: String) {
        if (LEVEL <= VERBOSE) {
            Log.v(TAG, "LogUtil: $msg")
        }
    }

    fun d(msg: String) {
        if (LEVEL <= DEBUG) {
            Log.d(TAG, "LogUtil: $msg")
        }
    }

    fun i(msg: String) {
        if (LEVEL <= INFO) {
            Log.i(TAG, "LogUtil: $msg")
        }
    }

    fun w(msg: String) {
        if (LEVEL <= WARN) {
            Log.w(TAG, "LogUtil: $msg")
        }
    }

    fun e(msg: String) {
        if (LEVEL <= ERROR) {
            Log.e(TAG, "LogUtil: $msg")
        }
    }

}