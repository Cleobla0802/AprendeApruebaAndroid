package es.iescarrillo.aprendeaprueba.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.iescarrillo.aprendeaprueba.R;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "REGISTER_GOOGLE";
    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnGoogle;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> registerWithEmail());
        btnGoogle.setOnClickListener(v -> loginWithGoogle());
        tvLogin.setOnClickListener(v -> finish()); // Vuelve al login
    }

    private void registerWithEmail() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Error en el registro";
                        Toast.makeText(this, "Error en el registro: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(this, request, null, Runnable::run,
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Toast.makeText(RegisterActivity.this, "Error al iniciar con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleSignIn(Credential credential) {
        // Aprovechamos Java 21 para usar el 'instanceof' moderno
        if (credential instanceof CustomCredential customCredential
                && credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            try {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCredential.getData());
                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
            } catch (Exception e) {
                Toast.makeText(RegisterActivity.this, "Error al procesar credencial", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            comprobarUsuarioEnBaseDeDatos(user);
                        } else {
                            irAHome();
                        }
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Error de autenticación";
                        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void comprobarUsuarioEnBaseDeDatos(FirebaseUser user) {
        String uid = user.getUid();
        FirebaseDatabase.getInstance().getReference("usuarios")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            irAHome();
                        } else {
                            mostrarDialogoPassword(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        irAHome();
                    }
                });
    }

    private void mostrarDialogoPassword(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Establecer contraseña");
        builder.setMessage("¿Quieres poder iniciar sesión también con correo y contraseña? Establece una contraseña ahora.");

        View view = getLayoutInflater().inflate(R.layout.dialog_set_password, null);
        EditText etPass = view.findViewById(R.id.etPasswordDialog);
        EditText etConfirm = view.findViewById(R.id.etConfirmPasswordDialog);
        builder.setView(view);

        builder.setPositiveButton("Guardar", null);
        builder.setNegativeButton("Omitir", (dialog, which) -> irAHome());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String pass = etPass.getText().toString().trim();
                String confirm = etConfirm.getText().toString().trim();

                if (pass.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(this, "Rellena los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pass.equals(confirm)) {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pass.length() < 6) {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (user.getEmail() == null) {
                    Toast.makeText(this, "No se puede establecer contraseña: correo no disponible", Toast.LENGTH_LONG).show();
                    return;
                }

                AuthCredential emailCredential = EmailAuthProvider.getCredential(user.getEmail(), pass);
                user.linkWithCredential(emailCredential)
                        .addOnSuccessListener(a -> {
                            Toast.makeText(this, "Contraseña guardada. Ya puedes iniciar sesión con email.", Toast.LENGTH_LONG).show();
                            irAHome();
                        })
                        .addOnFailureListener(e -> {
                            String msg = e.getMessage() != null ? e.getMessage() : "Error al guardar la contraseña";
                            Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        });
            });
        });

        dialog.show();
    }

    private void irAHome() {
        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
        finish();
    }
}
