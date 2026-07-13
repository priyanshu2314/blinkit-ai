const json = (res) => {
  if (!res.ok) {
    return res.json().then(
      (body) => Promise.reject(new Error(body.error || `Request failed (${res.status})`)),
      () => Promise.reject(new Error(`Request failed (${res.status})`)),
    )
  }
  return res.json()
}

export const fetchProducts = ({ search, category } = {}) => {
  const params = new URLSearchParams()
  if (search) params.set('search', search)
  if (category) params.set('category', category)
  const qs = params.toString()
  return fetch(`/api/products${qs ? `?${qs}` : ''}`).then(json)
}

export const fetchCategories = () => fetch('/api/categories').then(json)

export const fetchCart = () => fetch('/api/cart').then(json)

export const addToCart = (productId, quantity = 1) =>
  fetch('/api/cart/items', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productId, quantity }),
  }).then(json)

export const setCartQuantity = (productId, quantity) =>
  fetch(`/api/cart/items/${productId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ quantity }),
  }).then(json)

export const addAllToCart = (items) =>
  fetch('/api/cart/add-all', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ items }),
  }).then(json)

export const suggestIngredients = (dish) =>
  fetch('/api/ai/suggest-ingredients', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ dish }),
  }).then(json)
