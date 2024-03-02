package com.example.myapplication;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InventoryFragment extends Fragment {
    private InventoryViewModel viewModel;
    private LinearLayout layoutContainer;
    private Map<String, EditText> editTextMap = new LinkedHashMap<>();
    private Map<String, List<String>> categoryItems;
    private LinearLayout forecastContainer;
    private Map<String, EditText> forecastFields = new HashMap<>();

    private double latestForecast;
    private SharedPreferences sharedPreferences;
    private SharedViewModel sharedViewModel;
    private String[] produceItems = new String[]{
            "Beef", "HotDogs", "Bacon", "CheeseSliced", "CheeseCurds",
            "Lettuce", "Tomatoes", "Onion", "GreenPeppers", "JalapenoPeppers",
            "Mushrooms", "Pickles", "Relish", "MilkshakeBase", "Strawberries",
            "Bananas", "A1Sauce", "HotSauce", "BBQSauce", "ReesesCups", "WhippedCream", "Simple Syrup", "Chocolate Fudge", "Caramel", "Peanut Butter", "Sea Salt Flakes (SPC)", "Mayonnaise", "Ketchup (Cryovac)", "Coke Bottle", "Iced Tea Bottle", "Sprite Bottle", "Vitamin Water (Lemon)", "Vitamin Water (Purple)", "Dasani Water", "Rootbeer Bottle", "Gingerale Bottle", "BIB Coke", "BIB Diet Coke", "BIB Iced Tea", "BIB Root Beer", "BIB Sprite", "BIB Fruitopia", "BIB Fanta (Orange)", "Small Gloves", "Medium Gloves", "Large Gloves", "Extra Large Gloves", "Hairnets", "Beardnets", "AFVT (Veggie Wash)", "2-in-1 Floor CleanerDegreaser", "2-in-1 Peroxide/Multi-surface", "Clorox Bleach", "Stainless Steel Polish",
            "Ketchup (Packets)", "Mustard (Cryovac)", "Oreo Pieces", "Salt (bag)", "Peanut Trays", "Drink Carriers", "6lb Paper Bag", "12lb Paper Bag", "20lb Paper Bag", "Delivery Bags (Twisted Bag)", "Straws", "9oz Fry Cup", "12oz Fry Cup", "24oz Fry Cup", "16oz Milkshake Cup", "16oz Bubble Lid", "21oz Drink Cup", "21oz Lid", "Poutine Hinged Bowls", "Aluminum Bowls", "Gravy Bowl", "Gravy Lid", "Napkins", "Bacon Paper", "Aluminum Foil", "Patty Paper", "Cajun Spice", "2oz Souffle Cups", "2oz Souffle Lids", "Garbage Bags", "Hygiene Disposal Bags", "Baby Table Pads", "K5 Sanitizer Pills", "Kayquat Sanitizer", "Dish Sink Detergent",
            "Hand Soap", "Green Pads", "Filter Paper (Fryers", "Sanitizer Cloths", "Toilet Paper", "Brown Paper (Hands)"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel = new InventoryViewModel(requireActivity().getApplication());
        layoutContainer = view.findViewById(R.id.layoutContainer);
        forecastContainer  = view.findViewById(R.id.forecastContainer);



        createForecastInputFields();
        initializeCategoryItems();
        createDynamicInventoryInput();
        loadEditTextValues();
        loadForecastValue();
        return view;
    }

    private String getItemTypeSuffix(String itemName) {
        if (itemName.contains("BOTTLE") || itemName.contains(("Water")) || itemName.contains(("Chocolate")) || itemName.contains(("Caramel")) || itemName.contains(("Syrup")))
            return "(in bottles)";
        if (itemName.contains("Pickles") || itemName.contains(("Relish")) || itemName.contains(("Peanut")) && itemName!= "Peanuts Salted" && itemName!= "Peanut Trays" && itemName != "Peanut Oil")
            return "(in jars)";
        if (itemName.contains("Whipped Cream")) return "(in cans)";
        if (itemName.contains("Gloves") || itemName.contains(("Aluminum")) || itemName.contains(("Poutine")) || itemName.contains(("Cajun")) || itemName.contains(("Garbage")))
            return "(boxes)";
        if (itemName.contains("Bag") && itemName != "SALT Bag" || itemName.contains(("Oreo")) || itemName.contains(("Mayonnaise")) || itemName.contains(("LIDS")) || itemName.contains(("CUPS")) || itemName.contains(("Cups")) || itemName.contains(("Lids")))
            return "(sleeves)";
        return "";
    }

    private void initializeCategoryItems() {
        categoryItems = new LinkedHashMap<>();

        categoryItems.put("Walk-In", Arrays.asList("Beef", "Hot Dogs", "Bacon", "BOTTLE Coke Classic", "BOTTLE Iced Tea", "BOTTLE Sprite", "BOTTLE Diet Coke",
                "BOTTLE Vitamin Water (Multi-V Lemonade)", "BOTTLE Vitamin Water (xxx)", "Water", "BOTTLE Root Beer", "BOTTLE Ginger Ale",
                "Relish", "Pickles", "Cheese", "Green Pepper", "Jalapenos", "Cheese Curds", "Mushrooms", "Tomatoes",
                "Milkshake Base",  "Reese's Cups", "Whipped Cream", "Strawberry", "Bananas", "SAUCE A1", "Sauce Hot", "SAUCE BBQ", "Onion", "Lettuce"));

        categoryItems.put("BOH", Arrays.asList("Syrup Chocolate Fudge", "Syrup Caramel", "Syrup Sweetener", "Oreo Cookie Pieces",
                "Hairnets Nylon", "Beardnets", "Salt Sea", "Syrup Peanut Butter", "Mayonnaise", "Patty Paper",
                "Brown Bags 6 LB", "Brown Bags 12 LB",  "Mart Shopping Bags", "Brown Bags 20 LB", "SALT Bag",
                "Souffle CUPS 2 oz", "Souffle LIDS 2 Oz", "Peanut Trays", "KETCHUP Crayovac", "Mustard", "Peanuts Salted",
                "Gloves Vinyl Powdered Small", "Gloves Vinyl Powdered Medium", "Gloves Vinyl Powdered Large", "Gloves Vinyl Powdered Extra Large",
                "Straws", "Napkins", "Bacon Paper", "Poutine Bowls", "Foil Pan 7",
                "Drink CUPS 21 oz", "LIDS Drink 22 oz",  "Drink CUPS 32 oz", "LIDS Drink 32 oz", "Milkshake CUPS 16 oz", "LIDS Milkshake 16 oz",
                "Fry Cups 9 oz", "Fry Cups 12 oz", "Fry Cups 24 oz", "Gravy Bowl 8 Oz",  "Lids Gravy Bowl 8 oz",
                "BIB Coke Classic", "BIB Coke Diet", "BIB Tea Iced", "BIB Root Beer", "BIB Sprite", "BIB Fruitopia", "BIB Orange Fanta",
                "Toilet Paper", "Cup Trays",  "Hand Towels (800 FT)"));
        categoryItems.put("Line", Arrays.asList("Cajun Spice", "Poutine Mix",  "Aluminum Foil"));
        categoryItems.put("Chemicals", Arrays.asList("Towel Wipes", "Fryer Filter Paper", "Garbage Bags Black Extra Strong 42 x 48", "Scour Pads 6x9 IN",
                "Stainless Steel Polish", "Hand Soap Foam", "Peroxide Multi-Surface", "Floor Cleaner No Rinse", "Sanitizer Bags",
                "Dish Soap", "Sanitizer K5", "Bathroom Cleaner (Peroxide Multi-surface)", "Degreaser Heavy Duty Bag",  "Disinfectant Bleach", "Wash Antimicrobial Fruit & Vegetable", "Window Cleaner"));

        categoryItems.put("Lobby", Arrays.asList("Peanut Oil", "Potatoes", "VINEGAR Malt", "VINEGAR White",
                "Ketchup Packet SS", "Black Pepper", "Salt Packets",
                "Forks", "Knives"));
        // ... Add other categories like "Drinks", "Chemicals", "Lobby" ...
    }
    private void createDynamicInventoryInput() {
        for (Map.Entry<String, List<String>> entry : categoryItems.entrySet()) {
            String categoryText = entry.getKey().trim();
            List<String> items = entry.getValue();

            if (!categoryText.isEmpty()) {
                // Create a centered and bold TextView for the category header
                TextView categoryHeader = new TextView(getContext());
                categoryHeader.setText(categoryText);
                categoryHeader.setTextSize(24); // Increase the text size (adjust as needed)
                categoryHeader.setTypeface(null, Typeface.BOLD); // Make it bold
                categoryHeader.setGravity(Gravity.CENTER); // Center the text
                layoutContainer.addView(categoryHeader);
            }

            for (String item : items) {
                String itemText = item.trim();

                if (!itemText.isEmpty()) {
                    LinearLayout itemLayout = new LinearLayout(getContext());
                    itemLayout.setOrientation(LinearLayout.HORIZONTAL);

                    // Get the appropriate suffix for the item type
                    String suffix = getItemTypeSuffix(itemText);

                    TextView itemLabel = new TextView(getContext());
                    itemLabel.setText(itemText + " " + suffix);  // Include the suffix in the TextView

                    if (!itemLabel.getText().toString().trim().isEmpty()) {
                        EditText itemInput = new EditText(getContext());
                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        itemInput.setLayoutParams(params);
                        itemInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                        itemLayout.addView(itemLabel);
                        itemLayout.addView(itemInput);

                        layoutContainer.addView(itemLayout);

                        editTextMap.put(item, itemInput);  // Map the EditText to the item for easy retrieval
                    }
                }
            }
        }
    }

    private void saveInventoryValues() {
        HashMap<String, Double> newQuantities = new HashMap<>(); // Use Double instead of Integer
        for (Map.Entry<String, EditText> entry : editTextMap.entrySet()) {
            String item = entry.getKey();
            EditText editText = entry.getValue();
            String valueStr = editText.getText().toString();
            if (!valueStr.isEmpty()) {
                try {
                    double quantity = Double.parseDouble(valueStr); // Parse as double
                    newQuantities.put(item, quantity);
                    saveInventoryItemQuantity(item, quantity);
                } catch (NumberFormatException e) {
                    Log.e("InventoryFragment", "Error parsing quantity for item " + item, e);
                }
            }
        }
        // Assume viewModel has a method to update quantities as Double
        viewModel.updateInventoryQuantities(newQuantities);
    }

    public Map<String, Double> getDailyForecast() {
        Map<String, Double> dailyForecast = new HashMap<>();
        for (Map.Entry<String, EditText> entry : forecastFields.entrySet()) {
            String day = entry.getKey();
            EditText editText = entry.getValue();
            String forecastValueStr = editText.getText().toString();
            double forecastValue = forecastValueStr.isEmpty() ? 0.0 : Double.parseDouble(forecastValueStr);
            dailyForecast.put(day, forecastValue);
        }
        return dailyForecast;
    }


    private void loadEditTextValues() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Check if the key is part of the inventory items to ensure it's a numeric value
            if (editTextMap.containsKey(key)) { // Assuming editTextMap contains keys for all inventory items
                try {
                    // Parse the value as a double only if it's a String and related to inventory
                    if (value instanceof String) {
                        double quantity = Double.parseDouble((String) value);
                        EditText editText = editTextMap.get(key);
                        if (editText != null) {
                            editText.setText(String.format(Locale.getDefault(), "%.2f", quantity));
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.e("InventoryFragment", "Error parsing quantity for item " + key, e);
                    // Handle the error or ignore this entry
                }
            }
        }
    }

    private void createForecastInputFields() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        forecastContainer.removeAllViews(); // Clear existing views if any
        forecastContainer.setOrientation(LinearLayout.HORIZONTAL); // Set the main container to horizontal

        // Adjust this based on how many days you want to forecast and the current day
        String[] weekdays;
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.WEDNESDAY) {
            weekdays = new String[]{"Thursday Forecast", "Friday Forecast", "Saturday Forecast", "Sunday Forecast"};
        } else {
            weekdays = new String[]{"Monday Forecast", "Tuesday Forecast", "Wednesday Forecast"};
        }

        for (String day : weekdays) {
            // Create a vertical LinearLayout for each day
            LinearLayout dayLayout = new LinearLayout(getContext());
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams dayLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            dayLayout.setLayoutParams(dayLayoutParams);

            TextView dayLabel = new TextView(getContext());
            dayLabel.setText(day);
            dayLabel.setTextSize(16); // Adjust text size as needed

            EditText editText = new EditText(getContext());
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setHint("Enter " + day.split(" ")[0] + " forecast"); // Hint example: "Enter Monday forecast"

            // Add the TextView and EditText to the dayLayout
            dayLayout.addView(dayLabel);
            dayLayout.addView(editText);

            // Add the dayLayout to the main forecastContainer
            forecastContainer.addView(dayLayout);

            // Optionally, keep a reference to the EditTexts if needed for later
            forecastFields.put(day, editText); // Assuming forecastFields is a Map to track EditTexts
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        updateForecast();
        saveForecastValue();
        saveEditTextValues();
        saveInventoryValues();
        showEmptyItemsPopup();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadForecastValue();
        loadEditTextValues();
    }

    private void updateForecast() {
        double totalForecast = 0;
        for (EditText forecastInput : forecastFields.values()) {
            String forecastValueStr = forecastInput.getText().toString();
            if (!forecastValueStr.isEmpty()) {
                try {
                    double forecastValue = Double.parseDouble(forecastValueStr);
                    totalForecast += forecastValue;
                } catch (NumberFormatException e) {
                    Log.e("InventoryFragment", "Error parsing forecast value: " + forecastValueStr, e);
                }
            }
        }

        // Save the aggregated total forecast
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("TotalForecast", (float) totalForecast);
        editor.apply();

        sharedViewModel.setForecast(totalForecast); // Assuming SharedViewModel can store a double
    }

    private void saveForecastValue() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        double totalForecast = 0;
        for (Map.Entry<String, EditText> entry : forecastFields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getText().toString();
            double dailyForecast = value.isEmpty() ? 0 : Double.parseDouble(value);
            editor.putString(key, Double.toString(dailyForecast)); // Save individual forecasts
            totalForecast += dailyForecast;
        }
        editor.putFloat("TotalForecast", (float) totalForecast); // Save the sum of all forecasts
        editor.apply();
    }

    private void loadForecastValue() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        for (String key : forecastFields.keySet()) {
            String forecastValue = sharedPreferences.getString(key, "");
            EditText forecastInput = forecastFields.get(key);
            if (forecastInput != null) {
                forecastInput.setText(forecastValue);
            }
        }
    }

    private void saveEditTextValues() {
        for (Map.Entry<String, EditText> entry : editTextMap.entrySet()) {
            String item = entry.getKey();
            EditText editText = entry.getValue();
            String value = editText.getText().toString();
            if (!value.isEmpty()) {
                try {
                    double quantity = Double.parseDouble(value); // Parse as double
                    viewModel.saveInventoryItemQuantitySync(item, quantity); // Adjust method to accept double
                } catch (NumberFormatException e) {
                    Log.e("InventoryFragment", "Error parsing quantity for item " + item, e);
                }
            }
        }
    }

    private void showEmptyItemsPopup() {
        StringBuilder emptyItemsBuilder = new StringBuilder();
        boolean hasEmptyItems = false;

        for (Map.Entry<String, EditText> entry : editTextMap.entrySet()) {
            String item = entry.getKey();
            EditText editText = entry.getValue();
            String value = editText.getText().toString();

            // Check if the EditText is empty or the quantity is 0
            if (value.isEmpty() || Double.parseDouble(value) == 0.0) {
                if (hasEmptyItems) {
                    emptyItemsBuilder.append("\n"); // Add a newline character for formatting
                }
                emptyItemsBuilder.append("* ").append(item);
                hasEmptyItems = true;
            }
        }

        if (hasEmptyItems) {
            // Use AlertDialog to display the message
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Items Not Entered");
            builder.setMessage("The following items have not been entered yet:\n" + emptyItemsBuilder.toString());
            builder.setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            //dialog.show();
        }
    }

    public void saveInventoryItemQuantity(String item, double quantity) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(item, Double.toString(quantity));
        editor.apply();
    }
}

