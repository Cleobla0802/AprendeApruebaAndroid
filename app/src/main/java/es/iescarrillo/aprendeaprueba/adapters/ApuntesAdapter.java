package es.iescarrillo.aprendeaprueba.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.fragments.DetalleApunteFragment;
import es.iescarrillo.aprendeaprueba.models.Apuntes;
import es.iescarrillo.aprendeaprueba.utils.GenerationStateUtils;

public class ApuntesAdapter extends RecyclerView.Adapter<ApuntesAdapter.ViewHolder> {

    private final Context context;
    private List<Apuntes> listaApuntes;
    private final OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDelete(Apuntes apunte, int position);
    }

    /**
     * Constructor del adaptador.
     * @param context Contexto de la actividad o fragmento que lo instancia.
     * @param listaApuntes Lista de apuntes a mostrar en el RecyclerView.
     * @param deleteListener Callback que se ejecuta al pulsar el botón de borrar.
     */
    public ApuntesAdapter(Context context, List<Apuntes> listaApuntes, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.listaApuntes = listaApuntes;
        this.deleteListener = deleteListener;
    }

    /**
     * Infla el layout de cada tarjeta de apunte y crea su ViewHolder.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_apunte, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Rellena los datos de un apunte en su tarjeta correspondiente.
     * Si el apunte está siendo generado, muestra la tarjeta semitransparente
     * y bloquea la navegación al detalle hasta que termine.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Apuntes apunte = listaApuntes.get(position);
        boolean generando = GenerationStateUtils.isApunteGenerating(apunte);

        holder.tvTitulo.setText(apunte.getTitulo());
        String desc = apunte.getDescripcion();
        holder.tvDescripcion.setText(desc != null && !desc.isEmpty() ? desc : apunte.getContenido());
        holder.tvCategoriaTag.setText(generando ? "GENERANDO" : (apunte.getCategoria() != null ? apunte.getCategoria() : "Sin categoria"));
        holder.cardApunte.setAlpha(generando ? 0.75f : 1f);

        holder.cardApunte.setOnClickListener(v -> {
            if (generando) {
                Toast.makeText(context, "El apunte se esta digitalizando. Espera a que termine para abrirlo.", Toast.LENGTH_LONG).show();
                return;
            }

            DetalleApunteFragment fragment = new DetalleApunteFragment();
            Bundle args = new Bundle();
            args.putString("id", apunte.getId());
            args.putString("titulo", apunte.getTitulo());
            args.putString("descripcion", apunte.getDescripcion());
            args.putString("contenido", apunte.getContenido());
            args.putString("categoria", apunte.getCategoria());
            fragment.setArguments(args);

            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        holder.btnBorrar.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(apunte, position);
            }
        });
    }

    /**
     * Devuelve el número total de apuntes en la lista.
     */
    @Override
    public int getItemCount() {
        return listaApuntes.size();
    }

    /**
     * Reemplaza la lista actual por una nueva y refresca el RecyclerView.
     * @param newList Nueva lista de apuntes a mostrar.
     */
    public void updateList(List<Apuntes> newList) {
        this.listaApuntes = newList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder que mantiene las referencias a las vistas de cada tarjeta de apunte.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvCategoriaTag;
        MaterialCardView cardApunte;
        ImageButton btnBorrar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCategoriaTag = itemView.findViewById(R.id.tvCategoriaTag);
            cardApunte = itemView.findViewById(R.id.cardApunte);
            btnBorrar = itemView.findViewById(R.id.btnBorrar);
        }
    }
}