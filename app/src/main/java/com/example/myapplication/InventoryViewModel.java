package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryViewModel extends AndroidViewModel {
    private InventoryDbHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private MutableLiveData<Map<String, Integer>> inventoryQuantitiesLiveData;
    private final Map<String, Double> cogValues = new HashMap<>();
    private MutableLiveData<Map<String, Double>> cogValuesLiveData = new MutableLiveData<>();
    private static final String PROCESSED_CSV_KEY = "processed_csv";
    private final Set<String> uploadedCsvFiles = new HashSet<>();
    private static final String UPLOADED_CSV_FILES_KEY = "uploaded_csv_files";
    private ExecutorService executorService;

    public InventoryViewModel(Application application) {
        super(application);
        dbHelper = InventoryDbHelper.getInstance(application);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        inventoryQuantitiesLiveData = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
        cogValuesLiveData = new MutableLiveData<>();
        checkAndProcessCsv();
        loadInitialInventory();
        loadProcessedCsvFileNames();

    }

    private void checkAndProcessCsv() {
        boolean isCsvProcessed = sharedPreferences.getBoolean(PROCESSED_CSV_KEY, false);
        if (!isCsvProcessed) {
            // Removed the direct call to processCsvFile
            sharedPreferences.edit().putBoolean(PROCESSED_CSV_KEY, true).apply();
        }
        loadCogValuesFromDb();
    }
    @SuppressLint("Range")
    private void loadCogValuesFromDb() {
        // Load COG values from the database and post to cogValuesLiveData
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT item, cog_value FROM cog_values", null);
            HashMap<String, Double> cogValues = new HashMap<>();
            while (cursor.moveToNext()) {
                String item = cursor.getString(cursor.getColumnIndex("item"));
                double cogValue = cursor.getDouble(cursor.getColumnIndex("cog_value"));
                cogValues.put(item, cogValue);
            }
            cursor.close();
            cogValuesLiveData.postValue(cogValues);
        }).start();
    }
    public LiveData<Map<String, Integer>> getInventoryQuantitiesLiveData() {
        return inventoryQuantitiesLiveData;
    }
    @SuppressLint("Range")
    public HashMap<String, String> loadAllEditTextValues() {
        HashMap<String, String> values = new HashMap<>();
        executeDbQuery("SELECT item, quantity FROM inventory", null, cursor -> {
            while (cursor.moveToNext()) {
                String item = cursor.getString(cursor.getColumnIndex("item"));
                String editTextValue = String.valueOf(cursor.getInt(cursor.getColumnIndex("quantity")));
                values.put(item, editTextValue);
            }
        });
        return values;
    }


    public void processCsvFile(Uri fileUri, Context context) {
        String fileName = fileUri.getLastPathSegment();
        if (uploadedCsvFiles.contains(fileName)) {
            Toast.makeText(context, "File already processed.", Toast.LENGTH_SHORT).show();
            return;
        }
        executorService.submit(() -> {
            try (InputStream inputStream = getApplication().getContentResolver().openInputStream(fileUri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String headerLine = reader.readLine(); // Read the header line
                String delimiter = determineDelimiter(headerLine);
                String[] headers = headerLine.split(delimiter); // Split the header line to find column indexes

                Log.d("InventoryViewModel", "Header columns: " + Arrays.toString(headers));

                // Assuming the "Item Description" is the third column and "Usage Per $10,000 (Cases)" is the fourth
                int itemDescriptionIndex = 2;
                int usagePer10kIndex = 3;

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(delimiter); // Split line using the determined delimiter
                    if (tokens.length > Math.max(itemDescriptionIndex, usagePer10kIndex)) {
                        String item = tokens[itemDescriptionIndex].trim().replace("\"", ""); // Remove quotes if present
                        double usagePer10k = Double.parseDouble(tokens[usagePer10kIndex].trim().replace("\"", ""));
                        cogValues.merge(item, usagePer10k, Double::sum); // Merge and sum if item already exists
                    }
                }
                // Save the values to the database
                saveCogValuesToDb();
                uploadedCsvFiles.add(fileName);
                saveProcessedCsvFileNames();
            } catch (Exception e) {
                Log.e("InventoryViewModel", "Error processing CSV file", e);
            }
        });
    }
    private void loadProcessedCsvFileNames() {
        String csvFiles = sharedPreferences.getString(UPLOADED_CSV_FILES_KEY, "");
        Collections.addAll(uploadedCsvFiles, csvFiles.split(","));
    }

    public Set<String> getProcessedCsvFileNames() {
        return uploadedCsvFiles;
    }

    private int findColumnIndex(String[] headers, String columnName) {
        for (int i = 0; i < headers.length; i++) {
            if (columnName.equalsIgnoreCase(headers[i].trim().replace("\"", ""))) {
                return i;
            }
        }
        return -1; // Column not found
    }

    private String cleanString(String input) {
        return input.trim().replace("\"", ""); // Remove quotes and trim spaces
    }


    private double averageValues(double oldValue, double newValue) {
        return (oldValue + newValue) / 2.0; // Basic average calculation
    }

    private void saveProcessedCsvFileNames() {
        String csvFiles = String.join(",", uploadedCsvFiles);
        sharedPreferences.edit().putString(UPLOADED_CSV_FILES_KEY, csvFiles).apply();
    }

    private static String determineDelimiter(String headerLine) {
        if (headerLine.contains(",")) {
            Log.d("InventoryViewModel", "Detected delimiter: ");
            return ",";
        } else if (headerLine.contains(";")) {
            return ";";
        } else if (headerLine.contains("\t")) {
            return "\t";
        } else if (headerLine.contains("|")) {
            return "|";
        }
        return ",";
    }

    public Set<String> getUploadedCsvFiles() {
        return uploadedCsvFiles;
    }

    private void saveCogValuesToDb() {
        if (!cogValues.isEmpty()) {
            double divisor = uploadedCsvFiles.size(); // Number of CSV files uploaded
            cogValues.forEach((item, value) -> {
                ContentValues contentValues = new ContentValues();
                contentValues.put("item", item);
                contentValues.put("cog_value", value / divisor); // Average the value
                executeDbUpdate("cog_values", contentValues);
            });
        }
    }

    public MutableLiveData<Map<String, Double>> getCogValuesLiveData() {
        return cogValuesLiveData;
    }

    @SuppressLint("Range")
    public Map<String, Double> loadAllInventoryItemQuantities() {
        ConcurrentHashMap<String, Double> inventoryQuantities = new ConcurrentHashMap<>();
        executeDbQuery("SELECT item, quantity FROM inventory", null, cursor -> {
            while (cursor.moveToNext()) {
                String item = cursor.getString(cursor.getColumnIndex("item"));
                Double quantity = cursor.getDouble(cursor.getColumnIndex("quantity"));
                inventoryQuantities.put(item, quantity);
            }
        });
        return inventoryQuantities;
    }
    public void saveCogValue(String item, double value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("item", item);
        contentValues.put("cog_value", value);
        executeDbUpdate("cog_values", contentValues);
    }

    private void executeDbQuery(String query, String[] selectionArgs, DbQueryConsumer consumer) {
        executorService.submit(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase(); // Get database instance
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(query, selectionArgs);
                consumer.accept(cursor);
            } catch (Exception e) {
                Log.e("InventoryViewModel", "Database query error", e);
            } finally {
                if (cursor != null) {
                    cursor.close(); // Close only the cursor, not the database
                }
                // Do not close the database here
            }
        });
    }

    private void executeDbUpdate(String tableName, ContentValues contentValues) {
        executorService.submit(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase(); // Get database instance
            try {
                db.insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            } catch (Exception e) {
                Log.e("InventoryViewModel", "Database update error", e);
            } finally {
                // Do not close the database here
            }
        });
    }

    @FunctionalInterface
    interface DbQueryConsumer {
        void accept(Cursor cursor);
    }
    @SuppressLint("Range")
    private void loadInitialInventory() {
        executeDbQuery("SELECT item, quantity FROM inventory", null, (Cursor cursor) -> {
            Map<String, Integer> inventoryQuantities = new HashMap<>();
            while (cursor.moveToNext()) {
                String item = cursor.getString(cursor.getColumnIndex("item"));
                Integer quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                Log.d("InventoryViewModel", "Item: " + item + ", Quantity: " + quantity);
                inventoryQuantities.put(item, quantity);
            }
            inventoryQuantitiesLiveData.postValue(inventoryQuantities);
        });
    }


    @SuppressLint("Range")
    public void updateInventoryQuantities(Map<String, Double> newQuantities) {
        executorService.submit(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Map.Entry<String, Double> entry : newQuantities.entrySet()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("item", entry.getKey());
                    contentValues.put("quantity", entry.getValue()); // Correctly save as REAL
                    db.insertWithOnConflict("inventory", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                }
                db.setTransactionSuccessful();
                loadInventoryQuantities();
            } catch (Exception e) {
                Log.e("InventoryViewModel", "Error updating inventory quantities", e);
            } finally {
                db.endTransaction();
                loadInventoryQuantities(); // Make sure to reload the data
            }
        });
    }
    private void executeDbOperation(Runnable operation) {
        executorService.submit(operation);
    }

    @SuppressLint("Range")
    public void loadInventoryQuantities() {
        // Load inventory quantities and update LiveData
        executeDbOperation(() -> {
            Map<String, Integer> inventoryQuantities = new HashMap<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = null;
            try {
                cursor = db.rawQuery("SELECT item, quantity FROM inventory", null);
                while (cursor.moveToNext()) {
                    String item = cursor.getString(cursor.getColumnIndex("item"));
                    int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                    inventoryQuantities.put(item, quantity);
                }
            } catch (Exception e) {
                Log.e("InventoryViewModel", "Error loading inventory quantities", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            inventoryQuantitiesLiveData.postValue(inventoryQuantities);
        });
    }

    public void saveItemQuantity(String item, int quantity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("item", item);
        contentValues.put("quantity", quantity);
        executeDbUpdate("inventory", contentValues);
    }

    private void insertDefaultCogValues() {
        Map<String, Double> defaultCogValues = getDefaultCogValues();
        executorService.submit(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                for (Map.Entry<String, Double> entry : defaultCogValues.entrySet()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("item", entry.getKey());
                    contentValues.put("cog_value", entry.getValue());
                    db.insertWithOnConflict("cog_values", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                }
            } catch (Exception e) {
                Log.e("InventoryViewModel", "Error inserting default COG values", e);
            } finally {
                db.close();
            }
        });
    }

    public void saveInventoryItemQuantitySync(String item, double quantity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("item", item);
        contentValues.put("quantity", quantity);
        executeDbUpdate("inventory", contentValues);
    }

    public void populateDefaultCogValuesIfNeeded() {
        executeDbQuery("SELECT COUNT(*) FROM cog_values", null, (Cursor cursor) -> {
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                insertDefaultCogValues();
            }
        });
    }
    @SuppressLint("Range")
    public double getCogPer10k(String item) {
        final double[] cogValuePer10k = {0.0};
        executeDbQuery("SELECT cog_value FROM cog_values WHERE item = ?", new String[]{item}, cursor -> {
            if (cursor.moveToFirst()) {
                cogValuePer10k[0] = cursor.getDouble(cursor.getColumnIndex("cog_value"));
            }
        });
        return cogValuePer10k[0];
    }

    @SuppressLint("Range")
    public Set<String> getAllInventoryItems() {
        Set<String> allItems = new HashSet<>();
        executeDbQuery("SELECT DISTINCT item FROM inventory", null, (Cursor cursor) -> {
            while (cursor.moveToNext()) {
                allItems.add(cursor.getString(cursor.getColumnIndex("item")));
            }
        });
        return allItems;
    }


    @SuppressLint("Range")
    public LiveData<Map<String, Double>> loadCogValues() {
        MutableLiveData<Map<String, Double>> cogValuesLiveData = new MutableLiveData<>();
        executorService.submit(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = null;
            try {
                cursor = db.rawQuery("SELECT item, cog_value FROM cog_values", null);
                Map<String, Double> cogValues = new HashMap<>();
                while (cursor.moveToNext()) {
                    String item = cursor.getString(cursor.getColumnIndex("item"));
                    double cogValue = cursor.getDouble(cursor.getColumnIndex("cog_value"));
                    cogValues.put(item, cogValue);
                }
                cogValuesLiveData.postValue(cogValues);
            } catch (Exception e) {
                Log.e("InventoryViewModel", "Database query error", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
        return cogValuesLiveData;
    }


    private Map<String, Double> getDefaultCogValues() {
        String[] produceItems = new String[]{
                "Beef", "HotDogs", "Bacon", "CheeseSliced", "CheeseCurds",
                "Lettuce", "Tomatoes", "Onions", "GreenPeppers", "JalapenoPeppers",
                "Mushrooms", "Pickles", "Relish", "MilkshakeBase", "Strawberries",
                "Bananas", "A1Sauce", "HotSauce", "BBQSauce", "ReesesCups", "WhippedCream", "Simple Syrup", "Chocolate Fudge", "Caramel", "Peanut Butter", "Sea Salt Flakes (SPC)", "Mayonnaise", "Ketchup (Cryovac)", "Coke Bottle", "Iced Tea Bottle", "Sprite Bottle", "Vitamin Water (Lemon)", "Vitamin Water (Purple)", "Dasani Water", "Rootbeer Bottle", "Gingerale Bottle", "BIB Coke", "BIB Diet Coke", "BIB Iced Tea", "BIB Root Beer", "BIB Sprite", "BIB Fruitopia", "BIB Fanta (Orange)", "Small Gloves", "Medium Gloves", "Large Gloves", "Extra Large Gloves", "Hairnets", "Beardnets", "AFVT (Veggie Wash)", "2-in-1 Floor CleanerDegreaser", "2-in-1 Peroxide/Multi-surface", "Clorox Bleach", "Stainless Steel Polish",
                "Ketchup (Packets)", "Mustard (Cryovac)", "Oreo Pieces", "Salt (bag)", "Peanut Trays", "Drink Carriers", "6lb Paper Bag", "12lb Paper Bag", "20lb Paper Bag", "Delivery Bags (Twisted Bag)", "Straws", "9oz Fry Cup", "12oz Fry Cup", "24oz Fry Cup", "16oz Milkshake Cup", "16oz Bubble Lid", "21oz Drink Cup", "21oz Lid", "Poutine Hinged Bowls", "Aluminum Bowls", "Gravy Bowl", "Gravy Lid", "Napkins", "Bacon Paper", "Aluminum Foil", "Patty Paper", "Cajun Spice", "2oz Souffle Cups", "2oz Souffle Lids", "Garbage Bags", "Hygiene Disposal Bags", "Baby Table Pads", "K5 Sanitizer Pills", "Kayquat Sanitizer", "Dish Sink Detergent",
                "Hand Soap", "Green Pads", "Filter Paper (Fryers", "Sanitizer Cloths", "Toilet Paper", "Brown Paper (Hands)"
        };

        Map<String, Double> defaultValues = new HashMap<>();
        for (String item : produceItems) {
            defaultValues.put(item, 0.0); // Default value
        }
        return defaultValues;
    }
}

