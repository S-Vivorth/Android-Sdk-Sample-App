# Android Sdk sample app

> Instructions for the billers to integrate bill24 android sdk.

## Table of contents
* Compatiblility
* Setup
* Usage
* Contact

## Compatiblility
- Support from android version 5+ (Android sdk 21+)

## Setup
* **First Step**

Download bill24 sdk with aar file extension from "--url--"

* **Second Step**

Find libs folder in your app directory or create new one if you could not find libs folder and drag bill24 sdk with aar file extention
to this folder

* **Third Step**

Add require dependencies:
```gradle
    dependencies {

        //Bill24 Android SDK and require dependencies
        api files('libs/bill24sdk.aar')
        implementation("com.squareup.okhttp3:okhttp:4.9.3")
        api 'com.marozzi.roundbutton:round-button:1.0.7'
        implementation 'com.github.zcweng:switch-button:0.0.3@aar'
        implementation ('io.socket:socket.io-client:2.0.1') {
            // excluding org.json which is provided by Android
            exclude group: 'org.json', module: 'json'
        }
        annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
        implementation 'com.github.bumptech.glide:glide:4.12.0'
        annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'

    }
```

## Usage

In your checkout screen:

**Kotlin**
```kotlin
fun createSaleOrder(){

        //create okhttp instance if you use okhttp for networking
        val client = OkHttpClient()

        // biller's payload will be given to Sdk
        val orderDetailsJson = """
            {
        "customer": {
            "customer_ref": "C00001",
            "customer_email": "example@gmail.com",
            "customer_phone": "010801252",
            "customer_name": "test customer"
        },
        "billing_address": {
            "province": "Phnom Penh",
            "country": "Cambodia",
            "address_line_2": "string",
            "postal_code": "12000",
            "address_line_1": "No.01, St.01, Toul Kork"
        },
        "description": "Extra note",
        "language": "km",
        "order_items": [
            {
                "item_name": "Men T-Shirt",
                "quantity": 1,
                "price": 1,
                "amount": 1,
                "item_ref": "P1001",
                "discount_amount": 0
            }
        ],
        "payment_success_url": "/payment/success",
        "currency": "USD",
        "amount": 1,
        "pay_later_url": "/payment/paylater",
        "shipping_address": {
            "province": "Phnom Penh",
            "country": "Cambodia",
            "address_line_2": "string",
            "postal_code": "12000",
            "address_line_1": "No.01, St.01, Toul Kork"
        },
        "order_ref":"${binding.orderRef.text}",
        "payment_fail_url": "payment/fail",
        "payment_cancel_url": "payment/cancel",
        "continue_shopping_url": "payment/cancel"
    }
        """.trimIndent()

        // convert payload string to JsonObject
        val jsonObject = JSONObject(orderDetailsJson)
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

        // unique token will be given by Bill24 to the biller once registered
        val request = Request.Builder().header("token",token)
            .header("Accept","application/json")
            .url("$url/order/init")
            .post(jsonObject.toString().toRequestBody(mediaTypeJson))
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Exception","$e")
            }

            override fun onResponse(call: Call, response: Response) {
                val responsed = response
                val responses = responsed.body!!.string()
                val checkoutObject = JSONObject(responses)
                if (responsed.code != 200) {
                    Toast.makeText(applicationContext,JSONObject(responses).optString("message")
                    ,Toast.LENGTH_SHORT).show()
                }
                else{
                    sessionId = checkoutObject.optJSONObject("data")!!.optString("session_id")
                    runOnUiThread {
                        kotlin.run {
                            callSdk(checkoutObject)
                        }
                    }
                }
            }
        })
    }

    fun callSdk(checkoutObject: JSONObject){
        if (sessionId != "null") {

            // supportFragmentManager can be get from the activity
            // pay_later() is activity which will be navigated to, when user choose pay later option
            // sessionId is the string which get from checkout response
            // clientID is the string given by bill24 to the biller
            // activity is the current activity
            // payment_succeeded is the activity which will be navigated to, when the payment is succeeded
            // language is the string that specify the language. Language can be "en" or "kh" only.
            // continue_shopping is the activity which will be navigated to, when user press continue shopping button

            val bottomsheetFrag = bottomSheetController(supportFragmentManager = supportFragmentManager,paylater = pay_later(),
                sessionId = sessionId,
                clientID = clientId, activity = this@checkOutKotlin
                ,payment_succeeded = payment_succeeded(),language = language,continue_shopping =  homescreen())

            // to call sdk screen
            bottomsheetFrag.show(supportFragmentManager,"bottomsheetsdk")
        }

        else{
            Toast.makeText(applicationContext,checkoutObject.optString("message"),Toast.LENGTH_SHORT)
                .show()
        }
    }
```

