package com.racebox.app.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.racebox.app.data.db.AppDatabase
import com.racebox.app.data.security.CryptoUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class RaceExporter(
    private val context: Context,
    private val database: AppDatabase,
    private val crypto: CryptoUtils
) {

    suspend fun exportCsv(raceId: Long): Uri? {
        val detail = database.raceDao().getDetail(raceId) ?: return null
        val sb = StringBuilder()
        sb.appendLine("race_id,timestamp,latitude,longitude,speed_kmh,lap_number")
        detail.laps.forEach { entry ->
            entry.points.forEach { point ->
                sb.appendLine(
                    buildString {
                        append(raceId); append(',')
                        append(point.timestamp); append(',')
                        append(decryptOrZero { crypto.decryptDouble(point.latitudeEnc) }); append(',')
                        append(decryptOrZero { crypto.decryptDouble(point.longitudeEnc) }); append(',')
                        append(decryptOrZero { crypto.decryptDouble(point.speedKmhEnc) }); append(',')
                        append(entry.lap.lapNumber)
                    }
                )
            }
        }
        val file = File(exportDir(), "race_${raceId}.csv")
        file.parentFile?.mkdirs()
        file.writeText(sb.toString())
        return uriFor(file)
    }

    suspend fun exportJson(raceId: Long): Uri? {
        val detail = database.raceDao().getDetail(raceId) ?: return null
        val root = JSONObject()
            .put("race_id", raceId)
            .put("user_id", detail.race.userId)
            .put("start_time", detail.race.startTime)
            .put("end_time", detail.race.endTime)
            .put("total_distance_km", detail.race.totalDistanceKm)
            .put("avg_speed_kmh", detail.race.avgSpeedKmh)
            .put("max_speed_kmh", detail.race.maxSpeedKmh)

        val lapsArray = JSONArray()
        detail.laps.forEach { entry ->
            val pointsArray = JSONArray()
            entry.points.forEach { point ->
                pointsArray.put(
                    JSONObject()
                        .put("timestamp", point.timestamp)
                        .put("latitude", decryptOrZero { crypto.decryptDouble(point.latitudeEnc) })
                        .put("longitude", decryptOrZero { crypto.decryptDouble(point.longitudeEnc) })
                        .put("speed_kmh", decryptOrZero { crypto.decryptDouble(point.speedKmhEnc) })
                )
            }
            lapsArray.put(
                JSONObject()
                    .put("lap_number", entry.lap.lapNumber)
                    .put("start_time", entry.lap.startTime)
                    .put("end_time", entry.lap.endTime)
                    .put("distance_km", entry.lap.distanceKm)
                    .put("avg_speed_kmh", entry.lap.avgSpeedKmh)
                    .put("max_speed_kmh", entry.lap.maxSpeedKmh)
                    .put("points", pointsArray)
            )
        }
        root.put("laps", lapsArray)

        val file = File(exportDir(), "race_${raceId}.json")
        file.parentFile?.mkdirs()
        file.writeText(root.toString(2))
        return uriFor(file)
    }

    fun share(context: Context, uri: Uri) {
        val type = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val intent = Intent(Intent.ACTION_SEND).apply {
            this.type = type
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    private fun exportDir(): File = File(context.filesDir, "exports")

    private fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    private fun decryptOrZero(block: () -> Double): Double =
        try {
            block()
        } catch (_: Exception) {
            0.0
        }
}