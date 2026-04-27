package es.iescarrillo.aprendeaprueba.activities;

import android.content.Intent;
<<<<<<< HEAD
import android.os.Bundle;
import android.view.View;
=======
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
>>>>>>> develop
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
<<<<<<< HEAD
=======
import androidx.constraintlayout.widget.ConstraintLayout;
>>>>>>> develop
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

<<<<<<< HEAD
import com.google.android.material.bottomnavigation.BottomNavigationView;
=======
>>>>>>> develop
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.fragments.ApuntesFragment;
import es.iescarrillo.aprendeaprueba.fragments.PruebasFragment;
import es.iescarrillo.aprendeaprueba.fragments.ResumenesFragment;

public class HomeActivity extends AppCompatActivity {

<<<<<<< HEAD
    BottomNavigationView bottomNavigationView;
=======
>>>>>>> develop
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawerLayout);
<<<<<<< HEAD
        navigationView = findViewById(R.id.navigationView);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        cargarDatosUsuario();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        loadFragment(new ApuntesFragment());
=======
        navigationView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
>>>>>>> develop

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

<<<<<<< HEAD
            if (itemId == R.id.nav_apuntes) {
                selectedFragment = new ApuntesFragment();
            } else if (itemId == R.id.nav_pruebas) {
                selectedFragment = new PruebasFragment();
            } else if (itemId == R.id.nav_resumenes) {
                selectedFragment = new ResumenesFragment();
=======
        cargarDatosUsuario();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
>>>>>>> develop
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
<<<<<<< HEAD
=======

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

        loadFragment(new ApuntesFragment());
        setActiveNav(0);

>>>>>>> develop
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

<<<<<<< HEAD
=======
    private void setActiveNav(int index) {
        int colorActivo = Color.parseColor("#6C63FF");
        int colorInactivo = Color.parseColor("#AAAAAA");

        ConstraintLayout[] circles = {
                findViewById(R.id.circleApuntes),
                findViewById(R.id.circlePruebas),
                findViewById(R.id.circleResumenes)
        };
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

        for (int i = 0; i < circles.length; i++) {
            if (i == index) {
                circles[i].setVisibility(View.VISIBLE);
                iconsFlat[i].setVisibility(View.GONE);
                texts[i].setTextColor(colorActivo);
            } else {
                circles[i].setVisibility(View.GONE);
                iconsFlat[i].setVisibility(View.VISIBLE);
                texts[i].setTextColor(colorInactivo);
            }
        }
    }

>>>>>>> develop
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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
<<<<<<< HEAD


=======
>>>>>>> develop
}