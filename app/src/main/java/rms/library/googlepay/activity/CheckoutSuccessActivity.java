/*
 * Copyright 2023 Razer Merchant Services.
 */

package rms.library.googlepay.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import rms.library.googlepay.databinding.ActivityCheckoutSuccessBinding;

public class CheckoutSuccessActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityCheckoutSuccessBinding layoutBinding = ActivityCheckoutSuccessBinding.inflate(getLayoutInflater());
    setContentView(layoutBinding.getRoot());
  }
}