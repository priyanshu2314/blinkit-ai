

https://github.com/user-attachments/assets/8102d7e2-f9fb-4524-88d6-11854372154d

# blinkAIt — Blinkit-style grocery app with AI ingredient planning

A demo grocery delivery web app (like Blinkit) with an AI feature: type a dish
name ("Sauté vegetables", "Paneer butter masala"…) and it suggests every
ingredient from the store catalog, with one tap to add them all to your cart.

## Stack

- **Backend** — Java 21, Spring Boot (`backend/`), REST API on port **8080**
- **Frontend** — React + Vite (`frontend/`), dev server on port **5173** (proxies `/api` to 8080)
- **AI** — Claude (`claude-opus-4-8`) via the official Anthropic Java SDK, with a
  built-in offline recipe book as fallback so the app works without an API key

## Run it

Terminal 1 — backend (Maven wrapper downloads everything on first run):

```
cd backend
mvnw.cmd spring-boot:run
```

Terminal 2 — frontend:

```
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

## Enable real AI suggestions

Without a key, "Cook with AI" uses the built-in recipe book (14 curated dishes +
fuzzy matching) and shows a "📖 Recipe book" badge.

To have Claude plan ingredients for *any* dish, add your Anthropic API key.
Open `backend/.env` and paste your key after the `=` (no quotes):

```
ANTHROPIC_API_KEY=sk-ant-api03-...
```

Then start the backend normally — no environment variable needed. Responses now
show a "✨ AI generated" badge. If the API call fails for any reason, the app
silently falls back to the recipe book.

`backend/.env` is **git-ignored** — your key never gets committed. Get a key at
https://platform.claude.com → Settings → API Keys. (An `ANTHROPIC_API_KEY`
environment variable also still works if you prefer that.)

## API overview

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/products?search=&category=` | Product catalog (55 items) |
| GET | `/api/categories` | Category list |
| GET | `/api/cart` | Current cart with totals & savings |
| POST | `/api/cart/items` | Add `{productId, quantity}` |
| POST | `/api/cart/add-all` | Add `{items: [{productId, quantity}]}` in one call |
| PATCH | `/api/cart/items/{id}` | Set quantity (0 removes) |
| DELETE | `/api/cart` | Clear cart |
| POST | `/api/ai/suggest-ingredients` | `{dish}` → `{recipeName, description, items[], missing[], source}` |

## How the AI feature works

`RecipeAiService` sends Claude the dish name plus the full store catalog
(id | name | category | unit) and asks for strict JSON: which products to buy,
how many units, a prep note per ingredient, and anything the store doesn't
carry (`missing`). Product ids are validated against the catalog before they
reach the UI. `RecipeKnowledgeBase` provides the offline fallback.

## Notes

- The cart is in-memory (resets on backend restart) — no database needed for the demo.
- Product images are emoji to keep the demo self-contained.
