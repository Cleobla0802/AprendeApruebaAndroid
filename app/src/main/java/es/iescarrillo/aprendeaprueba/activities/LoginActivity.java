package es.iescarrillo.aprendeaprueba.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private CredentialManager credentialManager;

    /**
     * Inicializa la pantalla de login: enlaza los componentes del layout,
     * configura los listeners de los botones y redirige directamente a Home
     * si el usuario ya tiene sesión iniciada.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RecuperarPasswordActivity.class))
        );

        instanceAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        if (instanceAuth.getCurrentUser() != null) {
            irAMain();
        }

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
                            String msg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Credenciales incorrectas";
                            Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        btnGoogle.setOnClickListener(v -> loginWithGoogle());
    }

    /**
     * Lanza el flujo de login con Google usando el CredentialManager.
     * Solicita las cuentas disponibles en el dispositivo sin filtrar
     * solo las ya autorizadas, para permitir añadir cuentas nuevas.
     */
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
                        handleGoogleCredential(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Toast.makeText(LoginActivity.this, "Error al iniciar con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Procesa la credencial devuelta por Google y extrae el ID token
     * para autenticar al usuario en Firebase.
     * Si la credencial no es del tipo esperado o falla el parseo, muestra un error.
     *
     * @param credential Credencial obtenida del CredentialManager
     */
    private void handleGoogleCredential(Credential credential) {
        if (credential instanceof CustomCredential customCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            try {
                GoogleIdTokenCredential googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(customCredential.getData());
                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
            } catch (Exception e) {
                Toast.makeText(LoginActivity.this, "Error al procesar credencial", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Autentica al usuario en Firebase usando el ID token de Google.
     * Si la autenticación es exitosa, comprueba si el usuario ya existe
     * en la base de datos antes de redirigirlo.
     *
     * @param idToken Token de identidad obtenido de Google
     */
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

    /**
     * Comprueba si el usuario autenticado con Google ya tiene perfil en la base de datos.
     * Si existe, va directamente a Home. Si no existe, redirige al registro
     * pasando su email y UID para pre-rellenar el formulario.
     * En caso de error de base de datos, redirige a Home como fallback seguro.
     *
     * @param user Usuario autenticado en Firebase
     */
    private void comprobarUsuarioEnBaseDeDatos(FirebaseUser user) {
        String uid = user.getUid();
        String email = user.getEmail();

        FirebaseDatabase.getInstance().getReference("usuarios")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            irAMain();
                        } else {
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
                        // Si falla la consulta, se redirige a Home como medida de seguridad
                        irAMain();
                    }
                });
    }

    /**
     * Navega a la pantalla principal (HomeActivity) y elimina LoginActivity
     * de la pila de navegación para que el usuario no pueda volver atrás.
     */
    private void irAMain() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}