package com.atakmap.android.soothsayer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import com.atakmap.android.ipc.AtakBroadcast
import com.atakmap.android.soothsayer.models.request.Environment
import com.atakmap.android.soothsayer.util.FOLDER_PATH
import java.io.File
import java.io.FileOutputStream

class FilePickerActivity : Activity() {
    companion object {
        const val PICK_FILE_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        startActivityForResult(pickIntent, PICK_FILE_REQUEST)
    }

    private fun getFileName(uri: Uri): String {
        var res: String? = null

        if ("content" == uri.scheme) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    res = cursor.getString(nameIdx)
                }
            } finally {
                cursor?.close()
            }
        }

        return res ?: ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            val fileUri: Uri = data?.data ?: return
            val fileName = getFileName(fileUri)

            val sharedDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "SOOTHSAYER templates")
            sharedDir.mkdirs()
            val sharedFile = File(sharedDir, fileName)

            contentResolver.openInputStream(fileUri)?.use { input ->
                sharedFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val uriToBroadcast = Uri.fromFile(sharedFile)

            val intent = Intent("com.soothsayer.FILE_SELECTED").apply {
                putExtra("uri", uriToBroadcast)
                putExtra("file_name", fileName)
                `package` = "com.atakmap.app.civ"
            }

            sendBroadcast(intent)
        }

        finish()
    }

}