package es.iescarrillo.aprendeaprueba.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.fragments.CrearApunteFragment;
import es.iescarrillo.aprendeaprueba.models.Apuntes;

public class ApuntesAdapter extends RecyclerView.Adapter<ApuntesAdapter.ViewHolder> {

    private Context context;
    private List<Apuntes> listaApuntes;

    public ApuntesAdapter(Context context, List<Apuntes> listaApuntes) {
        this.context = context;
        this.listaApuntes = listaApuntes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_apunte, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Apuntes apunte = listaApuntes.get(position);
        holder.tvTitulo.setText(apunte.getTitulo());
        holder.tvDescripcion.setText(apunte.getDescripcion());

        holder.cardApunte.setOnClickListener(v -> {
            // 1. Creamos el Fragment del formulario
            CrearApunteFragment fragment = new CrearApunteFragment();

            // 2. Pasamos los datos del apunte usando un Bundle (el equivalente a los extras del Intent)
            Bundle args = new Bundle();
            args.putString("apunteId", apunte.getId());
            args.putString("titulo", apunte.getTitulo());
            args.putString("descripcion", apunte.getDescripcion());
            args.putString("contenido", apunte.getContenido());
            fragment.setArguments(args);

            // 3. Ejecutamos la transición para cambiar de fragmento
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out,
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                        )
                        .replace(R.id.fragment_container, fragment) // El ID de tu MainActivity
                        .addToBackStack(null) // Permite volver a la lista con el botón "Atrás"
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaApuntes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion;
        MaterialCardView cardApunte;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            cardApunte = (MaterialCardView) itemView;
        }
    }
}