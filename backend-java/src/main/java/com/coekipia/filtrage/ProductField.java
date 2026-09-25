package com.coekipia.filtrage;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Registre explicite (whitelist) des champs de Product exposables via l'API.
 * Chaque constante est construite a partir d'une reference de methode : un champ
 * absent de cette liste (ou renomme) ne compile pas, il n'y a pas de reflexion
 * "cherche n'importe quelle propriete par son nom".
 */
public enum ProductField {
    ID("id", "number", Product::getId, Comparator.comparingInt(Product::getId), Integer::parseInt),
    NAME("name", "string", Product::getName, Comparator.comparing(Product::getName), value -> value),
    CATEGORY("category", "string", Product::getCategory, Comparator.comparing(Product::getCategory), value -> value),
    PRICE("price", "number", Product::getPrice, Comparator.comparingDouble(Product::getPrice), Double::parseDouble),
    STOCK("stock", "number", Product::getStock, Comparator.comparingInt(Product::getStock), Integer::parseInt),
    RATING("rating", "number", Product::getRating, Comparator.comparingDouble(Product::getRating), Double::parseDouble);

    private final String jsonName;
    private final String jsonType;
    private final Function<Product, Object> valueExtractor;
    private final Comparator<Product> comparator;
    private final Function<String, Object> parser;

    @SuppressWarnings("unchecked")
    <R> ProductField(
            String jsonName,
            String jsonType,
            Function<Product, R> valueExtractor,
            Comparator<Product> comparator,
            Function<String, R> parser
    ) {
        this.jsonName = jsonName;
        this.jsonType = jsonType;
        this.valueExtractor = (Function<Product, Object>) valueExtractor;
        this.comparator = comparator;
        this.parser = (Function<String, Object>) parser;
    }

    public String jsonName() {
        return jsonName;
    }

    public String jsonType() {
        return jsonType;
    }

    public Comparator<Product> comparator() {
        return comparator;
    }

    public Object extractValue(Product product) {
        return valueExtractor.apply(product);
    }

    public Object parseValue(String raw) {
        return parser.apply(raw);
    }

    public static ProductField fromJsonName(String name) {
        for (ProductField field : values()) {
            if (field.jsonName.equals(name)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Champ inconnu: " + name);
    }
}
