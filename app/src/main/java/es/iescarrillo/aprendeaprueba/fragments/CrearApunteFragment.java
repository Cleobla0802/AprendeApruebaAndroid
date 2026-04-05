package es.iescarrillo.aprendeaprueba.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.api.RetrofitClient;
import es.iescarrillo.aprendeaprueba.models.ImgBBResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearApunteFragment extends Fragment {

    private TextInputEditText etTitulo;
    private Spinner spinnerCategoria, spinnerDificultad;
    private MaterialCardView btnSeleccionarImagen;
    private ImageView ivPreview;
    private Button btnLimpiar, btnGuardar;
    private Uri imagenUri;

    private FirebaseAuth mAuth;
    private final String apiKeyImgBB = "c7a45042cb4b545d896d5c8730252add";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imagenUri = result.getData().getData();
                    if (ivPreview != null) {
                        ivPreview.setImageURI(imagenUri);
                        ivPreview.setImageTintList(null);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_crear_apunte, container, false);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Vincular vistas
        etTitulo = root.findViewById(R.id.etTitulo);
        spinnerCategoria = root.findViewById(R.id.spinnerCategoria);
        spinnerDificultad = root.findViewById(R.id.spinnerDificultad); // ¡Importante!
        btnSeleccionarImagen = root.findViewById(R.id.btnSeleccionarImagen);
        ivPreview = root.findViewById(R.id.ivPreview);
        btnLimpiar = root.findViewById(R.id.btnLimpiar);
        btnGuardar = root.findViewById(R.id.btnGuardar);

        configurarSpinners();

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());
        btnGuardar.setOnClickListener(v -> validarYGuardar());

        return root;
    }

    private void configurarSpinners() {
        String[] categorias = {"Matemáticas", "Historia", "Lengua", "Ciencias", "Otros"};
        String[] dificultades = {"Fácil", "Intermedio", "Difícil"};

        if (getContext() != null) {
            // Configurar Categoría
            ArrayAdapter<String> adapterCat = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categorias);
            spinnerCategoria.setAdapter(adapterCat);

            // Configurar Dificultad (Esto era lo que faltaba)
            ArrayAdapter<String> adapterDif = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, dificultades);
            spinnerDificultad.setAdapter(adapterDif);
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void validarYGuardar() {
        String titulo = etTitulo.getText().toString().trim();
        if (titulo.isEmpty() || imagenUri == null) {
            Toast.makeText(getContext(), "Añade título e imagen", Toast.LENGTH_SHORT).show();
            return;
        }
        subirImagenAImgBB();
    }

    private void subirImagenAImgBB() {
        try {
            if (getContext() == null) return;

            File file = new File(getContext().getCacheDir(), "temp_image");
            InputStream inputStream = getContext().getContentResolver().openInputStream(imagenUri);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            RetrofitClient.getImgBBService().uploadImage(apiKeyImgBB, body).enqueue(new Callback<ImgBBResponse>() {
                @Override
                public void onResponse(Call<ImgBBResponse> call, Response<ImgBBResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        guardarEnBackend(response.body().getData().getUrl());
                    } else {
                        Toast.makeText(getContext(), "Error en ImgBB", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ImgBBResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de red en imagen", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("UPLOAD_ERR", e.getMessage());
        }
    }

    private void guardarEnBackend(String urlImagen) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el mapa de datos para el Body
        Map<String, String> payload = new HashMap<>();
        payload.put("url", urlImagen);
        payload.put("titulo", etTitulo.getText().toString());
        payload.put("userId", user.getUid()); // Usamos el UID real de Firebase
        payload.put("categoria", spinnerCategoria.getSelectedItem().toString());
        payload.put("dificultad", spinnerDificultad.getSelectedItem().toString());

        Toast.makeText(getContext(), "Digitalizando con IA...", Toast.LENGTH_SHORT).show();

        RetrofitClient.getApunteService().digitalizarApunte(payload).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String textoIA = response.body().get("textoIA");
                    Toast.makeText(getContext(), "¡Éxito! Texto extraído correctamente", Toast.LENGTH_LONG).show();
                    limpiarFormulario();
                } else {
                    Toast.makeText(getContext(), "Error servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Cuerpo error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e("ERROR_CONEXION", t.toString()); // Mira esto en el Logcat de Android Studio
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void limpiarFormulario() {
        if (etTitulo != null) etTitulo.setText("");
        imagenUri = null;
        if (ivPreview != null) {
            ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        if (spinnerCategoria != null) spinnerCategoria.setSelection(0);
        if (spinnerDificultad != null) spinnerDificultad.setSelection(0);
    }
}