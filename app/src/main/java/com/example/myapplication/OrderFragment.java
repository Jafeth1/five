package com.example.myapplication;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.InventoryViewModel;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderFragment extends Fragment {
    private InventoryViewModel viewModel;
    private HashMap<String, TextView> orderTextViews;
    private LinearLayout layoutContainer;

    private SharedViewModel sharedViewModel;
    private Map<String, Double> latestInventoryQuantities = new HashMap<String, Double>();
    private Map<String, Double> latestCogValues = new HashMap<>();
    private Set<String> processedItems = new HashSet<>();
    private MediatorLiveData<Pair<Map<String, Integer>, Map<String, Double>>> combinedData = new MediatorLiveData<>();



    private Set<String> AllItems = new LinkedHashSet<>(Arrays.asList(
            // Walk-In
            "Beef", "Hot Dogs", "Bacon", "BOTTLE Coke Classic", "BOTTLE Iced Tea", "BOTTLE Sprite", "BOTTLE Diet Coke",
            "BOTTLE Vitamin Water (Multi-V Lemonade)", "BOTTLE Vitamin Water (xxx)", "Water", "BOTTLE Root Beer", "BOTTLE Ginger Ale",
            "Relish", "Pickles", "Cheese", "Green Pepper", "Jalapenos", "Cheese Curds", "Mushrooms", "Tomatoes",
            "Milkshake Base",  "Reese's Cups", "Whipped Cream", "Strawberry", "Bananas", "SAUCE A1", "Sauce Hot", "SAUCE BBQ", "Onion", "Lettuce",

            // BOH
             "Syrup Chocolate Fudge", "Syrup Caramel", "Syrup Sweetener", "Oreo Cookie Pieces",
            "Hairnets Nylon", "Beardnets", "Salt Sea", "Syrup Peanut Butter", "Mayonnaise", "Patty Paper",
            "Brown Bags 6 LB", "Brown Bags 12 LB",  "Mart Shopping Bags", "Brown Bags 20 LB", "SALT Bag",
            "Souffle CUPS 2 oz", "Souffle LIDS 2 Oz", "Peanut Trays", "KETCHUP Crayovac", "Mustard", "Peanuts Salted",
            "Gloves Vinyl Powdered Small", "Gloves Vinyl Powdered Medium", "Gloves Vinyl Powdered Large", "Gloves Vinyl Powdered Extra Large",
            "Straws", "Napkins", "Bacon Paper", "Poutine Bowls", "Foil Pan 7",
            "Drink CUPS 21 oz", "LIDS Drink 22 oz",  "Drink CUPS 32 oz", "LIDS Drink 32 oz", "Milkshake CUPS 16 oz", "LIDS Milkshake 16 oz",
            "Fry Cups 9 oz", "Fry Cups 12 oz", "Fry Cups 24 oz", "Gravy Bowl 8 Oz",  "Lids Gravy Bowl 8 oz",
            "BIB Coke Zero","BIB Coke Classic", "BIB Coke Diet", "BIB Tea Iced", "BIB Root Beer", "BIB Sprite", "BIB Fruitopia", "BIB Orange Fanta",
            "Toilet Paper", "Cup Trays",  "Hand Towels (800 FT)",

            // Line
            "Cajun Spice", "Poutine Mix",  "Aluminum Foil",

            // Chemicals
            "Towel Wipes", "Fryer Filter Paper", "Garbage Bags Black Extra Strong 42 x 48", "Scour Pads 6x9 IN",
            "Stainless Steel Polish", "Hand Soap Foam", "Peroxide Multi-Surface", "Floor Cleaner No Rinse", "Sanitizer Bags",
            "Dish Soap", "Sanitizer K5", "Bathroom Cleaner (Peroxide Multi-surface)", "Degreaser Heavy Duty Bag",  "Disinfectant Bleach", "Wash Antimicrobial Fruit & Vegetable", "Window Cleaner",

            // Lobby
            "Peanut Oil", "Potatoes", "VINEGAR Malt", "VINEGAR White",
            "Ketchup Packet SS", "Black Pepper", "Salt Packets",
            "Forks", "Knives"

    ));

    private final Set<String> produceItems = new HashSet<>(Arrays.asList(
            "Beef", "Hot Dogs", "Bacon", "CheeseSliced", "CheeseCurds",
            "Lettuce", "Tomatoes", "Onion", "GreenPeppers", "JalapenoPeppers",
            "Mushrooms", "Pickles", "Relish", "MilkshakeBase", "Strawberries",
            "Bananas", "A1Sauce", "HotSauce", "BBQSauce", "ReesesCups", "WhippedCream"
    ));
    private String[] nonProduceItems = new String[]{
            "Lettuce", "Mushrooms", "Pickles", "Relish", "MilkshakeBase", "Strawberries",
            "Bananas", "A1Sauce", "HotSauce", "BBQSauce", "ReesesCups", "WhippedCream", "Simple Syrup", "Chocolate Fudge", "Caramel", "Peanut Butter", "Salt Sea", "Mayonnaise", "Ketchup (Cryovac)", "Coke Bottle", "Iced Tea Bottle", "Sprite Bottle", "Vitamin Water (Lemon)", "Vitamin Water (Purple)", "Dasani Water", "Rootbeer Bottle", "Gingerale Bottle", "BIB Coke", "BIB Diet Coke", "BIB Coke Zero", "BIB Iced Tea", "BIB Root Beer", "BIB Sprite", "BIB Fruitopia", "BIB Fanta (Orange)", "Small Gloves", "Medium Gloves", "Large Gloves", "Extra Large Gloves", "Hairnets", "Beardnets", "AFVT (Veggie Wash)", "Floor Cleaner No Rinse", "2-in-1 Peroxide/Multi-surface", "Clorox Bleach", "Stainless Steel Polish",
            "Ketchup (Packets)", "Mustard (Cryovac)", "Oreo Pieces", "Salt (bag", "Peanut Trays", "Drink Carriers", "6lb Paper Bag", "12lb Paper Bag", "20lb Paper Bag", "Delivery Bags (Twisted Bag)", "Straws", "9oz Fry Cup", "12oz Fry Cup", "24oz Fry Cup", "16oz Milkshake Cup", "16oz Bubble Lid", "21oz Drink Cup", "21oz Lid", "Poutine Hinged Bowls", "Aluminum Bowls", "Gravy Bowl", "Gravy Lid", "Napkins", "Bacon Paper", "Aluminum Foil", "Patty Paper", "Cajun Spice", "2oz Souffle Cups", "2oz Souffle Lids", "Garbage Bags", "Hygiene Disposal Bags", "Baby Table Pads", "K5 Sanitizer Pills", "Kayquat Sanitizer", "Dish Sink Detergent",
            "Hand Soap", "Green Pads", "Filter Paper (Fryers", "Sanitizer Cloths", "Toilet Paper", "Brown Paper (Hands)"
    };

    // Define the items that are considered as cases and their maximum quantity before an entire box

    private HashMap<String, Integer> caseItemsMaxQuantity = new HashMap<String, Integer>() {{
        put("Chocolate Fudge", 2);
        put("Milkshake Base", 2);
        put("Pickles", 4);
        put("BOTTLE Coke Classic", 24);
        put("BOTTLE Iced Tea", 24);
        put("BOTTLE Sprite", 24);
        put("BOTTLE Diet Coke", 24);
        put("BOTTLE Vitamin Water (Multi-V Lemonade)", 24);
        put("BOTTLE Vitamin Water (xxx)", 24);
        put("Water", 24);
        put("BOTTLE Root Beer", 24);
        put("BOTTLE Ginger Ale", 24);
        put("Salted Caramel", 2);
        put("Simple Syrup", 2);
        put("Oreo Pieces", 12);
        put("Hair Nets", 1);
        put("Beard Nets", 1);
        put("Sea Salt Flakes (SPC)", 6);
        put("Peanut Butter", 6);
        put("Mayonnaise", 2);
        put("Patty Paper", 24);
        put("6lb Paper Bag", 4);
        put("12lb Paper Bag", 2);
        put("Delivery Bags (Twisted Bag)", 1);
        put("20lb Paper Bag", 1);
        put("Salt (bag)", 1);
        put("2oz Souffle Cups", 25);
        put("2oz Souffle Lids", 25);
        put("Peanut Trays", 4);
        put("Peanut Butter", 6);
        put("Ketchup (Packets)", 1000);
        put("Mustard (Cryovac)", 2);
        put("Peanuts", 1);
        put("Small Gloves", 10);
        put("Medium Gloves", 10);
        put("Large Gloves", 10);
        put("Extra Large Gloves", 10);
        put("Straws", 9);
        put("Napkins", 12);
        put("Bacon Paper", 24);
        put("Poutine Hinged Bowls", 3);
        put("Aluminum Bowls", 6);
        put("Bowl Lids (Usually Poutine Bowls)", 3);
        put("21oz Drink Cup", 24);
        put("21oz Lid", 12);
        put("32oz Cups", 15);
        put("32oz Lids", 10);
        put("16oz Shake Cups", 20);
        put("16oz Shake Lids", 10);
        put("9oz Fry Cup", 24);
        put("12oz Fry Cup", 24);
        put("24oz Fry Cup", 24);
        put("8oz Gravy Bowl", 20);
        put("8oz Gravy Lid", 20);
        put("BIB Coke", 1);
        put("BIB Diet Coke", 1);
        put("BIB Iced Tea", 1);
        put("BIB Root Beer", 1);
        put("BIB Sprite", 1);
        put("BIB Fruitopia", 1);
        put("BIB Fanta (Orange)", 1);
        put("Drink Carriers", 4); // Double check
        put("Cajun Spice", 6);
        put("Poutine Gravy", 4); // Double check
        put("Aluminum Foil", 6);
        put("Sanitizer Cloths", 215);
        put("Filter Paper (Fryers)", 100);
        put("Green Pads", 6);
        put("Clorox Bleach", 4); // Double check
        put("Stainless Steel Polish", 4); // Double check
        put("Hand Soap", 1); // Double check
        put("2-in-1 Peroxide/Multi-surface", 1);
        put("2-in-1 Floor Cleaner Degreaser", 1);
        put("Kayquat Sanitizer", 1);
        put("Dish Sink Detergent", 1);
        put("K5 Sanitizer Pills", 1);
        put("Hygiene Disposal Bags", 1);
        put("Baby Table Pads", 1);
        put("Toilet Paper", 18);
        put("Whipped Cream", 12);

    }};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        layoutContainer = view.findViewById(R.id.layoutContainer);
        orderTextViews = new HashMap<>();
        latestInventoryQuantities = new HashMap<String, Double>();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        viewModel.getInventoryQuantitiesLiveData().observe(getViewLifecycleOwner(), this::handleInventoryUpdate);
        viewModel.getCogValuesLiveData().observe(getViewLifecycleOwner(), this::handleCogValuesUpdate);


        loadInventoryQuantities();
        createDynamicOrderUI();
        return view;
    }



    private void handleInventoryUpdate(Map<String, ? extends Number> inventoryQuantities) {
        if (inventoryQuantities != null) {
            Log.d("OrderFragment", "Inventory quantities received: " + inventoryQuantities.toString());
            latestInventoryQuantities.clear();
            // Convert and update the map to Double values
            inventoryQuantities.forEach((key, value) -> latestInventoryQuantities.put(key, value.doubleValue()));
            updateOrderDetails();
        } else {
            Log.d("OrderFragment", "Received null inventory quantities");
        }
    }




    @Override
    public void onStart() {
        super.onStart();

        // Observe Inventory Quantities
        viewModel.getInventoryQuantitiesLiveData().observe(getViewLifecycleOwner(), inventoryQuantities -> {
            if (inventoryQuantities != null) {
                Log.d("OrderFragment", "Inventory quantities updated: " + inventoryQuantities);
                latestInventoryQuantities.clear();
                // Convert and update the map to Double values
                for (Map.Entry<String, ? extends Number> entry : inventoryQuantities.entrySet()) {
                    latestInventoryQuantities.put(entry.getKey(), entry.getValue().doubleValue());
                }
                updateOrderDetails();
            }
        });

        // Observe COG Values
        viewModel.getCogValuesLiveData().observe(this, cogValues -> {
            if (cogValues != null) {
                latestCogValues.clear();
                latestCogValues.putAll(cogValues);
                if (!latestInventoryQuantities.isEmpty()) {
                    updateOrderDetails();
                }
            }
        });
    }


    private void handleCogValuesUpdate(Map<String, Double> cogValues) {
        if (cogValues != null) {
            latestCogValues.clear();
            latestCogValues.putAll(cogValues);
            if (!latestInventoryQuantities.isEmpty()) {
                updateOrderDetails();
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        combinedData.removeObservers(this);
        latestInventoryQuantities.clear(); // Clear the cache
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unobserve LiveData to avoid memory leaks
        viewModel.getInventoryQuantitiesLiveData().removeObservers(this);
        viewModel.loadCogValues().removeObservers(this);
        sharedViewModel.getForecast().removeObservers(this);
    }


    private void loadInventoryQuantities() {
        new Thread(() -> {
            Map<String, Double> savedInventoryValues = viewModel.loadAllInventoryItemQuantities();
            getActivity().runOnUiThread(() -> handleInventoryUpdate(savedInventoryValues));
        }).start();
    }


    private void updateOrderDetails() {
        for (String item : AllItems) {
            int orderAmount = calculateOrderAmount(item);

            TextView orderTextView = orderTextViews.get(item);
            if (orderTextView != null) {
                String displayText = item + " Order: " + orderAmount;
                orderTextView.setText(displayText);
            }
        }
    }

    private int calculatePeanutOilOrderAmount(double currentInventory, double forecastValue, double peanutOilCog) {
        // Constants defining minimum and maximum inventory levels
        final int minimumRequired = 7;
        final int maximumInventory = 14;

        // Calculate the number of boxes needed based on forecast value and cost of goods (COG) for peanut oil
        // Assuming peanutOilCog represents the cost for one box and forecastValue represents the total forecasted sales
        double boxesNeededBasedOnForecast = forecastValue / peanutOilCog;

        // Calculate desired order amount based on forecasted need
        int desiredOrderAmount = (int) Math.ceil(boxesNeededBasedOnForecast - currentInventory);

        // Determine the day of the week to adjust order strategy
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // On Monday to Wednesday, adjust the order to ensure inventory is built up to the target level (14 boxes)
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.WEDNESDAY) {
            if (currentInventory + desiredOrderAmount < maximumInventory) {
                desiredOrderAmount = maximumInventory - (int) currentInventory;
            }
        } else {
            // Ensure the order amount does not cause inventory to fall below minimum or exceed maximum levels
            if (currentInventory + desiredOrderAmount > maximumInventory) {
                desiredOrderAmount = maximumInventory - (int) currentInventory; // Adjust to not exceed max
            } else if (currentInventory + desiredOrderAmount < minimumRequired) {
                desiredOrderAmount = minimumRequired - (int) currentInventory; // Adjust to meet minimum
            }
        }

        // Final adjustment to ensure the order does not result in negative values or exceed maximum capacity
        desiredOrderAmount = Math.max(desiredOrderAmount, 0); // Prevent negative orders
        return Math.min(desiredOrderAmount, maximumInventory - (int) currentInventory); // Prevent exceeding max
    }

    private int calculatePotatoBagOrderAmount(double forecastValue, double currentInventory) {
        final int minimumRequired = 28; // Minimum inventory required for Potatoes
        final int maximumStorage = 48; // Maximum storage capacity for Potatoes
        final int forecastedOrder = (int) Math.ceil(forecastValue / 10000 * 30); // Assuming each $10,000 of sales forecasts 30 units of Potatoes

        int orderAmount = Math.max(forecastedOrder, minimumRequired) - (int) currentInventory;
        orderAmount = Math.max(orderAmount, 0); // Ensure the order amount is not negative
        return Math.min(orderAmount, maximumStorage - (int) currentInventory);
    }


    private int calculateOrderAmount(String itemName) {
        double forecastValue = sharedViewModel.getForecast().getValue(); // Forecasted sales in dollars.
        double currentInventoryUnits = latestInventoryQuantities.getOrDefault(itemName, 0.0); // Current inventory, including fractional units.
        double usagePer10k = latestCogValues.getOrDefault(itemName, 0.0); // Usage rate per $10k of sales, also representing itemCOG.

        // Calculate the daily usage estimate for dynamic fractional impact evaluation.
        double dailyUsageEstimate = (forecastValue / 10000.0) * usagePer10k / 30; // Assuming a 30-day month for simplicity.

        if (itemName.equals("Peanut Oil")) {
            return calculatePeanutOilOrderAmount(currentInventoryUnits, forecastValue, usagePer10k);
        } else if (itemName.equals("Potatoes")) {
            return calculatePotatoBagOrderAmount(forecastValue, currentInventoryUnits);
        }

        // Calculate base required cases with surplus factor applied.
        double baseRequiredCases = (forecastValue / 10000.0) * usagePer10k;
        double surplusFactor = produceItems.contains(itemName) ? 1.15 : 1.10;
        double totalRequiredCasesWithSurplus = baseRequiredCases * surplusFactor;

        // Evaluate the significance of fractional inventory.
        double fractionalInventoryImpact = evaluateFractionalImpact(currentInventoryUnits, usagePer10k, dailyUsageEstimate, itemName);

        // Adjust required cases based on fractional inventory significance.
        double adjustedTotalRequiredCases = totalRequiredCasesWithSurplus - fractionalInventoryImpact;

        // Apply risk adjustment factor.
        double riskAdjustment = calculateRiskAdjustmentFactor(itemName, currentInventoryUnits, adjustedTotalRequiredCases);
        adjustedTotalRequiredCases += riskAdjustment;

        // Final calculation for additional cases or units needed.
        return calculateFinalOrderQuantity(itemName, currentInventoryUnits, adjustedTotalRequiredCases);
    }

    private double evaluateFractionalImpact(double currentInventoryUnits, double usagePer10k, double dailyUsageEstimate, String itemName) {
        double fractionalPart = currentInventoryUnits % 1;
        int unitsPerCase = caseItemsMaxQuantity.getOrDefault(itemName, 1);

        // Adjust evaluation based on the proportion of a case and its daily usage impact.
        double significanceThreshold = calculateSignificanceThreshold(usagePer10k, dailyUsageEstimate, unitsPerCase, fractionalPart);

        return fractionalPart >= significanceThreshold ? fractionalPart * usagePer10k : 0;
    }

    private double calculateSignificanceThreshold(double usagePer10k, double dailyUsageEstimate, int unitsPerCase, double fractionalPart) {
        // Dynamic calculation of significance threshold based on item specifics.
        double dailyFractionalUsage = fractionalPart * (usagePer10k / unitsPerCase) / dailyUsageEstimate;

        // Sensitivity adjustment based on fractional usage compared to daily demand.
        return dailyFractionalUsage * unitsPerCase; // Normalize against units per case for relative impact.
    }

    private double calculateRiskAdjustmentFactor(String itemName, double currentInventoryUnits, double adjustedTotalRequiredCases) {
        // Risk adjustment for volatility, especially for high-turnover or critical items.
        double riskFactor = itemName.matches(".*Bottle.*") ? 0.20 : 0.10;
        double shortfall = adjustedTotalRequiredCases - currentInventoryUnits;
        return shortfall > 0 ? shortfall * riskFactor : 0;
    }

    private int calculateFinalOrderQuantity(String itemName, double currentInventoryUnits, double adjustedTotalRequiredCases) {
        if (caseItemsMaxQuantity.containsKey(itemName)) {
            int unitsPerCase = caseItemsMaxQuantity.get(itemName);
            double existingCases = Math.floor(currentInventoryUnits / unitsPerCase);
            double additionalCasesNeeded = Math.ceil(adjustedTotalRequiredCases - existingCases);

            return (int) Math.max(additionalCasesNeeded, 0);
        } else {
            // Direct calculation for non-case items.
            double additionalUnitsNeeded = Math.ceil(adjustedTotalRequiredCases - currentInventoryUnits);
            return (int) Math.max(additionalUnitsNeeded, 0);
        }
    }







    @Override
    public void onResume() {
        super.onResume();
        updateOrderDetails(); // Update UI with the latest data
        loadInventoryQuantities();
    }


    private void createDynamicOrderUI() {
        // Ensure every item in AllItems gets a TextView
        for (String item : AllItems) {
            TextView textView = new TextView(getContext());
            textView.setText(item + " Order: ");
            layoutContainer.addView(textView);
            orderTextViews.put(item, textView);
        }
    }

}
