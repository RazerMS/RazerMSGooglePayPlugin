package rms.library.googlepay.Helper;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AlgorithmHelper {
    private static final String TAG = "AlgoHelper";

    public static byte[] sha256(String data) {
        Log.d(TAG, String.format("data: %s", data));
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            byte[] result = digest.digest(data.getBytes());
            Log.d(TAG, String.format("result: %s", UtilityHelper.ByteArrayToHexString(result)));
            return result;
        } catch (Exception exception) {
            exception.printStackTrace();
            return new byte[0];
        }
    }

    public static byte[] sha1(String data) {
        Log.d(TAG, String.format("data: %s", data));
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            byte[] result = digest.digest(data.getBytes());
            ;
            Log.d(TAG, String.format("result: %s", UtilityHelper.ByteArrayToHexString(result)));
            return result;
        } catch (Exception exception) {
            exception.printStackTrace();
            return new byte[0];
        }
    }

    public static byte[] md5(String data) {
        Log.d(TAG, String.format("data: %s", data));
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            byte[] result = digest.digest(data.getBytes());
            Log.d(TAG, String.format("result: %s", UtilityHelper.ByteArrayToHexString(result)));
            return result;
        } catch (Exception exception) {
            exception.printStackTrace();
            return new byte[0];
        }
    }

    public static byte[] encryptAES(byte[] data, byte[] key) {
        try {
            Log.d(TAG, String.format("data: %s", UtilityHelper.ByteArrayToHexString(data)));
            Log.d(TAG, String.format("key: %s", UtilityHelper.ByteArrayToHexString(key)));

            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] result = cipher.doFinal(data);
            Log.d(TAG, String.format("result: %s", UtilityHelper.ByteArrayToHexString(result)));
            return result;
        } catch (Exception exception) {
            exception.printStackTrace();
            return new byte[0];
        }
    }

    public static byte[] decryptAES(byte[] data, byte[] key) {
        try {
            byte[] iv = new byte[16];
            Log.d(TAG, String.format("data: %s", UtilityHelper.ByteArrayToHexString(data)));
            Log.d(TAG, String.format("key: %s", UtilityHelper.ByteArrayToHexString(key)));
            Log.d(TAG, String.format("iv: %s", UtilityHelper.ByteArrayToHexString(iv)));

            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            byte[] result = cipher.doFinal(data);
            Log.d(TAG, String.format("result: %s", UtilityHelper.ByteArrayToHexString(result)));
            return result;
        } catch (Exception exception) {
            Log.d(TAG, String.format("exception: %s", exception.getMessage()));
            return new byte[0];
        }
    }

    public static String encodeBase64(String data) {
        Log.d(TAG, String.format("data: %s", data));
        byte[] base64Data = data.getBytes(StandardCharsets.UTF_8);
        String result = Base64.encodeToString(base64Data, Base64.DEFAULT);
        Log.d(TAG, String.format("result: %s", result));
        return result;
    }

    public static byte[] hmac_sha256(byte[] data, byte[] key) {
        try {
            Log.d(TAG, String.format("data: %s", UtilityHelper.ByteArrayToHexString(data)));
            Log.d(TAG, String.format("key: %s", UtilityHelper.ByteArrayToHexString(key)));

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
            mac.init(secretKeySpec);
            byte[] result = mac.doFinal(data);
            Log.d(TAG, String.format("result: %s", UtilityHelper.ByteArrayToHexString(result)));
            return result;
        } catch (Exception exception) {
            exception.printStackTrace();
            return new byte[0];
        }
    }
}
