# Razorpay Simulation Payment Flow

This project uses a **Razorpay-style simulation only**. It does not charge real money and does not call the real Razorpay gateway.

Flow:

1. `index.html` — Student fee dashboard & Razorpay Standard Checkout modal trigger
2. Official Razorpay Checkout Modal (`new Razorpay(options).open()`) — secure payment handling
3. Backend (`/api/payments/verify-payment`) — HMAC-SHA256 signature verification & Supabase PostgreSQL saving
4. `payment-success.html` — Confirmation page with link to full receipt
5. `receipt.html` — Printable student fee receipt
6. `payment-failed.html` — Friendly error page if payment is declined or verification fails

Important database fix:
- `TransactionRepository` now explicitly inserts `version`.
- If the existing PostgreSQL table was created earlier without a default, run:
  `ALTER TABLE transactions ALTER COLUMN version SET DEFAULT 1;`
- Existing rows are not modified by `CREATE TABLE IF NOT EXISTS`, so this ALTER may be needed once on the current database.

No browser `alert()` is used for payment errors.
