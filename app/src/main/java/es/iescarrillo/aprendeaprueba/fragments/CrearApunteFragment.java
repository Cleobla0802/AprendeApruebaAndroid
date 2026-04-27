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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.api.RetrofitClient;
import es.iescarrillo.aprendeaprueba.models.Apuntes;
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
            // Usamos TU nuevo layout: R.layout.spinner_item_blanco
            ArrayAdapter<String> adapterCat = new ArrayAdapter<>(getContext(),
                    R.layout.spinner_item_blanco, categorias);
            // Esto es para que la lista al abrirse también use el texto blanco
            adapterCat.setDropDownViewResource(R.layout.spinner_item_blanco);
            spinnerCategoria.setAdapter(adapterCat);

            // Lo mismo para Dificultad
            ArrayAdapter<String> adapterDif = new ArrayAdapter<>(getContext(),
                    R.layout.spinner_item_blanco, dificultades);
            adapterDif.setDropDownViewResource(R.layout.spinner_item_blanco);
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

    // 1. PRIMER PASO: Subir la imagen a ImgBB
    private void subirImagenAImgBB() {
        try {
            if (getContext() == null) return;

            // Crear archivo temporal para la subida
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

            // Llamada a ImgBB
            RetrofitClient.getImgBBService().uploadImage(apiKeyImgBB, body).enqueue(new Callback<ImgBBResponse>() {
                @Override
                public void onResponse(Call<ImgBBResponse> call, Response<ImgBBResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Si ImgBB nos da la URL, pasamos al paso 2 (Backend IA)
                        guardarEnBackend(response.body().getData().getUrl());
                    } else {
                        Toast.makeText(getContext(), "Error al subir imagen a ImgBB", Toast.LENGTH_SHORT).show();
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

    // 2. SEGUNDO PASO: Pedir al Backend que digitalice la imagen
    private void guardarEnBackend(String urlImagen) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, String> payload = new HashMap<>();
        payload.put("url", urlImagen);

        Toast.makeText(getContext(), "Digitalizando texto con IA...", Toast.LENGTH_SHORT).show();

        // Llamada a tu servidor en Render
        RetrofitClient.getApunteService().digitalizarApunte(payload).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String textoIA = response.body().get("textoIA");

                    // Si la IA nos devuelve el texto, pasamos al paso 3 (Firebase directo)
                    guardarEnFirebaseDesdeAndroid(textoIA, urlImagen, user.getUid());
                } else {
                    Toast.makeText(getContext(), "Error en el servidor de IA", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 3. TERCER PASO: Guardar todo en Firebase Realtime Database
    private void guardarEnFirebaseDesdeAndroid(String textoIA, String urlImagen, String userId) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("apuntes");
        String apunteId = mDatabase.push().getKey();

        // Construimos el objeto con los datos que ya tenemos en el formulario
        Apuntes nuevoApunte = new Apuntes();
        nuevoApunte.setId(apunteId);
        nuevoApunte.setTitulo(etTitulo.getText().toString().trim());
        nuevoApunte.setContenido(textoIA); // El texto que viene de la IA
        nuevoApunte.setImagenUrl(urlImagen);     // La URL que viene de ImgBB
        nuevoApunte.setUserId(userId);
        nuevoApunte.setCategoria(spinnerCategoria.getSelectedItem().toString());

        // Si tienes campo dificultad en tu modelo Apuntes, actívalo aquí:
        // nuevoApunte.setDificultad(spinnerDificultad.getSelectedItem().toString());

        if (apunteId != null) {
            mDatabase.child(apunteId).setValue(nuevoApunte).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Apunte creado y digitalizado con éxito", Toast.LENGTH_SHORT).show();
                limpiarFormulario();
                // Cerramos el fragmento y volvemos al listado
                getParentFragmentManager().popBackStack();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error al guardar en Firebase", Toast.LENGTH_SHORT).show();
            });
        }
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