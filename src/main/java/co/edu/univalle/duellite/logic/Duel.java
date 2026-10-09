package co.edu.univalle.duellite.logic;

import co.edu.univalle.duellite.model.Card;
import co.edu.univalle.duellite.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contiene toda la lógica del duelo Yu-Gi-Oh! Duel Lite.
 * Esta clase NO importa nada de Swing; se comunica con la UI
 * exclusivamente a través de {@link BattleListener}.
 *
 * Reglas implementadas:
 * <ul>
 *   <li>El turno inicial es aleatorio.</li>
 *   <li>Quien tiene el turno ATACA; el otro elige posición (Ataque o Defensa).</li>
 *   <li>Ambos en Ataque: gana el mayor ATK.</li>
 *   <li>Atacante vs Defensor: ATK atacante vs DEF defensor. Si ATK > DEF, gana atacante.</li>
 *   <li>Desempate 1: se compara la otra estadística (DEF si ambos atacan, ATK si hay defensor).</li>
 *   <li>Desempate 2: gana quien tiene el turno (el atacante).</li>
 *   <li>Primera en llegar a 2 victorias gana el duelo.</li>
 * </ul>
 */
public class Duel {

    // ---- Estado del duelo ----

    private final List<Card> cartasJugador;
    private final List<Card> cartasMaquina;

    /** true = le toca atacar al jugador; false = le toca a la máquina */
    private boolean turnoJugador;

    private int puntosJugador;
    private int puntosMaquina;

    /** Índices de las cartas ya usadas por el jugador */
    private final List<Integer> usadasJugador;
    /** Índices de las cartas ya usadas por la máquina */
    private final List<Integer> usadasMaquina;

    private boolean duelTerminado;

    private final Random random;
    private final BattleListener listener;

    // ---- Constructor ----

    /**
     * Crea un nuevo duelo con las cartas de ambos jugadores.
     *
     * @param cartasJugador Lista de 3 cartas del jugador
     * @param cartasMaquina Lista de 3 cartas de la máquina
     * @param listener      Receptor de eventos del duelo (la UI)
     */
    public Duel(List<Card> cartasJugador, List<Card> cartasMaquina, BattleListener listener) {
        this.cartasJugador = new ArrayList<>(cartasJugador);
        this.cartasMaquina = new ArrayList<>(cartasMaquina);
        this.listener = listener;
        this.random = new Random();
        this.usadasJugador = new ArrayList<>();
        this.usadasMaquina = new ArrayList<>();
        this.puntosJugador = 0;
        this.puntosMaquina = 0;
        this.duelTerminado = false;

        // Definir turno inicial al azar
        this.turnoJugador = random.nextBoolean();
    }

    // ---- Métodos públicos ----

    /**
     * Indica quién ataca en el turno actual.
     *
     * @return "Jugador" o "Máquina"
     */
    public String getAtacanteActual() {
        return turnoJugador ? "Jugador" : "Máquina";
    }

    /** @return true si es el turno del jugador (él ataca) */
    public boolean isTurnoJugador() {
        return turnoJugador;
    }

    /** @return true si el duelo ya terminó */
    public boolean isDuelTerminado() {
        return duelTerminado;
    }

    public int getPuntosJugador() { return puntosJugador; }
    public int getPuntosMaquina() { return puntosMaquina; }

    /**
     * Notifica a la UI quién ataca en el turno actual.
     * Debe llamarse justo después de crear el Duel o al inicio de cada turno.
     */
    public void notificarInicioTurno() {
        listener.onTurnStarted(getAtacanteActual());
    }

    /**
     * Resuelve un turno completo del duelo.
     * La máquina elige su carta y posición automáticamente.
     *
     * @param indiceCartaJugador Índice (0-2) de la carta elegida por el jugador
     * @param posicionJugador    Posición elegida por el jugador (ATAQUE o DEFENSA)
     * @throws IllegalArgumentException si la carta ya fue usada o el índice es inválido
     * @throws IllegalStateException    si el duelo ya terminó
     */
    public void jugarTurno(int indiceCartaJugador, Position posicionJugador) {
        if (duelTerminado) {
            throw new IllegalStateException("El duelo ya terminó.");
        }
        if (indiceCartaJugador < 0 || indiceCartaJugador >= cartasJugador.size()) {
            throw new IllegalArgumentException("Índice de carta inválido: " + indiceCartaJugador);
        }
        if (usadasJugador.contains(indiceCartaJugador)) {
            throw new IllegalArgumentException("Esa carta ya fue usada.");
        }

        // La máquina elige una carta al azar entre las que no ha usado
        int indiceCartaMaquina = elegirCartaMaquina();

        // La máquina elige posición al azar
        Position posicionMaquina = random.nextBoolean() ? Position.ATAQUE : Position.DEFENSA;

        Card cartaJugador = cartasJugador.get(indiceCartaJugador);
        Card cartaMaquina = cartasMaquina.get(indiceCartaMaquina);

        // Marcar cartas como usadas
        usadasJugador.add(indiceCartaJugador);
        usadasMaquina.add(indiceCartaMaquina);

        // Resolver la batalla y determinar el ganador del turno
        String ganador = resolverBatalla(cartaJugador, posicionJugador, cartaMaquina, posicionMaquina);

        // Actualizar puntaje
        if ("Jugador".equals(ganador)) {
            puntosJugador++;
        } else {
            puntosMaquina++;
        }

        // Descripción para el log de la UI
        String descJugador = cartaJugador.getNombre()
                + " [ATK:" + cartaJugador.getAtk() + "/DEF:" + cartaJugador.getDef() + "]"
                + " en " + posicionJugador;
        String descMaquina = cartaMaquina.getNombre()
                + " [ATK:" + cartaMaquina.getAtk() + "/DEF:" + cartaMaquina.getDef() + "]"
                + " en " + posicionMaquina;

        // Notificar a la UI el resultado del turno
        listener.onTurn(descJugador, descMaquina, ganador);
        listener.onScoreChanged(puntosJugador, puntosMaquina);

        // Verificar si el duelo terminó (2 puntos gana)
        if (puntosJugador >= 2 || puntosMaquina >= 2) {
            duelTerminado = true;
            String ganadorDuel = puntosJugador >= 2 ? "Jugador" : "Máquina";
            listener.onDuelEnded(ganadorDuel);
        } else {
            // Alternar turno para la siguiente ronda
            turnoJugador = !turnoJugador;
            listener.onTurnStarted(getAtacanteActual());
        }
    }

