package es.iescarrillo.aprendeaprueba.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.api.RetrofitClient;
import es.iescarrillo.aprendeaprueba.models.Apuntes;
import es.iescarrillo.aprendeaprueba.models.Resumen;
import es.iescarrillo.aprendeaprueba.models.Test;
import es.iescarrillo.aprendeaprueba.services.TestService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearTipoTestFragment extends Fragment {

    private RadioGroup radioGroupFuente;
    private Spinner spinnerFuente;
    private MaterialButton btnGenerar, btnCancelar;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private List<Apuntes> listaApuntes = new ArrayList<>();
    private List<Resumen> listaResumenes = new ArrayList<>();
    private ArrayAdapter<String> adapterSpinner;
    private List<String> titulosSpinner = new ArrayList<>();

    public CrearTipoTestFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_tipo_test, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        radioGroupFuente = view.findViewById(R.id.radioGroupFuente);
        spinnerFuente = view.findViewById(R.id.spinnerFuente);
        btnGenerar = view.findViewById(R.id.btnGenerarTest);
        btnCancelar = view.findViewById(R.id.btnCancelarCrear);

        adapterSpinner = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_blanco, titulosSpinner);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFuente.setAdapter(adapterSpinner);

        cargarApuntes();

        radioGroupFuente.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioApunte) cargarApuntes();
            else cargarResumenes();
        });

        btnGenerar.setOnClickListener(v -> generarTest());
        btnCancelar.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void cargarApuntes() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("apuntes").orderByChild("userId").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaApuntes.clear();
                        titulosSpinner.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Apuntes a = ds.getValue(Apuntes.class);
                            if (a != null) {
                                a.setId(ds.getKey());
                                listaApuntes.add(a);
                                titulosSpinner.add(a.getTitulo());
                            }
                        }
                        adapterSpinner.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void cargarResumenes() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("resumenes").orderByChild("userId").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaResumenes.clear();
                        titulosSpinner.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Resumen r = ds.getValue(Resumen.class);
                            if (r != null) {
                                r.setId(ds.getKey());
                                listaResumenes.add(r);
                                titulosSpinner.add(r.getTitulo());
                            }
                        }
                        adapterSpinner.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void generarTest() {
        if (titulosSpinner.isEmpty()) {
            Toast.makeText(getContext(), "No hay elementos disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spinnerFuente.getSelectedItemPosition();
        boolean esApunte = radioGroupFuente.getCheckedRadioButtonId() == R.id.radioApunte;

        String contenido, titulo, categoria;

        if (esApunte) {
            Apuntes apunte = listaApuntes.get(pos);
            contenido = apunte.getContenido();
            titulo = apunte.getTitulo();
            categoria = apunte.getCategoria();
        } else {
            Resumen resumen = listaResumenes.get(pos);
            contenido = resumen.getContenido();
            titulo = resumen.getTitulo();
            categoria = resumen.getCategoria();
        }

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, String> body = new HashMap<>();
        body.put("contenido", contenido);
        body.put("userId", uid);
        body.put("titulo", titulo);
        body.put("categoria", categoria);

        btnGenerar.setEnabled(false);
        btnGenerar.setText("Generando...");

        TestService service = RetrofitClient.getTestService();
        service.generarTest(body).enqueue(new Callback<Test>() {
            @Override
            public void onResponse(@NonNull Call<Test> call, @NonNull Response<Test> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Test test = response.body();
                    if (test.getPreguntas() == null || test.getPreguntas().isEmpty()) {
                        Toast.makeText(getContext(), "La IA no pudo generar preguntas, inténtalo de nuevo", Toast.LENGTH_SHORT).show();
                        resetBoton();
                        return;
                    }
                    test.setUserId(uid);
                    test.setFecha(System.currentTimeMillis());
                    test.setCompletado(false);
                    test.setUltimaNota(0);

                    DatabaseReference testsRef = FirebaseDatabase.getInstance().getReference("tests");
                    String nuevoId = testsRef.push().getKey();
                    test.setId(nuevoId);
                    testsRef.child(nuevoId).setValue(test)
                            .addOnSuccessListener(a -> {
                                Toast.makeText(getContext(), "¡Test creado!", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                                resetBoton();
                            });
                } else {
                    Toast.makeText(getContext(), "Error al generar el test", Toast.LENGTH_SHORT).show();
                    resetBoton();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Test> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetBoton();
            }
        });
    }

    private void resetBoton() {
        btnGenerar.setEnabled(true);
        btnGenerar.setText("Generar Test");
    }
}