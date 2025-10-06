package colectivo.aplicacion;

import colectivo.servicio.SistemaColectivos;

public class Main {
    public static void main(String[] args) {
        SistemaColectivos sistema = new SistemaColectivos();
        sistema.iniciar();
    }
}
