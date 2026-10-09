package co.edu.univalle.duellite.model;

import javax.swing.ImageIcon;

/**
 * Representa una carta Monster de Yu-Gi-Oh!
 * Almacena los atributos básicos de combate y la imagen ya cargada.
 *
 * Decisión de diseño: las cartas sin DEF (Link Monsters) se descartan
 * en el cliente de la API; por tanto, def siempre es >= 0 aquí.
 */
public class Card {

    private final String nombre;
    private final int atk;
    private final int def;
    private final String imageUrl;   // URL de la imagen pequeña
    private ImageIcon imagen;        // Se carga en segundo plano después de crear la carta

    /**
     * Crea una carta con sus atributos básicos.
     * La imagen se asigna después con {@link #setImagen(ImageIcon)}.
     *
     * @param nombre   Nombre de la carta
     * @param atk      Puntos de ataque
     * @param def      Puntos de defensa
     * @param imageUrl URL de la imagen (tamaño pequeño)
     */
    public Card(String nombre, int atk, int def, String imageUrl) {
        this.nombre = nombre;
        this.atk = atk;
        this.def = def;
        this.imageUrl = imageUrl;
        this.imagen = null;
    }

    // ---- Getters ----

    public String getNombre() {
        return nombre;
    }

    public int getAtk() {
        return atk;
    }

    public int getDef() {
        return def;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ImageIcon getImagen() {
        return imagen;
    }

    // ---- Setter (solo para la imagen, que se carga async) ----

    public void setImagen(ImageIcon imagen) {
        this.imagen = imagen;
    }

    @Override
    public String toString() {
        return nombre + " [ATK:" + atk + " / DEF:" + def + "]";
    }
}
