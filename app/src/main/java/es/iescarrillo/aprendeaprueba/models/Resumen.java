package es.iescarrillo.aprendeaprueba.models;

public class Resumen implements java.io.Serializable {
    private String id;
    private String userId;
    private String titulo;
    private String contenido;
    private String fecha;
    private String categoria;

    public Resumen() {}

    public Resumen(String id, String userId, String titulo, String contenido, String fecha, String categoria) {
        this.id = id;
        this.userId = userId;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fecha = fecha;
        this.categoria = categoria;
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }

    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
