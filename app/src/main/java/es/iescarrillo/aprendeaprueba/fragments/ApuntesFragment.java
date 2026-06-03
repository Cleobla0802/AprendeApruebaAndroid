package es.iescarrillo.aprendeaprueba.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.adapters.ApuntesAdapter;
import es.iescarrillo.aprendeaprueba.models.Apuntes;

public class ApuntesFragment extends Fragment {

    private RecyclerView rvApuntes;
    private ChipGroup chipGroup;
    private ApuntesAdapter adapter;
    private final List<Apuntes> listaCompleta = new ArrayList<>();
    private String textoFiltro = "";
    private String categoriaFiltro = "Todos";
    private Query apuntesQuery;
    private ValueEventListener apuntesListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apuntes, container, false);

        rvApuntes = view.findViewById(R.id.rvItems);
        EditText etSearch = view.findViewById(R.id.etSearch);
        chipGroup = view.findViewById(R.id.chipGroupCategorias);

        rvApuntes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ApuntesAdapter(requireContext(), new ArrayList<>(), (apunte, position) -> confirmarBorrado(apunte));
        rvApuntes.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoFiltro = s.toString().toLowerCase().trim();
                aplicarFiltros();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) categoriaFiltro = chip.getText().toString();
            }
            aplicarFiltros();
        });

        view.findViewById(R.id.btnActionPrincipal).setOnClickListener(v -> {
            if (!isAdded()) return;
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CrearApunteFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cargarDatosFirebase();
        return view;
    }

    private void confirmarBorrado(Apuntes apunte) {
        if (!isAdded() || getContext() == null || apunte == null) return;

        String apunteId = apunte.getId();
        if (apunteId == null || apunteId.trim().isEmpty()) {
            Toast.makeText(getContext(), "No se pudo borrar este apunte", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar apunte")
                .setMessage("¿Borrar '" + apunte.getTitulo() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) ->
                        FirebaseDatabase.getInstance().getReference("apuntes")
                                .child(apunteId)
                                .removeValue()
                                .addOnSuccessListener(a -> {
                                    if (!isAdded() || getContext() == null) return;
                                    Toast.makeText(getContext(), "Apunte eliminado", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    if (!isAdded() || getContext() == null) return;
                                    Toast.makeText(getContext(), "No se pudo eliminar el apunte", Toast.LENGTH_SHORT).show();
                                })
                )
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarChips() {
        if (!isAdded() || getContext() == null || chipGroup == null) return;

        Set<String> categorias = new LinkedHashSet<>();
        for (Apuntes a : listaCompleta) {
            if (a.getCategoria() != null && !a.getCategoria().isEmpty()) {
                categorias.add(a.getCategoria());
            }
        }

        if (chipGroup.getChildCount() > 1) {
            chipGroup.removeViews(1, chipGroup.getChildCount() - 1);
        }

        for (String cat : categorias) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTextColor(0xFFFFFFFF);
            chip.setChipBackgroundColorResource(R.color.chip_state_color);
            chipGroup.addView(chip);
        }

        if (chipGroup.getCheckedChipId() == View.NO_ID) {
            chipGroup.check(R.id.chipAll);
        }
    }

    private void aplicarFiltros() {
        if (adapter == null) return;
        List<Apuntes> listaFiltrada = new ArrayList<>();
        for (Apuntes a : listaCompleta) {
            boolean coincideTexto = a.getTitulo() != null && a.getTitulo().toLowerCase().contains(textoFiltro);
            boolean coincideCategoria = categoriaFiltro.equals("Todos") ||
                    (a.getCategoria() != null && a.getCategoria().equalsIgnoreCase(categoriaFiltro));
            if (coincideTexto && coincideCategoria) listaFiltrada.add(a);
        }
        adapter.updateList(listaFiltrada);
    }

    private void cargarDatosFirebase() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        apuntesQuery = FirebaseDatabase.getInstance().getReference("apuntes")
                .orderByChild("userId").equalTo(uid);
        apuntesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;
                listaCompleta.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Apuntes apunte = data.getValue(Apuntes.class);
                    if (apunte != null) {
                        apunte.setId(data.getKey());
                        listaCompleta.add(apunte);
                    }
                }
                actualizarChips();
                aplicarFiltros();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "No se pudieron cargar los apuntes", Toast.LENGTH_SHORT).show();
            }
        };
        apuntesQuery.addValueEventListener(apuntesListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (apuntesQuery != null && apuntesListener != null) {
            apuntesQuery.removeEventListener(apuntesListener);
        }
        apuntesQuery = null;
        apuntesListener = null;
    }
}
