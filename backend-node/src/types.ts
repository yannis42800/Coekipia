export interface Product {
  id: number;
  name: string;
  category: string;
  price: number;
  stock: number;
  rating: number;
}

export const PRODUCT_KEYS = ['id', 'name', 'category', 'price', 'stock', 'rating'] as const;

export type ProductKey = (typeof PRODUCT_KEYS)[number];

// Verifie a la compilation que PRODUCT_KEYS couvre exactement les cles de Product
// (si un champ est ajoute/renomme dans Product sans mettre a jour PRODUCT_KEYS, ceci ne compile plus).
type AssertKeysMatch = ProductKey extends keyof Product
  ? keyof Product extends ProductKey
    ? true
    : never
  : never;
const _assertKeysMatch: AssertKeysMatch = true;

export function isProductKey(key: string): key is ProductKey {
  return (PRODUCT_KEYS as readonly string[]).includes(key);
}
