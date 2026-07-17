package com.pixeldarken.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object SpotStore {
    private const val PREFS = "pixel_darken_prefs"
    private const val KEY_SPOTS = "spots"

    fun load(context: Context): MutableList<DarkSpot> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SPOTS, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableListOf<DarkSpot>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out += DarkSpot(
                id = o.getLong("id"),
                xPx = o.getInt("x"),
                yPx = o.getInt("y"),
                radiusPx = o.optInt("r", 24),
                alpha = o.optInt("a", 220)
            )
        }
        return out
    }

    fun save(context: Context, spots: List<DarkSpot>) {
        val arr = JSONArray()
        spots.forEach { s ->
            arr.put(
                JSONObject()
                    .put("id", s.id)
                    .put("x", s.xPx)
                    .put("y", s.yPx)
                    .put("r", s.radiusPx)
                    .put("a", s.alpha)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SPOTS, arr.toString())
            .apply()
    }
}
