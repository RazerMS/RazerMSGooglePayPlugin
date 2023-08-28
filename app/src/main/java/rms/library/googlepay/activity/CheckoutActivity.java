/*
 * Copyright 2023 Razer Merchant Services.
 */

package rms.library.googlepay.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.button.ButtonConstants;
import com.google.android.gms.wallet.button.ButtonOptions;
import com.google.android.gms.wallet.button.PayButton;

import org.json.JSONException;
import org.json.JSONObject;

import rms.library.googlepay.R;
import rms.library.googlepay.WebActivity;
import rms.library.googlepay.databinding.ActivityCheckoutBinding;
import rms.library.googlepay.util.PaymentsUtil;
import rms.library.googlepay.viewmodel.CheckoutViewModel;

/**
 * Checkout implementation for the app
 */
public class CheckoutActivity extends AppCompatActivity {

    public JSONObject paymentInput = new JSONObject();

    private static final int LOAD_TRANSACTION_DATA_REQUEST_CODE = 998;

    private CheckoutViewModel model;

    private ProgressBar pbLoading;
    private PayButton googlePayButton;

    // Handle potential conflict from calling loadPaymentData.
    ActivityResultLauncher<IntentSenderRequest> resolvePaymentForResult = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                Log.e("logGooglePay" , "resolvePaymentForResult");
                switch (result.getResultCode()) {
                    case Activity.RESULT_OK:
                        Intent resultData = result.getData();
                        if (resultData != null) {
                            PaymentData paymentData = PaymentData.getFromIntent(result.getData());
                            if (paymentData != null) {
                                handlePaymentSuccess(paymentData);
                            }
                        }
                        break;

                    case Activity.RESULT_CANCELED:
                        // The user cancelled the payment attempt
                        break;
                }
            });

    /**
     * Initialize the Google Pay API on creation of the activity
     *
     * @see Activity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUi();

        // Check Google Pay availability
        model = new ViewModelProvider(this).get(CheckoutViewModel.class);
        model.canUseGooglePay.observe(this, this::setGooglePayAvailable);
    }

    private void initializeUi() {

        // Use view binding to access the UI elements
        ActivityCheckoutBinding layoutBinding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(layoutBinding.getRoot());

        pbLoading = layoutBinding.pbLoading;

        // The Google Pay button is a layout file – take the root view
        googlePayButton = layoutBinding.googlePayButton;
        try {
            // TODO: Choose your preferred Google Pay button : https://developers.google.com/pay/api/android/guides/resources/update-to-new-payment-button
            googlePayButton.initialize(
                    ButtonOptions.newBuilder()
                            .setButtonTheme(ButtonConstants.ButtonTheme.DARK)
                            .setButtonType(ButtonConstants.ButtonType.PAY)
                            .setCornerRadius(99)
                            .setAllowedPaymentMethods(PaymentsUtil.getAllowedPaymentMethods().toString())
                            .build()
            );
            googlePayButton.setOnClickListener(this::requestPayment);
        } catch (JSONException e) {
            // Keep Google Pay button hidden (consider logging this to your app analytics service)
        }

    }

    /**
     * If isReadyToPay returned {@code true}, show the button and hide the "checking" text.
     * Otherwise, notify the user that Google Pay is not available. Please adjust to fit in with
     * your current user flow. You are not required to explicitly let the user know if isReadyToPay
     * returns {@code false}.
     *
     * @param available isReadyToPay API response.
     */
    private void setGooglePayAvailable(boolean available) {
        if (available) {
            googlePayButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.google_pay_status_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    public void requestPayment(View view) {

        Log.e("logGooglePay" , "requestPayment");

        // Disables the button to prevent multiple clicks.
        googlePayButton.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        long totalPriceCents = 101;

        final Task<PaymentData> task = model.getLoadPaymentDataTask(totalPriceCents);

        task.addOnCompleteListener(completedTask -> {
            Log.e("logGooglePay" , "addOnCompleteListener");

            if (completedTask.isSuccessful()) {
                handlePaymentSuccess(completedTask.getResult());
            } else {
                Exception exception = completedTask.getException();
                if (exception instanceof ResolvableApiException) {
                    PendingIntent resolution = ((ResolvableApiException) exception).getResolution();
                    resolvePaymentForResult.launch(new IntentSenderRequest.Builder(resolution).build());

                } else if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    handleError(apiException.getStatusCode(), apiException.getMessage());

                } else {
                    handleError(CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
                            " exception when trying to deliver the task result to an activity!");
                }
            }

            // Re-enables the Google Pay payment button.
            googlePayButton.setClickable(true);
        });
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see <a href="https://developers.google.com/pay/api/android/reference/
     * object#PaymentData">PaymentData</a>
     */
    private void handlePaymentSuccess(PaymentData paymentData) {

        pbLoading.setVisibility(View.VISIBLE);
        Log.e("logGooglePay" , "handlePaymentSuccess");

        final String paymentInfo = paymentData.toJson();

        try {
            // TODO: Send the payment info e.g. (all info are compulsory) :
            paymentInput.put("orderId", "order123"); // Unique payment order id
            paymentInput.put("amount", "1.01"); // Payment amount
            paymentInput.put("currency", "MYR"); // Payment currency
            paymentInput.put("billName", "Cat Steven"); // Payer name
            paymentInput.put("billEmail", "cat.steven@gmail.com"); // Payer email
            paymentInput.put("billPhone", "601234567890"); // Payer phone
            paymentInput.put("billDesc", "Google Pay Testing"); // Payment description
            paymentInput.put("merchantId", "xxxxxxxxx"); // Your registered merchantId
            paymentInput.put("verificationKey", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"); // Your registered verificationKey

            /*
            TODO: Follow Google’s instructions to request production access for your app: https://developers.google.com/pay/api/android/guides/test-and-deploy/request-prod-access
            *
             Choose the integration type Gateway when prompted, and provide screenshots of your app for review.
             After your app has been approved, test your integration in production by setting the environment to GooglePayEnvironment.Production
             Then launching Google Pay from a signed, release build of your app.
             Remember to use your live mode verificationKey & merchantId. Set isSandbox = false for production environment.
             */
            paymentInput.put("isSandbox", "true"); // True = Testing ; False = Production

            JSONObject paymentInputObj = paymentInput;

            String paymentInput = paymentInputObj.toString();

            runOnUiThread(() -> {
                Intent i = new Intent(CheckoutActivity.this, WebActivity.class); // Redirect To WebActivity (RMS library)
                i.putExtra("paymentInput", paymentInput);
                i.putExtra("paymentInfo", paymentInfo);
                startActivityForResult(i, LOAD_TRANSACTION_DATA_REQUEST_CODE);
            });

        } catch (JSONException e) {
//            Log.e(Constants.LOG_GOOGLE_PAY, "handlePaymentSuccess JSONException: " + e);
        }
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode holds the value of any constant from CommonStatusCode or one of the
     *                   WalletConstants.ERROR_CODE_* constants.
     * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/wallet/
     * WalletConstants#constant-summary">Wallet Constants Library</a>
     */
    private void handleError(int statusCode, @Nullable String message) {
//        Log.e("loadPaymentData failed", String.format(Locale.getDefault(), "Error code: %d, Message: %s", statusCode, message));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        CharSequence response;

        if (requestCode == LOAD_TRANSACTION_DATA_REQUEST_CODE) {

            pbLoading.setVisibility(View.GONE);

            switch (resultCode) {
                
                case AppCompatActivity.RESULT_OK:

                    // Response Success CallBack
                    response = data.getStringExtra("response");

                    Toast toast = Toast.makeText(this, response, Toast.LENGTH_LONG);
                    toast.show();

                    startActivity(new Intent(this, CheckoutSuccessActivity.class));

                    break;

                case AppCompatActivity.RESULT_CANCELED:
                    // The user cancelled the payment attempt
                    // Response Error CallBack
                    response = data.getStringExtra("response");

                    Toast toast2 = Toast.makeText(this, response, Toast.LENGTH_LONG);
                    toast2.show();
                    break;

                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    handleError(status.getStatusCode() , status.getStatusMessage());
                    break;
            }
        }
    }

}
