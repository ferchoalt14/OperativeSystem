/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package operativesystem;

/**
 *
 * @author Leandro
 */
import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
 
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public class Reproductormusica extends JInternalFrame {
    private enum Estado {DETENIDO, REPRODUCIENDO, PAUSADO}
    
    private final ListaEnlazada<File> canciones = new ListaEnlazada<>();
    private final DefaultListModel<File> modeloLista = new DefaultListModel<>();
    private final JList<File> listaCanciones = new JList<>(modeloLista);
    
    private final JLabel lblCaratula = new JLabel();
    private final JLabel lblDescripcion = new JLabel(" ");
    private final JButton btnPlay = TemaUI.crearBotonPrimario("Play");
    private final JButton btnPause = new JButton("Pause");
    private final JButton btnStop = new JButton("Stop");
 
    private Estado estado = Estado.DETENIDO;
    private File cancionActual;
    private AdvancedPlayer reproductorActual;
 
    private volatile float msPorFrame = 26.0f;
    private volatile int msTranscurridos = 0;
    private volatile boolean detenidoManualmente = false;
    
    public Reproductormusica(File carpetaMusica) {
        super("Reproductor", true, true, true, true);
        setSize(440, 380);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(TemaUI.SUPERFICIE);
 
        cargarCanciones(carpetaMusica);
        construirInterfaz();
        conectarEventos();
    }
    
    private void cargarCanciones(File carpetaMusica) {
        File[] archivos = carpetaMusica.listFiles((dir, nombre) -> nombre.toLowerCase().endsWith(".mp3"));
        if (archivos != null) {
            java.util.Arrays.sort(archivos, java.util.Comparator.comparing(File::getName));
            for (File archivo : archivos) {
                canciones.agregar(archivo);
            }
        }
        
        for (File archivo : canciones) {
            modeloLista.addElement(archivo);
        }
    }
    
     private void construirInterfaz() {
        listaCanciones.setCellRenderer((lista, archivo, indice, seleccionado, foco) -> {
            Lectoretiquetasid3.Etiquetas etiquetas = Lectoretiquetasid3.leer(archivo);
            String texto = (etiquetas.titulo != null) ? etiquetas.titulo : archivo.getName();
            JLabel etiqueta = new JLabel("  " + texto);
            etiqueta.setOpaque(true);
            etiqueta.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
            etiqueta.setBackground(seleccionado ? TemaUI.ACCENT_CLARO : Color.WHITE);
            etiqueta.setForeground(seleccionado ? TemaUI.ACCENT_OSCURO : TemaUI.TEXTO);
            return etiqueta;
        });
        listaCanciones.setBackground(Color.WHITE);
 
        lblCaratula.setPreferredSize(new Dimension(120, 120));
        lblCaratula.setHorizontalAlignment(SwingConstants.CENTER);
        lblCaratula.setBorder(BorderFactory.createLineBorder(TemaUI.BORDE));
        lblCaratula.setOpaque(true);
        lblCaratula.setBackground(TemaUI.FONDO);
        mostrarCaratulaPorDefecto();
 
        lblDescripcion.setVerticalAlignment(SwingConstants.TOP);
 
        JPanel panelInfo = new JPanel(new BorderLayout(10, 10));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        panelInfo.setBackground(TemaUI.SUPERFICIE);
        panelInfo.add(lblCaratula, BorderLayout.WEST);
        panelInfo.add(lblDescripcion, BorderLayout.CENTER);
 
        btnPause.setEnabled(false);
        btnStop.setEnabled(false);
        JPanel panelControles = new JPanel();
        panelControles.setBackground(TemaUI.SUPERFICIE);
        panelControles.add(btnPlay);
        panelControles.add(btnPause);
        panelControles.add(btnStop);
 
        JScrollPane scroll = new JScrollPane(listaCanciones);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 
        add(panelInfo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(panelControles, BorderLayout.SOUTH);
 
        if (canciones.estaVacia()) {
            lblDescripcion.setText("<html>No hay archivos .mp3 en tu carpeta de Música.<br>"
                    + "Copia alguno ahí desde el Explorador y vuelve a abrir el Reproductor.</html>");
        }
    }
     
     private void conectarEventos() {
        btnPlay.addActionListener(e -> alPresionarPlay());
        btnPause.addActionListener(e -> alPresionarPause());
        btnStop.addActionListener(e -> alPresionarStop());
 
        listaCanciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaCanciones.getSelectedValue() != null
                    && !listaCanciones.getSelectedValue().equals(cancionActual)) {
                mostrarMetadata(listaCanciones.getSelectedValue());
            }
        });
 
        listaCanciones.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    File seleccion = listaCanciones.getSelectedValue();
                    if (seleccion != null) {
                        reproducirDesdeElInicio(seleccion);
                    }
                }
            }
        });
    }
     
     
     //actionlisteners para los botonesssssssssssssssssssssss
     private void alPresionarPlay() {
        if (estado == Estado.PAUSADO && cancionActual != null) {
            reanudar();
            return;
        }
        File seleccion = listaCanciones.getSelectedValue();
        if (seleccion == null && !modeloLista.isEmpty()) {
            seleccion = modeloLista.get(0);
            listaCanciones.setSelectedIndex(0);
        }
        if (seleccion != null) {
            reproducirDesdeElInicio(seleccion);
        }
    }
 
    private void alPresionarPause() {
        if (estado == Estado.REPRODUCIENDO) {
            pausar();
        }
    }
 
    private void alPresionarStop() {
        if (estado != Estado.DETENIDO) {
            detener();
        }
    }
    
    //cosas de play stop etc
    
    private void reproducirDesdeElInicio(File archivo) {
        detenerReproductorActual();
        cancionActual = archivo;
        msTranscurridos = 0;
        msPorFrame = calcularMsPorFrame(archivo);
        iniciarHiloDeReproduccion(0);
        mostrarMetadata(archivo);
    }
 
    private void reanudar() {
        int frameDeInicio = Math.round(msTranscurridos / msPorFrame);
        iniciarHiloDeReproduccion(frameDeInicio);
    }
 
    private void pausar() {
        detenerReproductorActual();
        estado = Estado.PAUSADO;
        actualizarBotones();
    }
 
    private void detener() {
        detenerReproductorActual();
        cancionActual = null;
        msTranscurridos = 0;
        estado = Estado.DETENIDO;
        actualizarBotones();
    }
    
    private void detenerReproductorActual() {
        detenidoManualmente = true;
        if (reproductorActual != null) {
            reproductorActual.stop();
        }
    }
 
    private void iniciarHiloDeReproduccion(int frameDeInicio) {
        detenidoManualmente = false;
        estado = Estado.REPRODUCIENDO;
        actualizarBotones();
 
        Thread hiloReproduccion = new Thread(() -> {
            try (FileInputStream flujo = new FileInputStream(cancionActual)) {
                AdvancedPlayer player = new AdvancedPlayer(new BufferedInputStream(flujo));
                reproductorActual = player;
                player.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackStarted(PlaybackEvent evento) {
                    }
 
                    @Override
                    public void playbackFinished(PlaybackEvent evento) {
                        msTranscurridos = evento.getFrame();
                        boolean fueManual = detenidoManualmente;
                        detenidoManualmente = false;
                        if (!fueManual) {
                            SwingUtilities.invokeLater(Reproductormusica.this::alTerminarCancionSola);
                        }
                    }
                });
 
                if (frameDeInicio > 0) {
                    player.play(frameDeInicio, Integer.MAX_VALUE);
                } else {
                    player.play();
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    estado = Estado.DETENIDO;
                    actualizarBotones();
                    JOptionPane.showMessageDialog(this,
                            "No se pudo reproducir el archivo:\n" + ex.getMessage(),
                            "Reproductor de música", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "hilo-reproductor-musica");
 
        hiloReproduccion.start();
    }
    
    private void alTerminarCancionSola() {
        int indiceActual = modeloLista.indexOf(cancionActual);
        estado = Estado.DETENIDO;
        cancionActual = null;
        msTranscurridos = 0;
 
        if (indiceActual >= 0 && indiceActual + 1 < modeloLista.size()) {
            File siguiente = modeloLista.get(indiceActual + 1);
            listaCanciones.setSelectedIndex(indiceActual + 1);
            reproducirDesdeElInicio(siguiente);
        } else {
            actualizarBotones();
        }
    }
    
    private float calcularMsPorFrame(File archivo) {
        try (FileInputStream flujo = new FileInputStream(archivo)) {
            Bitstream bitstream = new Bitstream(new BufferedInputStream(flujo));
            Header primerFrame = bitstream.readFrame();
            if (primerFrame != null) {
                return primerFrame.ms_per_frame();
            }
        } catch (Exception ex) {
        }
        return 26.0f;
    }
    
    //musicui
    
    private void mostrarMetadata(File archivo) {
        Lectoretiquetasid3.Etiquetas etiquetas = Lectoretiquetasid3.leer(archivo);
 
        String titulo = (etiquetas.titulo != null) ? etiquetas.titulo : archivo.getName();
        String artista = (etiquetas.artista != null) ? etiquetas.artista : "Artista desconocido";
        String album = (etiquetas.album != null) ? etiquetas.album : "";
 
        lblDescripcion.setText("<html><b>" + titulo + "</b><br>" + artista
                + (album.isEmpty() ? "" : " — " + album) + "</html>");
 
        if (etiquetas.tieneCaratula()) {
            ImageIcon icono = new ImageIcon(etiquetas.caratula);
            Image escalada = icono.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
            lblCaratula.setIcon(new ImageIcon(escalada));
            lblCaratula.setText(null);
        } else {
            mostrarCaratulaPorDefecto();
        }
    }
 
    private void mostrarCaratulaPorDefecto() {
        lblCaratula.setIcon(null);
        lblCaratula.setText("<html><center><font color='#999999'>Sin<br>carátula</font></center></html>");
    }
 
    private void actualizarBotones() {
        SwingUtilities.invokeLater(() -> {
            btnPlay.setEnabled(estado != Estado.REPRODUCIENDO);
            btnPause.setEnabled(estado == Estado.REPRODUCIENDO);
            btnStop.setEnabled(estado != Estado.DETENIDO);
        });
    }
    
    
}
