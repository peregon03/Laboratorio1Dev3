package co.edu.univalle.duellite.api;

import co.edu.univalle.duellite.model.Card;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente para la API pública de YGOProDeck.
 * Endpoint usado: https://db.ygoprodeck.com/api/v7/randomcard.php
 *
 * Soporta dos formatos de respuesta posibles:
 *   1. Objeto de carta directo: { "name": "...", "type": "...", ... }
 *   2. Objeto envuelto en data: { "data": [ { "name": "...", ... } ] }
 *
 * Decisiones de diseño:
 *   - Cartas que no sean Monster (type no contiene "Monster") se descartan.
 *   - Link Monsters (sin campo "def") se descartan y se pide otra carta.
 *   - Se reintenta hasta MAX_REINTENTOS veces para no quedar en bucle infinito.
 */
public class YgoApiClient {

    private static final String API_URL = "https://db.ygoprodeck.com/api/v7/randomcard.php";
    private static final int MAX_REINTENTOS = 15;
    private static final int TIMEOUT_SEGUNDOS = 10;

    /** HttpClient reutilizable (thread-safe) */
    private final HttpClient httpClient;

    public YgoApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SEGUNDOS))
                // Seguir redirecciones HTTP (la API devuelve 301 en algunos entornos)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Obtiene una carta Monster aleatoria válida de la API.
     * Reintenta hasta {@value MAX_REINTENTOS} veces si la carta no cumple
     * los criterios (no es Monster o no tiene DEF).
     *
     * @return Una carta Monster con nombre, ATK, DEF e imageUrl
     * @throws ApiException si no se puede obtener una carta válida
     */
    public Card fetchRandomMonster() throws ApiException {
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                Card carta = intentarObtenerCarta();
                if (carta != null) {
                    return carta;
                }
                // carta == null significa que no era Monster o no tenía DEF → reintentar
            } catch (ApiException e) {
                // Si es el último intento, relanzamos el error al llamador
                if (intento == MAX_REINTENTOS) {
                    throw e;
                }
                // Si no, seguimos intentando (podría ser un fallo temporal de red)
            }
        }
        throw new ApiException("No se pudo obtener una carta Monster válida después de "
                + MAX_REINTENTOS + " intentos.");
    }

    /**
     * Hace UNA petición a la API y devuelve la carta si es Monster con DEF,
     * o null si hay que descartar la carta (no es Monster o no tiene DEF).
     *
     * @return La carta, o null para indicar "reintentar"
     * @throws ApiException si hay un error de red o HTTP
     */
    private Card intentarObtenerCarta() throws ApiException {
        // Construir la petición con User-Agent y timeout
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(TIMEOUT_SEGUNDOS))
                .header("User-Agent", "YuGiOh-DuelLite/1.0 (Laboratorio Univalle)")
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ApiException("Error de red al conectar con la API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("La petición a la API fue interrumpida.", e);
        }

        // Validar código HTTP
        if (response.statusCode() != 200) {
            throw new ApiException("La API respondió con código HTTP " + response.statusCode()
                    + ". Intente de nuevo.");
        }

        // Parsear el JSON
        String cuerpo = response.body();
        JSONObject raiz = new JSONObject(cuerpo);

        // Determinar el formato de la respuesta y extraer el objeto de carta
        JSONObject datosCarta;
        if (raiz.has("data")) {
            // Formato 2: { "data": [ {...} ] }
            JSONArray data = raiz.getJSONArray("data");
            if (data.isEmpty()) {
                throw new ApiException("La API devolvió un array 'data' vacío.");
            }
            datosCarta = data.getJSONObject(0);
        } else {
            // Formato 1: objeto directo
            datosCarta = raiz;
        }

        // Verificar que sea una carta Monster
        String tipo = datosCarta.optString("type", "");
        if (!tipo.contains("Monster")) {
            return null; // No es Monster → descartar
        }

        // Verificar que tenga DEF (los Link Monsters no tienen)
        if (!datosCarta.has("def")) {
            return null; // Sin DEF → descartar
        }

        // Extraer los campos necesarios
        String nombre = datosCarta.getString("name");
        int atk = datosCarta.optInt("atk", 0);
        int def = datosCarta.optInt("def", 0);

        // Obtener la URL de la imagen pequeña
        String imageUrl = "";
        if (datosCarta.has("card_images")) {
            JSONArray imagenes = datosCarta.getJSONArray("card_images");
            if (!imagenes.isEmpty()) {
                JSONObject primeraImagen = imagenes.getJSONObject(0);
                // Preferimos image_url_small para los paneles de la UI
                imageUrl = primeraImagen.optString("image_url_small",
                           primeraImagen.optString("image_url", ""));
            }
        }

        return new Card(nombre, atk, def, imageUrl);
    }
}
