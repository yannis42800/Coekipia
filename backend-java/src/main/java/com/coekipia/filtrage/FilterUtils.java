package com.coekipia.filtrage;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FilterUtils {

    private FilterUtils() {
    }

    /**
     * Filtre items sur keyExtractor(item) == filterValue, puis trie sur comparator.
     *
     * keyExtractor et comparator sont des references de methode (ex: Product::getCategory,
     * Comparator.comparing(Product::getPrice)), verifiees a la compilation : impossible de
     * reference une propriete qui n'existe pas sur T, contrairement a un lookup par String
     * + reflexion.
     */
    public static <T, R> List<T> filterAndSort(
            List<T> items,
            Function<T, R> keyExtractor,
            R filterValue,
            Comparator<T> comparator
    ) {
        List<T> filtered = items.stream()
                .filter(item -> Objects.equals(keyExtractor.apply(item), filterValue))
                .collect(Collectors.toList());
        filtered.sort(comparator);
        return filtered;
    }
}
