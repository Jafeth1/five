package com.example.myapplication;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class PowerBIService {

    private final OkHttpClient client = new OkHttpClient();
    private static final String APPLICATION_ID = "YOUR_APPLICATION_ID";
    private static final String DIRECTORY_ID = "YOUR_DIRECTORY_ID";
    private static final String BEARER_TOKEN = "YOUR_BEARER_TOKEN";

    public void fetchCOGReport() {
        HttpUrl url = HttpUrl.parse("https://api.powerbi.com/v1.0/myorg/reports")
                .newBuilder()
                .addQueryParameter("applicationId", APPLICATION_ID)
                .addQueryParameter("directoryId", DIRECTORY_ID)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of("Authorization", "Bearer " + BEARER_TOKEN,
                        "Content-Type", "application/json"))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the failure, e.g., log the error or notify the user
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // Handle unsuccessful response, e.g., log the error or notify the user
                } else {
                    String responseData = response.body().string();
                    processCOGData(responseData);
                }
            }
        });
    }

    private void processCOGData(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray reports = jsonObject.getJSONArray("value");
            for (int i = 0; i < reports.length(); i++) {
                JSONObject report = reports.getJSONObject(i);
                if (report.getString("name").equals("COG")) {
                    JSONArray items = report.getJSONArray("items");
                    for (int j = 0; j < items.length(); j++) {
                        JSONObject item = items.getJSONObject(j);
                        String inventoryCategory = item.getString("Inventory");
                        String itemName = item.getString("Name");
                        double usagePer10kCases = item.getDouble("Usage Per $10,000 (Cases)");
                        // Implement your logic to process each item
                    }
                }
            }
        } catch (Exception e) {
            // Handle parsing or calculation errors
        }
    }
}
