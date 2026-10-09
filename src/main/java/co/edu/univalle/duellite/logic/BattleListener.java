package co.edu.univalle.duellite.logic;

/**
 * Interfaz que desacopla la lógica del duelo ({@link Duel}) de la interfaz gráfica.
 * La UI implementa esta interfaz y recibe notificaciones de cada evento del duelo.
 *
 * IMPORTANTE: Los métodos de esta interfaz son llamados desde el hilo del EDT
 * (Event Dispatch Thread) usando SwingUtilities.invokeLater, por lo que es seguro
 * actualizar componentes Swing directamente dentro de las implementaciones.
 */
public interface BattleListener {

    /**
     * Notifica el inicio de un turno, indicando quién ataca.
     *
     * @param quienAtaca "Jugador" o "Máquina"
     */
    void onTurnStarted(String quienAtaca);

    /**
     * Notifica el resultado de un turno ya resuelto.
     *
     * @param descripcionJugador Carta del jugador con su posición (ej: "Dark Magician en ATAQUE")
     * @param descripcionMaquina Carta de la máquina con su posición
     * @param ganadorTurno       "Jugador", "Máquina" o "Empate" (aunque el diseño evita empates)
     */
    void onTurn(String descripcionJugador, String descripcionMaquina, String ganadorTurno);

    /**
     * Notifica un cambio en el marcador.
     *
     * @param puntosJugador Puntos actuales del jugador
     * @param puntosMaquina Puntos actuales de la máquina
     */
    void onScoreChanged(int puntosJugador, int puntosMaquina);

    /**
     * Notifica que el duelo ha terminado.
     *
     * @param ganador "Jugador" o "Máquina"
     */
    void onDuelEnded(String ganador);
}
