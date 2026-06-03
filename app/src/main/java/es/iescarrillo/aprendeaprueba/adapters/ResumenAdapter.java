package es.iescarrillo.aprendeaprueba.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.models.Resumen;
import es.iescarrillo.aprendeaprueba.utils.GenerationStateUtils;

public class ResumenAdapter extends RecyclerView.Adapter<ResumenAdapter.ResumenViewHolder> {

    private List<Resumen> listaResumenes;
    private final OnResumenClickListener listener;

    public interface OnResumenClickListener {
        void onVerDetallesClick(Resumen resumen);
        void onEliminarClick(Resumen resumen);
    }

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
        boolean generando = GenerationStateUtils.isResumenGenerating(resumen);

        holder.tvCategoria.setText(generando ? "GENERANDO" : resumen.getCategoria());
        holder.tvTitulo.setText(resumen.getTitulo());
        holder.tvFecha.setText(generando ? "Generando contenido en segundo plano..." : resumen.getFechaFormateada());
        holder.itemView.setAlpha(generando ? 0.75f : 1f);

        holder.cardView.setOnClickListener(v -> {
            if (listener == null) return;
            if (generando) {
                Toast.makeText(v.getContext(), "El resumen se esta generando. Espera a que termine para abrirlo.", Toast.LENGTH_LONG).show();
                return;
            }
            listener.onVerDetallesClick(resumen);
        });

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

    public void updateList(List<Resumen> newList) {
        this.listaResumenes = newList;
        notifyDataSetChanged();
    }

    public static class ResumenViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha, tvCategoria;
        ImageButton btnEliminar;
        MaterialCardView cardView;

        public ResumenViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloResumenItem);
            tvFecha = itemView.findViewById(R.id.tvFechaResumenItem);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaResumenItem);
            btnEliminar = itemView.findViewById(R.id.btnEliminarResumen);
            cardView = (MaterialCardView) itemView;
        }
    }
}
