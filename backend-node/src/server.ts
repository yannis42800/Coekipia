import express, { Request, Response } from 'express';
import cors from 'cors';
import { filterAndSort, SortOrder } from './filter';
import { products } from './data';
import { PRODUCT_KEYS, ProductKey, isProductKey } from './types';

const app = express();
app.use(cors());

const PORT = 3000;

// Contrat partage avec le backend Java : la liste des champs filtrables/triables
// et leur type, pour que le front puisse construire son UI sans rien coder en dur.
app.get('/api/products/fields', (_req: Request, res: Response) => {
  res.json({
    fields: PRODUCT_KEYS.map((key) => ({
      name: key,
      type: typeof products[0][key],
    })),
  });
});

// GET /api/products?filterField=category&filterValue=Electronique&sortField=price&sortOrder=asc
app.get('/api/products', (req: Request, res: Response) => {
  const { filterField, filterValue, sortField, sortOrder } = req.query;

  const effectiveSortField = typeof sortField === 'string' && sortField.length > 0 ? sortField : 'id';
  const effectiveSortOrder: SortOrder = sortOrder === 'desc' ? 'desc' : 'asc';

  if (!isProductKey(effectiveSortField)) {
    res.status(400).json({ error: `Champ de tri inconnu: ${effectiveSortField}` });
    return;
  }

  let hasFilter = false;
  let filterKey: ProductKey = 'id';

  if (typeof filterField === 'string' && filterField.length > 0) {
    if (!isProductKey(filterField)) {
      res.status(400).json({ error: `Champ de filtre inconnu: ${filterField}` });
      return;
    }
    filterKey = filterField;
    hasFilter = true;
  }

  if (!hasFilter) {
    res.json(sortAll(effectiveSortField, effectiveSortOrder));
    return;
  }

  const result = filterAndSort(
    products,
    filterKey,
    castFilterValue(filterKey, String(filterValue ?? '')),
    effectiveSortField,
    effectiveSortOrder
  );

  res.json(result);
});

function sortAll(sortKey: ProductKey, sortOrder: SortOrder) {
  return [...products].sort((a, b) => {
    const valueA = a[sortKey];
    const valueB = b[sortKey];
    if (valueA < valueB) return sortOrder === 'asc' ? -1 : 1;
    if (valueA > valueB) return sortOrder === 'asc' ? 1 : -1;
    return 0;
  });
}

function castFilterValue(key: ProductKey, raw: string): (typeof products)[number][ProductKey] {
  const sample = products[0][key];
  if (typeof sample === 'number') {
    return Number(raw) as never;
  }
  return raw as never;
}

app.listen(PORT, () => {
  console.log(`[Node/TS] API disponible sur http://localhost:${PORT}`);
});
