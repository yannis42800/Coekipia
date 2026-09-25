# Exercice technique — Filtrage & tri avancé

Exercice réalisé pour l'entretien Coekipia, montrant l'usage de l'IA comme outil de
production (conception, code, vérification) plutôt que comme simple générateur.

## Consignes couvertes

- Fonction générique qui **filtre** un tableau d'objets sur une valeur donnée et **trie**
  le résultat.
- **Typage strict** : impossible de cibler une propriété qui n'existe pas sur l'objet
  (vérifié à la compilation dans les deux langages, cf. détails plus bas).
- Objet métier `Product` avec **6 propriétés** (`id`, `name`, `category`, `price`,
  `stock`, `rating`).
- **Deux implémentations**, deux langages : `backend-node` (TypeScript) et
  `backend-java` (Java, JDK pur).
- **Un seul front**, qui fonctionne sans modification avec l'un ou l'autre backend.

## Architecture

```
frontend/index.html   <-- front unique (HTML/JS vanilla, aucune dépendance)
        |
        |  HTTP (contrat identique)
        v
backend-node/   OU   backend-java/   <-- un seul lancé à la fois, sur le port 3000
```

Le front ne connaît pas le langage du backend : il consomme un contrat REST identique,
exposé à l'identique par les deux implémentations.

### Contrat API commun

- `GET /api/products/fields`
  → `{"fields":[{"name":"id","type":"number"}, ...]}`
  (permet au front de construire dynamiquement les listes de champs filtrables/triables)

- `GET /api/products?filterField=<champ>&filterValue=<valeur>&sortField=<champ>&sortOrder=asc|desc`
  → tableau JSON de produits filtrés puis triés.
  `filterField`/`filterValue` sont optionnels (pas de filtre = tri seul).
  Un champ inconnu renvoie `400 {"error": "..."}`.

## La fonction de filtrage/tri, dans les deux langages

### TypeScript — `backend-node/src/filter.ts`

```ts
export function filterAndSort<T, K extends keyof T>(
  items: readonly T[],
  filterKey: K,
  filterValue: T[K],
  sortKey: K,
  sortOrder: SortOrder = 'asc'
): T[]
```

`K extends keyof T` interdit à la compilation toute clé qui n'existe pas sur `T`, et
`T[K]` force `filterValue` à avoir le type de la propriété ciblée
(`filterAndSort(products, 'colour', ...)` ne compile pas).

Comme la route HTTP reçoit les noms de champs sous forme de `string` (query params), un
garde de type (`isProductKey`, dans `types.ts`) valide dynamiquement la chaîne reçue
contre la liste exhaustive `PRODUCT_KEYS`, avant de la passer à `filterAndSort` — on ne
perd jamais la sécurité de type, y compris à la frontière HTTP.

### Java — `backend-java/src/.../FilterUtils.java`

```java
public static <T, R> List<T> filterAndSort(
    List<T> items,
    Function<T, R> keyExtractor,
    R filterValue,
    Comparator<T> comparator
)
```

Ici l'équivalent de `keyof T` est la **référence de méthode** (`Product::getCategory`) :
si la propriété n'existe pas, le code ne compile pas. Comme en TypeScript, la couche
HTTP reçoit des noms de champs en `String` ; l'énumération `ProductField` (dans
`ProductField.java`) fait office de whitelist explicite reliant chaque nom à sa
référence de méthode, son `Comparator` et son parseur — pas de réflexion, pas de lookup
par nom de propriété arbitraire.

## Lancer le projet

Un seul backend à la fois, tous deux sur `http://localhost:3000`.

### Backend Node/TypeScript

```bash
cd backend-node
npm install
npm run build && npm start
# ou en dev : npm run dev
```

### Backend Java

```bash
cd backend-java
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.coekipia.filtrage.Server
```

### Front

Ouvrir `frontend/index.html` directement dans un navigateur (double-clic, ou
`open frontend/index.html`). Il appelle `http://localhost:3000` — aucune configuration
à changer selon le backend démarré.

## Vérifications effectuées

- `npx tsc --noEmit` : aucune erreur de type côté Node.
- `javac` : compilation Java propre.
- Tests manuels `curl` sur les deux backends : mêmes réponses JSON pour les mêmes
  paramètres, erreurs 400 identiques sur un champ inconnu, en-tête CORS présent sur les
  deux pour l'appel depuis le front.
