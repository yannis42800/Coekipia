// Contrat commun aux deux backends (Node/TypeScript et Java) :
//  GET /api/products/fields
//  GET /api/products?filterField=&filterValue=&sortField=&sortOrder=asc|desc
// Le front est identique quel que soit le backend choisi ; seul le port change
// (Node: 3000, Java: 3001), sélectionnable via les boutons de bascule.
let API_BASE = 'http://localhost:3000';

const filterFieldSelect = document.getElementById('filterField');
const filterValueInput = document.getElementById('filterValue');
const filterValueList = document.getElementById('filterValueList');
const sortFieldSelect = document.getElementById('sortField');
const sortOrderSelect = document.getElementById('sortOrder');
const tableHead = document.getElementById('table-head');
const tableBody = document.getElementById('table-body');
const errorBox = document.getElementById('error');
const backendStatus = document.getElementById('backend-status');
const dot = document.getElementById('dot');
const sCount = document.getElementById('s-count');
const sPrice = document.getElementById('s-price');
const sStock = document.getElementById('s-stock');
const backendButtons = [
  document.getElementById('backend-node-btn'),
  document.getElementById('backend-java-btn'),
];

let fields = [];
let allProducts = [];

async function loadFields() {
  const res = await fetch(`${API_BASE}/api/products/fields`);
  const data = await res.json();
  fields = data.fields;

  filterFieldSelect.innerHTML = '<option value="">(aucun)</option>';
  sortFieldSelect.innerHTML = '';
  for (const field of fields) {
    filterFieldSelect.add(new Option(field.name, field.name));
    sortFieldSelect.add(new Option(field.name, field.name));
  }
}

async function loadAllProducts() {
  const res = await fetch(`${API_BASE}/api/products`);
  allProducts = await res.json();
  updateFilterValueSuggestions();
}

function updateFilterValueSuggestions() {
  const field = filterFieldSelect.value;
  filterValueList.innerHTML = '';
  if (!field) return;

  const values = [...new Set(allProducts.map((item) => item[field]))]
    .filter((v) => v !== undefined && v !== null && v !== '')
    .sort((a, b) => (a < b ? -1 : a > b ? 1 : 0));

  for (const value of values) {
    filterValueList.appendChild(new Option(value));
  }
}

function renderHead() {
  const sf = sortFieldSelect.value;
  tableHead.innerHTML = `<tr>${fields
    .map((f) => `<th class="${f.name === sf ? 'sorted' : ''}">${f.name}</th>`)
    .join('')}</tr>`;
}

function renderStats(rows) {
  const n = rows.length;
  sCount.textContent = n;

  const hasPrice = fields.some((f) => f.name === 'price');
  sPrice.textContent = hasPrice && n
    ? (rows.reduce((s, r) => s + Number(r.price || 0), 0) / n).toFixed(0)
    : '—';

  const hasStock = fields.some((f) => f.name === 'stock');
  sStock.textContent = hasStock
    ? rows.reduce((s, r) => s + Number(r.stock || 0), 0)
    : '—';
}

function renderRows(rows) {
  tableBody.innerHTML = rows.length
    ? rows
        .map((item) => `<tr>${fields
          .map((f) => {
            const v = item[f.name];
            if (f.name === 'category') return `<td><span class="tag">${v}</span></td>`;
            if (f.name === 'stock') return `<td class="num ${Number(v) <= 12 ? 'low' : ''}">${v}</td>`;
            if (typeof v === 'number') return `<td class="num">${v}</td>`;
            return `<td>${v}</td>`;
          })
          .join('')}</tr>`)
        .join('')
    : `<tr><td colspan="${fields.length || 1}" class="empty">Aucun produit ne correspond.</td></tr>`;
}

async function loadProducts() {
  errorBox.textContent = '';

  const params = new URLSearchParams();
  if (filterFieldSelect.value) {
    params.set('filterField', filterFieldSelect.value);
    params.set('filterValue', filterValueInput.value);
  }
  params.set('sortField', sortFieldSelect.value);
  params.set('sortOrder', sortOrderSelect.value);

  renderHead();

  const res = await fetch(`${API_BASE}/api/products?${params.toString()}`);
  const data = await res.json();

  if (!res.ok) {
    errorBox.textContent = data.error ?? 'Erreur inconnue';
    tableBody.innerHTML = '';
    renderStats([]);
    return;
  }

  renderRows(data);
  renderStats(data);
}

document.getElementById('filter-form').addEventListener('submit', (event) => {
  event.preventDefault();
  loadProducts();
});

filterFieldSelect.addEventListener('change', updateFilterValueSuggestions);

function setActiveBackendButton() {
  for (const btn of backendButtons) {
    btn.setAttribute('aria-pressed', String(btn.dataset.base === API_BASE));
  }
}

async function connect() {
  setActiveBackendButton();
  dot.className = 'dot';
  backendStatus.textContent = 'détection…';
  try {
    await loadFields();
    backendStatus.textContent = `Backend actif : en ligne sur ${API_BASE}`;
    await loadAllProducts();
    await loadProducts();
  } catch (err) {
    dot.className = 'dot off';
    backendStatus.textContent = `Backend injoignable sur ${API_BASE}`;
    errorBox.textContent = `Impossible de contacter l'API sur ${API_BASE}. Démarrez le backend Node ou Java.`;
  }
}

for (const btn of backendButtons) {
  btn.addEventListener('click', () => {
    if (btn.dataset.base === API_BASE) return;
    API_BASE = btn.dataset.base;
    connect();
  });
}

connect();
