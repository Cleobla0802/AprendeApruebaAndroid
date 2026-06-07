package es.iescarrillo.aprendeaprueba.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
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
import es.iescarrillo.aprendeaprueba.utils.GenerationStateUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearApunteFragment extends Fragment {

    private TextInputEditText etTitulo, etDescripcion;
    private Spinner spinnerCategoria;
    private MaterialCardView btnSeleccionarImagen;
    private ImageView ivPreview;
    private Button btnLimpiar, btnGuardar;
    private Uri imagenUri;
    private FirebaseAuth mAuth;

    private final String[] categoriasValores = {"matematicas", "ciencias", "historia", "ingles", "tecnologia"};
    private final String[] categoriasNombres = {"Matemáticas", "Ciencias", "Historia", "Inglés", "Tecnología"};

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

        mAuth = FirebaseAuth.getInstance();
        etTitulo = root.findViewById(R.id.etTitulo);
        etDescripcion = root.findViewById(R.id.etDescripcion);
        spinnerCategoria = root.findViewById(R.id.spinnerCategoria);
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
        if (getContext() == null) return;
        ArrayAdapter<String> adapterCat = new ArrayAdapter<>(getContext(), R.layout.spinner_item_blanco, categoriasNombres);
        adapterCat.setDropDownViewResource(R.layout.spinner_item_blanco);
        spinnerCategoria.setAdapter(adapterCat);
    }

    private String getCategoriaValor() {
        int pos = spinnerCategoria.getSelectedItemPosition();
        return categoriasValores[Math.max(0, Math.min(pos, categoriasValores.length - 1))];
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void validarYGuardar() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        if (titulo.isEmpty() || imagenUri == null) {
            Toast.makeText(getContext(), "Anade titulo e imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Vuelve a iniciar sesion", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        Uri uriFinal = imagenUri;
        String apunteId = guardarApunteInicial(titulo, user.getUid());
        if (apunteId == null) {
            btnGuardar.setEnabled(true);
            Toast.makeText(getContext(), "No se pudo crear el apunte", Toast.LENGTH_SHORT).show();
            return;
        }

        subirImagenAlBackend(apunteId, uriFinal);
    }

    private String guardarApunteInicial(String titulo, String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("apuntes");
        String apunteId = ref.push().getKey();
        if (apunteId == null) return null;

        Map<String, Object> apunteInicial = new HashMap<>();
        apunteInicial.put("id", apunteId);
        apunteInicial.put("titulo", titulo);
        apunteInicial.put("descripcion", etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "");
        apunteInicial.put("contenido", GenerationStateUtils.APUNTE_GENERANDO);
        apunteInicial.put("estado", "generando");
        apunteInicial.put("categoria", getCategoriaValor());
        apunteInicial.put("userId", userId);
        apunteInicial.put("fecha", System.currentTimeMillis());

        ref.child(apunteId).setValue(apunteInicial).addOnFailureListener(e -> {
            if (getContext() != null)
                Toast.makeText(getContext(), "Error al crear el apunte", Toast.LENGTH_SHORT).show();
        });
        Toast.makeText(getContext(), "Apunte creado. La digitalizacion continua en segundo plano...", Toast.LENGTH_SHORT).show();
        limpiarFormulario();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) getParentFragmentManager().popBackStack();
        }, 700);

        return apunteId;
    }

    private void subirImagenAlBackend(String apunteId, Uri uri) {
        try {
            if (getContext() == null || uri == null) {
                marcarError(apunteId, "Error al leer la imagen. Edita el contenido manualmente.");
                return;
            }

            String contentType = getContext().getContentResolver().getType(uri);
            if (contentType == null) contentType = "image/jpeg";

            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                marcarError(apunteId, "Error al leer la imagen. Edita el contenido manualmente.");
                return;
            }

            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType);
            File file = new File(getContext().getCacheDir(), "apunte_" + System.currentTimeMillis() + "." + (extension != null ? extension : "jpg"));
            try (FileOutputStream outputStream = new FileOutputStream(file); InputStream in = inputStream) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse(contentType), file);
            MultipartBody.Part archivo = MultipartBody.Part.createFormData("archivo", file.getName(), requestFile);

            RetrofitClient.getApunteService().digitalizarArchivo(archivo).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String textoIA = response.body().string();
                            actualizarContenidoApunte(apunteId, textoIA != null && !textoIA.trim().isEmpty()
                                    ? textoIA
                                    : "Error al digitalizar. Edita el contenido manualmente.", "listo");
                        } else {
                            marcarError(apunteId, "Error en el servidor de IA. Edita el contenido manualmente.");
                        }
                    } catch (Exception e) {
                        marcarError(apunteId, "Error al leer la respuesta. Edita el contenido manualmente.");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    marcarError(apunteId, "Error de conexion. Edita el contenido manualmente.");
                }
            });
        } catch (Exception e) {
            marcarError(apunteId, "Error al procesar imagen. Edita el contenido manualmente.");
        }
    }

    private void marcarError(String apunteId, String contenido) {
        actualizarContenidoApunte(apunteId, contenido, "error");
    }

    private void actualizarContenidoApunte(String apunteId, String contenido, String estado) {
        if (apunteId == null || apunteId.trim().isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("apuntes").child(apunteId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("contenido", contenido);
        updates.put("estado", estado);
        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                ref.updateChildren(updates).addOnFailureListener(e -> {});
            }
        });
    }

    private void limpiarFormulario() {
        if (etTitulo != null) etTitulo.setText("");
        if (etDescripcion != null) etDescripcion.setText("");
        imagenUri = null;
        if (ivPreview != null) ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        if (spinnerCategoria != null) spinnerCategoria.setSelection(0);
    }
}
