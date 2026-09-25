export type SortOrder = 'asc' | 'desc';

/**
 * Filtre `items` sur `filterKey === filterValue`, puis trie le resultat sur `sortKey`.
 *
 * `K extends keyof T` force le compilateur a refuser toute propriete qui n'existe pas
 * sur T (ex: filterAndSort(products, 'colour', ...) ne compile pas).
 * `T[K]` force `filterValue` a avoir le meme type que la propriete ciblee.
 */
export function filterAndSort<T, K extends keyof T>(
  items: readonly T[],
  filterKey: K,
  filterValue: T[K],
  sortKey: K,
  sortOrder: SortOrder = 'asc'
): T[] {
  const filtered = items.filter((item) => item[filterKey] === filterValue);

  return [...filtered].sort((a, b) => {
    const valueA = a[sortKey];
    const valueB = b[sortKey];

    if (valueA < valueB) return sortOrder === 'asc' ? -1 : 1;
    if (valueA > valueB) return sortOrder === 'asc' ? 1 : -1;
    return 0;
  });
}

/*
Exemple d'erreur de compilation volontaire (decommenter pour verifier) :

import { Product } from './types';
filterAndSort<Product, 'colour'>([], 'colour', 'red', 'colour');
// -> Type '"colour"' does not satisfy the constraint 'keyof Product'
*/
