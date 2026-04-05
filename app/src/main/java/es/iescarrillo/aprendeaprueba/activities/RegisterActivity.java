package es.iescarrillo.aprendeaprueba.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import es.iescarrillo.aprendeaprueba.R;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "REGISTER_GOOGLE";
    private EditText etEmail, etPassword;
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

        etEmail = findViewById(R.id.etEmail); // Asegúrate de que los IDs coincidan con tu XML
        etPassword = findViewById(R.id.etPassword);
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

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.e(TAG, "Error: " + e.getMessage());
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
                Log.e(TAG, "Error al procesar token", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}