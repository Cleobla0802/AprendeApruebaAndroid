package es.iescarrillo.aprendeaprueba;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar navegación
        setupNavigation();

        // Configurar Bottom Navigation
        setupBottomNavigation();

        // Configurar Toolbar
        setupToolbar();

        // Restaurar estado si existe
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }

    /**
     * Configura el NavController con el NavHostFragment
     */
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
    }

    /**
     * Configura el BottomNavigationView con el NavController
     */
    private void setupBottomNavigation() {
        bottomNavigationView = binding.bottomNavigation;

        // Método 1: Conexión automática con NavController (Recomendado)
        if (navController != null) {
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        // Método 2: Listener personalizado (Opcional - para lógica adicional)
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_pruebas) {
                    navigateToPruebas();
                    return true;
                } else if (itemId == R.id.navigation_apuntes) {
                    navigateToApuntes();
                    return true;
                } else if (itemId == R.id.navigation_resumenes) {
                    navigateToResumenes();
                    return true;
                }

                return false;
            }
        });

        // Listener para re-selección del mismo ítem
        bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                // Acción cuando se vuelve a tocar el mismo ítem
                // Por ejemplo: scroll to top en RecyclerView
                handleReselection(item.getItemId());
            }
        });

        // Agregar badges (notificaciones) - Ejemplo
        setupBadges();
    }

    /**
     * Configura el Toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);

        // Actualizar título según el fragmento actual
        if (navController != null) {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(destination.getLabel());
                }
            });
        }
    }

    /**
     * Configura badges (notificaciones) en los ítems
     */
    private void setupBadges() {
        // Ejemplo: Agregar badge al ítem de Pruebas
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.navigation_pruebas);
        badge.setVisible(true);
        badge.setNumber(3);
        badge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark, null));

        // Para ocultar el badge:
        // badge.setVisible(false);

        // Para remover completamente:
        // bottomNavigationView.removeBadge(R.id.navigation_pruebas);
    }

    /**
     * Navega al fragmento de Pruebas
     */
    private void navigateToPruebas() {
        if (navController != null) {
            navController.navigate(R.id.pruebasFragment);
        }
    }

    /**
     * Navega al fragmento de Apuntes
     */
    private void navigateToApuntes() {
        if (navController != null) {
            navController.navigate(R.id.apuntesFragment);
        }
    }

    /**
     * Navega al fragmento de Resúmenes
     */
    private void navigateToResumenes() {
        if (navController != null) {
            navController.navigate(R.id.resumenesFragment);
        }
    }

    /**
     * Maneja la re-selección de un ítem
     */
    private void handleReselection(int itemId) {
        if (itemId == R.id.navigation_pruebas) {
            // Por ejemplo: scroll to top en el RecyclerView de Pruebas
            // O refrescar el contenido
        } else if (itemId == R.id.navigation_apuntes) {
            // Acción para Apuntes
        } else if (itemId == R.id.navigation_resumenes) {
            // Acción para Resúmenes
        }
    }

    /**
     * Muestra u oculta el BottomNavigationView
     * Útil para pantallas completas o scrolling
     */
    public void setBottomNavigationVisibility(boolean visible) {
        if (binding.bottomNavigation != null) {
            binding.bottomNavigation.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Establece el ítem seleccionado programáticamente
     */
    public void setSelectedBottomNavItem(int itemId) {
        if (binding.bottomNavigation != null) {
            binding.bottomNavigation.setSelectedItemId(itemId);
        }
    }

    /**
     * Actualiza el contador de un badge
     */
    public void updateBadgeCount(int menuItemId, int count) {
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(menuItemId);
        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
        } else {
            badge.setVisible(false);
        }
    }

    /**
     * Guarda el estado de la instancia
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding.bottomNavigation != null) {
            outState.putInt("selected_item_id", binding.bottomNavigation.getSelectedItemId());
        }
    }

    /**
     * Restaura el estado guardado
     */
    private void restoreInstanceState(Bundle savedInstanceState) {
        int selectedItemId = savedInstanceState.getInt("selected_item_id", R.id.navigation_pruebas);
        setSelectedBottomNavItem(selectedItemId);
    }

    /**
     * Maneja el botón de retroceso
     */
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    /**
     * Limpia el binding cuando se destruye la actividad
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}