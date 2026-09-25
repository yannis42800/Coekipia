package com.coekipia.filtrage;

public final class Product {
    private final int id;
    private final String name;
    private final String category;
    private final double price;
    private final int stock;
    private final double rating;

    public Product(int id, String name, String category, double price, int stock, double rating) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public double getRating() {
        return rating;
    }

    public String toJson() {
        return "{"
                + "\"id\":" + id + ","
                + "\"name\":\"" + JsonUtils.escape(name) + "\","
                + "\"category\":\"" + JsonUtils.escape(category) + "\","
                + "\"price\":" + price + ","
                + "\"stock\":" + stock + ","
                + "\"rating\":" + rating
                + "}";
    }
}
