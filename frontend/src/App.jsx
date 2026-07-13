import { useEffect, useMemo, useRef, useState } from 'react'
import Header from './components/Header.jsx'
import ProductCard from './components/ProductCard.jsx'
import CartDrawer from './components/CartDrawer.jsx'
import AiChefModal from './components/AiChefModal.jsx'
import {
  addToCart,
  fetchCart,
  fetchCategories,
  fetchProducts,
  setCartQuantity,
} from './api.js'

const EMPTY_CART = { items: [], itemCount: 0, subtotal: 0, savings: 0 }

export default function App() {
  const [allProducts, setAllProducts] = useState([])
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [cart, setCart] = useState(EMPTY_CART)
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('All')
  const [cartOpen, setCartOpen] = useState(false)
  const [aiOpen, setAiOpen] = useState(false)
  const [error, setError] = useState('')
  const debounceRef = useRef(null)

  useEffect(() => {
    Promise.all([fetchProducts(), fetchCategories(), fetchCart()])
      .then(([prods, cats, cartView]) => {
        setAllProducts(prods)
        setProducts(prods)
        setCategories(cats)
        setCart(cartView)
      })
      .catch(() => setError('Could not reach the store. Is the backend running on port 8080?'))
  }, [])

  useEffect(() => {
    clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => {
      fetchProducts({ search, category: search ? '' : category })
        .then(setProducts)
        .catch(() => {})
    }, 250)
    return () => clearTimeout(debounceRef.current)
  }, [search, category])

  const productById = useMemo(() => {
    const map = new Map()
    allProducts.forEach((p) => map.set(p.id, p))
    return map
  }, [allProducts])

  const quantities = useMemo(() => {
    const map = new Map()
    cart.items.forEach((line) => map.set(line.product.id, line.quantity))
    return map
  }, [cart])

  const handleAdd = (productId) => addToCart(productId).then(setCart).catch(() => {})
  const handleSetQty = (productId, qty) => setCartQuantity(productId, qty).then(setCart).catch(() => {})

  return (
    <div className="app">
      <Header
        search={search}
        onSearch={setSearch}
        itemCount={cart.itemCount}
        subtotal={cart.subtotal}
        onCartClick={() => setCartOpen(true)}
      />

      <main className="container">
        <section className="ai-banner" onClick={() => setAiOpen(true)}>
          <div>
            <h2>✨ Cook with AI</h2>
            <p>Tell us the dish — we'll pick every ingredient and add them to your cart.</p>
          </div>
          <button className="ai-banner-btn">Try it →</button>
        </section>

        {error && <div className="error-note">{error}</div>}

        <div className="category-chips">
          {['All', ...categories].map((cat) => (
            <button
              key={cat}
              className={`chip ${category === cat && !search ? 'chip-active' : ''}`}
              onClick={() => {
                setSearch('')
                setCategory(cat)
              }}
            >
              {cat}
            </button>
          ))}
        </div>

        <h3 className="section-title">
          {search ? `Results for "${search}"` : category === 'All' ? 'All products' : category}
          <span className="section-count">{products.length} items</span>
        </h3>

        <div className="product-grid">
          {products.map((p) => (
            <ProductCard
              key={p.id}
              product={p}
              quantity={quantities.get(p.id) || 0}
              onAdd={() => handleAdd(p.id)}
              onSetQty={(qty) => handleSetQty(p.id, qty)}
            />
          ))}
          {products.length === 0 && !error && (
            <p className="empty-note">No products found. Try a different search.</p>
          )}
        </div>
      </main>

      <CartDrawer
        open={cartOpen}
        cart={cart}
        onClose={() => setCartOpen(false)}
        onSetQty={handleSetQty}
      />

      <AiChefModal
        open={aiOpen}
        onClose={() => setAiOpen(false)}
        productById={productById}
        onCartUpdate={(view) => {
          setCart(view)
          setCartOpen(true)
        }}
      />
    </div>
  )
}
