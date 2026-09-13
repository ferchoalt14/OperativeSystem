package operativesystem;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDeviceBase;


public class AudioDeviceConVolumen extends AudioDeviceBase {

    private SourceDataLine linea;
    private AudioFormat formato;
    private byte[] bufferBytes = new byte[4096];

    private FloatControl controlGanancia;
    private volatile float volumenPendiente = 1.0f;  ///1.0 max vol


    public void setVolumen(float valor0a1) {
        volumenPendiente = Math.max(0f, Math.min(1f, valor0a1));
        aplicarVolumenALaLinea();
    }

    private void aplicarVolumenALaLinea() {
        if (controlGanancia == null) {
            return;
        }

        float minimoDb = controlGanancia.getMinimum();
        float maximoDb = controlGanancia.getMaximum();
        float db = minimoDb + (maximoDb - minimoDb) * volumenPendiente;
        controlGanancia.setValue(db);
    }

    @Override
    protected void openImpl() {

    }

    private AudioFormat obtenerFormato() {
        if (formato == null) {
            Decoder decoder = getDecoder();
            formato = new AudioFormat(decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false);
        }
        return formato;
    }

    private void crearLinea() throws JavaLayerException {
        AudioFormat fmt = obtenerFormato();
        Throwable causa = null;
        try {
            Line line = AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, fmt));
            if (line instanceof SourceDataLine) {
                linea = (SourceDataLine) line;
                linea.open(fmt);

                if (linea.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    controlGanancia = (FloatControl) linea.getControl(FloatControl.Type.MASTER_GAIN);
                } else if (linea.isControlSupported(FloatControl.Type.VOLUME)) {
                    controlGanancia = (FloatControl) linea.getControl(FloatControl.Type.VOLUME);
                }
                aplicarVolumenALaLinea(); // el punto clave que a JLayer se le quedó comentado

                linea.start();
            }
        } catch (RuntimeException | LinkageError | LineUnavailableException ex) {
            causa = ex;
        }
        if (linea == null) {
            throw new JavaLayerException("No se pudo obtener una línea de audio", causa);
        }
    }

    @Override
    protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException {
        if (linea == null) {
            crearLinea();
        }
        byte[] b = aBytes(samples, offs, len);
        linea.write(b, 0, len * 2);
    }

    private byte[] aBytes(short[] samples, int offs, int len) {
        if (bufferBytes.length < len * 2) {
            bufferBytes = new byte[len * 2 + 1024];
        }
        int idx = 0;
        for (int i = 0; i < len; i++) {
            short s = samples[offs + i];
            bufferBytes[idx++] = (byte) s;
            bufferBytes[idx++] = (byte) (s >>> 8);
        }
        return bufferBytes;
    }

    @Override
    protected void flushImpl() {
        if (linea != null) {
            linea.drain();
        }
    }

    @Override
    protected void closeImpl() {
        if (linea != null) {
            linea.close();
        }
    }

    @Override
    public int getPosition() {
        return linea != null ? (int) (linea.getMicrosecondPosition() / 1000) : 0;
    }
}