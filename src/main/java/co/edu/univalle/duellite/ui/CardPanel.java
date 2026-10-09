package co.edu.univalle.duellite.ui;

import co.edu.univalle.duellite.model.Card;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel que representa visualmente una carta en la interfaz del duelo.
 *
 * Modos de visualización:
 * <ul>
 *   <li><b>Normal</b>: muestra imagen, nombre, ATK y DEF.</li>
 *   <li><b>Boca abajo</b> (facedDown=true): muestra el dorso de la carta.</li>
 *   <li><b>Usada</b>: se desactiva visualmente con opacidad reducida.</li>
 *   <li><b>Seleccionada</b>: borde amarillo para indicar la selección del jugador.</li>
 * </ul>
 */
public class CardPanel extends JPanel {

    // ---- Colores y dimensiones ----
    private static final Color COLOR_FONDO_NORMAL    = new Color(30, 30, 50);
    private static final Color COLOR_FONDO_BOCA_ABAJO = new Color(15, 15, 40);
    private static final Color COLOR_BORDE_NORMAL    = new Color(80, 80, 120);
    private static final Color COLOR_BORDE_SELECCION = new Color(255, 215, 0);  // Dorado
    private static final Color COLOR_BORDE_USADA     = new Color(60, 60, 60);
    private static final Color COLOR_TEXTO           = Color.WHITE;
    private static final Color COLOR_TEXTO_STATS     = new Color(200, 200, 255);
    private static final int   ANCHO_IMAGEN          = 100;
    private static final int   ALTO_IMAGEN           = 140;

    // ---- Componentes internos ----
    private final JLabel lblImagen;
    private final JLabel lblNombre;
    private final JLabel lblStats;

    // ---- Estado ----
    private Card carta;
    private boolean facedDown;    // boca abajo (cartas de la máquina sin revelar)
    private boolean seleccionada;
    private boolean usada;

    /** Runnable que se ejecuta cuando el jugador hace clic en la carta */
    private Runnable onSeleccionado;

