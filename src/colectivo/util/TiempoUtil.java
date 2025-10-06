package colectivo.util;

import java.time.LocalTime;
import java.time.Duration;

public class TiempoUtil {
    public static int minutosEntre(LocalTime inicio, LocalTime fin) {
        return (int) Duration.between(inicio, fin).toMinutes();
    }
}
