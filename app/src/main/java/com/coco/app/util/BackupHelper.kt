package com.coco.app.util

import com.coco.app.domain.Note
import org.json.JSONArray
import org.json.JSONObject

object BackupHelper {

    fun exportToJson(notes: List<Note>): String {
        val jsonArray = JSONArray()
        notes.forEach { note ->
            val obj = JSONObject().apply {
                put("id", note.id)
                put("content", note.content)
                put("createdAt", note.createdAt)
                put("updatedAt", note.updatedAt)
                put("isPinned", note.isPinned)
                if (note.remindAt != null) put("remindAt", note.remindAt) else put("remindAt", JSONObject.NULL)
                put("colorIndex", note.colorIndex)
                put("isArchived", note.isArchived)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    fun importFromJson(jsonString: String): List<Note> {
        val result = mutableListOf<Note>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val content = obj.optString("content", "")
                if (content.isNotBlank()) {
                    val remindVal = if (obj.isNull("remindAt")) null else obj.optLong("remindAt")
                    result.add(
                        Note(
                            id = 0, // 0 para que Room genere un nuevo ID y no haya conflictos
                            content = content,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                            isPinned = obj.optBoolean("isPinned", false),
                            remindAt = remindVal,
                            colorIndex = obj.optInt("colorIndex", 0),
                            isArchived = obj.optBoolean("isArchived", false)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
