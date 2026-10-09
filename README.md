# Yu-Gi-Oh! Duel Lite

**Laboratorio #1 — Desarrollo de Software III**
Universidad del Valle, Sede Tuluá

| Integrante | Código |
|---|---|
| [Nombre Estudiante 1 - Código] | |
| [Nombre Estudiante 2 - Código] | |

**Docente:** Mg(c). Juan Pablo Pinillos Reina

---

## Descripción

Aplicación de escritorio en Java Swing que simula un duelo sencillo de Yu-Gi-Oh! entre un jugador humano y la máquina. Cada jugador recibe 3 cartas Monster aleatorias obtenidas en tiempo real de la API pública **YGOProDeck**. El duelo se juega en rondas por turnos: quien gane 2 de 3 rondas gana el duelo.

---

## Requisitos

- **JDK 11 o superior** (se recomienda JDK 17 LTS)
- **Apache Maven 3.6+**
- **Conexión a internet** (para consultar la API de YGOProDeck y descargar las imágenes de las cartas)

---

## Instrucciones de ejecución

### Con Maven (recomendado)

```bash
# 1. Compilar y empaquetar (genera el JAR en target/)
mvn clean package

# 2. Ejecutar directamente con Maven
mvn exec:java

# 3. O ejecutar el JAR fat generado (con todas las dependencias incluidas)
java -jar target/duellite-1.0-SNAPSHOT-fat.jar
```

### Desde IntelliJ IDEA

1. Abrir IntelliJ IDEA → **File → Open** → seleccionar la carpeta `Lab1`.
2. IntelliJ detectará el `pom.xml` automáticamente y descargará las dependencias.
3. Esperar a que termine la indexación del proyecto.
4. Abrir `src/main/java/co/edu/univalle/duellite/Main.java`.
5. Hacer clic en el botón ▶ verde junto al método `main`, o presionar **Shift+F10**.

### Ejecutar los tests

```bash
mvn test
```

---

## Diseño del proyecto

El proyecto sigue una separación clara en cuatro capas:

- **`model`**: Contiene `Card` (los datos de una carta: nombre, ATK, DEF, imagen) y `Position` (enum ATAQUE/DEFENSA). Son clases simples de datos sin dependencias externas.
- **`api`**: `YgoApiClient` encapsula toda la comunicación HTTP con la API de YGOProDeck usando el `HttpClient` de Java 11. Valida el tipo de carta, maneja los dos formatos posibles de respuesta JSON, descarta cartas inválidas y lanza `ApiException` con mensajes claros ante cualquier fallo de red o HTTP.
- **`logic`**: `Duel` contiene las reglas del juego de forma completamente independiente de Swing. No importa ni una clase del paquete `javax.swing`. Se comunica con la interfaz gráfica exclusivamente a través de la interfaz `BattleListener`, lo que facilita las pruebas unitarias y desacopla totalmente la lógica de la presentación.
- **`ui`**: `MainFrame` implementa `BattleListener` y actúa como controlador de la vista. `CardPanel` encapsula la representación gráfica de una carta individual.

Para evitar bloquear el **Event Dispatch Thread (EDT)** de Swing, todas las peticiones HTTP y descargas de imágenes se realizan dentro de `SwingWorker`. El método `done()` del worker (que corre en el EDT) es el único lugar donde se modifican los componentes de la interfaz tras una operación en segundo plano.

---

## Reglas del juego implementadas

1. **Turno inicial**: se decide al azar al crear el duelo.
2. **Turno**: quien tiene el turno **ATACA**. El oponente elige su posición (ATAQUE o DEFENSA). El jugador elige con radio buttons; la máquina elige al azar.
3. **Comparación**:
   - *Ambos en ATAQUE*: gana el mayor ATK.
   - *Atacante vs Defensor en DEFENSA*: se compara el ATK del atacante con la DEF del defensor. Si ATK > DEF gana el atacante; si DEF > ATK gana el defensor.
4. **Desempate**: si la comparación principal empata, se compara la otra estadística (DEF en ataque-ataque, ATK en ataque-defensa). Si sigue empatado, gana quien tiene el turno (el atacante).
5. **Fin del duelo**: gana quien llegue primero a 2 victorias de ronda.
6. **Cartas sin DEF** (Link Monsters): se descartan en el cliente de la API y se solicita otra carta. Esto garantiza que todas las cartas en el juego tienen DEF definida.

---

## Capturas de pantalla

> Las capturas se tomarán una vez la aplicación esté en ejecución.

| Pantalla de inicio | Duelo en progreso | Ganador |
|---|---|---|
| ![Inicio](docs/capturas/inicio.png) | ![Duelo](docs/capturas/duelo.png) | ![Ganador](docs/capturas/ganador.png) |

---

## Estructura de paquetes

```
Lab1/
├── pom.xml
├── .gitignore
├── README.md
├── docs/
│   └── capturas/
│       └── .gitkeep
└── src/
    ├── main/
    │   └── java/
    │       └── co/edu/univalle/duellite/
    │           ├── Main.java                  ← Punto de entrada
    │           ├── model/
    │           │   ├── Card.java              ← Datos de una carta
    │           │   └── Position.java          ← Enum ATAQUE/DEFENSA
    │           ├── api/
    │           │   ├── YgoApiClient.java      ← Cliente HTTP + parseo JSON
    │           │   └── ApiException.java      ← Excepción de la API
    │           ├── logic/
    │           │   ├── Duel.java              ← Reglas y estado del duelo
    │           │   └── BattleListener.java    ← Interfaz de eventos
    │           └── ui/
    │               ├── MainFrame.java         ← Ventana principal
    │               └── CardPanel.java         ← Vista de una carta
    └── test/
        └── java/
            └── co/edu/univalle/duellite/
                └── logic/
                    └── DuelTest.java          ← Pruebas unitarias de Duel
```

---

## Dependencias

| Dependencia | Versión | Uso |
|---|---|---|
| `org.json:json` | 20240303 | Parseo de respuestas JSON de la API |
| `org.junit.jupiter:junit-jupiter` | 5.10.2 | Pruebas unitarias (solo en test) |

---

## Notas técnicas

- El proyecto cumple el requisito de **no bloquear el EDT**: todas las operaciones lentas (HTTP, imágenes) usan `SwingWorker`.
- Si una imagen falla al cargar, la carta muestra un placeholder gris sin romper la aplicación.
- El log de batalla hace autoscroll para siempre mostrar el último evento.
