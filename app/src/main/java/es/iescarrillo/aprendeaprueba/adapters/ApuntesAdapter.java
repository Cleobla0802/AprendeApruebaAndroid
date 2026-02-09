package es.iescarrillo.aprendeaprueba.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.activities.CrearEditarApunteActivity;
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
            Intent intent = new Intent(context, CrearEditarApunteActivity.class);
            intent.putExtra("apunteId", apunte.getId());
            intent.putExtra("titulo", apunte.getTitulo());
            intent.putExtra("descripcion", apunte.getDescripcion());
            intent.putExtra("contenido", apunte.getContenido());
            context.startActivity(intent);
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
