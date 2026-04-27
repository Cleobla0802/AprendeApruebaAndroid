package es.iescarrillo.aprendeaprueba.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.adapters.TestAdapter;
import es.iescarrillo.aprendeaprueba.models.Test;

public class PruebasFragment extends Fragment {

    private RecyclerView rvPruebas;
    private MaterialButton btnCrearPrueba;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private TestAdapter adapter;
    private List<Test> listaPruebas;

    public PruebasFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tipo_test, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("tests");

        rvPruebas = view.findViewById(R.id.rvPruebas);
        btnCrearPrueba = view.findViewById(R.id.btnCrearPrueba);

        rvPruebas.setLayoutManager(new LinearLayoutManager(getContext()));
        listaPruebas = new ArrayList<>();

        adapter = new TestAdapter(listaPruebas, new TestAdapter.OnTestClickListener() {
            @Override
            public void onRealizarClick(Test test) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("test_objeto", test);
                RealizarTipoTestFragment realizarFragment = new RealizarTipoTestFragment();
                realizarFragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, realizarFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onEliminarClick(Test test) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Eliminar prueba")
                        .setMessage("¿Borrar '" + test.getTitulo() + "'?")
                        .setPositiveButton("Eliminar", (dialog, which) ->
                                mDatabase.child(test.getId()).removeValue()
                                        .addOnSuccessListener(a -> Toast.makeText(getContext(), "Prueba eliminada", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()))
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        rvPruebas.setAdapter(adapter);

        btnCrearPrueba.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CrearTipoTestFragment())
                        .addToBackStack(null)
                        .commit());

        cargarPruebas();
        return view;
    }

    private void cargarPruebas() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        mDatabase.orderByChild("userId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaPruebas.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Test test = ds.getValue(Test.class);
                            if (test != null) {
                                test.setId(ds.getKey());
                                listaPruebas.add(test);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}