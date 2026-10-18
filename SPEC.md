# Closet Finder: Android app spec

Single-user Android app for a small clothing reseller. She buys articles from different stores and posts photos on WhatsApp Status. When a customer asks about one later, she forgets which store it came from and what it cost. This app lets her find the item by **code**, **tags**, or **image similarity**, entirely on-device.

## Constraints
- One user, one phone. No backend, no login, no cloud.
- Scale: hundreds to a few thousand items.
- Everything offline. Open-source models only.
- Saving an item must take **under ~10 seconds**. Capture speed matters more than search accuracy.

## Stack
- Kotlin, Jetpack Compose, Material 3
- Room (SQLite) with an FTS4 table for text search
- MediaPipe Tasks `ImageEmbedder` (default MobileNet-based model) for image embeddings
- Image cropping: CanHub Android-Image-Cropper (or similar)
- Coil for image loading
- minSdk 26

## Data model
`Item`
- `id` (Long, PK)
- `code` (String, unique, e.g. `A001`, auto-generated and sequential)
- `imagePath` (app-private copy of the image)
- `store` (String, autocomplete from previous values)
- `price` (Int, PKR, what she paid)
- `salePrice` (Int?, optional)
- `tags` (String, comma-separated)
- `notes` (String?)
- `createdAt` (Long)
- `embedding` (ByteArray, float vector)

`ItemFts` (FTS4 over `code`, `store`, `tags`, `notes`)

## Features

### 1. Add item
- Entry points: in-app camera/gallery picker, and the **Android share sheet** (`ACTION_SEND` image/*) from WhatsApp, Gallery, etc.
- Optional crop step.
- Form: store (autocomplete), price, tags (chips with suggestions from existing tags), notes.
- On save: copy the image into app storage, compute the embedding, assign the next code.

### 2. Share to Status with stamped code
- From an item, a "Share" action renders the code (e.g. `#A023`) in a corner of the image, legible on both light and dark images, and opens the share sheet, so she can pick WhatsApp.

### 3. Search screen (home)
- One search box:
  - If the input matches the code pattern, jump straight to that item.
  - Otherwise run FTS over code, store, tags and notes (prefix matching, so "mar" finds "maroon").
- **Image search** button: pick an image, crop it, then show the top 5 by cosine similarity with thumbnail, code, store, price and score.
- A separate share-sheet target, "Find in Closet Finder", runs image search directly on a shared image (e.g. a customer's screenshot).
- Filters: store, price range, date added.
- Grid of recent items when the search box is empty.

### 4. Item detail and edit
- View, edit, delete, and share with the stamped code.

### 5. Backup
- Export and import a zip (database plus images) to device storage, so she doesn't lose data when changing phones.

## Image search details
- Brute-force cosine similarity over all embeddings loaded in memory. No vector index needed at this scale.
- Keep the embedding model behind an interface (`Embedder`) so it can be swapped for MobileCLIP or DINOv2-small later. Store a `modelVersion` and re-embed all items if it changes.
- Always show the top few results, not a single answer; similar designs in different colours are expected.

## Milestones
1. Project setup, Room schema, add item (picker plus form), list/grid view
2. Code generation, FTS text search, filters
3. MediaPipe embedder, image search, crop step
4. Share-sheet entry points (add item, find item)
5. Stamped-code share to WhatsApp
6. Backup export/import
7. Polish: speed up the add flow, empty states, Urdu-friendly text input

## Out of scope (for now)
- Multiple users or sync, sales/inventory accounting, a server
