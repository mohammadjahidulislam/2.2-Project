package com.attendance.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

/**
 * Helper methods to generate a QR code image from text (used by the teacher's
 * "start session" screen) and to decode a QR code from a captured camera
 * frame (used by the student's scan screen).
 */
public class QRUtil {

    /** Generates a QR code as a JavaFX Image, ready to place in an ImageView. */
    public static Image generateQRImage(String text, int size) throws WriterException {
        BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size);
        BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bufferedImage.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /**
     * Tries to decode a QR code from a captured webcam frame.
     * Returns the decoded text, or null if no QR code was found in this frame.
     */
    public static String decodeQRFromImage(BufferedImage image) {
        if (image == null) return null;
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null; // no QR code visible in this frame - normal, just keep scanning
        } catch (Exception e) {
            // ZXing can occasionally throw other runtime exceptions on noisy/blurry frames;
            // treat these the same as "no QR code found yet" instead of crashing the scan loop.
            return null;
        }
    }
}
