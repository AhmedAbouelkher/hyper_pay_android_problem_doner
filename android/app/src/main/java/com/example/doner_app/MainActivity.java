package com.example.doner_app;


import android.content.ComponentName;
import android.content.Intent;
import com.oppwa.mobile.connect.checkout.dialog.CheckoutActivity;
import com.oppwa.mobile.connect.checkout.meta.CheckoutSettings;
import com.oppwa.mobile.connect.exception.PaymentError;
import com.oppwa.mobile.connect.payment.BrandsValidation;
import com.oppwa.mobile.connect.payment.CheckoutInfo;
import com.oppwa.mobile.connect.payment.ImagesRequest;
import com.oppwa.mobile.connect.provider.Connect;
import com.oppwa.mobile.connect.provider.ITransactionListener;
import com.oppwa.mobile.connect.provider.Transaction;
import com.oppwa.mobile.connect.provider.TransactionType;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import io.flutter.embedding.android.FlutterActivity;
import androidx.annotation.NonNull;
import java.util.LinkedHashSet;
import java.util.Set;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;


public class MainActivity extends FlutterActivity implements MethodChannel.Result, ITransactionListener {

    private String CHANNEL = "Hyperpay.demo.fultter/channel";
    private String type = "";
    private String checkoutid = "";
    private MethodChannel.Result Result;
    private String mode = "";
    String brands = "";

//  private String language = "ar_AR";

    private Handler handler = new Handler(Looper.getMainLooper());


    @Override
    public void success(final Object result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Result.success(result);

            }
        });
    }

    @Override
    public void error(final String errorCode, final String errorMessage, final Object errorDetails) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Result.error(errorCode, errorMessage, errorDetails);
            }
        });
    }

    @Override
    public void notImplemented() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Result.notImplemented();
            }
        });
    }



    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, MethodChannel.Result result) {

                        Result = result;
                        if (call.method.equals("gethyperpayresponse")) {

                            type = call.argument("type");
                            mode = call.argument("mode");
                            brands = call.argument("brand");
                            // language = call.argument("language");
                            checkoutid = call.argument("checkoutid");

                             openCheckoutUI(checkoutid);

                        } 
                    }
                });
    }

    private void openCheckoutUI(String checkoutId) {
        Set<String> paymentBrands = new LinkedHashSet<String>();
        if (brands.equals("mada")) {
            paymentBrands.add("MADA");
        } else {
            paymentBrands.add("VISA");
            paymentBrands.add("MASTER");

        }

        CheckoutSettings checkoutSettings = new CheckoutSettings(checkoutId, paymentBrands, Connect.ProviderMode.TEST)
                .setShopperResultUrl("com.example.doner_app://result");

        if (mode.equals("LIVE")) {
            checkoutSettings = new CheckoutSettings(checkoutId, paymentBrands, Connect.ProviderMode.LIVE)
                    .setShopperResultUrl("com.example.doner_app://result");
        }

        ComponentName componentName = new ComponentName(getPackageName(), CheckoutBroadcastReceiver.class.getName());
//    checkoutSettings.setLocale(language);
        /* Set up the Intent and start the checkout activity. */
        Intent intent = checkoutSettings.createCheckoutActivityIntent(this, componentName);

        startActivityForResult(intent, CheckoutActivity.REQUEST_CODE_CHECKOUT);

    }






    @Override
    public void brandsValidationRequestSucceeded(BrandsValidation brandsValidation) {

    }

    @Override
    public void brandsValidationRequestFailed(PaymentError paymentError) {

    }

    @Override
    public void imagesRequestSucceeded(ImagesRequest imagesRequest) {

    }

    @Override
    public void imagesRequestFailed() {

    }

    @Override
    public void paymentConfigRequestSucceeded(CheckoutInfo checkoutInfo) {

    }

    @Override
    public void paymentConfigRequestFailed(PaymentError paymentError) {

    }

    @Override
    public void transactionCompleted(Transaction transaction) {


        if (transaction.getTransactionType() == TransactionType.SYNC) {

            success("SYNC");

        } else {
            /* wait for the callback in the s */

            Uri uri = Uri.parse(transaction.getRedirectUrl());

            Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent2);
        }

    }

    @Override
    public void transactionFailed(Transaction transaction, PaymentError paymentError) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case CheckoutActivity.RESULT_OK:
                /* transaction completed */

                Transaction transaction = data.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_TRANSACTION);

                /* resource path if needed */
                String resourcePath = data.getStringExtra(CheckoutActivity.CHECKOUT_RESULT_RESOURCE_PATH);

                if (transaction.getTransactionType() == TransactionType.SYNC) {
                    /* check the result of synchronous transaction */

                    success("SYNC");

                } else {
                    /* wait for the asynchronous transaction callback in the onNewIntent() */
                }

                break;
            case CheckoutActivity.RESULT_CANCELED:
                /* shopper canceled the checkout process */

                Toast.makeText(getBaseContext(), "canceled", Toast.LENGTH_LONG).show();

                error("2", "Canceled", "");

                break;
            case CheckoutActivity.RESULT_ERROR:
                /* error occurred */

                PaymentError error = data.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_ERROR);

                Toast.makeText(getBaseContext(), "error", Toast.LENGTH_LONG).show();

                Log.e("errorrr", String.valueOf(error.getErrorInfo()));

                Log.e("errorrr2", String.valueOf(error.getErrorCode()));

                Log.e("errorrr3", String.valueOf(error.getErrorMessage()));

                Log.e("errorrr4", String.valueOf(error.describeContents()));

                error("3", "Checkout Result Error", "");

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getScheme().equals("com.example.doner_app")) {

            success("success");

        }
    }
}
