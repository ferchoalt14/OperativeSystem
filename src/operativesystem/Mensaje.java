package operativesystem;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Mensaje implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String remitente;
    private final String destinatario;
    private final String texto;
    private final long fecha;
    private boolean leido;

    public Mensaje(String remitente, String destinatario, String texto) {
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.texto = texto;
        this.fecha = System.currentTimeMillis();
        this.leido = false;
    }

    public String getRemitente() { return remitente; }
    public String getDestinatario() { return destinatario; }
    public String getTexto() { return texto; }
    public long getFecha() { return fecha; }
    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }

    public String getHoraTexto() {
        return new SimpleDateFormat("HH:mm").format(new Date(fecha));
    }

    public String getFechaHoraTexto() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(fecha));
    }
}