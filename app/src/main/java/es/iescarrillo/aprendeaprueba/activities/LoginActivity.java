package es.iescarrillo.aprendeaprueba.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.iescarrillo.aprendeaprueba.R;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin, btnGoogle;
    private EditText etEmail, etPassword;
    private TextView tvRegister, tvForgotPassword;
    private FirebaseAuth instanceAuth;

    // Variables de configuración de Google SignIn
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;

    // Constante para el resultado de Google
    private static final int REQ_CODE_GOOGLE_SIGN_IN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Ajuste de márgenes del sistema (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Inicializar componentes usando los IDs de tu XML
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogle = findViewById(R.id.btnGoogle);

        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RecuperarPasswordActivity.class));
        });

        instanceAuth = FirebaseAuth.getInstance();

        // Deshabilitar verificación de app para desarrollo (reCAPTCHA)
        instanceAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        // 2. Configuración de Google SignIn
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // 3. Comprobación de sesión activa
        if (instanceAuth.getCurrentUser() != null) {
            irAMain();
        }

        // --- LISTENERS ---

        // Login con Email y Password
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            instanceAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            irAMain();
                        } else {
                            String msg = task.getException() != null ? task.getException().getMessage() : "Credenciales incorrectas";
                            Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Ir a la pantalla de Registro (según tu árbol de archivos es RegisterActivity)
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Login con Google
        btnGoogle.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, REQ_CODE_GOOGLE_SIGN_IN);
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Fallo al conectar con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        instanceAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = instanceAuth.getCurrentUser();
                if (user != null) {
                    comprobarUsuarioEnBaseDeDatos(user);
                }
            } else {
                Toast.makeText(this, "Error al autenticar en Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void comprobarUsuarioEnBaseDeDatos(FirebaseUser user) {
        String uid = user.getUid();
        String email = user.getEmail();

        // Buscamos en el nodo "persons" como en tu código original
        FirebaseDatabase.getInstance().getReference("persons")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Si el usuario ya está registrado en la DB, va al Main
                            irAMain();
                        } else {
                            // Si es nuevo, lo mandamos a completar el registro
                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                            intent.putExtra("userEmail", email);
                            intent.putExtra("uid", uid);
                            intent.putExtra("editMode", false);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void irAMain() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
