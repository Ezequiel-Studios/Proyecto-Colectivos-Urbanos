package colectivo.modelo;

public class Parada {
    private int codigo;
    private String direccion;
    private double latitud;
    private double longitud;

    public Parada(int codigo, String direccion, double latitud, double longitud) {
        this.codigo = codigo;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public int getCodigo() { return codigo; }
    public String getDireccion() { return direccion; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }

    @Override
    public String toString() {
        return "Parada{" +
                "codigo=" + codigo +
                ", direccion='" + direccion + '\'' +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                '}';
    }
}
