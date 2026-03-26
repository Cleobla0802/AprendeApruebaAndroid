package es.iescarrillo.aprendeaprueba.fragments;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.api.RetrofitClient;
import es.iescarrillo.aprendeaprueba.models.Apuntes;
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
    private long apunteId = -1;

    // Configura tu API KEY de ImgBB aquí
    private final String IMGBB_API_KEY = "TU_API_KEY_AQUI";

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imagenUri = uri;
                    ivPreview.setImageTintList(null);
                    ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivPreview.setImageURI(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_crear_apunte, container, false);
        initViews(root);
        setupSpinners();

        btnSeleccionarImagen.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());
        btnGuardar.setOnClickListener(v -> iniciarProcesoGuardado());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            apunteId = getArguments().getLong("apunteId", -1);
            if (apunteId != -1) {
                etTitulo.setText(getArguments().getString("titulo"));
                btnGuardar.setText("Actualizar Apunte");
            }
        }
    }

    private void iniciarProcesoGuardado() {
        if (!validarFormulario()) {
            Toast.makeText(getContext(), "Rellena los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si hay una imagen nueva, la subimos a ImgBB primero
        if (imagenUri != null) {
            subirAImgBB();
        } else {
            // Si no hay imagen nueva (estamos editando), guardamos directamente
            guardarEnSpringBoot(null);
        }
    }

    private void subirAImgBB() {
        try {
            // Convertir URI a File para poder enviarlo
            File file = new File(requireContext().getCacheDir(), "temp_image.jpg");
            InputStream is = requireContext().getContentResolver().openInputStream(imagenUri);
            FileOutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) os.write(buffer, 0, length);
            os.close();
            is.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            // Usamos el servicio de ImgBB (tendrás que definirlo en tu RetrofitClient)
            RetrofitClient.getImgBBService().uploadImage(IMGBB_API_KEY, body).enqueue(new Callback<ImgBBResponse>() {
                @Override
                public void onResponse(Call<ImgBBResponse> call, Response<ImgBBResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String urlFinal = response.body().data.url;
                        guardarEnSpringBoot(urlFinal);
                    }
                }

                @Override
                public void onFailure(Call<ImgBBResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Fallo al subir imagen", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarEnSpringBoot(String urlImagen) {
        Apuntes apunte = new Apuntes();
        apunte.setTitulo(etTitulo.getText().toString());
        apunte.setCategoria(spinnerCategoria.getSelectedItem().toString());
        if (urlImagen != null) apunte.setImagenUrl(urlImagen); // O el nombre que tengas en el modelo

        // Llamada a tu API de Spring Boot
        Call<Apuntes> call = (apunteId == -1)
                ? RetrofitClient.getApunteService().crearApunte(apunte)
                : RetrofitClient.getApunteService().actualizarApunte(apunteId, apunte);

        call.enqueue(new Callback<Apuntes>() {
            @Override
            public void onResponse(Call<Apuntes> call, Response<Apuntes> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Guardado correctamente", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void onFailure(Call<Apuntes> call, Throwable t) {
                Toast.makeText(getContext(), "Error al conectar con la API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ... (Mantén aquí tus métodos initViews, setupSpinners y configurarSpinner del código anterior)
}