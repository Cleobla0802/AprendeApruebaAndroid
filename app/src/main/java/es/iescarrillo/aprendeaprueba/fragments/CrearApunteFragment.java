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

import java.util.Arrays;
import java.util.List;

import es.iescarrillo.aprendeaprueba.R;

public class CrearApunteFragment extends Fragment {

    private TextInputEditText etTitulo;
    private Spinner spinnerCategoria, spinnerDificultad;
    private MaterialCardView btnSeleccionarImagen;
    private ImageView ivPreview;
    private Button btnLimpiar, btnGuardar;
    private Uri imagenUri;

    // Lanzador para la galería
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

        btnGuardar.setOnClickListener(v -> {
            if (validarFormulario()) {
                Toast.makeText(getContext(), "Digitalizando con IA...", Toast.LENGTH_SHORT).show();
                // Aquí irá la llamada a Retrofit más adelante
            } else {
                Toast.makeText(getContext(), "Faltan datos obligatorios", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void initViews(View v) {
        etTitulo = v.findViewById(R.id.etTitulo);
        spinnerCategoria = v.findViewById(R.id.spinnerCategoria);
        spinnerDificultad = v.findViewById(R.id.spinnerDificultad);
        btnSeleccionarImagen = v.findViewById(R.id.btnSeleccionarImagen);
        ivPreview = v.findViewById(R.id.ivPreview);
        btnLimpiar = v.findViewById(R.id.btnLimpiar);
        btnGuardar = v.findViewById(R.id.btnGuardar);
    }

    private void setupSpinners() {
        List<String> categorias = Arrays.asList("Selecciona categoría", "Matemáticas", "Historia", "Lengua", "Ciencias", "Otros");
        configurarSpinner(spinnerCategoria, categorias);

        List<String> dificultades = Arrays.asList("Selecciona dificultad", "Básico", "Intermedio", "Avanzado");
        configurarSpinner(spinnerDificultad, dificultades);
    }

    private void configurarSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE); // Texto blanco cuando está cerrado
                return tv;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.WHITE); // Texto blanco en la lista
                tv.setBackgroundColor(Color.parseColor("#1C1B1F")); // Fondo oscuro en la lista
                return tv;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private boolean validarFormulario() {
        return !etTitulo.getText().toString().trim().isEmpty()
                && spinnerCategoria.getSelectedItemPosition() > 0
                && imagenUri != null;
    }

    private void limpiarFormulario() {
        etTitulo.setText("");
        spinnerCategoria.setSelection(0);
        spinnerDificultad.setSelection(0);
        imagenUri = null;
        ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        ivPreview.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#55FFFFFF")));
        ivPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }
}