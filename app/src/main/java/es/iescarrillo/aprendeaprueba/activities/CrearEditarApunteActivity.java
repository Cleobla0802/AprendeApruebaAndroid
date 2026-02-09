package es.iescarrillo.aprendeaprueba.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.models.Apuntes;

public class CrearEditarApunteActivity extends AppCompatActivity {

    private EditText etTitulo, etDescripcion, etContenido;
    private Button btnGuardar;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    private String apunteId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_editar_apunte);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etContenido = findViewById(R.id.etContenido);
        btnGuardar = findViewById(R.id.btnGuardar);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Apuntes").child(auth.getCurrentUser().getUid());

        // Revisar si vienen datos de edición
        apunteId = getIntent().getStringExtra("apunteId");
        if (apunteId != null) {
            cargarApunte(apunteId);
        }

        btnGuardar.setOnClickListener(v -> guardarApunte());
    }

    private void cargarApunte(String id) {
        dbRef.child(id).get().addOnSuccessListener(snapshot -> {
            Apuntes apunte = snapshot.getValue(Apuntes.class);
            if (apunte != null) {
                etTitulo.setText(apunte.getTitulo());
                etDescripcion.setText(apunte.getDescripcion());
                etContenido.setText(apunte.getContenido());
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error al cargar apunte", Toast.LENGTH_SHORT).show()
        );
    }

    private void guardarApunte() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();

        if (titulo.isEmpty()) {
            etTitulo.setError("Introduce un título");
            return;
        }

        if (descripcion.isEmpty()) {
            etDescripcion.setError("Introduce una descripción");
            return;
        }

        if (contenido.isEmpty()) {
            etContenido.setError("Introduce contenido");
            return;
        }

        // Si es nuevo, generar ID, si no, usamos el mismo para actualizar
        String id = (apunteId != null) ? apunteId : UUID.randomUUID().toString();

        Apuntes apunte = new Apuntes(id, titulo, descripcion, contenido);
        dbRef.child(id).setValue(apunte).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Apunte guardado", Toast.LENGTH_SHORT).show();
                finish(); // Volver al fragmento
            } else {
                Toast.makeText(this, "Error al guardar apunte", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
