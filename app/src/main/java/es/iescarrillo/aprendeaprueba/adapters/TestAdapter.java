package es.iescarrillo.aprendeaprueba.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.models.Test;
import es.iescarrillo.aprendeaprueba.utils.GenerationStateUtils;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

    private List<Test> listaTests;
    private final OnTestClickListener listener;

    public interface OnTestClickListener {
        void onRealizarClick(Test test);
        void onEliminarClick(Test test);
    }

    public TestAdapter(List<Test> listaTests, OnTestClickListener listener) {
        this.listaTests = listaTests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tipo_test, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        Test test = listaTests.get(position);
        boolean generando = GenerationStateUtils.isTestGenerating(test);
        boolean error = GenerationStateUtils.isTestError(test);

        holder.tvTitulo.setText(test.getTitulo());
        holder.tvDescripcion.setText(generando
                ? "Generando preguntas en segundo plano..."
                : (error ? "No se pudieron generar preguntas. Puedes borrar este test y volver a intentarlo." : (test.getDescripcion() != null ? test.getDescripcion() : "Sin descripcion")));
        holder.tvCategoria.setText(generando ? "GENERANDO" : (error ? "ERROR" : (test.getCategoria() != null ? test.getCategoria().toUpperCase() : "")));
        holder.card.setAlpha(generando ? 0.75f : 1f);

        if (generando) {
            holder.tvNota.setVisibility(View.GONE);
            holder.card.setStrokeWidth(2);
            holder.card.setStrokeColor(0xFFE7D7FF);
            holder.btnRealizar.setText("Generando");
        } else if (error) {
            holder.tvNota.setVisibility(View.GONE);
            holder.card.setStrokeWidth(2);
            holder.card.setStrokeColor(0xFFFF4444);
            holder.btnRealizar.setText("No generado");
        } else if (test.isCompletado() || test.getCalificacion() != null) {
            Double calif = test.getCalificacion();
            int nota = test.getUltimaNota() > 0
                    ? test.getUltimaNota()
                    : (calif != null ? (int) Math.round(calif * 10) : 0);
            holder.tvNota.setVisibility(View.VISIBLE);
            holder.tvNota.setText(nota + "%");

            if (nota >= 75) {
                holder.tvNota.setTextColor(0xFF4CAF50);
                holder.card.setStrokeColor(0xFF4CAF50);
            } else if (nota >= 50) {
                holder.tvNota.setTextColor(0xFFFF9800);
                holder.card.setStrokeColor(0xFFFF9800);
            } else {
                holder.tvNota.setTextColor(0xFFFF4444);
                holder.card.setStrokeColor(0xFFFF4444);
            }
            holder.card.setStrokeWidth(2);
            holder.btnRealizar.setText("Repetir");
        } else {
            holder.tvNota.setVisibility(View.GONE);
            holder.card.setStrokeWidth(0);
            holder.btnRealizar.setText("Realizar");
        }

        holder.btnRealizar.setOnClickListener(v -> {
            if (listener != null) listener.onRealizarClick(test);
        });

        holder.card.setOnClickListener(v -> {
            if (listener != null) listener.onRealizarClick(test);
        });

        holder.btnBorrar.setOnClickListener(v -> {
            if (listener != null) listener.onEliminarClick(test);
        });
    }

    @Override
    public int getItemCount() {
        return listaTests.size();
    }

    public void updateList(List<Test> newList) {
        this.listaTests = newList;
        notifyDataSetChanged();
    }

    public static class TestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvCategoria, tvNota;
        ImageButton btnBorrar;
        MaterialButton btnRealizar;
        MaterialCardView card;

        public TestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaTag);
            tvNota = itemView.findViewById(R.id.tvNota);
            btnBorrar = itemView.findViewById(R.id.btnBorrar);
            btnRealizar = itemView.findViewById(R.id.btnRealizar);
            card = (MaterialCardView) itemView;
        }
    }
}
