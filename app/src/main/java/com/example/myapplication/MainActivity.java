package com.example.myapplication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Load the Inventory fragment as the default on app start
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new InventoryFragment()).commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            // Determine which fragment to load based on the selected item ID
            if (item.getItemId() == R.id.nav_inventory) {
                loadFragment(new InventoryFragment());
            } else if (item.getItemId() == R.id.nav_order) {
                loadFragment(new OrderFragment());
            }
            else if (item.getItemId() == R.id.nav_cog) {
                loadFragment(new CogFragment());
            }
            // Add else-if for other fragments as needed
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}
