package com.example.pinklibrary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class DetalleLibroActivity extends AppCompatActivity {

    private TextView tvDescripcion;
    private String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_libro);

        TextView tvVolver = findViewById(R.id.tvVolver);
        ImageView ivPortada = findViewById(R.id.ivPortadaDetalle);
        TextView tvTitulo = findViewById(R.id.tvTituloDetalle);
        TextView tvAutor = findViewById(R.id.tvAutorDetalle);
        TextView tvAnio = findViewById(R.id.tvAnioDetalle);
        Button btnOpenLibrary = findViewById(R.id.btnOpenLibrary);
        tvDescripcion = findViewById(R.id.tvDescripcion);

        Intent datos = getIntent();
        String titulo = datos.getStringExtra("titulo");
        String autor = datos.getStringExtra("autor");
        String anio = datos.getStringExtra("anio");
        String portada = datos.getStringExtra("portada");
        key = datos.getStringExtra("key");

        if (titulo == null) titulo = "Título no disponible";
        if (autor == null) autor = "Autor desconocido";
        if (anio == null) anio = "Año no disponible";
        if (key == null) key = "";

        tvTitulo.setText(titulo);
        tvAutor.setText(autor);
        tvAnio.setText(anio);


        if (portada != null && !portada.isEmpty()) {
            Picasso.get()
                    .load(portada.replace("-M.jpg", "-L.jpg"))
                    .placeholder(R.drawable.ic_libro)
                    .error(R.drawable.ic_libro)
                    .into(ivPortada);
        } else {
            ivPortada.setImageResource(R.drawable.ic_libro);
        }

        tvVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnOpenLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (key.isEmpty()) {
                    Toast.makeText(DetalleLibroActivity.this, "Este libro no tiene enlace disponible", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent navegador = new Intent(Intent.ACTION_VIEW, Uri.parse("https://openlibrary.org" + key));
                startActivity(navegador);
            }
        });

        if (key.isEmpty()) {
            btnOpenLibrary.setVisibility(View.GONE);
            tvDescripcion.setText("Este libro no tiene sinopsis disponible en Open Library.");
        } else {
            cargarDescripcion();
        }
    }


    private void cargarDescripcion() {
        tvDescripcion.setText("Cargando sinopsis...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String respuesta = descargarJson("https://openlibrary.org" + key + ".json");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String sinopsis = "Este libro no tiene sinopsis disponible en Open Library.";

                        if (respuesta != null) {
                            try {
                                JSONObject raiz = new JSONObject(respuesta);
                                Object descripcion = raiz.opt("description");

                                // La API devuelve la descripcion como texto o como objeto
                                if (descripcion instanceof String) {
                                    sinopsis = (String) descripcion;
                                } else if (descripcion instanceof JSONObject) {
                                    sinopsis = ((JSONObject) descripcion).optString("value", sinopsis);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        tvDescripcion.setText(sinopsis);
                    }
                });
            }
        }).start();
    }

    private String descargarJson(String direccion) {
        try {
            URL url = new URL(direccion);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");
            conexion.setConnectTimeout(15000);
            conexion.setReadTimeout(15000);
            conexion.connect();

            if (conexion.getResponseCode() != 200) {
                conexion.disconnect();
                return null;
            }

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream(), "UTF-8"));
            StringBuilder texto = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) != null) {
                texto.append(linea);
            }
            lector.close();
            conexion.disconnect();

            return texto.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
