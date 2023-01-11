package rms.library.googlepay;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import rms.library.googlepay.Helper.RMSGooglePay;
import rms.library.googlepay.model.Transaction;

public class WebActivity extends AppCompatActivity {

    private WebView wvGateway;
    public Transaction transaction = new Transaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Intent intent = getIntent();
        String paymentInput = intent.getStringExtra("paymentInput");
        String paymentInfo = intent.getStringExtra("paymentInfo");
        Log.d(TAG, String.format("paymentInput: %s - paymentInfo - %s", paymentInput, paymentInfo));

        // Transcation model from paymentInput
        JSONObject paymentInputObj = null;
        try {
            paymentInputObj = new JSONObject(paymentInput);
            transaction.setVkey(paymentInputObj.getString("verificationKey"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        wvGateway = findViewById(R.id.webView);
        wvGateway.setBackgroundColor(Color.WHITE);
        wvGateway.getSettings().setDomStorageEnabled(true);
        wvGateway.getSettings().setJavaScriptEnabled(true);
        wvGateway.getSettings().setAllowUniversalAccessFromFileURLs(true);
        wvGateway.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wvGateway.getSettings().setSupportMultipleWindows(true);


        PaymentThread paymentThread = new PaymentThread();
        paymentThread.setValue(paymentInput, paymentInfo); // set value
        Thread thread = new Thread(paymentThread);
        thread.start();
        try {
            thread.join();
            JSONObject paymentResult = new JSONObject(new JSONObject(paymentThread.getValue()).getString("responseBody"));
            onRequestData(paymentResult);
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }

//        PaymentTaskRunner runner = new PaymentTaskRunner();
//        runner.execute(paymentInput, paymentInfo);
    }

    private void onStartTimOut() {
        long minTimeOut = 180000; // 3 minutes
        long interval = 6000;
        final String[] queryResultStr = {null};
        Log.d(TAG, String.format("onStartTimOut"));
        final String[] trasactionJsonStr = {null};

        // Query Transaction ID for every 6 second in 3 minutes
        CountDownTimer countDownTimer = new CountDownTimer(minTimeOut, interval) {

            // Query Transaction ID for every 6 second in 3 minutes
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, String.format("onTick: %d", millisUntilFinished / interval));

                JSONObject transactionObject = new JSONObject();
                try {
                    transactionObject.put("txID", transaction.getTxID());
                    transactionObject.put("amount", transaction.getAmount());
                    transactionObject.put("merchantId", transaction.getDomain());
                    transactionObject.put("verificationKey", transaction.getVkey());
                    trasactionJsonStr[0] = transactionObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                QueryResultThread queryResultThread = new QueryResultThread();
                queryResultThread.setValue(trasactionJsonStr[0]); // set value
                Thread thread = new Thread(queryResultThread);
                thread.start();
                try {
                    thread.join();
                    queryResultStr[0] = queryResultThread.getValue();

                    try {
                        JSONObject queryResultObj = new JSONObject(queryResultStr[0]);
                        String responseBody = queryResultObj.getString("responseBody");
                        JSONObject responseBodyObj = new JSONObject(responseBody);

                        // If StatCode
                        if (responseBodyObj.has("StatCode")){
                            String statCodeValue = responseBodyObj.getString("StatCode");
                            if (statCodeValue != "22" ) {
                                //If not pending
                                Intent intent = new Intent();
//                                intent.putExtra("response", String.valueOf(queryResultStr[0]));
                                intent.putExtra("response", String.valueOf(responseBodyObj));
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, String.format("onTick QueryResultThread thread.join()"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish");
                try {
                    JSONObject queryResultObj = new JSONObject(queryResultStr[0]);
                    String responseBody = queryResultObj.getString("responseBody");
                    JSONObject responseBodyObj = new JSONObject(responseBody);
                    Intent intent = new Intent();
                    intent.putExtra("response", String.valueOf(queryResultStr[0]));
                    // If Fail
                    if (!responseBodyObj.has("StatCode")){
                        setResult(RESULT_CANCELED, intent);
                    } else {
                        setResult(RESULT_OK, intent);
                    }
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        countDownTimer.start();
    }

    private void onLoadHtmlWebView(String plainHtml) {
        String encodedHtml = Base64.encodeToString(plainHtml.getBytes(), Base64.NO_PADDING);
        Log.d(TAG, String.format("onLoadHtmlWebView: %s", encodedHtml));

        wvGateway.setVisibility(View.VISIBLE);
        wvGateway.loadData(encodedHtml, "text/html", "base64");
    }

    public void onRequestData(JSONObject response) {
        Log.d(TAG, "onGetPaymentRequestForm onComplete invoked");

        try {
            if (response.has("error_code") && response.has("error_desc")) {
//                mMobileSDKResult.onResult(String.format("{ code:%s, message:%s }", response.getString("error_code"), response.getString("error_desc")));
                Intent intent = new Intent();
                String strResponse = response.toString();
                intent.putExtra("response", strResponse);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
            if (response.has("TxnID")) {
//                AppData.getInstance().setTxnID(response.getString("TxnID"));
                try {
                    transaction.setTxID(response.getString("TxnID"));
                    transaction.setDomain(response.getString("MerchantID"));
                    transaction.setAmount(response.getString("TxnAmount"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (response.has("TxnData") && !response.has("pInstruction")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onStartTimOut();
                        }
                    });

                    JSONObject txnData = response.getJSONObject("TxnData");
                    Log.d(TAG, "TxnData: " + txnData);
                    StringBuilder html = new StringBuilder();
                    html.append(String.format("<form id='prForm' action='%s' method='%s'>\n",
                            txnData.getString("RequestURL"),
                            txnData.getString("RequestMethod"))
                    );
                    if (txnData.has("AppDeepLinkURL")) {
//                        AppData.getInstance().setRedirectAppUrl(txnData.getString("AppDeepLinkURL"));
                    }
                    if (txnData.has("RequestData")) {

                        if (txnData.get("RequestData") instanceof JSONObject) {
                            JSONObject requestData = txnData.getJSONObject("RequestData");
                            Log.d(TAG, "RequestData: " + requestData);

                            Iterator<String> keys = requestData.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                Log.d(TAG, "Key: " + key);
                                if (requestData.get(key) instanceof JSONObject) {
                                    Log.d(TAG, "param : " + requestData.get(key));
                                } else {
                                    if (requestData.has("checkoutUrl")) {
//                                        AppData.getInstance().setRedirectAppUrl(requestData.getString("checkoutUrl"));
                                    }
                                    html.append(String.format("<input type='hidden' name='%s' value='%s'>\n", key, requestData.getString(key)));
                                }
                            }
                        }
                    }

                    html.append("</form>");
                    html.append("<script> document.getElementById('prForm').submit();</script>");
                    Log.d(TAG, "HTML: " + html);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onLoadHtmlWebView(html.toString());
                        }
                    });
                } else {
                    Log.d(TAG, "pInstruction found : ");
                    Intent intent = new Intent();
                    String strResponse = response.toString();
                    intent.putExtra("response", strResponse);
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            } else {
                Log.d(TAG, "TxID not found ! : ");
                Intent intent = new Intent();
                String strResponse = response.toString();
                intent.putExtra("response", strResponse);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void processValue(String result) throws JSONException {
        //Update GUI, show toast, etc..
        JSONObject responseBodyObj = new JSONObject(new JSONObject(result).getString("responseBody"));
        onRequestData(responseBodyObj);
    }

    public static class PaymentThread implements Runnable {
        private volatile String resp;
        private String paymentInput;
        private String  paymentInfo;

        public String getValue() {
            return resp;
        }

        public void setValue(String paymentInput, String  paymentInfo) {
            this.paymentInput = paymentInput;
            this.paymentInfo = paymentInfo;
        }

        @Override
        public void run() {
            RMSGooglePay pay = new RMSGooglePay();
            JSONObject result = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                result = (JSONObject) pay.requestPayment(paymentInput, paymentInfo);
            }
            resp = result.toString();
            Log.d(TAG, String.format("PaymentThread response: %s", resp));
        }
    }


    public static class QueryResultThread implements Runnable {
        private volatile String resp;
        private String transaction;

        public String getValue() {
            return resp;
        }

        public void setValue(String transaction) {
            this.transaction = transaction;
        }

        @Override
        public void run() {
            RMSGooglePay pay = new RMSGooglePay();
            JSONObject result = (JSONObject) pay.queryPaymentResult(transaction);
            resp = result.toString();
        }
    }


    //DEPRECATED
    @SuppressLint("StaticFieldLeak")
    private class PaymentTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected String doInBackground(String... params) {
            try {
                RMSGooglePay pay = new RMSGooglePay();
                JSONObject result = (JSONObject) pay.requestPayment(
                        params[0],
                        params[1]
                );
                resp = result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            Log.i("PaymentTaskRunner doInBackground", resp);
            return resp;
        }
        @Override
        protected void onPostExecute(String result) {
            Log.i("PaymentTaskRunner onPostExecute", result);
            try {
                processValue(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void onPreExecute() {
            Log.i("PaymentTaskRunner onPreExecute", "preExecute");
        }
        @Override
        protected void onProgressUpdate(String... text) {
            Log.e("PaymentTaskRunner onProgressUpdate", "progressUpdate");
        }
    }

    private class QueryResultTaskRunner extends AsyncTask<String, String, String> {
        private String resp;
        @Override
        protected String doInBackground(String... params) {
            try {
                RMSGooglePay pay = new RMSGooglePay();
                JSONObject result = (JSONObject) pay.queryPaymentResult(
                        params[0]
                );

                resp = result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            Log.i("PaymentTaskRunner doInBackground", resp);
            return resp;
        }
        @Override
        protected void onPostExecute(String result) {
            Log.i("PaymentTaskRunner onPostExecute", result);
            try {
                processValue(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void onPreExecute() {
            Log.i("PaymentTaskRunner onPreExecute", "preExecute");
        }
        @Override
        protected void onProgressUpdate(String... text) {
            Log.e("PaymentTaskRunner onProgressUpdate", "progressUpdate");
        }
    }
}