package com.coekipia.filtrage;

import java.util.List;
import java.util.stream.Collectors;

final class JsonUtils {

    private JsonUtils() {
    }

    static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static String toJsonArray(List<Product> products) {
        return "[" + products.stream().map(Product::toJson).collect(Collectors.joining(",")) + "]";
    }

    static String error(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }
}
