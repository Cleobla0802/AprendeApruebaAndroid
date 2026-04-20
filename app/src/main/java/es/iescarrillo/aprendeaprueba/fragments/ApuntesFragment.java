package es.iescarrillo.aprendeaprueba.fragments;

import android.app.AlertDialog; // Importante para el diálogo de confirmación
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_apuntes, container, false);

        mAuth = FirebaseAuth.getInstance();
        btnCrearApunte = root.findViewById(R.id.btnCrearApunte);
        recyclerApuntes = root.findViewById(R.id.recyclerApuntes);

        recyclerApuntes.setLayoutManager(new LinearLayoutManager(getContext()));

        // --- CAMBIO AQUÍ: Inicializamos el adapter con el listener de borrado ---
        adapter = new ApuntesAdapter(getContext(), listaApuntes, (apunte, position) -> {
            // Cuando se pulsa el icono de la papelera, llamamos al diálogo de confirmación
            mostrarDialogoConfirmacion(apunte, position);
        });
        recyclerApuntes.setAdapter(adapter);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            cargarApuntesDesdeRailway(currentUser.getUid());
        }

        btnCrearApunte.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new CrearApunteFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return root;
    }

    // --- NUEVO MÉTODO: Diálogo de confirmación ---
    private void mostrarDialogoConfirmacion(Apuntes apunte, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar apunte")
                .setMessage("¿Estás seguro de que quieres borrar '" + apunte.getTitulo() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarApunteDesdeAPI(apunte.getId(), position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // --- NUEVO MÉTODO: Llamada a tu Service de Retrofit ---
    private void eliminarApunteDesdeAPI(String id, int position) {
        RetrofitClient.getApunteService().eliminarApunte(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    // Eliminamos de la lista local y avisamos al adapter
                    listaApuntes.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Apunte eliminado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarApuntesDesdeRailway(String uid) {
        RetrofitClient.getApunteService().getApuntesByUser(uid).enqueue(new Callback<List<Apuntes>>() {
            @Override
            public void onResponse(Call<List<Apuntes>> call, Response<List<Apuntes>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    listaApuntes.clear();
                    listaApuntes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("API_ERROR", "Código: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<Apuntes>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("API_FAILURE", t.getMessage());
            }
        });
    }
}