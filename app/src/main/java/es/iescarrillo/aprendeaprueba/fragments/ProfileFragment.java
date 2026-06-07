package es.iescarrillo.aprendeaprueba.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.activities.MainActivity;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvMensajeExito, tvMensajeError, tvErrorDelete, tvInfoGoogle;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnGuardarUsername, btnResetPassword, btnEliminarCuenta, btnCancelarDelete, btnConfirmarDelete;
    private View overlayBg;
    private MaterialCardView cardDeleteModal;
    private TextInputLayout layoutPassword;

    private FirebaseAuth auth;
    private FirebaseUser user;

    private String usernameActual = "";
    private boolean esGoogle = false;
    private boolean loadingUsername = false;
    private boolean loadingDelete = false;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    public ProfileFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class);
                            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                            reauthenticarYEliminar(credential);
                        } catch (ApiException e) {
                            tvErrorDelete.setText("Error de autenticación con Google");
                            tvErrorDelete.setVisibility(View.VISIBLE);
                            loadingDelete = false;
                            actualizarEstadoDelete();
                        }
                    } else {
                        loadingDelete = false;
                        actualizarEstadoDelete();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvMensajeExito = view.findViewById(R.id.tvMensajeExito);
        tvMensajeError = view.findViewById(R.id.tvMensajeError);
        tvErrorDelete = view.findViewById(R.id.tvErrorDelete);
        tvInfoGoogle = view.findViewById(R.id.tvInfoGoogle);
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPasswordDelete);
        btnGuardarUsername = view.findViewById(R.id.btnGuardarUsername);
        btnResetPassword = view.findViewById(R.id.btnResetPasswordProfile);
        btnEliminarCuenta = view.findViewById(R.id.btnDeleteAccount);
        btnCancelarDelete = view.findViewById(R.id.btnCancelarDelete);
        btnConfirmarDelete = view.findViewById(R.id.btnConfirmarDelete);
        overlayBg = view.findViewById(R.id.overlayDeleteBg);
        cardDeleteModal = view.findViewById(R.id.cardDeleteModal);
        layoutPassword = view.findViewById(R.id.layoutPasswordDelete);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        if (user != null) {
            tvEmail.setText(user.getEmail());
            esGoogle = esUsuarioGoogle();
            cargarUsername();
        }

        btnGuardarUsername.setOnClickListener(v -> guardarUsername());
        btnResetPassword.setOnClickListener(v -> enviarCorreoRestablecimiento());
        btnEliminarCuenta.setOnClickListener(v -> mostrarModalEliminar());
        overlayBg.setOnClickListener(v -> cerrarModalEliminar());
        btnCancelarDelete.setOnClickListener(v -> cerrarModalEliminar());
        btnConfirmarDelete.setOnClickListener(v -> confirmarEliminar());

        return view;
    }

    private boolean esUsuarioGoogle() {
        if (user == null) return false;
        for (UserInfo provider : user.getProviderData()) {
            if ("google.com".equals(provider.getProviderId())) return true;
        }
        return false;
    }

    private void cargarUsername() {
        if (user == null) return;
        FirebaseDatabase.getInstance().getReference("usuarios").child(user.getUid()).child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String username = snapshot.getValue(String.class);
                        usernameActual = username != null ? username : (user.getDisplayName() != null ? user.getDisplayName() : "");
                        etUsername.setText(usernameActual);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Error al cargar el usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void guardarUsername() {
        String nuevo = etUsername.getText().toString().trim();
        if (nuevo.isEmpty() || nuevo.equals(usernameActual) || user == null) return;

        loadingUsername = true;
        btnGuardarUsername.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", nuevo);

        FirebaseDatabase.getInstance().getReference("usuarios").child(user.getUid())
                .updateChildren(updates)
                .addOnSuccessListener(a -> {
                    usernameActual = nuevo;
                    mostrarExito("Nombre actualizado correctamente");
                })
                .addOnFailureListener(e -> mostrarError("Error al actualizar el nombre"))
                .addOnCompleteListener(t -> {
                    loadingUsername = false;
                    btnGuardarUsername.setEnabled(true);
                });
    }

    private void enviarCorreoRestablecimiento() {
        if (user == null || user.getEmail() == null) return;
        auth.sendPasswordResetEmail(user.getEmail())
                .addOnSuccessListener(a -> mostrarExito("Correo de restablecimiento enviado a " + user.getEmail()))
                .addOnFailureListener(e -> mostrarError("Error al enviar el correo"));
    }

    private void mostrarExito(String msg) {
        tvMensajeExito.setText(msg);
        tvMensajeExito.setVisibility(View.VISIBLE);
        tvMensajeError.setVisibility(View.GONE);
    }

    private void mostrarError(String msg) {
        tvMensajeError.setText(msg);
        tvMensajeError.setVisibility(View.VISIBLE);
        tvMensajeExito.setVisibility(View.GONE);
    }

    private void mostrarModalEliminar() {
        overlayBg.setVisibility(View.VISIBLE);
        cardDeleteModal.setVisibility(View.VISIBLE);
        tvErrorDelete.setVisibility(View.GONE);
        if (esGoogle) {
            layoutPassword.setVisibility(View.GONE);
            tvInfoGoogle.setVisibility(View.VISIBLE);
        } else {
            layoutPassword.setVisibility(View.VISIBLE);
            tvInfoGoogle.setVisibility(View.GONE);
            etPassword.setText("");
        }
    }

    private void cerrarModalEliminar() {
        overlayBg.setVisibility(View.GONE);
        cardDeleteModal.setVisibility(View.GONE);
    }

    private void confirmarEliminar() {
        if (user == null) return;

        loadingDelete = true;
        actualizarEstadoDelete();
        tvErrorDelete.setVisibility(View.GONE);

        if (esGoogle) {
            googleSignInClient.signOut();
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        } else {
            String password = etPassword.getText().toString().trim();
            if (password.isEmpty()) {
                tvErrorDelete.setText("Introduce tu contraseña");
                tvErrorDelete.setVisibility(View.VISIBLE);
                loadingDelete = false;
                actualizarEstadoDelete();
                return;
            }
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            reauthenticarYEliminar(credential);
        }
    }

    private void reauthenticarYEliminar(AuthCredential credential) {
        user.reauthenticate(credential)
                .addOnSuccessListener(a -> borrarCuentaCompleta())
                .addOnFailureListener(e -> {
                    String error = "Error al eliminar la cuenta. Inténtalo de nuevo";
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        error = "Contraseña incorrecta";
                    } else if (e.getMessage() != null && e.getMessage().toLowerCase().contains("password")) {
                        error = "Contraseña incorrecta";
                    }
                    tvErrorDelete.setText(error);
                    tvErrorDelete.setVisibility(View.VISIBLE);
                    loadingDelete = false;
                    actualizarEstadoDelete();
                });
    }

    private void borrarCuentaCompleta() {
        if (user == null) return;
        String uid = user.getUid();

        int[] pendientes = {3};
        Runnable onAllDone = () -> {
            if (!isAdded()) return;
            user.delete()
                    .addOnSuccessListener(unused -> {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Cuenta eliminada correctamente", Toast.LENGTH_LONG).show();
                        auth.signOut();
                        startActivity(new Intent(requireContext(), MainActivity.class));
                        requireActivity().finish();
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        tvErrorDelete.setText("No se pudo eliminar la cuenta. Vuelve a iniciar sesión y prueba otra vez.");
                        tvErrorDelete.setVisibility(View.VISIBLE);
                        loadingDelete = false;
                        actualizarEstadoDelete();
                        cerrarModalEliminar();
                    });
        };

        borrarDatosUsuario("apuntes", uid, pendientes, onAllDone);
        borrarDatosUsuario("resumenes", uid, pendientes, onAllDone);
        borrarDatosUsuario("tests", uid, pendientes, onAllDone);
    }

    private void borrarDatosUsuario(String nodo, String uid, int[] pendientes, Runnable onAllDone) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(nodo);
        ref.orderByChild("userId").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                if (count == 0) {
                    if (--pendientes[0] == 0) onAllDone.run();
                    return;
                }
                long[] deleteCount = {count};
                for (DataSnapshot child : snapshot.getChildren()) {
                    child.getRef().removeValue().addOnCompleteListener(task -> {
                        if (--deleteCount[0] == 0) {
                            if (--pendientes[0] == 0) onAllDone.run();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (--pendientes[0] == 0) onAllDone.run();
            }
        });
    }

    private void actualizarEstadoDelete() {
        btnConfirmarDelete.setEnabled(!loadingDelete);
        btnCancelarDelete.setEnabled(!loadingDelete);
    }
}
