<!--
 # license: Copyright © 2011-2022 Razer Merchant Services Sdn Bhd. All Rights Reserved. 
 -->
 
![banner RMSxGooglePay](https://user-images.githubusercontent.com/17770615/199203191-891462c9-05b3-4ad5-b2a9-eaa67d873698.png)


# [Mobile Plugin] - RazerMS Google Pay Plugin

This is the complete and functional Razer Merchant Services Google Pay Plugin payment module that is ready to be implemented into Android Studio application project through Maven framework.

## Recommended configurations

    - Android Studio version: Chipmunk | 2021.2.1 Patch 1

    - Minimum target version: Android 8.0 - API Level 26

## Installation Guidance

**Installation**

### Maven

Maven primarily aids in the download of dependencies, which are libraries or JAR files, for Java-based applications. For usage and installation instructions, visit their website. To integrate RazerMS Google Pay Plugin into your Android Studio project using CocoaPods, specify it in your file:

    In file settings.gradle

        pluginManagement {
            repositories {
                ...
                mavenCentral()
            }
        }
        dependencyResolutionManagement {
            ...
            repositories {
                ...
                mavenCentral()
                maven { url 'https://jitpack.io' }
            }
        }
        rootProject.name = "Application Name"
        include ':app'

     In file build.gradle under app folder

        plugins {
            id 'com.android.application'
        }

        android {
            ...

            defaultConfig {
                ...
                minSdk 26
                ...
            }
            ...
        }

        dependencies {
            ...
            implementation 'com.github.RazerMS:RazerMSGooglePayPlugin:1.0.3'
            implementation 'com.google.android.gms:play-services-wallet:19.1.0'
            ...

        }

## Prepare the Payment detail object

        JSONObject paymentInputObj = new JSONObject();

        // Mandatory String. Payment values.
        paymentInputObj.put("orderId", "order111");
        paymentInputObj.put("amount", "1.10");
        paymentInputObj.put("currency", "MYR");

        // Optional, but required payment values. User input will be required when values not passed.
        paymentInputObj.put("billName", "Masso Dasuki");
        paymentInputObj.put("billEmail", "masso@gmail.com");
        paymentInputObj.put("billPhone", "601234567890");
        paymentInputObj.put("billDesc", "Google Pay Testing");


        // Mandatory String, Merchant Config
        paymentInputObj.put("merchantId", "merchant_Dev");
        paymentInputObj.put("verificationKey", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        paymentInputObj.put("isSandbox", "false");

    
## Start Razer GooglePay payment module using AsyncTask

        private void handlePaymentSuccess(@Nullable PaymentData paymentData) {
            final String paymentInfo = paymentData.toJson();

            try {

                JSONObject paymentInputObj = new JSONObject();

                paymentInputObj.put("orderId", "order111");
                paymentInputObj.put("amount", "1.10");
                paymentInputObj.put("currency", "MYR");
                paymentInputObj.put("billName", "Masso Dasuki");
                paymentInputObj.put("billEmail", "masso@gmail.com");
                paymentInputObj.put("billPhone", "601234567890");
                paymentInputObj.put("billDesc", "Google Pay Testing");
                paymentInputObj.put("merchantId", "merchant_Dev");
                paymentInputObj.put("verificationKey", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                paymentInputObj.put("isSandbox", "false");

                String paymentInput = paymentInputObj.toString();

                // Execute the payment using PaymentTaskRunner (AsyncTask Process)
                PaymentTaskRunner runner = new PaymentTaskRunner();
                runner.execute(paymentInput, paymentInfo);

            } catch (JSONException e) {
                throw new RuntimeException("The selected garment cannot be parsed from the list of elements");
            }
        }


## Create PaymentTaskRunner extend by AsyncTask

        private class PaymentTaskRunner extends AsyncTask<String, String, String> {

            private String resp;
            @Override
            protected String doInBackground(String... params) {
                try {
                    RMSGooglePay pay = new RMSGooglePay();
                    Object result = pay.requestPayment(
                            params[0],
                            params[1]
                    );

                    resp = result.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    resp = e.getMessage();
                }
                return resp;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.i("PaymentTaskRunner onPostExecute", "Done");
                processValue(result);
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


## Full Code

        private void handlePaymentSuccess(@Nullable PaymentData paymentData) {
            final String paymentInfo = paymentData.toJson();

            try {

                JSONObject paymentInputObj = new JSONObject();

                paymentInputObj.put("orderId", "order111");
                paymentInputObj.put("amount", "1.10");
                paymentInputObj.put("currency", "MYR");
                paymentInputObj.put("billName", "Masso Dasuki");
                paymentInputObj.put("billEmail", "masso@gmail.com");
                paymentInputObj.put("billPhone", "01234567890");
                paymentInputObj.put("billDesc", "Google Pay Testing");
                paymentInputObj.put("merchantId", "merchant_Dev");
                paymentInputObj.put("verificationKey", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                paymentInputObj.put("isSandbox", "false");

                String paymentInput = paymentInputObj.toString();

                PaymentTaskRunner runner = new PaymentTaskRunner();
                runner.execute(paymentInput, paymentInfo);

            } catch (JSONException e) {
                throw new RuntimeException("The selected garment cannot be parsed from the list of elements");
            }
        }

        private class PaymentTaskRunner extends AsyncTask<String, String, String> {

            private String resp;
            @Override
            protected String doInBackground(String... params) {
                try {
                    RMSGooglePay pay = new RMSGooglePay();
                    Object result = pay.requestPayment(
                            params[0],
                            params[1]
                    );

                    resp = result.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    resp = e.getMessage();
                }
                return resp;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.i("PaymentTaskRunner onPostExecute", "Done");
                processValue(result);
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

## Handling the response callback

    public class CheckoutActivity extends AppCompatActivity {
        ...
        void finishTask(String resutlVal) {

            //Update GUI, show toast, etc..
            // Toast Example
            Toast.makeText(
                    this, getString(R.string.payments_show_name, resutlVal),
                    Toast.LENGTH_LONG).show();
        }
        ...

        private class PaymentTaskRunner extends AsyncTask<String, String, String> {
            ...
        @Override
            protected void onPostExecute(String result) {
                Log.i("PaymentTaskRunner onPostExecute", "Done");
                finishTask(result);
            }
            ...
        }

    }

## Payment results

    =========================================
    Sample transaction result in JSON string:
    =========================================

    {
        amount = "1.10";
        appcode = 179367;
        channel = CREDIT;
        currency = MYR;
        domain = "merchant_Dev";
        orderid = order54;
        paydate = "2022-09-22 17:49:08";
        skey = 54XXXXXXXXXXXXXXXXXXXXXXXXXXd;
        status = 00;
        tranID = 1278569348;
        xdkHTMLRedirection = "xxxxxxxxxx";
    };

    Parameter and meaning:
    
    "status_code" - "00" for Success, "11" for Failed, "22" for *Pending. 
    (*Pending status only applicable to cash channels only)
    "amount" - The transaction amount
    "paydate" - The transaction date
    "order_id" - The transaction order id
    "channel" - The transaction channel description
    "txn_ID" - The transaction id generated by MOLPay
    
    * Notes: You may ignore other parameters and values not stated above

    =====================================
    * Sample error result in JSON string:
    =====================================
    
    {
        "error_code" = A01;
        "error_desc" = "Fail to detokenize Apple Pay Token given";
        status = 0;
    }
    
    Parameter and meaning:
    
    "Fail to detokenize Apple Pay Token given" - Error starting a payment process due to several possible reasons, please contact Razer Merchant Services support should the error persists.
    1) Misconfigure ApplePay setup
    2) API credentials (username, password, merchant id, verify key)
    3) Razer Merchant Services server offline.

## Resources

- GitHub:     https://github.com/RazerMS
- Website:    https://merchant.razer.com/
- Twitter:    https://twitter.com/Razer_MS
- YouTube:    https://www.youtube.com/c/RazerMerchantServices
- Facebook:   https://www.facebook.com/RazerMerchantServices/
- Instagram:  https://www.instagram.com/RazerMerchantServices/


## Support

Submit issue to this repository or email to our support-sa@razer.com

Merchant Technical Support / Customer Care : suppor-sa@razer.com<br>
Sales/Reseller Enquiry : sales-sa@razer<br>
Marketing Campaign : marketing-sa@razer<br>
Channel/Partner Enquiry : channel-sa@razer<br>
Media Contact : media-sa@razer.com<br>
R&D and Tech-related Suggestion : technical-sa@razer.com<br>
Abuse Reporting : abuse-sa@razer.com
