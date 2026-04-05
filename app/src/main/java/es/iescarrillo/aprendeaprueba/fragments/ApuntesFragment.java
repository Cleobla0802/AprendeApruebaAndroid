package es.iescarrillo.aprendeaprueba.fragments;

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

    // Instancia de Firebase Auth
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_apuntes, container, false);

        mAuth = FirebaseAuth.getInstance();
        btnCrearApunte = root.findViewById(R.id.btnCrearApunte);
        recyclerApuntes = root.findViewById(R.id.recyclerApuntes);

        // Configuración del RecyclerView
        recyclerApuntes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ApuntesAdapter(getContext(), listaApuntes);
        recyclerApuntes.setAdapter(adapter);

        // OBTENER USUARIO ACTUAL Y CARGAR DATOS
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            Log.d("FIREBASE_AUTH", "Cargando apuntes para el UID: " + uid);
            cargarApuntesDesdeRailway(uid);
        } else {
            Toast.makeText(getContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
            // Aquí podrías redirigir al LoginActivity si quisieras
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

    private void cargarApuntesDesdeRailway(String uid) {
        RetrofitClient.getApunteService().getApuntesByUser(uid).enqueue(new Callback<List<Apuntes>>() {
            @Override
            public void onResponse(Call<List<Apuntes>> call, Response<List<Apuntes>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaApuntes.clear();
                    listaApuntes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("API_ERROR", "Error: " + response.code());
                    Toast.makeText(getContext(), "No se han podido cargar tus apuntes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Apuntes>> call, Throwable t) {
                Log.e("API_FAILURE", t.getMessage());
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}