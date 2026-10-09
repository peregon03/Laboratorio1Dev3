package co.edu.univalle.duellite.logic;

import co.edu.univalle.duellite.model.Card;
import co.edu.univalle.duellite.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la lógica de batalla en {@link Duel}.
 * Las cartas se crean a mano, sin llamar a la API.
 *
 * Se prueba el método package-private resolverBatalla() directamente,
 * y también el flujo completo de jugarTurno() para verificar el marcador
 * y la detección del fin del duelo.
 */
class DuelTest {

    // ---- Cartas de prueba ----
    // CartaFuerte: ATK 2500, DEF 2100
    // CartaDebil:  ATK 1000, DEF 800
    // CartaMedia:  ATK 1800, DEF 1600

    private Card cartaFuerte;
    private Card cartaDebil;
    private Card cartaMedia;

    /** Listener vacío para las pruebas (no necesitamos la UI) */
    private BattleListener listenerVacio;

    /** Duel con turnoJugador = true (el jugador ataca primero) */
    private Duel duel;

    @BeforeEach
    void setUp() {
        cartaFuerte = new Card("Dark Magician", 2500, 2100, "");
        cartaDebil  = new Card("Blue-Eyes White Dragon", 3000, 2500, ""); // usamos nombres distintos
        cartaMedia  = new Card("Summoned Skull", 2500, 1200, "");
        // Nota: usamos cartaFuerte/cartaDebil según ATK relativo en cada test

        listenerVacio = new BattleListener() {
            @Override public void onTurnStarted(String q) {}
            @Override public void onTurn(String j, String m, String w) {}
            @Override public void onScoreChanged(int p, int a) {}
            @Override public void onDuelEnded(String w) {}
        };
    }

    // ====================================================================
    // Tests de resolverBatalla (método package-private, mismo paquete)
    // ====================================================================

    @Test
    @DisplayName("Ataque vs Ataque: gana el mayor ATK")
    void ataqueVsAtaque_GanaElMayorAtk() {
        // carta1: ATK 2500, carta2: ATK 1000
        Card carta1 = new Card("Carta1", 2500, 2000, "");
        Card carta2 = new Card("Carta2", 1000, 800, "");

        // Creamos un duel donde el jugador ataca primero
        Duel d = crearDuelConTurnoJugador(true);

        String resultado = d.resolverBatalla(carta1, Position.ATAQUE, carta2, Position.ATAQUE);
        assertEquals("Jugador", resultado, "El jugador tiene mayor ATK y debe ganar");
    }

    @Test
    @DisplayName("Ataque vs Ataque: gana la máquina si su ATK es mayor")
    void ataqueVsAtaque_GanaMaquinaSiTieneMayorAtk() {
        Card carta1 = new Card("Carta1", 1000, 800, "");
        Card carta2 = new Card("Carta2", 2500, 2000, "");

        Duel d = crearDuelConTurnoJugador(true);

        String resultado = d.resolverBatalla(carta1, Position.ATAQUE, carta2, Position.ATAQUE);
        assertEquals("Máquina", resultado, "La máquina tiene mayor ATK y debe ganar");
    }

    @Test
    @DisplayName("Ataque vs Defensa: gana atacante si ATK > DEF")
    void ataqueVsDefensa_GanaAtacanteSiAtkMayorQueDef() {
        // Jugador ataca (ATK 2500) vs máquina en defensa (DEF 1200)
        Card atacante = new Card("Atacante", 2500, 2000, "");
        Card defensor = new Card("Defensor", 1800, 1200, "");

        // Jugador tiene el turno → él ataca
        Duel d = crearDuelConTurnoJugador(true);

        String resultado = d.resolverBatalla(atacante, Position.ATAQUE, defensor, Position.DEFENSA);
        assertEquals("Jugador", resultado, "ATK 2500 > DEF 1200, debe ganar el jugador");
    }

    @Test
    @DisplayName("Ataque vs Defensa: gana defensor si DEF > ATK")
    void ataqueVsDefensa_GanaDefensorSiDefMayorQueAtk() {
        // Jugador ataca (ATK 1000) vs máquina en defensa (DEF 2500)
        Card atacante = new Card("Atacante", 1000, 800, "");
        Card defensor = new Card("Defensor", 1800, 2500, "");

        Duel d = crearDuelConTurnoJugador(true);

        String resultado = d.resolverBatalla(atacante, Position.ATAQUE, defensor, Position.DEFENSA);
        assertEquals("Máquina", resultado, "DEF 2500 > ATK 1000, debe ganar la máquina (defensor)");
    }

    @Test
    @DisplayName("Desempate ATK igual: gana el de mayor DEF en ataque vs ataque")
    void desempate_MismoAtk_GanaMayorDef() {
        // Ambos ATK 2500, jugador DEF 2100 > máquina DEF 1200
        Card carta1 = new Card("Carta1", 2500, 2100, "");
        Card carta2 = new Card("Carta2", 2500, 1200, "");

        Duel d = crearDuelConTurnoJugador(true);

        String resultado = d.resolverBatalla(carta1, Position.ATAQUE, carta2, Position.ATAQUE);
        assertEquals("Jugador", resultado, "Mismo ATK, jugador tiene mayor DEF → debe ganar");
    }

