package com.example.pinklibrary;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class LibrosAdapter extends RecyclerView.Adapter<LibrosAdapter.LibroViewHolder> {

    private Context contexto;
    private List<Libro> listaLibros;

    public LibrosAdapter(Context contexto, List<Libro> listaLibros) {
        this.contexto = contexto;
        this.listaLibros = listaLibros;
    }

    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(contexto).inflate(R.layout.item_libro, parent, false);
        return new LibroViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        final Libro libro = listaLibros.get(position);

        holder.tvTitulo.setText(libro.getTitulo());
        holder.tvAutor.setText(libro.getAutor());
        holder.tvAnio.setText(libro.getAnio());

        if (libro.getPortadaUrl() != null && !libro.getPortadaUrl().isEmpty()) {
            Picasso.get()
                    .load(libro.getPortadaUrl())
                    .placeholder(R.drawable.ic_libro)
                    .error(R.drawable.ic_libro)
                    .into(holder.ivPortada);
        } else {
            holder.ivPortada.setImageResource(R.drawable.ic_libro);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(contexto, DetalleLibroActivity.class);
                intent.putExtra("titulo", libro.getTitulo());
                intent.putExtra("autor", libro.getAutor());
                intent.putExtra("anio", libro.getAnio());
                intent.putExtra("portada", libro.getPortadaUrl());
                intent.putExtra("key", libro.getKey());
                contexto.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaLibros.size();
    }

    public static class LibroViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPortada;
        TextView tvTitulo;
        TextView tvAutor;
        TextView tvAnio;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.ivPortada);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvAnio = itemView.findViewById(R.id.tvAnio);
        }
    }
}
