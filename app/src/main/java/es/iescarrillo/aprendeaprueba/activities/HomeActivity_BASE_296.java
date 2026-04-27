package es.iescarrillo.aprendeaprueba.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.fragments.ApuntesFragment;
import es.iescarrillo.aprendeaprueba.fragments.PruebasFragment;
import es.iescarrillo.aprendeaprueba.fragments.ResumenesFragment;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Fragment por defecto
        loadFragment(new ApuntesFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // Cambiar switch por if-else
            if (itemId == R.id.nav_apuntes) {
                selectedFragment = new ApuntesFragment();
            } else if (itemId == R.id.nav_pruebas) {
                selectedFragment = new PruebasFragment();
            } else if (itemId == R.id.nav_resumenes) {
                selectedFragment = new ResumenesFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
