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

public class MainActivity extends AppCompatActivity {

    Button btnStart;

    ImageView imgLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        imgLogo = findViewById(R.id.logo);

        // URL directa del logo
        String logoUrl = "https://drive.google.com/uc?export=download&id=1QrX-2GAmryhv7rsPTvrM1-e430BlajrT";

        // Cargar con Picasso
        Picasso.get()
                .load(logoUrl)
                .into(imgLogo);

        btnStart = findViewById(R.id.button_start);

        btnStart.setOnClickListener(v -> {

            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {

                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            } else {

                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }

            finish();
        });
}
}