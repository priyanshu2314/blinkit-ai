import { useState } from 'react'
import { addAllToCart, suggestIngredients } from '../api.js'

const EXAMPLES = ['Sauté vegetables', 'Paneer butter masala', 'Veg fried rice', 'Tomato soup']

export default function AiChefModal({ open, onClose, productById, onCartUpdate }) {
  const [dish, setDish] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [selected, setSelected] = useState(new Set())
  const [error, setError] = useState('')
  const [adding, setAdding] = useState(false)

  if (!open) return null

  const runSuggest = (name) => {
    const query = (name ?? dish).trim()
    if (!query) return
    setDish(query)
    setLoading(true)
    setError('')
    setResult(null)
    suggestIngredients(query)
      .then((res) => {
        setResult(res)
        setSelected(new Set(res.items.map((i) => i.productId)))
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }

  const toggle = (productId) => {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(productId)) next.delete(productId)
      else next.add(productId)
      return next
    })
  }

  const selectedItems = result ? result.items.filter((i) => selected.has(i.productId)) : []
  const selectedTotal = selectedItems.reduce((sum, item) => {
    const p = productById.get(item.productId)
    return sum + (p ? p.price * item.quantity : 0)
  }, 0)

  const handleAddAll = () => {
    setAdding(true)
    addAllToCart(selectedItems.map((i) => ({ productId: i.productId, quantity: i.quantity })))
      .then((view) => {
        onCartUpdate(view)
        handleClose()
      })
      .catch((e) => setError(e.message))
      .finally(() => setAdding(false))
  }

  const handleClose = () => {
    setDish('')
    setResult(null)
    setError('')
    setLoading(false)
    onClose()
  }

  return (
    <>
      <div className="overlay overlay-open" onClick={handleClose} />
      <div className="ai-modal">
        <div className="ai-modal-header">
          <div>
            <h3>✨ Cook with AI</h3>
            <p>What do you want to cook today?</p>
          </div>
          <button className="close-btn" onClick={handleClose}>
            ✕
          </button>
        </div>

        <form
          className="ai-input-row"
          onSubmit={(e) => {
            e.preventDefault()
            runSuggest()
          }}
        >
          <input
            autoFocus
            value={dish}
            onChange={(e) => setDish(e.target.value)}
            placeholder="e.g. Sauté vegetables"
          />
          <button type="submit" disabled={loading || !dish.trim()}>
            {loading ? '…' : 'Get ingredients'}
          </button>
        </form>

        <div className="ai-examples">
          {EXAMPLES.map((ex) => (
            <button key={ex} className="chip" onClick={() => runSuggest(ex)}>
              {ex}
            </button>
          ))}
        </div>

        {loading && (
          <div className="ai-loading">
            <div className="spinner" />
            <p>Planning your ingredients…</p>
          </div>
        )}

        {error && <div className="error-note">{error}</div>}

        {result && !loading && (
          <div className="ai-result">
            <div className="ai-recipe-head">
              <h4>{result.recipeName}</h4>
              <span className={`source-badge ${result.source === 'ai' ? 'badge-ai' : 'badge-book'}`}>
                {result.source === 'ai' ? '✨ AI generated' : '📖 Recipe book'}
              </span>
            </div>
            <p className="ai-recipe-desc">{result.description}</p>

            {result.items.length > 0 && (
              <div className="ai-items">
                {result.items.map((item) => {
                  const p = productById.get(item.productId)
                  if (!p) return null
                  const checked = selected.has(item.productId)
                  return (
                    <label className={`ai-item ${checked ? '' : 'ai-item-off'}`} key={item.productId}>
                      <input type="checkbox" checked={checked} onChange={() => toggle(item.productId)} />
                      <span className="ai-item-emoji">{p.emoji}</span>
                      <span className="ai-item-info">
                        <span className="ai-item-name">
                          {p.name} {item.quantity > 1 ? `× ${item.quantity}` : ''}
                        </span>
                        <span className="ai-item-note">{item.note || p.unit}</span>
                      </span>
                      <span className="ai-item-price">₹{Math.round(p.price * item.quantity * 100) / 100}</span>
                    </label>
                  )
                })}
              </div>
            )}

            {result.missing.length > 0 && (
              <div className="ai-missing">
                <b>Not available in store:</b> {result.missing.join(', ')}
              </div>
            )}

            {result.items.length > 0 && (
              <button className="add-all-btn" disabled={selectedItems.length === 0 || adding} onClick={handleAddAll}>
                {adding
                  ? 'Adding…'
                  : `Add ${selectedItems.length} item${selectedItems.length === 1 ? '' : 's'} to cart · ₹${
                      Math.round(selectedTotal * 100) / 100
                    }`}
              </button>
            )}
          </div>
        )}
      </div>
    </>
  )
}
