package com.cherry.doc

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.cherry.doc.util.BasicSet
import com.cherry.doc.util.DocUtil
import com.cherry.doc.util.WordUtils
import com.cherry.lib.doc.DocViewerActivity
import com.cherry.lib.doc.bean.DocEngine
import com.cherry.lib.doc.bean.DocSourceType
import com.cherry.lib.doc.bean.FileType
import com.cherry.lib.doc.util.FileUtils
import com.cherry.permissions.lib.EasyPermissions
import com.cherry.permissions.lib.EasyPermissions.hasPermissions
import com.cherry.permissions.lib.annotations.AfterPermissionGranted
import com.cherry.permissions.lib.dialogs.SettingsDialog
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.listener.DownloadListener2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity(), OnItemClickListener, EasyPermissions.PermissionCallbacks {

    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION = 124
        const val REQUEST_CODE_STORAGE_PERMISSION11 = 125
        const val REQUEST_CODE_SELECT_DOCUMENT = 0x100
        const val TAG = "MainActivity"
    }

    var url =
        "http://cdn07.foxitsoftware.cn/pub/foxit/manual/phantom/en_us/API%20Reference%20for%20Application%20Communication.pdf"
//    var url = "https://xdts.xdocin.com/demo/resume3.docx"
//    var url = "http://172.16.28.95:8080/data/test2.ppt"
//    var url = "http://172.16.28.95:8080/data/testdocx.ll"

    private var mDocAdapter: DocAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
    }

    private fun hasRwPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val isExternalStorageManager = Environment.isExternalStorageManager()
            return isExternalStorageManager
        }
        val read = hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val write = hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        return read && write
    }

    @AfterPermissionGranted(REQUEST_CODE_STORAGE_PERMISSION)
    private fun requestStoragePermission() {
        if (hasRwPermission()) {
            // Have permission, do things!
            lifecycleScope.launch(Dispatchers.IO) {
                val datas = DocUtil.getDocFile(this@MainActivity)
                withContext(Dispatchers.Main) {
                    mDocAdapter?.showDatas(datas)
                }
            }
        } else {
            // Ask for one permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                get11Permission()
                return
            }
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to your storage to load local doc",
                REQUEST_CODE_STORAGE_PERMISSION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun get11Permission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse(java.lang.String.format("package:%s", packageName))
            startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION11)
        } catch (e: Exception) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION11)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_assets -> {
                openDoc("test.docx", DocSourceType.ASSETS)
                return true
            }

            R.id.action_online -> {
                val downloadFile = getDir("doc_temp", MODE_PRIVATE)
                val fileName = "test_pptx"
                DownloadTask.Builder(url, downloadFile)
                    .setFilename(fileName)
                    .build().enqueue(object : DownloadListener2() {
                        override fun taskStart(task: DownloadTask) {
                        }

                        override fun taskEnd(
                            task: DownloadTask,
                            cause: EndCause,
                            realCause: java.lang.Exception?
                        ) {
                            if (cause == EndCause.COMPLETED) {
                                Log.e("OkDownload", "download success")
                                openDoc(
                                    downloadFile.path + File.separator + fileName,
                                    DocSourceType.PATH,
                                    FileType.PDF
                                )
                            }
                        }
                    })

//                openDoc(url,DocSourceType.URL,null)
                return true
            }

            R.id.action_select -> {
                // 使用Intent打开文件管理器并选择文档

                // 使用Intent打开文件管理器并选择文档
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setType("*/*") // 设置要选择的文件类型，此处为任意文件类型

                startActivityForResult(intent, REQUEST_CODE_SELECT_DOCUMENT) // 启动Activity并设置请求码
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initView() {
        setSupportActionBar(findViewById(R.id.toolbar))
        mDocAdapter = DocAdapter(this, this)
        findViewById<RecyclerView>(R.id.mRvDoc).adapter = mDocAdapter
    }

    private fun initData() {
        requestStoragePermission()
    }

    private fun checkSupport(path: String): Boolean {
        val fileType = FileUtils.getFileTypeForUrl(path)
        Log.e(javaClass.simpleName, "fileType = $fileType")
        return fileType != FileType.NOT_SUPPORT
    }

    fun openDoc(path: String, docSourceType: Int, type: Int? = null) {
        DocViewerActivity.launchDocViewer(this, docSourceType, path, type, DocEngine.INTERNAL.value)
    }

    override fun onItemClick(p0: AdapterView<*>?, v: View?, position: Int, id: Long) {
        when (v?.id) {
            R.id.mCvDocCell -> {
                val groupInfo = mDocAdapter?.datas?.get(id.toInt())
                val docInfo = groupInfo?.docList?.get(position)
                val path = docInfo?.path ?: ""
                if (checkSupport(path)) {
                    openDoc(path, DocSourceType.PATH)
                }
                // word2Html(path)
                // WordActivity.launchDocViewer(this,path)
            }
        }
    }

    fun word2Html(sourceFilePath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val htmlFilePath = cacheDir.absolutePath + "/html"
            val htmlFileName = "word_pdf"
            val bs = BasicSet(this@MainActivity, sourceFilePath, htmlFilePath, htmlFileName)
            bs.picturePath = htmlFilePath
            WordUtils.getInstance(bs).word2html()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION11) {
            if (hasRwPermission()) {
                requestStoragePermission()
            }
        } else if (requestCode == REQUEST_CODE_SELECT_DOCUMENT && resultCode == RESULT_OK) {
            val documentUri = data?.data
            Log.d(TAG, "documentUri = $documentUri")
            documentUri?.let {
                openDoc(it.toString(), DocSourceType.URI, null)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    // ============================================================================================
    //  Implementation Permission Callbacks
    // ============================================================================================

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        // 会回调 AfterPermissionGranted注解对应方法
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {

            val settingsDialogBuilder = SettingsDialog.Builder(this)
            when (requestCode) {
                REQUEST_CODE_STORAGE_PERMISSION -> {
                    settingsDialogBuilder.title = getString(
                        com.cherry.permissions.lib.R.string.title_settings_dialog,
                        "Storage Permission"
                    )
                    settingsDialogBuilder.rationale = getString(
                        com.cherry.permissions.lib.R.string.rationale_ask_again,
                        "Storage Permission"
                    )
                }
            }
            settingsDialogBuilder.build().show()
        }
    }


}