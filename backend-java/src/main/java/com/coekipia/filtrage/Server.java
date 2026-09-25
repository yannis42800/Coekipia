package com.coekipia.filtrage;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Meme contrat HTTP que le backend Node (backend-node/src/server.ts) :
 *  - GET /api/products/fields
 *  - GET /api/products?filterField=&filterValue=&sortField=&sortOrder=asc|desc
 * Le front (frontend/index.html) est identique quel que soit le backend lance ;
 * les deux backends tournent sur des ports distincts (Node: 3000, Java: 3001)
 * pour permettre de basculer de l'un a l'autre depuis le front.
 */
public final class Server {

    private static final int PORT = 3001;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/products/fields", Server::handleFields);
        server.createContext("/api/products", Server::handleProducts);
        server.start();
        System.out.println("[Java] API disponible sur http://localhost:" + PORT);
    }

    private static void handleFields(HttpExchange exchange) throws IOException {
        String body = "{\"fields\":[" + java.util.Arrays.stream(ProductField.values())
                .map(field -> "{\"name\":\"" + field.jsonName() + "\",\"type\":\"" + field.jsonType() + "\"}")
                .collect(Collectors.joining(",")) + "]}";
        sendJson(exchange, 200, body);
    }

    private static void handleProducts(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());

        String sortFieldParam = query.getOrDefault("sortField", "id");
        String sortOrderParam = query.getOrDefault("sortOrder", "asc");
        boolean descending = "desc".equals(sortOrderParam);

        ProductField sortField;
        try {
            sortField = ProductField.fromJsonName(sortFieldParam);
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, JsonUtils.error("Champ de tri inconnu: " + sortFieldParam));
            return;
        }

        Comparator<Product> comparator = descending ? sortField.comparator().reversed() : sortField.comparator();

        String filterFieldParam = query.get("filterField");
        List<Product> result;

        if (filterFieldParam == null || filterFieldParam.isEmpty()) {
            result = new java.util.ArrayList<>(ProductData.PRODUCTS);
            result.sort(comparator);
        } else {
            ProductField filterField;
            try {
                filterField = ProductField.fromJsonName(filterFieldParam);
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, JsonUtils.error("Champ de filtre inconnu: " + filterFieldParam));
                return;
            }

            Object filterValue = filterField.parseValue(query.getOrDefault("filterValue", ""));
            result = FilterUtils.filterAndSort(
                    ProductData.PRODUCTS,
                    filterField::extractValue,
                    filterValue,
                    comparator
            );
        }

        sendJson(exchange, 200, JsonUtils.toJsonArray(result));
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return params;
        }
        for (String pair : rawQuery.split("&")) {
            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            String value = separator >= 0 ? pair.substring(separator + 1) : "";
            params.put(decode(key), decode(value));
        }
        return params;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
