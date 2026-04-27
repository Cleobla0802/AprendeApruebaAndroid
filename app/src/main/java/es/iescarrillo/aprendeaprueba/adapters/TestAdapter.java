package es.iescarrillo.aprendeaprueba.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.models.Test;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

    public interface OnTestClickListener {
        void onRealizarClick(Test test);
        void onEliminarClick(Test test);
    }

    private final List<Test> lista;
    private final OnTestClickListener listener;

    public TestAdapter(List<Test> lista, OnTestClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        Test test = lista.get(position);
        holder.tvTitulo.setText(test.getTitulo());
        holder.tvCategoria.setText(test.getCategoria());
        holder.tvNota.setText(test.isCompletado() ? test.getUltimaNota() + "%" : "Sin nota");
        holder.btnRealizar.setOnClickListener(v -> listener.onRealizarClick(test));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminarClick(test));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class TestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvCategoria, tvNota;
        MaterialButton btnRealizar, btnEliminar;

        TestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloTest);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaTest);
            tvNota = itemView.findViewById(R.id.tvNotaTest);
            btnRealizar = itemView.findViewById(R.id.btnRealizarTest);
            btnEliminar = itemView.findViewById(R.id.btnEliminarTest);
        }
    }
}