package co.edu.univalle.duellite.api;

/**
 * Excepción lanzada cuando falla una petición a la API de YGOProDeck.
 * Puede deberse a un código HTTP distinto de 200, a un error de red
 * o a un JSON inesperado.
 */
public class ApiException extends Exception {

    /**
     * @param message Descripción del error (se muestra al usuario en la UI)
     */
    public ApiException(String message) {
        super(message);
    }

    /**
     * @param message Descripción del error
     * @param cause   Excepción original que causó el fallo
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
