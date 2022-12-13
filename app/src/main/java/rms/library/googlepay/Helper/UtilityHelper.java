package rms.library.googlepay.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;

import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class UtilityHelper {
    public static byte[] StringToByteArray(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static String ByteArrayToString(byte[] byteArray) {
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    public static String ByteArrayToHexString(byte[] byteArray) {
        StringBuilder result = new StringBuilder();
        for (byte b : byteArray) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static byte[] HexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4)
                    + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    public static Bitmap createBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap getScreenShot(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public static boolean storeImage(Context context, Bitmap bitmap, String fileName){
        final String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "Barcodes";
        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
