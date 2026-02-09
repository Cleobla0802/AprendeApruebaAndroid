package es.iescarrillo.aprendeaprueba.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.activities.CrearEditarApunteActivity;
import es.iescarrillo.aprendeaprueba.adapters.ApuntesAdapter;
import es.iescarrillo.aprendeaprueba.models.Apuntes;

public class ApuntesFragment extends Fragment {

    private RecyclerView recyclerApuntes;
    private Button btnCrearApunte;

    private ApuntesAdapter adapter;
    private List<Apuntes> listaApuntes;

    private DatabaseReference databaseReference;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragments_apuntes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerApuntes = view.findViewById(R.id.recyclerApuntes);
        btnCrearApunte = view.findViewById(R.id.btnCrearApunte);

        listaApuntes = new ArrayList<>();
        adapter = new ApuntesAdapter(getContext(), listaApuntes);
        recyclerApuntes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerApuntes.setAdapter(adapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Apuntes").child(userId);

        btnCrearApunte.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CrearEditarApunteActivity.class));
        });

        cargarApuntes();
    }

    private void cargarApuntes() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaApuntes.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Apuntes apunte = ds.getValue(Apuntes.class);
                    if (apunte != null) listaApuntes.add(apunte);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar apuntes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
