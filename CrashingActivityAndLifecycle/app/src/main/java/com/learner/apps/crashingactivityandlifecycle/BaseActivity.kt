package com.learner.apps.crashingactivityandlifecycle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

abstract class BaseActivity : AppCompatActivity() {

    abstract val modeName: String

    private var newIntentCount = 0
    private lateinit var infoView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "$modeName onCreate  instance=${id()} task=$taskId")

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        infoView = TextView(this).apply { textSize = 18f }
        root.addView(infoView)

        listOf(
            "Standard" to StandardActivity::class.java,
            "SingleTop" to SingleTopActivity::class.java,
            "SingleTask" to SingleTaskActivity::class.java,
            "SingleInstance" to SingleInstanceActivity::class.java
        ).forEach { (label, cls) ->
            root.addView(Button(this).apply {
                text = "Launch $label"
                setOnClickListener { startActivity(Intent(this@BaseActivity, cls)) }
            })
        }
        setContentView(ScrollView(this).apply { addView(root) })
        render()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        newIntentCount++
        Log.d(TAG, "$modeName onNewIntent instance=${id()} task=$taskId (count=$newIntentCount)")
        render()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "$modeName onDestroy instance=${id()} task=$taskId")
    }

    private fun render() {
        infoView.text = """
            Mode: $modeName
            Task ID: $taskId
            Instance: ${id()}
            onNewIntent calls: $newIntentCount
        """.trimIndent()
    }

    private fun id() = Integer.toHexString(System.identityHashCode(this))

    companion object {
        const val TAG = "LaunchModes"
    }
}