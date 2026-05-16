package com.parkease.payment.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ReceiptService — generates PDF payment receipts using iText 7.
 *
 * Receipts are written to /tmp and the file path is stored in Payment.receiptUrl.
 * In production, this path should be replaced with an object-storage URL (S3/GCS).
 *
 * Currency is INR (₹) — matching the platform's operating region.
 */
@Slf4j
@Service
public class ReceiptService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private static final DeviceRgb BRAND_BLUE  = new DeviceRgb(25, 60, 120);
    private static final DeviceRgb LIGHT_GRAY  = new DeviceRgb(245, 245, 245);

    /**
     * Generate a PDF receipt and return the file path.
     *
     * @param bookingId   the associated booking
     * @param paymentId   the payment ID (used in filename)
     * @param amount      amount paid in INR
     * @param paymentTime timestamp of payment confirmation
     * @return absolute file path of the generated PDF
     */
    public String generateReceipt(Long bookingId, Long paymentId,
                                  BigDecimal amount, LocalDateTime paymentTime) {
        String filePath = "/tmp/receipt_" + paymentId + ".pdf";
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            // ---- Header ----
            document.add(new Paragraph("ParkEase")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(26)
                    .setBold()
                    .setFontColor(BRAND_BLUE));

            document.add(new Paragraph("Smart Parking Management Platform")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11)
                    .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph("PAYMENT RECEIPT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold()
                    .setMarginTop(10));

            document.add(new Paragraph(" "));

            // ---- Receipt details table ----
            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60}));
            table.setWidth(UnitValue.createPercentValue(100));

            addRow(table, "Receipt No.",   "RCP-" + paymentId,             false);
            addRow(table, "Booking ID",    String.valueOf(bookingId),       true);
            addRow(table, "Amount Paid",   "₹" + amount.toPlainString(),   false);  // INR symbol
            addRow(table, "Payment Date",  paymentTime.format(FORMATTER),   true);
            addRow(table, "Status",        "PAID",                          false);

            document.add(table);
            document.add(new Paragraph(" "));

            // ---- Footer ----
            document.add(new Paragraph(
                    "This is a system-generated receipt. No signature required.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph("Thank you for parking with ParkEase!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setBold()
                    .setFontColor(BRAND_BLUE));

            log.info("Receipt generated: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Failed to generate receipt for paymentId={}: {}", paymentId, e.getMessage());
            throw new RuntimeException("Failed to generate receipt: " + e.getMessage(), e);
        }
    }

    private void addRow(Table table, String label, String value, boolean shaded) {
        DeviceRgb bg = shaded ? LIGHT_GRAY : new DeviceRgb(255, 255, 255);

        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold())
                .setBackgroundColor(bg)
                .setPadding(6);

        Cell valueCell = new Cell()
                .add(new Paragraph(value))
                .setBackgroundColor(bg)
                .setPadding(6);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
