export default function ProductCard({ product, quantity, onAdd, onSetQty }) {
  const hasDiscount = product.mrp > product.price
  const off = hasDiscount ? Math.round(((product.mrp - product.price) / product.mrp) * 100) : 0

  return (
    <div className="product-card">
      {hasDiscount && <span className="discount-tag">{off}% OFF</span>}
      <div className="product-emoji">{product.emoji}</div>
      <div className="delivery-time">⏱ {product.deliveryMinutes} MINS</div>
      <div className="product-name">{product.name}</div>
      <div className="product-unit">{product.unit}</div>
      <div className="product-footer">
        <div className="price-block">
          <span className="price">₹{product.price}</span>
          {hasDiscount && <span className="mrp">₹{product.mrp}</span>}
        </div>
        {quantity === 0 ? (
          <button className="add-btn" onClick={onAdd}>
            ADD
          </button>
        ) : (
          <div className="stepper">
            <button onClick={() => onSetQty(quantity - 1)}>−</button>
            <span>{quantity}</span>
            <button onClick={() => onSetQty(quantity + 1)}>+</button>
          </div>
        )}
      </div>
    </div>
  )
}
