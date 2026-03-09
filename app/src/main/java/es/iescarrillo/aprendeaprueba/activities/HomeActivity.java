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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.fragments.ApuntesFragment;

public class HomeActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

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

        findViewById(R.id.btnNavApuntes).setOnClickListener(v -> {
            loadFragment(new ApuntesFragment());
            setActiveNav(0);
        });

        loadFragment(new ApuntesFragment());
        setActiveNav(0);

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
}