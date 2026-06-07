package es.iescarrillo.aprendeaprueba.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        FirebaseDatabase.getInstance().getReference("usuarios")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean encontrado = false;
                        for (DataSnapshot user : snapshot.getChildren()) {
                            String emailGuardado = user.child("email").getValue(String.class);
                            if (emailGuardado != null && emailGuardado.equalsIgnoreCase(email)) {
                                encontrado = true;
                                break;
                            }
                        }

                        if (!encontrado) {
                            Toast.makeText(RecuperarPasswordActivity.this,
                                    "Este correo no está registrado", Toast.LENGTH_SHORT).show();
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
                                    String msg = e.getMessage() != null ? e.getMessage() : "Error al enviar el correo";
                                    Toast.makeText(RecuperarPasswordActivity.this,
                                            "Error: " + msg, Toast.LENGTH_LONG).show();
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(RecuperarPasswordActivity.this,
                                "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}