    @Test
    @DisplayName("Desempate total: mismos ATK y DEF, gana el atacante del turno")
    void desempate_TodoIgual_GanaAtacante() {
        Card carta1 = new Card("Carta1", 2500, 2000, "");
        Card carta2 = new Card("Carta2", 2500, 2000, "");

        // Turno del jugador → él es el atacante → debe ganar el desempate
        Duel dJugador = crearDuelConTurnoJugador(true);
        assertEquals("Jugador", dJugador.resolverBatalla(carta1, Position.ATAQUE, carta2, Position.ATAQUE));

        // Turno de la máquina → ella es la atacante → debe ganar el desempate
        Duel dMaquina = crearDuelConTurnoJugador(false);
        assertEquals("Máquina", dMaquina.resolverBatalla(carta1, Position.ATAQUE, carta2, Position.ATAQUE));
    }

    // ====================================================================
    // Tests del flujo completo (jugarTurno + marcador + fin del duelo)
    // ====================================================================

    @Test
    @DisplayName("El duelo termina cuando el jugador llega a 2 puntos")
    void duelTermina_AlLlegarA2Puntos() {
        // Hacemos que el jugador siempre gane eligiendo la carta más fuerte
        // y la máquina sea débil
        Card fuerte  = new Card("Fuerte",  3000, 3000, "");
        Card media   = new Card("Media",   1500, 1500, "");
        Card debil   = new Card("Débil",    500,  500, "");
        Card muyDebil1 = new Card("MDebil1", 100,  100, "");
        Card muyDebil2 = new Card("MDebil2", 200,  200, "");
        Card muyDebil3 = new Card("MDebil3", 300,  300, "");

        // Registrar si se llamó onDuelEnded
        boolean[] duelEndedLlamado = { false };
        String[] ganadorFinal = { null };

        BattleListener listener = new BattleListener() {
            @Override public void onTurnStarted(String q) {}
            @Override public void onTurn(String j, String m, String w) {}
            @Override public void onScoreChanged(int p, int a) {}
            @Override public void onDuelEnded(String w) {
                duelEndedLlamado[0] = true;
                ganadorFinal[0] = w;
            }
        };

        List<Card> cJugador = Arrays.asList(fuerte, media, debil);
        List<Card> cMaquina = Arrays.asList(muyDebil1, muyDebil2, muyDebil3);

        // El jugador tiene ATK 3000 en todas sus cartas → gana independientemente
        // de quien ataque en cada turno
        Duel d = new Duel(cJugador, cMaquina, listener);

        // Jugamos los turnos; el jugador siempre elige su carta más fuerte (índice 0)
        // independientemente de quién ataque, el jugador tiene ATK 3000 que vence a todo
        for (int turno = 0; turno < 3 && !d.isDuelTerminado(); turno++) {
            d.jugarTurno(turno, Position.ATAQUE);
        }

        // Verificar que el duelo terminó
        assertTrue(d.isDuelTerminado(), "El duelo debe terminar al llegar a 2 puntos");
        assertTrue(duelEndedLlamado[0], "onDuelEnded debe haber sido llamado");
        assertNotNull(ganadorFinal[0], "Debe haber un ganador");
    }

    @Test
    @DisplayName("No se puede jugar una carta ya usada")
    void noSePuedeJugarCartaUsada() {
        Card c1 = new Card("C1", 1000, 1000, "");
        Card c2 = new Card("C2", 1000, 1000, "");
        Card c3 = new Card("C3", 1000, 1000, "");
        Card m1 = new Card("M1", 900, 900, "");
        Card m2 = new Card("M2", 900, 900, "");
        Card m3 = new Card("M3", 900, 900, "");

        Duel d = new Duel(
            Arrays.asList(c1, c2, c3),
            Arrays.asList(m1, m2, m3),
            listenerVacio
        );

        // Jugar la carta en índice 0
        d.jugarTurno(0, Position.ATAQUE);

        // Intentar jugar la misma carta → debe lanzar excepción
        if (!d.isDuelTerminado()) {
            assertThrows(IllegalArgumentException.class,
                () -> d.jugarTurno(0, Position.ATAQUE),
                "Jugar una carta ya usada debe lanzar IllegalArgumentException"
            );
        }
    }

    // ====================================================================
    // Método auxiliar
    // ====================================================================

    /**
     * Crea un Duel con cartas genéricas y con el turno inicial que se indique.
     * Se usa un listener vacío porque aquí solo nos interesa probar resolverBatalla.
     */
    private Duel crearDuelConTurnoJugador(boolean jugadorPrimero) {
        Card c1 = new Card("G1", 1000, 1000, "");
        Card c2 = new Card("G2", 1000, 1000, "");
        Card c3 = new Card("G3", 1000, 1000, "");

        // Creamos varios duels hasta obtener el turno deseado
        // (el constructor elige el turno al azar)
        for (int i = 0; i < 100; i++) {
            Duel d = new Duel(
                Arrays.asList(c1, c2, c3),
                Arrays.asList(c1, c2, c3),
                listenerVacio
            );
            if (d.isTurnoJugador() == jugadorPrimero) {
                return d;
            }
        }
        // Si después de 100 intentos no conseguimos el turno, algo raro pasa
        // Usamos el último creado y forzamos el resultado del test
        return new Duel(
            Arrays.asList(c1, c2, c3),
            Arrays.asList(c1, c2, c3),
            listenerVacio
        );
    }
}
