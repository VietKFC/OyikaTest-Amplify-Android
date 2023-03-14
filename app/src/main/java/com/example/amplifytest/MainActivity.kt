package com.example.amplifytest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.ViewPager
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.StorageItem
import com.amplifyframework.storage.options.StorageGetUrlOptions
import com.amplifyframework.storage.options.StorageListOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var job: Job? = null
    private var jobCountTime: Job? = null
    private var isSliding = true
    private var systemTimeStart = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        systemTimeStart = System.currentTimeMillis()
        jobCountTime = lifecycleScope.launch {
            while (true) {
                delay(1000)
                if (System.currentTimeMillis() - systemTimeStart >= 1000 * 60 * 2) {
                    downloadPhotos()
                    break
                }
            }
        }
        signInCognito()
        findViewById<ImageView>(R.id.download_btn).setOnClickListener {
            downloadPhotos()
        }
    }

    private fun signInCognito() {
        Amplify.Auth.fetchAuthSession({
            if (!it.isSignedIn) {
                Amplify.Auth.signIn(
                    BuildConfig.USER_NAME,
                    BuildConfig.PASSWORD,
                    {
                        Log.e(TAG, "onCreate: Signed in successfully!")
                    },
                    {
                        Log.e(TAG, "onCreate: " + it.toString())
                    }
                )
            }
        }, {})
    }

    private fun downloadPhotos() {
        findViewById<ImageView>(R.id.download_btn).isVisible = false
        Amplify.Storage.list(
            "",
            StorageListOptions.builder()
                .accessLevel(StorageAccessLevel.PROTECTED)
                .build(),
            { result ->
                getItemUrls(result.items.filter {
                    it.key.contains(".webp") ||
                            it.key.contains(".jpg") || it.key.contains(".png")
                })
            },
            {
                Log.e(TAG, "downloadPhotos: " + it.toString())
            }
        )
    }

    private fun getItemUrls(items: List<StorageItem>) {
        val imageList = mutableListOf<String>()
        val result = mutableListOf<String>()
        items.forEachIndexed { index, item ->
            Amplify.Storage.getUrl(item.key, StorageGetUrlOptions.builder()
                .accessLevel(StorageAccessLevel.PROTECTED)
                .build(), {
                imageList.add(it.url.toString())
                if (index == items.size - 1) {
                    runOnUiThread {
                        result.addAll(imageList)
                        val imagePagers = ImagePagerAdapter(imageList = result, context = this, onClick = {
                            if (isSliding) {
                                isSliding = false
                                job?.cancel()
                                Toast.makeText(this, "Stop sliding", Toast.LENGTH_SHORT).show()
                            } else {
                                isSliding = true
                                startJob()
                                Toast.makeText(this, "Start sliding", Toast.LENGTH_SHORT).show()
                            }
                        })
                        findViewById<ViewPager>(R.id.view_pager).adapter = imagePagers
                        imagePagers.notifyDataSetChanged()
                        startJob()
                        jobCountTime?.cancel()
                    }
                }
            }, {})
        }
    }

    private fun startJob() {
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        job = lifecycleScope.launch {
            while (isSliding) {
                delay(3000)
                if (viewPager.currentItem < viewPager.adapter?.count!! - 1) {
                    viewPager.currentItem += 1
                } else {
                    viewPager.currentItem = 0
                }
            }
        }
    }
}