export default function CartDrawer({ open, cart, onClose, onSetQty }) {
  return (
    <>
      <div className={`overlay ${open ? 'overlay-open' : ''}`} onClick={onClose} />
      <aside className={`cart-drawer ${open ? 'cart-drawer-open' : ''}`}>
        <div className="cart-header">
          <h3>My Cart</h3>
          <button className="close-btn" onClick={onClose}>
            ✕
          </button>
        </div>

        {cart.items.length === 0 ? (
          <div className="cart-empty">
            <div className="cart-empty-emoji">🛒</div>
            <p>Your cart is empty</p>
            <span>Try "Cook with AI" to fill it in one tap!</span>
          </div>
        ) : (
          <>
            <div className="cart-savings">
              ⏱ Delivery in 10 minutes {cart.savings > 0 && <b>· Saving ₹{cart.savings}</b>}
            </div>
            <div className="cart-items">
              {cart.items.map((line) => (
                <div className="cart-item" key={line.product.id}>
                  <span className="cart-item-emoji">{line.product.emoji}</span>
                  <div className="cart-item-info">
                    <div className="cart-item-name">{line.product.name}</div>
                    <div className="cart-item-unit">{line.product.unit}</div>
                    <div className="cart-item-price">₹{line.lineTotal}</div>
                  </div>
                  <div className="stepper stepper-green">
                    <button onClick={() => onSetQty(line.product.id, line.quantity - 1)}>−</button>
                    <span>{line.quantity}</span>
                    <button onClick={() => onSetQty(line.product.id, line.quantity + 1)}>+</button>
                  </div>
                </div>
              ))}
            </div>
            <div className="cart-footer">
              <div className="cart-total">
                <span>
                  ₹{cart.subtotal}
                  <small>TOTAL</small>
                </span>
                <button className="checkout-btn" onClick={() => alert('This is a demo — checkout is not wired up.')}>
                  Proceed to Checkout →
                </button>
              </div>
            </div>
          </>
        )}
      </aside>
    </>
  )
}
