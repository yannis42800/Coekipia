package com.coekipia.filtrage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class ProductData {

    private ProductData() {
    }

    static final List<Product> PRODUCTS = Collections.unmodifiableList(Arrays.asList(
            new Product(1, "Casque audio", "Electronique", 89.99, 34, 4.3),
            new Product(2, "Clavier mecanique", "Electronique", 129.0, 12, 4.6),
            new Product(3, "Chaise de bureau", "Mobilier", 219.5, 8, 4.1),
            new Product(4, "Bureau assis-debout", "Mobilier", 459.0, 5, 4.7),
            new Product(5, "Souris sans fil", "Electronique", 39.9, 50, 4.0),
            new Product(6, "Lampe de bureau", "Mobilier", 29.9, 40, 3.8),
            new Product(7, "Ecran 27 pouces", "Electronique", 249.0, 15, 4.5),
            new Product(8, "Etagere murale", "Mobilier", 59.0, 22, 3.9),
            new Product(9, "Webcam HD", "Electronique", 49.9, 28, 3.7),
            new Product(10, "Tapis de souris XL", "Accessoire", 19.9, 60, 4.2)
    ));
}
