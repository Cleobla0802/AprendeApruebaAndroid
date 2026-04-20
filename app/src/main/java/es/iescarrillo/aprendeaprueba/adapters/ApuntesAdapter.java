package es.iescarrillo.aprendeaprueba.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.fragments.CrearApunteFragment;
import es.iescarrillo.aprendeaprueba.fragments.DetalleApunteFragment;
import es.iescarrillo.aprendeaprueba.models.Apuntes;

public class ApuntesAdapter extends RecyclerView.Adapter<ApuntesAdapter.ViewHolder> {

    private Context context;
    private List<Apuntes> listaApuntes;
    // Definimos la interfaz para el borrado
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDelete(Apuntes apunte, int position);
    }

    public ApuntesAdapter(Context context, List<Apuntes> listaApuntes, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.listaApuntes = listaApuntes;
        this.deleteListener = deleteListener;
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
            DetalleApunteFragment fragment = new DetalleApunteFragment();

            Bundle args = new Bundle();
            args.putString("id", apunte.getId());
            args.putString("titulo", apunte.getTitulo());
            args.putString("contenido", apunte.getContenido());
            args.putString("categoria", apunte.getCategoria());

            fragment.setArguments(args);

            // 3. Navegación
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Clic en el botón borrar
        holder.btnBorrar.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(apunte, position);
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
        ImageButton btnBorrar; // Añadimos la referencia al botón

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            cardApunte = itemView.findViewById(R.id.cardApunte);
            btnBorrar = itemView.findViewById(R.id.btnBorrar); // Buscamos el ID del XML
        }
    }
}