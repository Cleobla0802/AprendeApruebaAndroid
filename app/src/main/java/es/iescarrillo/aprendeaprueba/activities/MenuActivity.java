package es.iescarrillo.aprendeaprueba.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import es.iescarrillo.aprendeaprueba.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_apuntes);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Obtener el NavHostFragment y el NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        // Configurar el BottomNavigationView para que funcione con el NavController
        // Esto gestiona automáticamente los clics y cambia de fragmento
        NavigationUI.setupWithNavController(bottomNav, navController);
    }
}
