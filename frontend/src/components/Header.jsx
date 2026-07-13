export default function Header({ search, onSearch, itemCount, subtotal, onCartClick }) {
  return (
    <header className="header">
      <div className="container header-inner">
        <div className="brand">
          <span className="brand-name">
            blink<span className="brand-ai">AI</span>t
          </span>
          <div className="delivery-info">
            <strong>Delivery in 10 minutes</strong>
            <span>HSR Layout, Bengaluru ▾</span>
          </div>
        </div>

        <div className="search-box">
          <span className="search-icon">🔍</span>
          <input
            value={search}
            onChange={(e) => onSearch(e.target.value)}
            placeholder='Search "milk", "paneer", "vegetables"…'
          />
        </div>

        <button className="cart-btn" onClick={onCartClick}>
          🛒
          <span>
            {itemCount === 0 ? 'My Cart' : `${itemCount} item${itemCount > 1 ? 's' : ''} · ₹${subtotal}`}
          </span>
        </button>
      </div>
    </header>
  )
}
