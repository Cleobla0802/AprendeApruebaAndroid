package es.iescarrillo.aprendeaprueba.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import es.iescarrillo.aprendeaprueba.R;

public class RecuperarPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnRecuperar;
    private TextView tvVolver;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_password);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmailRecuperar);
        btnRecuperar = findViewById(R.id.btnRecuperar);
        tvVolver = findViewById(R.id.tvVolverLogin);

        btnRecuperar.setOnClickListener(v -> recuperarPassword());
        tvVolver.setOnClickListener(v -> finish());
    }

    private void recuperarPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Introduce tu correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(a -> {
                    Toast.makeText(RecuperarPasswordActivity.this,
                            "¡Correo enviado! Revisa tu bandeja de entrada",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RecuperarPasswordActivity.this,
                            "Este correo no está registrado en nuestro sistema",
                            Toast.LENGTH_SHORT).show();
                });
    }
}