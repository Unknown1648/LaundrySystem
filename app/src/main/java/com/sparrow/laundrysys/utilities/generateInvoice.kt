package com.sparrow.laundrysys.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun generateInvoice(
    context: Context,
    orderId: String,
    customerName: String,
    customerPhone: String,
    customerLocation: String,
    serviceItems: List<Map<String, Any>>,
    totalPrice: Double,
    isPaid: Boolean
) {
    val receiptWidth = 384
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(receiptWidth, 900, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    var yPosition = 40

    // Business Info
    paint.textSize = 20f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText("THE LAUNDRY ROOM", (receiptWidth / 2).toFloat(), yPosition.toFloat(), paint)

    paint.textSize = 12f
    paint.typeface = Typeface.DEFAULT
    yPosition += 20
    canvas.drawText("Mbagathi Greens Behind Maasai Mall", (receiptWidth / 2).toFloat(), yPosition.toFloat(), paint)

    yPosition += 15
    canvas.drawText("Phone: 0737 823 581", (receiptWidth / 2).toFloat(), yPosition.toFloat(), paint)
    yPosition += 45

    // Invoice Title
    paint.textSize = 16f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText("RECEIPT", (receiptWidth / 2).toFloat(), yPosition.toFloat(), paint)

    // Decorative elements around RECEIPT
    paint.strokeWidth = 2f
    canvas.drawLine(100f, yPosition.toFloat(), 130f, yPosition.toFloat(), paint)
    canvas.drawLine(254f, yPosition.toFloat(), 284f, yPosition.toFloat(), paint)

    yPosition += 20

    // Order Number**
    paint.textSize = 11f
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Order No: #$orderId", (receiptWidth / 2).toFloat(), yPosition.toFloat(), paint)

    yPosition += 25

    // Customer Info Section with border
    // Draw a rounded rectangle border
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1f
    val customerInfoRect = RectF(10f, yPosition.toFloat(), (receiptWidth - 10).toFloat(), (yPosition + 85).toFloat())
    canvas.drawRoundRect(customerInfoRect, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    yPosition += 25
    paint.textSize = 14f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText("BILL TO:", 20f, yPosition.toFloat(), paint)

    paint.textSize = 12f
    paint.typeface = Typeface.DEFAULT
    yPosition += 20
    canvas.drawText("Name: $customerName", 20f, yPosition.toFloat(), paint)
    yPosition += 15
    canvas.drawText("Phone: ${customerPhone.substring(0, 6)}*****${customerPhone.takeLast(2)}", 20f, yPosition.toFloat(), paint)
    yPosition += 15
    canvas.drawText("Address: $customerLocation", 20f, yPosition.toFloat(), paint)

    yPosition += 35

    // Table Header with borders
    paint.style = Paint.Style.STROKE
    canvas.drawRect(10f, yPosition.toFloat() - 15f, (receiptWidth - 10).toFloat(), yPosition.toFloat() + 5f, paint)
    paint.style = Paint.Style.FILL

    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 12f
    canvas.drawText("Item", 15f, yPosition.toFloat(), paint)
    canvas.drawText("Qty", 160f, yPosition.toFloat(), paint)
    canvas.drawText("UoM", 200f, yPosition.toFloat(), paint)
    canvas.drawText("Price", 250f, yPosition.toFloat(), paint)
    canvas.drawText("Total", 320f, yPosition.toFloat(), paint)

    yPosition += 15
    paint.strokeWidth = 1f
    canvas.drawLine(10f, yPosition.toFloat(), (receiptWidth - 10).toFloat(), yPosition.toFloat(), paint)
    yPosition += 15

    paint.typeface = Typeface.DEFAULT

    // Service Items List with subtle separators
    serviceItems.forEach { item ->
        val name = item["item"] as? String ?: "Unknown"
        val quantity = (item["quantity"] as? Number)?.toInt() ?: 0
        val uom = item["uom"] as? String ?: "-"
        val price = (item["price"] as? Number)?.toDouble() ?: 0.0
        val total = (item["total"] as? Number)?.toDouble() ?: 0.0

        canvas.drawText(name, 15f, yPosition.toFloat(), paint)
        canvas.drawText(quantity.toString(), 160f, yPosition.toFloat(), paint)
        canvas.drawText(uom, 200f, yPosition.toFloat(), paint)
        canvas.drawText("Ksh %.2f".format(price), 250f, yPosition.toFloat(), paint)
        canvas.drawText("Ksh %.2f".format(total), 320f, yPosition.toFloat(), paint)

        yPosition += 15
        // Draw a dotted line between items
        paint.strokeWidth = 0.5f
        val pathEffect = DashPathEffect(floatArrayOf(3f, 3f), 0f)
        paint.pathEffect = pathEffect
        canvas.drawLine(15f, yPosition.toFloat(), (receiptWidth - 15).toFloat(), yPosition.toFloat(), paint)
        paint.pathEffect = null
        yPosition += 10
    }

    yPosition += 5
    paint.strokeWidth = 1f
    canvas.drawLine(10f, yPosition.toFloat(), (receiptWidth - 10).toFloat(), yPosition.toFloat(), paint)
    yPosition += 20

    // Total Price & Payment Status Section
    paint.style = Paint.Style.STROKE
    val totalRect = RectF(200f, yPosition.toFloat() - 15f, (receiptWidth - 10).toFloat(), (yPosition + 45).toFloat())
    canvas.drawRoundRect(totalRect, 5f, 5f, paint)
    paint.style = Paint.Style.FILL

    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Total:", 210f, yPosition.toFloat(), paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Ksh %.2f".format(totalPrice), (receiptWidth - 20).toFloat(), yPosition.toFloat(), paint)

    yPosition += 20
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText("Payment Status:", 210f, yPosition.toFloat(), paint)

    // Payment status
    paint.typeface = if (isPaid) {
        Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    } else {
        Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
    }
    paint.textAlign = Paint.Align.RIGHT
    val paymentStatus = if (isPaid) "PAID" else "PENDING"
    canvas.drawText(paymentStatus, (receiptWidth - 20).toFloat(), yPosition.toFloat(), paint)

    yPosition += 30

    // QR Code for digital receipt with order ID
    try {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode("Order:$orderId", BarcodeFormat.QR_CODE, 90, 90)
        val qrCodeBitmap = createBitmap(90, 90, Bitmap.Config.RGB_565)

        for (x in 0 until 90) {
            for (y in 0 until 90) {
                qrCodeBitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        canvas.drawBitmap(qrCodeBitmap, (receiptWidth / 2 - 45).toFloat(), yPosition.toFloat(), paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        yPosition += 100
        canvas.drawText("Scan to view your digital receipt", (receiptWidth / 2).toFloat(), yPosition.toFloat(), paint)

    } catch (e: Exception) {
        // If QR code generation fails, skip it
        yPosition += 20
    }

    yPosition += 30

    // Welcome Back Message with decorative stars
    canvas.drawText("★", (receiptWidth / 2 - 120).toFloat(), yPosition.toFloat(), paint)
    canvas.drawText("★", (receiptWidth / 2 + 120).toFloat(), yPosition.toFloat(), paint)

    paint.textSize = 14f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText("Thank You for Choosing Us!", (receiptWidth / 2).toFloat(), yPosition.toFloat(), paint)

    yPosition += 20
    paint.textSize = 12f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("We look forward to serving you again!", (receiptWidth / 2).toFloat(), yPosition.toFloat(), paint)

    // Footer with decorative line
    yPosition += 35
    paint.strokeWidth = 1f
    canvas.drawLine(50f, yPosition.toFloat(), (receiptWidth - 50).toFloat(), yPosition.toFloat(), paint)

    yPosition += 20
    paint.textSize = 10f
    canvas.drawText("Find us on social media @TheLaundryRoomKE", (receiptWidth / 2).toFloat(), yPosition.toFloat(), paint)

    pdfDocument.finishPage(page)

    // Save PDF to cache directory first
    val cacheFile = File(context.cacheDir, "Invoice_$orderId.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(cacheFile))

        // Now print the PDF
        printPdf(context, cacheFile, "Invoice_$orderId")

        // Also save a copy to Downloads for record-keeping
        val downloadsFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Invoice_$orderId.pdf")
        cacheFile.copyTo(downloadsFile, overwrite = true)
        Toast.makeText(context, "Copy of Invoice saved in downloads!", Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Error processing invoice: ${e.message}", Toast.LENGTH_LONG).show()
    }

    pdfDocument.close()
}