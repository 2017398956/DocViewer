package com.cherry.doc

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherry.lib.doc.util.Constant
import com.cherry.lib.doc.widget.PoiViewer

class WordActivity : AppCompatActivity() {
    companion object {
        fun launchDocViewer(activity: AppCompatActivity, path: String?) {
            val intent = Intent(activity, WordActivity::class.java)
            intent.putExtra(Constant.INTENT_DATA_KEY, path)
            activity.startActivity(intent)
        }
    }

    private lateinit var mFlDocContainer: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word)
        mFlDocContainer = findViewById(R.id.mFlDocContainer)
        val path = intent?.getStringExtra(Constant.INTENT_DATA_KEY)
        word2Html(path ?: "")
    }

    fun word2Html(sourceFilePath: String) {
        val mPoiViewer = PoiViewer(this)
        try {
            mPoiViewer.loadFile(mFlDocContainer, sourceFilePath)
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show()
        }
    }
}