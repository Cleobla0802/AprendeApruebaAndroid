package es.iescarrillo.aprendeaprueba.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.adapters.ResumenAdapter;
import es.iescarrillo.aprendeaprueba.models.Resumen;

public class ResumenesFragment extends Fragment {

    private RecyclerView rvResumenes;
    private Button btnIrACrearResumen;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ResumenAdapter adapter;
    private List<Resumen> listaResumenes;

    public ResumenesFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumenes, container, false);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("resumenes");

        // Referencias de la UI
        rvResumenes = view.findViewById(R.id.rvResumenes);
        btnIrACrearResumen = view.findViewById(R.id.btnIrACrearResumen);

        rvResumenes.setLayoutManager(new LinearLayoutManager(getContext()));
        listaResumenes = new ArrayList<>();

        adapter = new ResumenAdapter(listaResumenes, new ResumenAdapter.OnResumenClickListener() {
            @Override
            public void onVerDetallesClick(Resumen resumen) {
                // Aquí abriremos el fragmento de edición/detalle
                abrirDetalleResumen(resumen);
            }

            @Override
            public void onEliminarClick(Resumen resumen) {
                mostrarDialogoConfirmacion(resumen);
            }
        });

        rvResumenes.setAdapter(adapter);

        // Configurar navegación al fragmento de creación
        btnIrACrearResumen.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CrearResumenFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cargarResumenesDesdeFirebase();

        return view;
    }

    private void cargarResumenesDesdeFirebase() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        mDatabase.orderByChild("userId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaResumenes.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Resumen resumen = postSnapshot.getValue(Resumen.class);
                            if (resumen != null) {
                                listaResumenes.add(resumen);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void mostrarDialogoConfirmacion(Resumen resumen) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar resumen")
                .setMessage("¿Borrar '" + resumen.getTitulo() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarResumen(resumen))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarResumen(Resumen resumen) {
        mDatabase.child(resumen.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Resumen eliminado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void abrirDetalleResumen(Resumen resumen) {
        // 1. Creamos el Bundle para pasar el objeto
        Bundle bundle = new Bundle();
        bundle.putSerializable("resumen_objeto", resumen);

        // 2. Creamos la instancia del nuevo fragmento
        DetalleResumenFragment detalleFragment = new DetalleResumenFragment();
        detalleFragment.setArguments(bundle);

        // 3. Realizamos la transición
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detalleFragment)
                .addToBackStack(null) // Para que al dar atrás vuelva a la lista
                .commit();
    }
}