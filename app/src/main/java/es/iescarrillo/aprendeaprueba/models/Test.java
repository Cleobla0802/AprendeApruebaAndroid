package es.iescarrillo.aprendeaprueba.models;

import java.io.Serializable;
import java.util.List;

public class Test implements Serializable {
    private String id;
    private String userId;
    private String titulo;
    private String categoria;
    private long fecha;
    private List<Pregunta> preguntas;
    private boolean completado;
    private int ultimaNota;

    public Test() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }
    public List<Pregunta> getPreguntas() { return preguntas; }
    public void setPreguntas(List<Pregunta> preguntas) { this.preguntas = preguntas; }
    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }
    public int getUltimaNota() { return ultimaNota; }
    public void setUltimaNota(int ultimaNota) { this.ultimaNota = ultimaNota; }
}