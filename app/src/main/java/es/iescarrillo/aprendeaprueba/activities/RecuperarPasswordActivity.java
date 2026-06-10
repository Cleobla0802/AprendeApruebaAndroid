package es.iescarrillo.aprendeaprueba.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import es.iescarrillo.aprendeaprueba.R;

/**
 * Pantalla para recuperar la contraseña mediante correo electrónico.
 * Usa Firebase Auth para enviar un email de restablecimiento al usuario.
 */
public class RecuperarPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnRecuperar;
    private TextView tvVolver;
    private FirebaseAuth mAuth;

    /**
     * Inicializa la pantalla: enlaza los componentes del layout y configura
     * el botón de recuperación y el enlace para volver al login.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_password);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmailRecuperar);
        btnRecuperar = findViewById(R.id.btnRecuperar);
        tvVolver = findViewById(R.id.tvVolverLogin);

        btnRecuperar.setOnClickListener(v -> recuperarPassword());

        // finish() cierra esta pantalla y vuelve al Login sin necesidad de otra Activity
        tvVolver.setOnClickListener(v -> finish());
    }

    /**
     * Valida el email introducido y solicita a Firebase el envío
     * de un correo de restablecimiento de contraseña.
     * Si el correo no está registrado, Firebase devuelve un error y se informa al usuario.
     * Si el envío es exitoso, cierra la pantalla automáticamente.
     */
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