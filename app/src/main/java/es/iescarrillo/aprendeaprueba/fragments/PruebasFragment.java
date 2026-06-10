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
import es.iescarrillo.aprendeaprueba.adapters.TestAdapter;
import es.iescarrillo.aprendeaprueba.models.Test;
import es.iescarrillo.aprendeaprueba.utils.GenerationStateUtils;

public class PruebasFragment extends Fragment {

    private RecyclerView rvPruebas;
    private ChipGroup chipGroup;
    private TestAdapter adapter;
    private List<Test> listaCompleta = new ArrayList<>();
    private String textoFiltro = "";
    private String categoriaFiltro = "Todos";
    private Query testsQuery;
    private ValueEventListener testsListener;

    /**
     * Infla el layout del fragmento, inicializa el RecyclerView con su adaptador,
     * configura el buscador, los chips de categoría y el botón para crear un nuevo test.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tipo_test, container, false);

        rvPruebas = view.findViewById(R.id.rvItems);
        EditText etSearch = view.findViewById(R.id.etSearch);
        chipGroup = view.findViewById(R.id.chipGroupCategorias);

        rvPruebas.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TestAdapter(new ArrayList<>(), new TestAdapter.OnTestClickListener() {
            @Override
            public void onRealizarClick(Test test) {
                if (GenerationStateUtils.isTestGenerating(test)) {
                    Toast.makeText(getContext(), "El test aun esta generando preguntas. Espera a que termine.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (GenerationStateUtils.isTestError(test)) {
                    Toast.makeText(getContext(), "Este test no tiene preguntas. Borralo y vuelve a generarlo.", Toast.LENGTH_LONG).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("test_objeto", test);
                RealizarTipoTestFragment fragment = new RealizarTipoTestFragment();
                fragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onEliminarClick(Test test) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Eliminar test")
                        .setMessage("¿Borrar '" + test.getTitulo() + "'?")
                        .setPositiveButton("Eliminar", (dialog, which) ->
                                FirebaseDatabase.getInstance().getReference("tests")
                                        .child(test.getId()).removeValue()
                                        .addOnSuccessListener(a -> {
                                            if (getContext() != null)
                                                Toast.makeText(getContext(), "Test eliminado", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (getContext() != null)
                                                Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show();
                                        })
                        )
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        rvPruebas.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
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

        view.findViewById(R.id.btnActionPrincipal).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CrearTipoTestFragment())
                        .addToBackStack(null)
                        .commit()
        );

        cargarDatosFirebase();
        return view;
    }

    /**
     * Regenera los chips de categoría a partir de los tests cargados.
     * Conserva siempre el chip "Todos" en primera posición y evita duplicados
     * usando un LinkedHashSet para mantener el orden de inserción.
     */
    private void actualizarChips() {
        if (!isAdded()) return;
        Set<String> categorias = new LinkedHashSet<>();
        for (Test t : listaCompleta) {
            if (t.getCategoria() != null && !t.getCategoria().isEmpty())
                categorias.add(t.getCategoria());
        }

        chipGroup.removeViews(1, chipGroup.getChildCount() - 1);

        for (String cat : categorias) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTextColor(0xFFFFFFFF);
            chip.setChipBackgroundColorResource(R.color.chip_state_color);
            chipGroup.addView(chip);
        }

        if (chipGroup.getCheckedChipId() == View.NO_ID)
            chipGroup.check(R.id.chipAll);
    }

    /**
     * Filtra la lista completa de tests según el texto del buscador
     * y la categoría seleccionada, y actualiza el adaptador con el resultado.
     */
    private void aplicarFiltros() {
        if (!isAdded()) return;
        List<Test> filtrada = new ArrayList<>();
        for (Test t : listaCompleta) {
            boolean matchTexto = t.getTitulo() != null && t.getTitulo().toLowerCase().contains(textoFiltro);
            boolean matchCat = categoriaFiltro.equals("Todos") ||
                    (t.getCategoria() != null && t.getCategoria().equalsIgnoreCase(categoriaFiltro));
            if (matchTexto && matchCat) filtrada.add(t);
        }
        adapter.updateList(filtrada);
    }

    /**
     * Registra un listener en tiempo real en Firebase que escucha los tests
     * del usuario actual y actualiza la lista, los chips y los filtros ante cualquier cambio.
     */
    private void cargarDatosFirebase() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        testsQuery = FirebaseDatabase.getInstance().getReference("tests")
                .orderByChild("userId").equalTo(uid);
        testsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaCompleta.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Test t = data.getValue(Test.class);
                    if (t != null) { t.setId(data.getKey()); listaCompleta.add(t); }
                }
                actualizarChips();
                aplicarFiltros();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        };
        testsQuery.addValueEventListener(testsListener);
    }

    /**
     * Al destruir la vista se elimina el listener de Firebase
     * para evitar fugas de memoria y callbacks sobre vistas ya destruidas.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (testsQuery != null && testsListener != null) {
            testsQuery.removeEventListener(testsListener);
        }
        testsQuery = null;
        testsListener = null;
    }
}