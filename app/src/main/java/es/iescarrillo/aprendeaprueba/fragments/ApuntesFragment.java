package es.iescarrillo.aprendeaprueba.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import es.iescarrillo.aprendeaprueba.R;

public class ApuntesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragments_apuntes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button createNotesButton = view.findViewById(R.id.btn_create_notes);
        createNotesButton.setOnClickListener(v -> {
            // Aquí iría la lógica para navegar a la pantalla de creación de apuntes
            Toast.makeText(getContext(), "Abriendo editor de apuntes...", Toast.LENGTH_SHORT).show();
        });

        // Aquí podrías añadir la lógica para comprobar si hay apuntes.
        // Si los hay, podrías ocultar el texto y el botón y mostrar un RecyclerView con los apuntes.
    }
}
