package es.iescarrillo.aprendeaprueba.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.adapters.ResumenAdapter;
import es.iescarrillo.aprendeaprueba.models.Resumen;

public class ResumenesFragment extends Fragment {

    private RecyclerView rvResumenes;
    private Button btnIrACrearResumen;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ResumenAdapter adapter;
    private ChipGroup chipGroup;
    private List<Resumen> listaResumenes = new ArrayList<>();
    private String textoFiltro = "";
    private String categoriaFiltro = "Todos";
    private Query resumenesQuery;
    private ValueEventListener resumenesListener;

    public ResumenesFragment() {}

    /**
     * Inicializa la vista del fragmento: enlaza los componentes del layout,
     * configura el RecyclerView con su adaptador, el buscador de texto,
     * el filtro por categorías y el botón para crear un nuevo resumen.
     * Al final lanza la carga de datos desde Firebase.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumenes, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("resumenes");

        rvResumenes = view.findViewById(R.id.rvItems);
        btnIrACrearResumen = view.findViewById(R.id.btnActionPrincipal);
        EditText etSearch = view.findViewById(R.id.etSearch);
        chipGroup = view.findViewById(R.id.chipGroupCategorias);

        rvResumenes.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ResumenAdapter(listaResumenes, new ResumenAdapter.OnResumenClickListener() {
            @Override
            public void onVerDetallesClick(Resumen resumen) {
                abrirDetalleResumen(resumen);
            }
            @Override
            public void onEliminarClick(Resumen resumen) {
                mostrarDialogoConfirmacion(resumen);
            }
        });

        rvResumenes.setAdapter(adapter);

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

        btnIrACrearResumen.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CrearResumenFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cargarResumenesDesdeFirebase();
        return view;
    }

    /**
     * Recorre la lista de resúmenes cargados y genera dinámicamente un chip
     * por cada categoría única. Mantiene siempre el chip "Todos" como primer elemento.
     * Si ningún chip está seleccionado, selecciona "Todos" por defecto.
     */
    private void actualizarChips() {
        if (!isAdded() || getContext() == null) return;
        Set<String> categorias = new LinkedHashSet<>();
        for (Resumen r : listaResumenes) {
            if (r.getCategoria() != null && !r.getCategoria().isEmpty())
                categorias.add(r.getCategoria());
        }

        int childCount = chipGroup.getChildCount();
        if (childCount > 1) chipGroup.removeViews(1, childCount - 1);

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
     * Filtra la lista de resúmenes según el texto introducido en el buscador
     * y la categoría seleccionada en los chips. Actualiza el adaptador con el resultado.
     */
    private void aplicarFiltros() {
        if (!isAdded() || getContext() == null) return;
        List<Resumen> filtrada = new ArrayList<>();
        for (Resumen r : listaResumenes) {
            boolean matchTexto = r.getTitulo() != null && r.getTitulo().toLowerCase().contains(textoFiltro);
            boolean matchCat = categoriaFiltro.equals("Todos") ||
                    (r.getCategoria() != null && r.getCategoria().equalsIgnoreCase(categoriaFiltro));
            if (matchTexto && matchCat) filtrada.add(r);
        }
        adapter.updateList(filtrada);
    }

    /**
     * Registra un listener en tiempo real sobre Firebase que carga todos los resúmenes
     * del usuario autenticado. Cada vez que hay cambios en la base de datos, reconstruye
     * la lista, actualiza los chips de categoría y aplica los filtros activos.
     */
    private void cargarResumenesDesdeFirebase() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        resumenesQuery = mDatabase.orderByChild("userId").equalTo(uid);
        resumenesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;
                listaResumenes.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    try {
                        Resumen resumen = new Resumen();
                        resumen.setId(postSnapshot.getKey());
                        resumen.setUserId(postSnapshot.child("userId").getValue(String.class));
                        resumen.setTitulo(postSnapshot.child("titulo").getValue(String.class));
                        resumen.setDescripcion(postSnapshot.child("descripcion").getValue(String.class));
                        resumen.setResumenTexto(postSnapshot.child("resumenTexto").getValue(String.class));
                        resumen.setEstado(postSnapshot.child("estado").getValue(String.class));
                        resumen.setCategoria(postSnapshot.child("categoria").getValue(String.class));
                        resumen.setIdApunteOriginal(postSnapshot.child("idApunteOriginal").getValue(String.class));
                        resumen.setContenidoHash(postSnapshot.child("contenidoHash").getValue(String.class));
                        Object fechaRaw = postSnapshot.child("fecha").getValue();
                        resumen.setFecha(fechaRaw instanceof Long ? (Long) fechaRaw : 0L);
                        listaResumenes.add(resumen);
                    } catch (Exception ignored) {}
                }
                actualizarChips();
                aplicarFiltros();
            }

            /**
             * Se ejecuta si Firebase cancela la escucha por un error de permisos u otro problema.
             * Muestra el mensaje de error al usuario.
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        resumenesQuery.addValueEventListener(resumenesListener);
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar un resumen,
     * indicando el título del resumen afectado para evitar borrados accidentales.
     *
     * @param resumen El resumen que se quiere eliminar
     */
    private void mostrarDialogoConfirmacion(Resumen resumen) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar resumen")
                .setMessage("¿Borrar '" + resumen.getTitulo() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarResumen(resumen))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Elimina el resumen indicado de Firebase usando su ID como clave.
     * Muestra un Toast según el resultado de la operación.
     *
     * @param resumen El resumen a eliminar
     */
    private void eliminarResumen(Resumen resumen) {
        mDatabase.child(resumen.getId()).removeValue()
                .addOnSuccessListener(a -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Resumen eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navega al fragmento de detalle pasando el resumen seleccionado como argumento serializado.
     *
     * @param resumen El resumen cuyo detalle se quiere visualizar
     */
    private void abrirDetalleResumen(Resumen resumen) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("resumen_objeto", resumen);
        DetalleResumenFragment detalleFragment = new DetalleResumenFragment();
        detalleFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detalleFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Limpia el listener de Firebase al destruir la vista para evitar
     * fugas de memoria y callbacks sobre un fragmento ya desvinculado.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (resumenesQuery != null && resumenesListener != null) {
            resumenesQuery.removeEventListener(resumenesListener);
        }
        resumenesQuery = null;
        resumenesListener = null;
    }
}