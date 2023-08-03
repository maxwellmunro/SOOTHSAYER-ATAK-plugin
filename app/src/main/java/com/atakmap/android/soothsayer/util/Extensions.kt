package com.atakmap.android.soothsayer.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.atakmap.android.soothsayer.PluginDropDownReceiver
import com.atakmap.android.soothsayer.models.request.TemplateDataModel
import com.atakmap.android.soothsayer.plugin.R
import com.atakmap.coremap.log.Log
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

val FOLDER_PATH = Environment.getExternalStorageDirectory().toString() + "/atak/SOOTHSAYER"
private val TEMPLATES_PATH = "$FOLDER_PATH/templates"
val KMZ_FOLDER = "$FOLDER_PATH/KMZ"
const val SOOTHSAYER = "SOOTHSAYER_"
const val PNG_IMAGE = ".png"
const val KMZ_FILE = ".kmz"

/**
 * Note - this will become a API offering in 4.5.1 and beyond.
 * @param drawableId
 * @return
 */
fun Context.getBitmap(drawableId: Int): Bitmap? {
    return when (val drawable = ContextCompat.getDrawable(this, drawableId)) {
        is BitmapDrawable -> {
            BitmapFactory.decodeResource(this.resources, drawableId)
        }
        is VectorDrawable -> {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
        else -> {
            null
        }
    }
}

fun Context.createAndStoreFiles(fileList: List<String>?) {
    val folder = File(TEMPLATES_PATH)
    if (!folder.exists()) {
        Log.d(PluginDropDownReceiver.TAG, "createAndStoreFiles creating  new folder....")
        folder.mkdirs()
    }
    Log.d(PluginDropDownReceiver.TAG, "createAndStoreFiles folder path : $FOLDER_PATH")
    // code to add files to folder.
    fileList?.forEach { fileName ->
//        val fileName = file
        val inputStream = assets.open(fileName)
        val outputStream = FileOutputStream(File(folder, fileName))
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
    }
}

fun getTemplatesFromFolder(): ArrayList<TemplateDataModel> {
    val folder = File(TEMPLATES_PATH)
    val templateList: ArrayList<TemplateDataModel> = ArrayList()
    if (folder.exists()) {
        val files = folder.listFiles()?.filter { it.path.endsWith(".json") } ?: ArrayList()
//        Log.d(PluginDropDownReceiver.TAG, "files : ${files.size}")
        for (file in files) {
            val jsonString = File(TEMPLATES_PATH, file.name).readText()
            try {
                val jsonData = Gson().fromJson(jsonString, TemplateDataModel::class.java)
                jsonData.transmitter?.let {
                    templateList.add(jsonData)
                    Log.d(
                        PluginDropDownReceiver.TAG,
                        "fileName: ${file.name} \n${JSONObject(jsonString)}"
                    )
                }
            } catch (e: Exception) {
                Log.d(
                        PluginDropDownReceiver.TAG,
                        "Bad template: ${file.name}")
                Log.e(PluginDropDownReceiver.TAG, e.stackTrace.toString())
            }
        }
    }
//    Log.d(PluginDropDownReceiver.TAG, "templateList : ${Gson().toJson(templateList)}")
    return templateList
}

fun Double.roundValue(): Double {
    return round(this * 100000) / 100000
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.isConnected(): Boolean {
    var result = false
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.activeNetwork
        networkCapabilities?.let { capabilities ->
            val actNw = connectivityManager.getNetworkCapabilities(capabilities)
            actNw?.let {
                result = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> {
                        false
                    }
                }
            }
        }
    } else {
        result = try {
            connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    return result
}

fun String.getFileName():String{
   return "${SimpleDateFormat("ddHHmmSS", Locale.getDefault()).format(Date())}_$SOOTHSAYER$this"
}

fun Context.getLineColor(signalValue:Double): Int?{
    Log.d(PluginDropDownReceiver.TAG, "getLineColor : $signalValue")
    val colorId = when{
        signalValue >= -80.0 -> R.color.green
        signalValue >=-85.0 && signalValue < -80.0 -> R.color.orange
        else -> null // signalValue < -90 -> null
    }
    return if(colorId == null){
        null
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.resources.getColor(colorId, this.theme)
    } else {
        this.resources.getColor(colorId)
    }
}