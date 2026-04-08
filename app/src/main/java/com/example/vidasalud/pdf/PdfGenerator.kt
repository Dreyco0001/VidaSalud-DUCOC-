package com.example.vidasalud.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    @RequiresApi(Build.VERSION_CODES.Q)
    fun generarInforme(
        context: Context,
        nombre: String
    ): Uri? {

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint()

        var y = 50

        // Título
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Informe VidaSalud", 80f, y.toFloat(), paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false

        y += 40

        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // Datos simulados
        val pasos = (3000..12000).random()
        val calorias = (1800..3000).random()
        val sueno = (5..9).random()

        canvas.drawText("Usuario: $nombre", 50f, y.toFloat(), paint)
        y += 25
        canvas.drawText("Fecha: $fecha", 50f, y.toFloat(), paint)

        y += 40

        canvas.drawText("Pasos: $pasos", 50f, y.toFloat(), paint)
        y += 25
        canvas.drawText("Calorías: $calorias kcal", 50f, y.toFloat(), paint)
        y += 25
        canvas.drawText("Sueño: $sueno hrs", 50f, y.toFloat(), paint)

        y += 50
        canvas.drawText("Generado automáticamente", 50f, y.toFloat(), paint)

        document.finishPage(page)

        // 🔥 Guardar en DESCARGAS (visible)
        val resolver = context.contentResolver

        val fileName = "informe_${System.currentTimeMillis()}.pdf"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)

            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                document.writeTo(outputStream)
            }

            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(it, contentValues, null, null)
        }

        document.close()

        return uri
    }
}