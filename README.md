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
- **Deux implémentations**, deux langages : `backend-node` (TypeScript, port 3000) et
  `backend-java` (Java, JDK pur, port 3001).
- **Un seul front**, qui fonctionne sans modification avec l'un ou l'autre backend — les
  deux peuvent tourner **en même temps**, un sélecteur dans le front permet de basculer
  de l'un à l'autre à la volée.

## Architecture

```
                    frontend/index.html (front unique)
                    sélecteur de backend : Node | Java
                          |                    |
                          v                    v
        backend-node (:3000)          backend-java (:3001)
```

Le front ne connaît pas le langage du backend : il consomme un contrat REST identique,
exposé à l'identique par les deux implémentations, chacune sur son propre port pour
pouvoir tourner simultanément.

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

Les deux backends peuvent tourner en même temps (ports différents), chacun dans un
terminal séparé.

### Backend Node/TypeScript (port 3000)

```bash
cd backend-node
npm install
npm run build && npm start
```

### Backend Java (port 3001)

```bash
cd backend-java
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.coekipia.filtrage.Server
```

### Front

Ouvrir `frontend/index.html` directement dans un navigateur (double-clic, ou
`open frontend/index.html`). Un menu déroulant en haut de page permet de choisir le
backend interrogé (Node `:3000` ou Java `:3001`) — le front lui-même ne change pas,
seule la base d'URL utilisée change. Un seul des deux backends suffit pour que le front
fonctionne ; les lancer tous les deux permet de basculer de l'un à l'autre en direct.

## Vérifications effectuées

- `npx tsc --noEmit` : aucune erreur de type côté Node.
- `javac` : compilation Java propre.
- Tests manuels `curl` sur les deux backends : mêmes réponses JSON pour les mêmes
  paramètres, erreurs 400 identiques sur un champ inconnu, en-tête CORS présent sur les
  deux pour l'appel depuis le front.
