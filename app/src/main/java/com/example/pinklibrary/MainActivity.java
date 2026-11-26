package com.example.pinklibrary;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private EditText etBuscar;
    private Button btnBuscar;
    private LinearLayout contenedorCategorias;
    private TextView tvSeccion;
    private TextView tvMensaje;
    private ProgressBar progressBar;
    private RecyclerView rvLibros;

    private List<Libro> listaLibros = new ArrayList<>();
    private LibrosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etBuscar = findViewById(R.id.etBuscar);
        btnBuscar = findViewById(R.id.btnBuscar);
        contenedorCategorias = findViewById(R.id.contenedorCategorias);
        tvSeccion = findViewById(R.id.tvSeccion);
        tvMensaje = findViewById(R.id.tvMensaje);
        progressBar = findViewById(R.id.progressBar);
        rvLibros = findViewById(R.id.rvLibros);


        adapter = new LibrosAdapter(this, listaLibros);
        rvLibros.setLayoutManager(new LinearLayoutManager(this));
        rvLibros.setAdapter(adapter);

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String texto = etBuscar.getText().toString().trim();
                if (texto.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Escribe algo para buscar", Toast.LENGTH_SHORT).show();
                    return;
                }
                tvSeccion.setText("Resultados de: " + texto);
                buscarLibros(texto);
            }
        });


        for (int i = 0; i < contenedorCategorias.getChildCount(); i++) {
            contenedorCategorias.getChildAt(i).setOnClickListener(listenerCategoria);
        }


        tvSeccion.setText("Recomendados para ti");
        cargarPorCategoria("romance");
    }


    private View.OnClickListener listenerCategoria = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button botonTocado = (Button) v;
            String categoria = v.getTag().toString();

            tvSeccion.setText("Categoría: " + botonTocado.getText().toString());
            cargarPorCategoria(categoria);
        }
    };


    private void buscarLibros(String texto) {
        final String direccion = "https://openlibrary.org/search.json?q=" + Uri.encode(texto)
                + "&fields=title,author_name,first_publish_year,cover_i,key&limit=20";

        mostrarCargando(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String respuesta = descargarJson(direccion);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mostrarCargando(false);

                        if (respuesta == null) {
                            mostrarResultado(new ArrayList<Libro>(), "No se pudo conectar con Open Library. Revisa tu internet.");
                            return;
                        }
                        mostrarResultado(leerBusqueda(respuesta), "No se encontraron libros con esa búsqueda.");
                    }
                });
            }
        }).start();
    }

    private List<Libro> leerBusqueda(String json) {
        List<Libro> lista = new ArrayList<>();
        try {
            JSONObject raiz = new JSONObject(json);
            JSONArray docs = raiz.optJSONArray("docs");
            if (docs == null) {
                return lista;
            }

            for (int i = 0; i < docs.length(); i++) {
                JSONObject libroJson = docs.getJSONObject(i);

                String titulo = libroJson.optString("title", "Título no disponible");

                String autor = "Autor desconocido";
                JSONArray autores = libroJson.optJSONArray("author_name");
                if (autores != null && autores.length() > 0) {
                    autor = autores.optString(0, "Autor desconocido");
                }

                String anio = "Año no disponible";
                int numeroAnio = libroJson.optInt("first_publish_year", 0);
                if (numeroAnio > 0) {
                    anio = String.valueOf(numeroAnio);
                }

                String portada = "";
                int idPortada = libroJson.optInt("cover_i", 0);
                if (idPortada > 0) {
                    portada = "https://covers.openlibrary.org/b/id/" + idPortada + "-M.jpg";
                }

                String key = libroJson.optString("key", "");

                lista.add(new Libro(titulo, autor, anio, portada, key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }


    private void cargarPorCategoria(String categoria) {
        final String direccion = "https://openlibrary.org/subjects/" + categoria + ".json?limit=20";

        mostrarCargando(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String respuesta = descargarJson(direccion);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mostrarCargando(false);

                        if (respuesta == null) {
                            mostrarResultado(new ArrayList<Libro>(), "No se pudo conectar con Open Library. Revisa tu internet.");
                            return;
                        }
                        mostrarResultado(leerCategoria(respuesta), "Esta categoría no devolvió libros.");
                    }
                });
            }
        }).start();
    }

    private List<Libro> leerCategoria(String json) {
        List<Libro> lista = new ArrayList<>();
        try {
            JSONObject raiz = new JSONObject(json);
            JSONArray works = raiz.optJSONArray("works");
            if (works == null) {
                return lista;
            }

            for (int i = 0; i < works.length(); i++) {
                JSONObject libroJson = works.getJSONObject(i);

                String titulo = libroJson.optString("title", "Título no disponible");

                String autor = "Autor desconocido";
                JSONArray autores = libroJson.optJSONArray("authors");
                if (autores != null && autores.length() > 0) {
                    JSONObject primerAutor = autores.optJSONObject(0);
                    if (primerAutor != null) {
                        autor = primerAutor.optString("name", "Autor desconocido");
                    }
                }

                String anio = "Año no disponible";
                int numeroAnio = libroJson.optInt("first_publish_year", 0);
                if (numeroAnio > 0) {
                    anio = String.valueOf(numeroAnio);
                }

                String portada = "";
                int idPortada = libroJson.optInt("cover_id", 0);
                if (idPortada > 0) {
                    portada = "https://covers.openlibrary.org/b/id/" + idPortada + "-M.jpg";
                }

                String key = libroJson.optString("key", "");

                lista.add(new Libro(titulo, autor, anio, portada, key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
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

    private void mostrarResultado(List<Libro> resultado, String mensajeSiEstaVacio) {
        listaLibros.clear();
        listaLibros.addAll(resultado);
        adapter.notifyDataSetChanged();

        if (listaLibros.isEmpty()) {
            tvMensaje.setText(mensajeSiEstaVacio);
            tvMensaje.setVisibility(View.VISIBLE);
        } else {
            tvMensaje.setVisibility(View.GONE);
            rvLibros.scrollToPosition(0);
        }
    }

    private void mostrarCargando(boolean cargando) {
        if (cargando) {
            progressBar.setVisibility(View.VISIBLE);
            tvMensaje.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