    // ---- Métodos privados / de lógica ----

    /**
     * Elige aleatoriamente una carta de la máquina que no haya sido usada.
     */
    private int elegirCartaMaquina() {
        List<Integer> disponibles = new ArrayList<>();
        for (int i = 0; i < cartasMaquina.size(); i++) {
            if (!usadasMaquina.contains(i)) {
                disponibles.add(i);
            }
        }
        return disponibles.get(random.nextInt(disponibles.size()));
    }

    /**
     * Resuelve la batalla entre dos cartas y devuelve "Jugador" o "Máquina".
     *
     * Reglas de comparación:
     * 1. Ambos en ATAQUE: gana el mayor ATK. Desempate: mayor DEF. Sigue empatado: gana el atacante.
     * 2. Atacante vs Defensor: ATK del atacante vs DEF del defensor.
     *    - ATK > DEF → gana atacante
     *    - DEF > ATK → gana defensor
     *    - Empate: se compara ATK del defensor vs DEF del atacante. Sigue empatado: gana atacante.
     *
     * "Atacante" = quien tiene el turno.
     */
    String resolverBatalla(Card cartaJugador, Position posJugador,
                           Card cartaMaquina, Position posMaquina) {
        // Determinar quién es el atacante y quién el defensor según el turno
        boolean jugadorAtaca = turnoJugador;

        if (posJugador == Position.ATAQUE && posMaquina == Position.ATAQUE) {
            // --- Caso 1: Ambos en ataque ---
            if (cartaJugador.getAtk() != cartaMaquina.getAtk()) {
                return cartaJugador.getAtk() > cartaMaquina.getAtk() ? "Jugador" : "Máquina";
            }
            // Desempate por DEF
            if (cartaJugador.getDef() != cartaMaquina.getDef()) {
                return cartaJugador.getDef() > cartaMaquina.getDef() ? "Jugador" : "Máquina";
            }
            // Desempate final: gana el atacante del turno
            return jugadorAtaca ? "Jugador" : "Máquina";

        } else {
            // --- Caso 2: Uno en ataque, otro en defensa ---
            Card atacante, defensor;
            String nombreAtacante, nombreDefensor;

            if (jugadorAtaca) {
                // El jugador ataca → su posición determina si realmente está atacando
                // Pero en este juego, quien tiene el turno ATACA, así que:
                // Si el jugador tiene el turno, él es el atacante independientemente de la posición
                // La posición del oponente determina qué stat se compara
                atacante = cartaJugador;
                defensor = cartaMaquina;
                nombreAtacante = "Jugador";
                nombreDefensor = "Máquina";
            } else {
                atacante = cartaMaquina;
                defensor = cartaJugador;
                nombreAtacante = "Máquina";
                nombreDefensor = "Jugador";
            }

            // La posición del oponente (defensor) determina qué comparar
            Position posDefensor = jugadorAtaca ? posMaquina : posJugador;

            if (posDefensor == Position.DEFENSA) {
                // Atacante ATK vs Defensor DEF
                if (atacante.getAtk() != defensor.getDef()) {
                    return atacante.getAtk() > defensor.getDef() ? nombreAtacante : nombreDefensor;
                }
                // Desempate: ATK del defensor vs DEF del atacante
                if (defensor.getAtk() != atacante.getDef()) {
                    return defensor.getAtk() > atacante.getDef() ? nombreDefensor : nombreAtacante;
                }
                // Desempate final: gana el atacante del turno
                return nombreAtacante;
            } else {
                // El "defensor" eligió ATAQUE → ambos en ataque (caso simétrico)
                if (atacante.getAtk() != defensor.getAtk()) {
                    return atacante.getAtk() > defensor.getAtk() ? nombreAtacante : nombreDefensor;
                }
                if (atacante.getDef() != defensor.getDef()) {
                    return atacante.getDef() > defensor.getDef() ? nombreAtacante : nombreDefensor;
                }
                return nombreAtacante;
            }
        }
    }

    /** Devuelve los índices de las cartas del jugador ya usadas (para la UI). */
    public List<Integer> getUsadasJugador() {
        return new ArrayList<>(usadasJugador);
    }

    /** Devuelve los índices de las cartas de la máquina ya usadas (para la UI). */
    public List<Integer> getUsadasMaquina() {
        return new ArrayList<>(usadasMaquina);
    }
}
