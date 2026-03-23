package es.iescarrillo.aprendeaprueba.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.iescarrillo.aprendeaprueba.R;

public class CrearEditarApunteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextInputEditText etTitulo;
    private Spinner spinnerCategoria, spinnerDificultad;
    private MaterialCardView btnSeleccionarImagen;
    private ImageView ivPreview;
    private Button btnLimpiar, btnGuardar;
    private Uri imagenUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_editar_apunte);

        initViews();
        setupSpinners();

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());
        btnGuardar.setOnClickListener(v -> digitalizarIA());
    }

    private void initViews() {
        etTitulo = findViewById(R.id.etTitulo);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerDificultad = findViewById(R.id.spinnerDificultad);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        ivPreview = findViewById(R.id.ivPreview);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        btnGuardar = findViewById(R.id.btnGuardar);
    }

    private void setupSpinners() {
        // Categorías
        List<String> categorias = Arrays.asList("Selecciona categoría", "Matemáticas", "Historia", "Lengua", "Ciencias", "Otros");
        configurarAdaptadorSpinner(spinnerCategoria, categorias);

        // Dificultades
        List<String> dificultades = Arrays.asList("Selecciona dificultad", "Básico", "Intermedio", "Avanzado");
        configurarAdaptadorSpinner(spinnerDificultad, dificultades);
    }

    private void configurarAdaptadorSpinner(Spinner spinner, List<String> items) {
        // Usamos un ArrayAdapter personalizado para forzar el color blanco del texto
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE); // Texto del Spinner cerrado
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.WHITE); // Texto de la lista desplegada
                tv.setBackgroundColor(Color.parseColor("#1C1B1F")); // Fondo de la lista
                return tv;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imagenUri = data.getData();
            ivPreview.setImageTintList(null); // Quitar el gris del icono
            ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivPreview.setImageURI(imagenUri);
        }
    }

    private void limpiarFormulario() {
        etTitulo.setText("");
        spinnerCategoria.setSelection(0);
        spinnerDificultad.setSelection(0);
        imagenUri = null;
        ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        ivPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ivPreview.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#55FFFFFF")));
    }

    private void digitalizarIA() {
        String titulo = etTitulo.getText().toString().trim();
        if (titulo.isEmpty() || spinnerCategoria.getSelectedItemPosition() == 0 || imagenUri == null) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        // Lógica de Retrofit próximamente...
        Toast.makeText(this, "Conectando con la IA...", Toast.LENGTH_SHORT).show();
    }
}