package es.iescarrillo.aprendeaprueba.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.fragments.ApuntesFragment;
import es.iescarrillo.aprendeaprueba.fragments.ProfileFragment;
import es.iescarrillo.aprendeaprueba.fragments.PruebasFragment;
import es.iescarrillo.aprendeaprueba.fragments.ResumenesFragment;

/**
 * Actividad principal de la app tras el login.
 * Gestiona el Navigation Drawer, la barra de navegación inferior personalizada
 * y la carga de fragmentos según la sección seleccionada.
 */
public class HomeActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth mAuth;

    // Referencias a los listeners y queries de Firebase para poder eliminarlos en onDestroy
    private ValueEventListener listenerApuntes, listenerResumenes, listenerPruebas;
    private Query qApuntes, qResumenes, qPruebas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);

        // Configurar la toolbar como ActionBar para que el toggle del drawer funcione
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toggle que sincroniza el icono hamburguesa con el estado abierto/cerrado del drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Cargar nombre/email del usuario en la cabecera del drawer
        cargarDatosUsuario();

        // Cargar los contadores de apuntes, resúmenes y pruebas del usuario
        cargarConteos();

        // Listener para los ítems del Navigation Drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_apuntes) {
                loadFragment(new ApuntesFragment());
                setActiveNav(0);
            } else if (itemId == R.id.nav_resumenes) {
                loadFragment(new ResumenesFragment());
                setActiveNav(2);
            } else if (itemId == R.id.nav_pruebas) {
                loadFragment(new PruebasFragment());
                setActiveNav(1);
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                setActiveNav(-1); // -1 indica que ningún ítem de la barra inferior está activo
            } else if (itemId == R.id.nav_logout) {
                // Cerrar sesión y volver a la pantalla de inicio
                mAuth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Botones de la barra de navegación inferior personalizada
        findViewById(R.id.btnNavApuntes).setOnClickListener(v -> {
            loadFragment(new ApuntesFragment());
            setActiveNav(0);
        });

        findViewById(R.id.btnNavResumenes).setOnClickListener(v -> {
            loadFragment(new ResumenesFragment());
            setActiveNav(2);
        });

        findViewById(R.id.btnNavPruebas).setOnClickListener(v -> {
            loadFragment(new PruebasFragment());
            setActiveNav(1);
        });

        // Fragmento por defecto al abrir la actividad
        loadFragment(new ApuntesFragment());
        setActiveNav(0);

        // Interceptar el botón Atrás: cerrar el drawer si está abierto, sino comportamiento normal
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * Escucha en tiempo real los nodos de Firebase del usuario actual
     * y actualiza los contadores que aparecen en la cabecera del drawer.
     */
    private void cargarConteos() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        View header = navigationView.getHeaderView(0);
        TextView tvApuntes = header.findViewById(R.id.tvStatApuntes);
        TextView tvResumenes = header.findViewById(R.id.tvStatResumenes);
        TextView tvPruebas = header.findViewById(R.id.tvStatPruebas);

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // Filtrar apuntes por userId y contar los resultados
        qApuntes = db.getReference("apuntes").orderByChild("userId").equalTo(uid);
        listenerApuntes = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tvApuntes.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(DatabaseError error) {
                tvApuntes.setText("0");
            }
        };
        qApuntes.addValueEventListener(listenerApuntes);

        // Filtrar resúmenes por userId y contar los resultados
        qResumenes = db.getReference("resumenes").orderByChild("userId").equalTo(uid);
        listenerResumenes = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tvResumenes.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(DatabaseError error) {
                tvResumenes.setText("0");
            }
        };
        qResumenes.addValueEventListener(listenerResumenes);

        // Filtrar pruebas/tests por userId y contar los resultados
        qPruebas = db.getReference("tests").orderByChild("userId").equalTo(uid);
        listenerPruebas = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tvPruebas.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(DatabaseError error) {
                tvPruebas.setText("0");
            }
        };
        qPruebas.addValueEventListener(listenerPruebas);
    }

    /**
     * Actualiza el color e icono de la barra de navegación inferior
     * para resaltar el ítem activo y dejar los demás en gris.
     * @param index Índice del botón activo (0=Apuntes, 1=Pruebas, 2=Resúmenes, -1=ninguno)
     */
    private void setActiveNav(int index) {
        int colorActivo = Color.parseColor("#6C63FF");
        int colorInactivo = Color.parseColor("#D0D0D0");

        ImageView[] iconsFlat = {
                findViewById(R.id.iconFlatApuntes),
                findViewById(R.id.iconFlatPruebas),
                findViewById(R.id.iconFlatResumenes)
        };
        TextView[] texts = {
                findViewById(R.id.textApuntes),
                findViewById(R.id.textPruebas),
                findViewById(R.id.textResumenes)
        };

        // Aplicar color activo al botón seleccionado y negrita; el resto en gris normal
        for (int i = 0; i < iconsFlat.length; i++) {
            if (i == index) {
                iconsFlat[i].setColorFilter(colorActivo);
                texts[i].setTextColor(colorActivo);
                texts[i].setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                iconsFlat[i].setColorFilter(colorInactivo);
                texts[i].setTextColor(colorInactivo);
                texts[i].setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }

    /**
     * Muestra el nombre y email del usuario autenticado en la cabecera del drawer.
     * Si no tiene displayName, muestra "Usuario" como valor por defecto.
     */
    private void cargarDatosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            View header = navigationView.getHeaderView(0);
            TextView tvName = header.findViewById(R.id.tvUserName);
            TextView tvEmail = header.findViewById(R.id.tvUserEmail);
            String nombre = user.getDisplayName();
            tvName.setText(nombre != null && !nombre.isEmpty() ? nombre : "Usuario");
            tvEmail.setText(user.getEmail());
        }
    }

    /**
     * Reemplaza el fragmento visible en el contenedor principal.
     * Usar replace en lugar de add evita apilar fragmentos innecesariamente.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Al destruir la actividad se eliminan los listeners de Firebase
     * para evitar fugas de memoria y lecturas innecesarias en segundo plano.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qApuntes != null && listenerApuntes != null)
            qApuntes.removeEventListener(listenerApuntes);
        if (qResumenes != null && listenerResumenes != null)
            qResumenes.removeEventListener(listenerResumenes);
        if (qPruebas != null && listenerPruebas != null)
            qPruebas.removeEventListener(listenerPruebas);
    }
}