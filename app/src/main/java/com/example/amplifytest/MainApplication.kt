package com.example.amplifytest

import android.app.Application
import android.util.Log
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin

class MainApplication : Application() {
    private val TAG = "MainApplication"
    override fun onCreate() {
        super.onCreate()
        try {
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
        } catch (e: Exception) {
        }
    }
}