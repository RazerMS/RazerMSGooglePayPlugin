package rms.library.googlepay.Service;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

//import rms.mobile.sdk.module.BuildConfig;
import rms.library.googlepay.Helper.ApplicationHelper;

import java.util.Base64;

public class ApiRequestService {

    public static JSONObject paymentDetail;
    private static final String TAG = "ApiServiceRequest";
    private static ApiRequestService single_instance = null;

    private String _msgType;

    static class Production {
        static final String BASE_PAYMENT = "https://pay.merchant.razer.com/";
        static final String API_PAYMENT = "https://api.merchant.razer.com/";
    }

    static class Development {
        static final String BASE_PAYMENT = "https://sandbox.merchant.razer.com/";
        static final String API_PAYMENT = "https://sandbox.merchant.razer.com/";
    }

//    static class Environment {
//        static final String PRODUCTION = "prod";
//        static final String DEVELOPMENT = "dev";
//    }

    public ApiRequestService() {
    }

    public static ApiRequestService getInstance() {
        if (single_instance == null) {
            single_instance = new ApiRequestService();
        }
        return single_instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Object GetPaymentRequest(JSONObject paymentInput, String paymentInfo ) {
        Log.d(TAG, "GetPaymentRequest invoked");
        try {
            String endPoint = "";
            String txnType = "SALS";
            String orderId = paymentInput.getString("orderId");
            String amount = paymentInput.getString("amount");
            String currency = paymentInput.getString("currency");
            String billName = paymentInput.getString("billName");
            String billEmail = paymentInput.getString("billEmail");
            String billPhone = paymentInput.getString("billPhone");
            String billDesc = paymentInput.getString("billDesc");
            String merchantId = paymentInput.getString("merchantId");
            String verificationKey = paymentInput.getString("verificationKey");
            String isSandbox = paymentInput.getString("isSandbox");

            if (isSandbox.equals("false")) {
                endPoint = Production.BASE_PAYMENT + "RMS/API/Direct/1.4.0/index.php";
            } else if (isSandbox.equals("true")) {
                endPoint = Development.BASE_PAYMENT + "RMS/API/Direct/1.4.0/index.php";
            }

            Uri uri = Uri.parse(endPoint)
                    .buildUpon()
                    .build();

            //"Signature": "<MD5(amount+merchantID+referenceNo+Vkey)>",
            String vCode = ApplicationHelper.getInstance().GetVCode(
                amount,
                merchantId,
                orderId,
                verificationKey
            );

            String GooglePayBase64 = Base64.getEncoder()
                                    .encodeToString(paymentInfo.getBytes());

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("MerchantID", merchantId)
                    .appendQueryParameter("ReferenceNo", orderId)
                    .appendQueryParameter("TxnType", txnType)
                    .appendQueryParameter("TxnCurrency", currency)
                    .appendQueryParameter("TxnAmount", amount)
                    .appendQueryParameter("CustName", billName)
                    .appendQueryParameter("CustEmail", billEmail)
                    .appendQueryParameter("CustContact", billPhone)
                    .appendQueryParameter("CustDesc", billDesc)
                    .appendQueryParameter("Signature", vCode)
                    .appendQueryParameter("mpsl_version", "2")
                    .appendQueryParameter("GooglePay", GooglePayBase64);

                return postRequest(uri, builder);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object GetPaymentResult(JSONObject transaction ) {
        Log.d(TAG, "GetPaymentRequest invoked");
        try {
            String endPoint = "";
            String isSandbox = "false";

            if (isSandbox.equals("false")) {
                endPoint = Production.API_PAYMENT + "RMS/query/q_by_tids.php";
            } else if (isSandbox.equals("true")) {
                endPoint = Development.API_PAYMENT + "RMS/query/q_by_tids.php";
            }

            Uri uri = Uri.parse(endPoint)
                    .buildUpon()
                    .build();

            String txID = transaction.getString("txID");
            String amount = transaction.getString("amount");
            String merchantId = transaction.getString("merchantId");
            String verificationKey = transaction.getString("verificationKey");

            String sKey = ApplicationHelper.getInstance().GetSKey(
                    txID,
                    merchantId,
                    verificationKey,
                    amount
            );

//            String sKey = ApplicationHelper.getInstance().GetSKey(
//                    AppData.getInstance().getTxnID(),
//                    mMobileSDKParam.getMerchantId(),
//                    mMobileSDKParam.getVerificationKey(),
//                    mMobileSDKParam.getAmount()
//            );

//            Uri.Builder builder = new Uri.Builder()
//                    .appendQueryParameter("amount", amount)
//                    .appendQueryParameter("txID", txID)
//                    .appendQueryParameter("domain", merchantId)
//                    .appendQueryParameter("skey", sKey)
//                    .appendQueryParameter("url", "")
//                    .appendQueryParameter("type", "0");



            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("tIDs", txID)
                    .appendQueryParameter("domain", merchantId)
                    .appendQueryParameter("skey", sKey)
                    .appendQueryParameter("url", "")
                    .appendQueryParameter("type", "0");

            return postRequest(uri, builder);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    private JSONObject postRequest(final Uri uri, final Uri.Builder params) throws JSONException {
        HttpURLConnection httpConnection = null;
        try {
            Log.d(TAG, "postRequest invoked");

            Log.d(TAG, String.format("endpoint: %s", uri));
            URL url = new URL(uri.toString());
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setRequestProperty("Cookies", "PHPSESSID=ad6081qpihsb9en1nr9nivbkl3");
            httpConnection.setRequestProperty("SDK-Version", "4.0.0");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);

            String query = params.build().getEncodedQuery();
            Log.d(TAG, String.format("parameter: %s", query));
            OutputStream outputStream = httpConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            writer.write(query);
            writer.flush();
            writer.close();
            outputStream.close();

            return parse(httpConnection);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject(String.format("{\"exception\":\"%s\"}", e.getMessage()));
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private JSONObject parse(HttpURLConnection httpURLConnection) throws JSONException {
        BufferedReader bufferedReader;
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, String> holder = new HashMap<>();
        JSONObject response = new JSONObject();
        String output;

        try {
            response.put("statusCode", httpURLConnection.getResponseCode());
            response.put("responseMessage", httpURLConnection.getResponseMessage());
            response.put("responseBody", getResponseBody(httpURLConnection));
            Log.d(TAG, String.format("code: %s - %s body - %s", response.getString("statusCode"),response.getString("responseMessage"), response.getString("responseBody")));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, String.format("response: %s", stringBuilder));
            return new JSONObject(String.format("{\"exception\":\"%s\"}", e.getMessage()));
        }
    }

    public static String getResponseBody(HttpURLConnection conn) {
        BufferedReader br = null;
        StringBuilder body = null;
        String line = "";
        try {
            br = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            body = new StringBuilder();
            while ((line = br.readLine()) != null)
                body.append(line);
            return body.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
}