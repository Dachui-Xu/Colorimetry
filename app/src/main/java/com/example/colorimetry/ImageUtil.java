package com.example.colorimetry;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

public class ImageUtil {
    public static String imageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0);
    }

    public static Bitmap base64ToImage(String bitmap64) {
        byte[] bytes = Base64.decode(bitmap64, 0);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
