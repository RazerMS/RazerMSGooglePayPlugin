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
            implementation 'com.github.RazerMS:RazerMSGooglePayPlugin:1.0.2'
            implementation 'com.google.android.gms:play-services-wallet:19.1.0'
            ...

        }
        
## Prepare Merchant Info

In file PaymentUtils.java

   private static JSONObject getMerchantInfo() throws JSONException {
           JSONObject merchantInfo = new JSONObject();
           merchantInfo.put("merchantId", "BCRXXXXXXXXXXXX5");
           merchantInfo.put("merchantName", "Google Bussiness Name");
           merchantInfo.put("merchantOrigin", "origin");
           return merchantInfo;
       }
       
## Prepare Payment Gateway Tokenainzation Name

In file Constants.java

       public static final String PAYMENT_GATEWAY_TOKENIZATION_NAME = "molpay";

       /**
        * Custom parameters required by the processor/gateway.
        * In many cases, your processor / gateway will only require a gatewayMerchantId.
        * Please refer to your processor's documentation for more information. The number of parameters
        * required and their names vary depending on the processor.
        *
        * @value #PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS
        */
       public static final HashMap<String, String> PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS =
               new HashMap<String, String>() {{
                   put("gateway", PAYMENT_GATEWAY_TOKENIZATION_NAME);
                   put("gatewayMerchantId", "molpay");
                   // Your processor may require additional parameters.
               }};

## Prepare the Payment detail object

        public JSONObject paymentInput = new JSONObject();

        // Mandatory String. Payment values.
        paymentInput.put("orderId", "order111");
        paymentInput.put("amount", "1.10");
        paymentInput.put("currency", "MYR");

        // Optional, but required payment values. User input will be required when values not passed.
        paymentInput.put("billName", "Masso Dasuki");
        paymentInput.put("billEmail", "masso@gmail.com");
        paymentInput.put("billPhone", "601234567890");
        paymentInput.put("billDesc", "Google Pay Testing");


        // Mandatory String, Merchant Config
        paymentInput.put("merchantId", "merchant_Dev");
        paymentInput.put("verificationKey", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        paymentInput.put("isSandbox", "false");

    
## Start Razer GooglePay payment module using AsyncTask

        
        public class CheckoutActivity extends AppCompatActivity {

        // Arbitrarily-picked constant integer you define to track a request for payment data activity.
        private static final int LOAD_TRANSACTION_DATA_REQUEST_CODE = 998;
        ...
        ...
        public JSONObject paymentInput = new JSONObject();
        
        public void requestPayment(View view) {

            // Disables the button to prevent multiple clicks.
            googlePayButton.setClickable(false);

            // The price provided to the API should include taxes and shipping.
            // This price is not displayed to the user.
            long priceCents = Long.parseLong(editAmount.getText().toString().replace(".", ""));
            long shippingCostCents = 10;
            long totalPriceCents = priceCents + shippingCostCents;

            try {

              paymentInput.put("orderId", "order111");
              paymentInput.put("amount", "1.10");
              paymentInput.put("currency", "MYR");
              paymentInput.put("billName", "Masso Dasuki");
              paymentInput.put("billEmail", "masso@gmail.com");
              paymentInput.put("billPhone", "601234567890");
              paymentInput.put("billDesc", "Google Pay Testing");
              paymentInput.put("merchantId", "merchant_Dev");
              paymentInput.put("verificationKey", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
              paymentInput.put("isSandbox", "false");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            final Task<PaymentData> task = model.getLoadPaymentDataTask(totalPriceCents);

            // Shows the payment sheet and forwards the result to the onActivityResult method.
            AutoResolveHelper.resolveTask(task, this, LOAD_PAYMENT_DATA_REQUEST_CODE);
         }
    }


## Create Redirection After Google Pay Successfully Handling Payment

        @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JSONObject paymentInputObj = paymentInput;

        Context context = getApplicationContext();
        CharSequence response = null;
        int duration = Toast.LENGTH_SHORT;

        Log.d(TAG, String.valueOf(requestCode));
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {

                    case AppCompatActivity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        
                        String paymentInfo = handlePaymentSuccess(paymentData);
                        String paymentInput = paymentInputObj.toString();
                        
                        Intent i = new Intent(CheckoutActivity.this, WebActivity.class); // Redirect To WebActivity (this library)
                        i.putExtra("paymentInput", paymentInput);
                        i.putExtra("paymentInfo", paymentInfo);
                        startActivityForResult(i, LOAD_TRANSACTION_DATA_REQUEST_CODE);
                        break;

                    case AppCompatActivity.RESULT_CANCELED:
                        // The user cancelled the payment attempt
                        break;

                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        handleError(status);
                        break;

                }
                break;

            case LOAD_TRANSACTION_DATA_REQUEST_CODE:
                switch (resultCode) {
                    ...
                    ...
                }

                // Re-enables the Google Pay payment button.
                googlePayButton.setClickable(true);
        }
    }





## Handling the response callback

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JSONObject paymentInputObj = paymentInput;

        Context context = getApplicationContext();
        CharSequence response = null;
        int duration = Toast.LENGTH_SHORT;

        Log.d(TAG, String.valueOf(requestCode));
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    ...
                    ...
                }
                break;

            case LOAD_TRANSACTION_DATA_REQUEST_CODE:
                switch (resultCode) {

                    case AppCompatActivity.RESULT_OK:
                        Log.d(TAG, "RESULT_OK");
                        Log.d(TAG, String.valueOf(requestCode));
                        
                        // Response Success CallBack
                        response = data.getStringExtra("response");
                        Toast toast = Toast.makeText(context, response, duration);
                        toast.show();
                        break;

                    case AppCompatActivity.RESULT_CANCELED:
                        // The user cancelled the payment attempt
                        Log.d(TAG, "RESULT_CANCELED");
                        
                        // Response Error CallBack
                        response = data.getStringExtra("response");
                        Toast toast2 = Toast.makeText(context, response, duration);
                        toast2.show();
                        break;

                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        handleError(status);
                        break;

                }

                // Re-enables the Google Pay payment button.
                googlePayButton.setClickable(true);
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
