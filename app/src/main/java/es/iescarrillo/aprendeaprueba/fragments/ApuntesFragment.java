package es.iescarrillo.aprendeaprueba.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.adapters.ApuntesAdapter;
import es.iescarrillo.aprendeaprueba.api.RetrofitClient;
import es.iescarrillo.aprendeaprueba.models.Apuntes;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApuntesFragment extends Fragment {

    private Button btnCrearApunte;
    private RecyclerView recyclerApuntes;
    private ApuntesAdapter adapter;
    private List<Apuntes> listaApuntes = new ArrayList<>();
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_apuntes, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("apuntes");

        btnCrearApunte = root.findViewById(R.id.btnCrearApunte);
        recyclerApuntes = root.findViewById(R.id.recyclerApuntes);
        recyclerApuntes.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ApuntesAdapter(getContext(), listaApuntes, (apunte, position) -> {
            mostrarDialogoConfirmacion(apunte, position);
        });
        recyclerApuntes.setAdapter(adapter);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            escucharCambiosFirebase(currentUser.getUid());
        }

        btnCrearApunte.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CrearApunteFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return root;
    }

    private void escucharCambiosFirebase(String uid) {
        // Query para filtrar por usuario
        mDatabase.orderByChild("userId").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaApuntes.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Apuntes apunte = ds.getValue(Apuntes.class);
                    if (apunte != null) {
                        apunte.setId(ds.getKey());
                        listaApuntes.add(apunte);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al leer datos", error.toException());
            }
        });
    }

    private void mostrarDialogoConfirmacion(Apuntes apunte, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar apunte")
                .setMessage("¿Borrar '" + apunte.getTitulo() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Eliminamos directamente de Firebase para que sea instantáneo
                    mDatabase.child(apunte.getId()).removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Apunte eliminado", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}