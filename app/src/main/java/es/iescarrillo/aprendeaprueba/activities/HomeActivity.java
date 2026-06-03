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

public class HomeActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth mAuth;

    private ValueEventListener listenerApuntes, listenerResumenes, listenerPruebas;
    private Query qApuntes, qResumenes, qPruebas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        cargarDatosUsuario();
        cargarConteos();

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
                setActiveNav(-1);
            } else if (itemId == R.id.nav_logout) {
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

    private void cargarConteos() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        View header = navigationView.getHeaderView(0);
        TextView tvApuntes = header.findViewById(R.id.tvStatApuntes);
        TextView tvResumenes = header.findViewById(R.id.tvStatResumenes);
        TextView tvPruebas = header.findViewById(R.id.tvStatPruebas);

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        qApuntes = db.getReference("apuntes").orderByChild("userId").equalTo(uid);
        listenerApuntes = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tvApuntes.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        qApuntes.addValueEventListener(listenerApuntes);

        qResumenes = db.getReference("resumenes").orderByChild("userId").equalTo(uid);
        listenerResumenes = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tvResumenes.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        qResumenes.addValueEventListener(listenerResumenes);

        qPruebas = db.getReference("tests").orderByChild("userId").equalTo(uid);
        listenerPruebas = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tvPruebas.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        qPruebas.addValueEventListener(listenerPruebas);
    }

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
