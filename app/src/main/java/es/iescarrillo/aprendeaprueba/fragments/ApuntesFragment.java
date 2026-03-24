package es.iescarrillo.aprendeaprueba.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.iescarrillo.aprendeaprueba.fragments.CrearApunteFragment;

import es.iescarrillo.aprendeaprueba.R;

public class ApuntesFragment extends Fragment {

    private Button btnCrearApunte;
    private RecyclerView recyclerApuntes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflamos tu layout actual (el que me pasaste con el botón y el recycler)
        View root = inflater.inflate(R.layout.fragment_apuntes, container, false);

        btnCrearApunte = root.findViewById(R.id.btnCrearApunte);
        recyclerApuntes = root.findViewById(R.id.recyclerApuntes);

        // Configuración básica del Recycler
        recyclerApuntes.setLayoutManager(new LinearLayoutManager(getContext()));
        // Aquí iría tu adaptador...

        // LÓGICA DE NAVEGACIÓN
        btnCrearApunte.setOnClickListener(v -> {
            // Creamos el nuevo fragmento del formulario
            CrearApunteFragment formulario = new CrearApunteFragment();

            // Realizamos el cambio dentro del fragment_container de la MainActivity
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    )
                    .replace(R.id.fragment_container, formulario)
                    .addToBackStack(null) // Esto permite volver a la lista con el botón "Atrás"
                    .commit();
        });

        return root;
    }
}