package com.pixeldarken.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pixeldarken.app.databinding.ActivityMainBinding
import com.pixeldarken.app.overlay.PixelDarkenService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val spots = mutableListOf<DarkSpot>()
    private lateinit var adapter: ArrayAdapter<String>

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* optional; overlay still works */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spots.clear()
        spots.addAll(SpotStore.load(this))

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        binding.spotList.adapter = adapter
        refreshList()

        binding.btnAdd.setOnClickListener { addSpot() }
        binding.btnClear.setOnClickListener {
            spots.clear()
            persistAndRefreshOverlay()
            refreshList()
        }
        binding.btnOverlayPermission.setOnClickListener { openOverlaySettings() }
        binding.btnStart.setOnClickListener { startOverlay() }
        binding.btnStop.setOnClickListener {
            PixelDarkenService.stop(this)
            updateStatus()
        }
        binding.spotList.setOnItemLongClickListener { _, _, position, _ ->
            if (position in spots.indices) {
                spots.removeAt(position)
                persistAndRefreshOverlay()
                refreshList()
            }
            true
        }

        maybeRequestNotificationPermission()
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun addSpot() {
        val x = binding.inputX.text?.toString()?.toIntOrNull()
        val y = binding.inputY.text?.toString()?.toIntOrNull()
        val radius = binding.inputRadius.text?.toString()?.toIntOrNull() ?: 24
        val alpha = binding.inputAlpha.text?.toString()?.toIntOrNull() ?: 220

        if (x == null || y == null) {
            Toast.makeText(this, R.string.error_xy_required, Toast.LENGTH_SHORT).show()
            return
        }

        spots += DarkSpot(
            id = System.currentTimeMillis(),
            xPx = x,
            yPx = y,
            radiusPx = radius.coerceAtLeast(1),
            alpha = alpha.coerceIn(0, 255)
        )
        persistAndRefreshOverlay()
        refreshList()
        binding.inputX.text?.clear()
        binding.inputY.text?.clear()
    }

    private fun persistAndRefreshOverlay() {
        SpotStore.save(this, spots)
        if (PixelDarkenService.isRunning(this)) {
            PixelDarkenService.refresh(this)
        }
    }

    private fun refreshList() {
        adapter.clear()
        spots.forEach { s ->
            adapter.add("x=${s.xPx}, y=${s.yPx}, r=${s.radiusPx}, α=${s.alpha}")
        }
        adapter.notifyDataSetChanged()
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, R.string.grant_overlay_first, Toast.LENGTH_LONG).show()
            openOverlaySettings()
            return
        }
        if (spots.isEmpty()) {
            Toast.makeText(this, R.string.add_spot_first, Toast.LENGTH_SHORT).show()
            return
        }
        PixelDarkenService.start(this)
        updateStatus()
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun updateStatus() {
        val overlayOk = Settings.canDrawOverlays(this)
        val running = PixelDarkenService.isRunning(this)
        binding.statusText.text = getString(
            R.string.status_fmt,
            if (overlayOk) getString(R.string.yes) else getString(R.string.no),
            if (running) getString(R.string.yes) else getString(R.string.no),
            spots.size
        )
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
