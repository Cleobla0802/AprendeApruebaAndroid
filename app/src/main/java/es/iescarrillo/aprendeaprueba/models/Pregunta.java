package es.iescarrillo.aprendeaprueba.models;

import java.util.List;

public class Pregunta {
    private int id;
    private String enunciado;
    private List<String> opciones;
    private int respuestaCorrecta;

    public Pregunta() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }
    public List<String> getOpciones() { return opciones; }
    public void setOpciones(List<String> opciones) { this.opciones = opciones; }
    public int getRespuestaCorrecta() { return respuestaCorrecta; }
    public void setRespuestaCorrecta(int respuestaCorrecta) { this.respuestaCorrecta = respuestaCorrecta; }
}