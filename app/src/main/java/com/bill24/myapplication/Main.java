package com.bill24.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Main extends AppCompatActivity {
    String language = "en";
    String sessionId;
    Switch switchLanguage;
    Button button;
    EditText orderRef;
    // environment must be "uat" or "prod" only
    String environtment = "uat";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switchLanguage = findViewById(R.id.switchLanguage);
        orderRef = findViewById(R.id.orderRef);
        switchLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    language = "kh";
                }
                else{
                    language = "en";
                }
            }
        });
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttpClient client = new OkHttpClient();
                String orderDetailsJson = String.format("{\n" +
                        "        \"customer\": {\n" +
                        "            \"customer_ref\": \"C00001\",\n" +
                        "            \"customer_email\": \"example@gmail.com\",\n" +
                        "            \"customer_phone\": \"010801252\",\n" +
                        "            \"customer_name\": \"test customer\"\n" +
                        "        },\n" +
                        "        \"billing_address\": {\n" +
                        "            \"province\": \"Phnom Penh\",\n" +
                        "            \"country\": \"Cambodia\",\n" +
                        "            \"address_line_2\": \"string\",\n" +
                        "            \"postal_code\": \"12000\",\n" +
                        "            \"address_line_1\": \"No.01, St.01, Toul Kork\"\n" +
                        "        },\n" +
                        "        \"description\": \"Extra note\",\n" +
                        "        \"language\": \"km\",\n" +
                        "        \"order_items\": [\n" +
                        "            {\n" +
                        "                \"consumer_code\": \"001\",\n" +
                        "                \"amount\": 1.2\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"payment_success_url\": \"/payment/success\",\n" +
                        "        \"currency\": \"USD\",\n" +
                        "        \"amount\": 1.2,\n" +
                        "        \"pay_later_url\": \"/payment/paylater\",\n" +
                        "        \"shipping_address\": {\n" +
                        "            \"province\": \"Phnom Penh\",\n" +
                        "            \"country\": \"Cambodia\",\n" +
                        "            \"address_line_2\": \"string\",\n" +
                        "            \"postal_code\": \"12000\",\n" +
                        "            \"address_line_1\": \"No.01, St.01, Toul Kork\"\n" +
                        "        },\n" +
                        "        \"order_ref\":\"%s\",\n" +
                        "        \"payment_fail_url\": \"payment/fail\",\n" +
                        "        \"payment_cancel_url\": \"payment/cancel\",\n" +
                        "        \"continue_shopping_url\": \"http://localhost:8090/order\"\n" +
                        "    }",orderRef.getText()) ;
                try {
                    JSONObject jsonObject = new JSONObject(orderDetailsJson);
                    MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                    Request request = new Request.Builder().header("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJQYXltZW50R2F0ZXdheSIsInN1YiI6IkVEQyIsImhhc2giOiJCQ0ZEQzE1MC0zMjRGLTQzRjQtQkQ3Qi0zMTVGN0Y5NDM3NDAifQ.OZ9AqnbRucNmVlJzQt6kqkRjDDDPjMAN81caYwqKuX4")
                            .header("Accept","application/json")
                            .url("http://203.217.169.102:50209/order/create/v1")
                            .post(RequestBody.create(mediaType,jsonObject.toString()))
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            String responses = response.body().string();
                            Log.d("responses",responses);
                            try {
                                JSONObject checkOutObject  = new JSONObject(responses);
                                sessionId = checkOutObject.optJSONObject("data").optString("session_id");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (sessionId != "null") {
//                                            paymentSdk paymentSdk =
//                                                    new paymentSdk(getSupportFragmentManager(),
//                                                            new pay_later(),
//                                                            sessionId,
//                                                            "fmDJiZyehRgEbBJTkXc7AQ==",
//                                                            Main.this,
//                                                            new payment_succeeded(),
//                                                            language,
//                                                            new homescreen(),
//                                                            environtment);
//                                            paymentSdk.show(getSupportFragmentManager(),"sdk_bottomsheet");
                                        }
                                        else{
                                            Toast.makeText(getApplicationContext(),checkOutObject.optString("message"),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}

