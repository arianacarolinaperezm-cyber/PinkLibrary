package com.example.pinklibrary;

public class Libro {

    private String titulo;
    private String autor;
    private String anio;
    private String portadaUrl;
    private String key;

    public Libro(String titulo, String autor, String anio, String portadaUrl, String key) {
        this.titulo = titulo;
        this.autor = autor;
        this.anio = anio;
        this.portadaUrl = portadaUrl;
        this.key = key;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getAnio() {
        return anio;
    }

    public String getPortadaUrl() {
        return portadaUrl;
    }

    public String getKey() {
        return key;
    }
}