**Java**

```java
    void createSaleOrder(){

        //create okhttp instance
        OkHttpClient client = new OkHttpClient();

        // biller's payload will be given to Sdk
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
                "                \"item_name\": \"Men T-Shirt\",\n" +
                "                \"quantity\": 1,\n" +
                "                \"price\": 1,\n" +
                "                \"amount\": 1,\n" +
                "                \"item_ref\": \"P1001\",\n" +
                "                \"discount_amount\": 0\n" +
                "            }\n" +
                "        ],\n" +
                "        \"payment_success_url\": \"/payment/success\",\n" +
                "        \"currency\": \"USD\",\n" +
                "        \"amount\": 1,\n" +
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
                "        \"continue_shopping_url\": \"payment/cancel\"\n" +
                "    }",orderRef.getText()) ;
                            try {
                            // convert payload string to JsonObject
                            JSONObject jsonObject = new JSONObject(orderDetailsJson);
                            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

                            // unique token will be given by Bill24 to the biller once registered
                            Request request = new Request.Builder().header("token",token)
                            .header("Accept","application/json")
                            .url(url+"/order/init")
                            .post(RequestBody.create(mediaType,jsonObject.toString()))
                            .build();
                            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String responses = response.body().string();
                        try {
                        JSONObject checkOutObject  = new JSONObject(responses);
                        sessionId = checkOutObject.optJSONObject("data").optString("session_id");
                        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callSdk(checkOutObject);
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
    void callSdk(JSONObject checkOutObject){
        if (sessionId != "null") {
            // supportFragmentManager can be get from the activity
            // pay_later() is activity which will be navigated to, when user choose pay later option
            // sessionId is the string which get from checkout response
            // clientID is the string given by bill24 to the biller
            // activity is the current activity
            // payment_succeeded is the activity which will be navigated to, when the payment is succeeded
            // language is the string that specify the language. Language can be "en" or "kh" only.
            // continue_shopping is the activity which will be navigated to, when user press continue shopping button

            bottomSheetController bottomSheetController =
                    new bottomSheetController(getSupportFragmentManager(),
                            new pay_later(),
                            sessionId,
                            clientId,
                            checkOutJava.this,
                            new payment_succeeded(),
                            language,
                            new homescreen(),
                            environtment);

            // to call sdk screen
            bottomSheetController.show(getSupportFragmentManager(), "bottomsheet");
        } else {
            Toast.makeText(getApplicationContext(), checkOutObject.optString("message"), Toast.LENGTH_SHORT).show();
        }
    }
```
Where:
* **getSupportFragmentManager** or **supportFragmentManager** can be get from the activity
* **pay_later()** is activity which will be navigated to, when user choose pay later option
* **sessionId** is the string which get from checkout response
* **clientID** is the string given by bill24 to the biller
* **activity** is the current activity
* **payment_succeeded** is the activity which will be navigated to, when the payment is succeeded
* **language** is the string that specify the language. Language can be "en" or "kh" only.
* **continue**_shopping is the activity which will be navigated to, when user press continue shopping button
* **environment** is the environment that you want to use. Environtment can be "uat" or "prod" only.

Congratulations, you are ready to use the SDK.

## Contact
Created by
[@bill24 team](vivorth.san@ubill24.com) - feel free to contact me!