    /**
     * Crea un CardPanel vacío (sin carta asignada todavía).
     *
     * @param facedDown true si la carta debe mostrarse boca abajo
     */
    public CardPanel(boolean facedDown) {
        this.facedDown = facedDown;
        this.seleccionada = false;
        this.usada = false;

        setLayout(new BorderLayout(4, 4));
        setBackground(COLOR_FONDO_NORMAL);
        setBorder(BorderFactory.createLineBorder(COLOR_BORDE_NORMAL, 2));
        setPreferredSize(new Dimension(120, 210));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Imagen de la carta
        lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setPreferredSize(new Dimension(ANCHO_IMAGEN, ALTO_IMAGEN));
        lblImagen.setOpaque(false);

        // Nombre de la carta (texto pequeño y con wrap si es largo)
        lblNombre = new JLabel("", SwingConstants.CENTER);
        lblNombre.setForeground(COLOR_TEXTO);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 9));

        // ATK / DEF
        lblStats = new JLabel("", SwingConstants.CENTER);
        lblStats.setForeground(COLOR_TEXTO_STATS);
        lblStats.setFont(new Font("SansSerif", Font.PLAIN, 9));

        JPanel panelInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        panelInfo.setOpaque(false);
        panelInfo.add(lblNombre);
        panelInfo.add(lblStats);

        add(lblImagen, BorderLayout.CENTER);
        add(panelInfo, BorderLayout.SOUTH);

        // Listener de clic para seleccionar la carta
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!usada && !facedDown && onSeleccionado != null) {
                    onSeleccionado.run();
                }
            }
        });

        // Mostrar placeholder hasta que se asigne una carta
        mostrarPlaceholder();
    }

    // ---- Métodos públicos ----

    /**
     * Asigna una carta al panel y actualiza la vista.
     *
     * @param carta La carta a mostrar
     */
    public void setCarta(Card carta) {
        this.carta = carta;
        actualizarVista();
    }

    /**
     * Actualiza la imagen de la carta si ya se cargó de forma asíncrona.
     */
    public void actualizarImagen() {
        if (carta != null && !facedDown) {
            actualizarVista();
        }
    }

    /**
     * Marca la carta como seleccionada (borde dorado).
     */
    public void setSeleccionada(boolean seleccionada) {
        this.seleccionada = seleccionada;
        actualizarBorde();
        repaint();
    }

    /**
     * Marca la carta como usada (gris y deshabilitada).
     */
    public void setUsada(boolean usada) {
        this.usada = usada;
        if (usada) {
            setBackground(new Color(40, 40, 40));
            setBorder(BorderFactory.createLineBorder(COLOR_BORDE_USADA, 2));
            setOpaque(true);
            // Aplicar una capa semitransparente sobre la imagen para oscurecerla
            lblImagen.setEnabled(false);
            lblNombre.setForeground(Color.GRAY);
            lblStats.setForeground(Color.GRAY);
        }
        repaint();
    }

    /**
     * Revela la carta (cambia de boca abajo a normal).
     * Útil para mostrar la carta de la máquina cuando se juega.
     */
    public void revelar() {
        this.facedDown = false;
        actualizarVista();
    }

    /**
     * Registra el callback que se ejecuta cuando el jugador hace clic en esta carta.
     */
    public void setOnSeleccionado(Runnable callback) {
        this.onSeleccionado = callback;
    }

    public Card getCarta() {
        return carta;
    }

    public boolean isSeleccionada() {
        return seleccionada;
    }

    public boolean isUsada() {
        return usada;
    }

    // ---- Métodos privados ----

    /**
     * Actualiza todos los componentes visuales según el estado actual de la carta.
     */
    private void actualizarVista() {
        if (carta == null) {
            mostrarPlaceholder();
            return;
        }

        if (facedDown) {
            // Mostrar dorso de la carta dibujado por código
            lblImagen.setIcon(crearDorso());
            lblNombre.setText("???");
            lblStats.setText("ATK:??? / DEF:???");
            setBackground(COLOR_FONDO_BOCA_ABAJO);
        } else {
            // Mostrar la carta normalmente
            ImageIcon icono = carta.getImagen();
            if (icono != null) {
                // Escalar la imagen al tamaño del panel
                Image imgEscalada = icono.getImage()
                        .getScaledInstance(ANCHO_IMAGEN, ALTO_IMAGEN, Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(imgEscalada));
            } else {
                // Placeholder mientras carga la imagen
                lblImagen.setIcon(crearPlaceholderIcono());
            }
            // Truncar el nombre si es muy largo
            String nombre = carta.getNombre();
            if (nombre.length() > 16) {
                nombre = nombre.substring(0, 14) + "..";
            }
            lblNombre.setText(nombre);
            lblStats.setText("ATK:" + carta.getAtk() + " DEF:" + carta.getDef());
            setBackground(COLOR_FONDO_NORMAL);
        }

        actualizarBorde();
        revalidate();
        repaint();
    }

    private void actualizarBorde() {
        if (usada) {
            setBorder(BorderFactory.createLineBorder(COLOR_BORDE_USADA, 2));
        } else if (seleccionada) {
            setBorder(BorderFactory.createLineBorder(COLOR_BORDE_SELECCION, 3));
        } else {
            setBorder(BorderFactory.createLineBorder(COLOR_BORDE_NORMAL, 2));
        }
    }

    /** Muestra un panel vacío con texto "Sin carta" */
    private void mostrarPlaceholder() {
        lblImagen.setIcon(crearPlaceholderIcono());
        lblNombre.setText("Sin carta");
        lblStats.setText("-");
    }

    /**
     * Dibuja un dorso de carta genérico usando Graphics2D.
     * Patrón cuadriculado azul oscuro con el logo "DL" en el centro.
     */
    private ImageIcon crearDorso() {
        int w = ANCHO_IMAGEN, h = ALTO_IMAGEN;
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // Fondo azul oscuro
        g.setColor(new Color(10, 10, 80));
        g.fillRect(0, 0, w, h);

        // Patrón de rombos decorativos
        g.setColor(new Color(30, 30, 120));
        for (int x = 0; x < w; x += 15) {
            for (int y = 0; y < h; y += 15) {
                g.drawRect(x + 3, y + 3, 9, 9);
            }
        }

        // Borde dorado
        g.setColor(new Color(180, 140, 0));
        g.setStroke(new BasicStroke(3));
        g.drawRect(3, 3, w - 6, h - 6);

        // Texto central
        g.setColor(new Color(200, 170, 50));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        String texto = "DL";
        g.drawString(texto, (w - fm.stringWidth(texto)) / 2, h / 2 + fm.getAscent() / 2);

        g.dispose();
        return new ImageIcon(img);
    }

    /**
     * Resetea el panel a su estado inicial (sin carta, boca abajo o no según se indique).
     * Se usa al iniciar un nuevo duelo.
     *
     * @param nuevoBocaAbajo true si la carta debe mostrarse boca abajo en el nuevo duelo
     */
    public void resetear(boolean nuevoBocaAbajo) {
        this.carta = null;
        this.facedDown = nuevoBocaAbajo;
        this.seleccionada = false;
        this.usada = false;
        setBackground(COLOR_FONDO_NORMAL);
        lblImagen.setEnabled(true);
        lblNombre.setForeground(COLOR_TEXTO);
        lblStats.setForeground(COLOR_TEXTO_STATS);
        mostrarPlaceholder();
        actualizarBorde();
        revalidate();
        repaint();
    }

    /**
     * Crea un ícono gris de placeholder para cuando no hay imagen disponible.
     */
    private ImageIcon crearPlaceholderIcono() {
        int w = ANCHO_IMAGEN, h = ALTO_IMAGEN;
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(60, 60, 80));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(100, 100, 120));
        g.drawRect(2, 2, w - 4, h - 4);
        g.setColor(Color.GRAY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("Sin imagen", 10, h / 2);
        g.dispose();
        return new ImageIcon(img);
    }
}
