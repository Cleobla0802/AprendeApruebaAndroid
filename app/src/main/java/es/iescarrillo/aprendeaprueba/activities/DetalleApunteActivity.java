package es.iescarrillo.aprendeaprueba.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.models.Apuntes;

public class DetalleApunteActivity extends AppCompatActivity {

    private EditText etTitulo, etDescripcion, etContenido;
    private Button btnGuardar, btnBorrar;

    private String apunteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_apunte);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etContenido = findViewById(R.id.etContenido);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnBorrar = findViewById(R.id.btnBorrar);

        apunteId = getIntent().getStringExtra("apunteId");

        // Cargar datos
        etTitulo.setText(getIntent().getStringExtra("titulo"));
        etDescripcion.setText(getIntent().getStringExtra("descripcion"));
        etContenido.setText(getIntent().getStringExtra("contenido"));

        btnGuardar.setOnClickListener(v -> actualizarApunte());
        btnBorrar.setOnClickListener(v -> borrarApunte());
    }

    private void actualizarApunte() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();

        HashMap<String, Object> update = new HashMap<>();
        update.put("titulo", titulo);
        update.put("descripcion", descripcion);
        update.put("contenido", contenido);

        FirebaseDatabase.getInstance().getReference("apuntes")
                .child(apunteId)
                .updateChildren(update)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Apunte actualizado", Toast.LENGTH_SHORT).show();
                    finish(); // Volver atrás para ver la lista actualizada
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void borrarApunte() {
        FirebaseDatabase.getInstance().getReference("apuntes")
                .child(apunteId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Apunte eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
