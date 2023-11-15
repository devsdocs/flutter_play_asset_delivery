package de.yucondigital.flutter_play_asset_delivery

import android.content.res.AssetManager
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File

/** FlutterPlayAssetDeliveryPlugin */
class FlutterPlayAssetDeliveryPlugin : FlutterPlugin, MethodCallHandler {
  private lateinit var channel: MethodChannel
  private lateinit var assetManager: AssetManager
  private lateinit var assetList: List<String>

  override fun onAttachedToEngine(
      @NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
  ) {
    assetManager = flutterPluginBinding.applicationContext.assets
    fetchAllAssets()

    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_play_asset_delivery")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getAssetFile") {
      var assetName: String = call.arguments.toString()

      if (assetList.contains(assetName)) {
        val file: File = createTempFile()
        file.writeBytes(assetManager.open(assetName).readBytes())
        result.success(file.absolutePath)
      } else {
        result.error("Asset not found", "Asset could not be found.", null)
      }
    } else if (call.method == "getAllFoldersAndFiles") {
      val foldersAndFiles: List<String> = getAllFoldersAndFiles()
      result.success(foldersAndFiles)
    } else {
      result.notImplemented()
    }
  }

  private fun fetchAllAssets() {
    assetList = assetManager.list("")?.asList() ?: emptyList()
  }

  private fun getAllFoldersAndFiles(): List<String> {
    val folderStack = mutableListOf<String>()
    val fileList = mutableListOf<String>()

    folderStack.add("") // Add an empty string to represent the root directory

    while (folderStack.isNotEmpty()) {
      val currentFolder = folderStack.removeAt(0)
      val assetsInFolder = assetManager.list(currentFolder) ?: emptyArray()

      for (asset in assetsInFolder) {
        val assetPath = "$currentFolder/$asset"
        val isDirectory = assetManager.list(assetPath)?.isNotEmpty() ?: false

        if (isDirectory) {
          folderStack.add(assetPath)
        } else {
          fileList.add(assetPath)
        }
      }
    }

    return fileList
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
