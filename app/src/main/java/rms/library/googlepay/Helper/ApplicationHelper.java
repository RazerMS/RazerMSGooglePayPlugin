package rms.library.googlepay.Helper;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.util.UUID;

public class ApplicationHelper {
    private static final String TAG = "ApplicationHelper";
    private static ApplicationHelper single_instance = null;

    private String _seed = "";

    protected ApplicationHelper() {
    }

    public static ApplicationHelper getInstance() {
        if (single_instance == null) {
            single_instance = new ApplicationHelper();
        }
        return single_instance;
    }

    public void GenerateSeed() {
        if (this._seed.isEmpty()) {
            this._seed = UUID.randomUUID().toString();
        }
    }

    public String GetSeed() {
        return this._seed;
    }

    public String EncryptedPassword(String merchantID, String appName, String verificationKey, String seed) {
        byte[] hashPassword = AlgorithmHelper.sha256(merchantID + appName);
        Log.d(TAG, merchantID + appName + seed);
        Log.d(TAG, UtilityHelper.ByteArrayToHexString(hashPassword));

        byte[] cipheredKey = AlgorithmHelper.decryptAES(UtilityHelper.HexStringToByteArray(verificationKey), hashPassword);
        Log.d(TAG, UtilityHelper.ByteArrayToHexString(cipheredKey));

        byte[] md5Key = AlgorithmHelper.md5(UtilityHelper.ByteArrayToHexString(cipheredKey));
        Log.d(TAG, UtilityHelper.ByteArrayToHexString(md5Key));

        byte[] finalHash = AlgorithmHelper.sha1(merchantID + appName + UtilityHelper.ByteArrayToHexString(md5Key));
        Log.d(TAG, merchantID + appName + UtilityHelper.ByteArrayToHexString(md5Key));
        Log.d(TAG, UtilityHelper.ByteArrayToHexString(finalHash));

        return UtilityHelper.ByteArrayToHexString(hashPassword);
    }

    public String GetAuthorizationData(String username, String password, String merchantID, String appName, String seed) {
        byte[] hashPassword = AlgorithmHelper.sha256(merchantID + appName + seed);
        Log.d(TAG, String.format("data: %s%s%s", merchantID, appName, seed));
        Log.d(TAG, String.format("hashPassword: %s", UtilityHelper.ByteArrayToHexString(hashPassword)));

        String plainText = username + ":" + password;
        Log.d(TAG, String.format("cipheredKey: %s", plainText));

        String base64Data = AlgorithmHelper.encodeBase64(plainText);
        Log.d(TAG, String.format("base64Data: %s", base64Data));

        return String.format("Basic %s", base64Data);
    }

    public String GetVCode(String amount, String merchantID, String orderId, String verifyKey) {
        byte[] hashData = AlgorithmHelper.md5(amount + merchantID + orderId + verifyKey);
        Log.d(TAG, String.format("data: %s%s%s%s", amount, merchantID, orderId, verifyKey));
        Log.d(TAG, String.format("hashData: %s", UtilityHelper.ByteArrayToHexString(hashData)));

        return String.format("%s", UtilityHelper.ByteArrayToHexString(hashData));
    }

    public String GetSKey(String txnID, String merchantID, String verifyKey, String amount) {
        byte[] hashData = AlgorithmHelper.md5(txnID + merchantID + verifyKey + amount);
        Log.d(TAG, String.format("data: %s%s%s%s", txnID, merchantID, verifyKey, amount));
        Log.d(TAG, String.format("hashData: %s", UtilityHelper.ByteArrayToHexString(hashData)));

        return String.format("%s", UtilityHelper.ByteArrayToHexString(hashData));
    }

    public String getStringByIdName(Context context, String idName) {
        Resources res = context.getResources();
        return res.getString(res.getIdentifier(idName, "string", context.getPackageName()));
    }
}
