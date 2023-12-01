package com.lge.devicecare.quicksettingtile

import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.lge.devicecare.R
import com.lge.devicecare.service.BackgroundServiceManager


class QuickSettingTile : TileService() {
    var flag: Boolean = false

    // Called when the user adds your tile.
    override fun onTileAdded() {
      //  replace()
        /* qsTile?.apply {
             state = Tile.STATE_ACTIVE
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                 subtitle = "Open App"
             }
             updateTile()
         }*/
        super.onTileAdded()
        val tile = qsTile
        if (tile != null) {
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }

    // Called when your app can update your tile.
    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile
        if (tile != null) {
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        super.onStopListening()
        val tile = qsTile
        if (tile != null) {
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()

        val tile = qsTile
        if (tile != null) {
            if (tile.state.equals(Tile.STATE_ACTIVE)) {
                tile.state = Tile.STATE_INACTIVE
                tile.updateTile()


            } else {
                tile.state = Tile.STATE_ACTIVE
                tile.updateTile()

            }


        }

        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             flag
             startForegroundService(
                 Intent(
                     applicationContext,
                     BackgroundServiceManager::class.java
                 )
             )
         } else {
             flag
             startService(Intent(applicationContext, BackgroundServiceManager::class.java))
         }*/
    }

    // Called when the user removes your tile.
    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    @SuppressLint("WrongConstant")
    fun replace() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // initializing status bar manager on below line.
            val statusBarManager =
                getSystemService(Context.STATUS_BAR_SERVICE) as StatusBarManager
            // adding request and tile service for status bar manager on below line.
            statusBarManager.requestAddTileService(
                // adding app's package components on below line.
                ComponentName(
                    "com.lge.devicecare",
                    "com.lge.devicecare.quicksettingtile",
                ),
                // adding app name on below line.
                resources.getString(R.string.app_name),
                // adding app icon on below line.
                Icon.createWithResource(this, R.mipmap.ic_launcher),
                {}, {}
            )
        } else {
            // on below line displaying toast message.
            Toast.makeText(
                this,
                "`requestAddTileService` can only be called in Android 13/Tiramisu.",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}