package es.iescarrillo.aprendeaprueba.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import es.iescarrillo.aprendeaprueba.R;

/**
 * Pantalla de bienvenida (splash) que se muestra al arrancar la app.
 * Si el usuario ya tiene sesión activa, redirige directamente a Home
 * sin mostrar la pantalla de inicio.
 */
public class MainActivity extends AppCompatActivity {

    Button btnStart;
    ImageView imgLogo;

    /**
     * Comprueba si hay sesión activa nada más arrancar.
     * Si la hay, salta directamente a HomeActivity sin inflar el layout.
     * Si no, muestra la pantalla de bienvenida con el logo y el botón de inicio.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Si ya hay sesión iniciada, ir a Home directamente sin mostrar esta pantalla
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        imgLogo = findViewById(R.id.logo);

        // Carga el logo desde Google Drive usando Picasso
        String logoUrl = "https://drive.google.com/uc?export=download&id=1QrX-2GAmryhv7rsPTvrM1-e430BlajrT";
        Picasso.get()
                .load(logoUrl)
                .into(imgLogo);

        btnStart = findViewById(R.id.button_start);

        // Al pulsar "Empezar", redirige a Login o a Home según si hay sesión activa
        btnStart.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }

            // finish() elimina MainActivity de la pila para que no se pueda volver atrás
            finish();
        });
    }
}