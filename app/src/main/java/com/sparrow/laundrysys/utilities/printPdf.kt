package com.sparrow.laundrysys.utilities

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import com.sparrow.laundrysys.classes.PdfDocumentAdapter
import java.io.File

fun printPdf(context: Context, pdfFile: File, jobName: String) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

    try {
        val printAdapter = PdfDocumentAdapter(pdfFile)
        printManager.print(
            jobName,
            printAdapter,
            PrintAttributes.Builder().build()
        )
    } catch (e: Exception) {
        Toast.makeText(context, "Error printing: ${e.message}", Toast.LENGTH_LONG).show()
    }
}