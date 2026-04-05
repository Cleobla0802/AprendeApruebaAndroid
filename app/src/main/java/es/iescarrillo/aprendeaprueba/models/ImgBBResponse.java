package es.iescarrillo.aprendeaprueba.models;

public class ImgBBResponse {
    public Data data;
    public boolean success;
    public int status;

    public static class Data {
        public String url;
        public String getUrl() { return url; }
    }

    public Data getData() { return data; }
}