package es.iescarrillo.aprendeaprueba.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.models.Resumen;

public class ResumenAdapter extends RecyclerView.Adapter<ResumenAdapter.ResumenViewHolder> {

    private List<Resumen> listaResumenes;
    private OnResumenClickListener listener; // 1. Declarar la interfaz

    // 2. Definir la interfaz para los clics
    public interface OnResumenClickListener {
        void onVerDetallesClick(Resumen resumen);
        void onEliminarClick(Resumen resumen);
    }

    // 3. Actualizar el constructor para recibir el listener
    public ResumenAdapter(List<Resumen> listaResumenes, OnResumenClickListener listener) {
        this.listaResumenes = listaResumenes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResumenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resumen, parent, false);
        return new ResumenViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ResumenViewHolder holder, int position) {
        Resumen resumen = listaResumenes.get(position);

        holder.tvCategoria.setText(resumen.getCategoria());
        holder.tvTitulo.setText(resumen.getTitulo());
        holder.tvFecha.setText(resumen.getFecha()); // Si es String, no hace falta String.valueOf

        // 4. Configurar el botón de Ver Detalles
        holder.btnVerDetalles.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVerDetallesClick(resumen);
            }
        });

        // 5. Configurar el botón de eliminar (usando el listener para que el Fragment decida)
        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarClick(resumen);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaResumenes.size();
    }

    public static class ResumenViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha, tvCategoria;
        ImageView btnEliminar, btnVerDetalles; // Añadido btnVerDetalles

        public ResumenViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloResumenItem);
            tvFecha = itemView.findViewById(R.id.tvFechaResumenItem);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaResumenItem);
            btnEliminar = itemView.findViewById(R.id.btnEliminarResumen);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles); // Inicializado
        }
    }
}