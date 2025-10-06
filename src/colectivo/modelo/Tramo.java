package colectivo.modelo;

public class Tramo {
    private Parada origen;
    private Parada destino;
    private int tiempo; // minutos

    public Tramo(Parada origen, Parada destino, int tiempo) {
        this.origen = origen;
        this.destino = destino;
        this.tiempo = tiempo;
    }

    public Parada getOrigen() { return origen; }
    public Parada getDestino() { return destino; }
    public int getTiempo() { return tiempo; }

    @Override
    public String toString() {
        return "Tramo{" +
                "origen=" + origen +
                ", destino=" + destino +
                ", tiempo=" + tiempo +
                '}';
    }
}
