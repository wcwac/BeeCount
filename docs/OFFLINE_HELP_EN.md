# BeeCount · Offline edition

Modified by wcwac from BeeCount by sunxiao (TNT-Likely). The original copyright and license are retained.

## Daily use

- Use the bottom navigation for transactions, statistics, accounts and settings.
- Tap the record button to add an expense, income or transfer.
- Tap the ledger name on the home screen to switch ledgers. Use ledger management to create, edit or delete a ledger and manage its budget.
- Recurring transactions and bookkeeping reminders are available under automation settings.
- Photo attachments are added only when you choose a file or take a photo. The app does not monitor screenshots or your photo library.

## Data and backups

Records are stored on this device. Cloud sync, shared ledgers, AI recognition, online exchange rates and in-app updates are unavailable.

Export backups regularly from data management and copy them to a safe location yourself. Uninstalling the app or clearing its data deletes local records. Automatic system cloud backup is disabled. Back up your current data before restoring another backup.

This edition has a different Android application ID from the original. To migrate, export from the original app and import into this edition. Check transaction counts, balances and attachments before removing your original data.

## Multiple currencies

Foreign-currency transactions use locally stored or manually entered rates. Review converted amounts after changing a ledger's base currency; the app does not fetch current rates.

## Appearance and security

Light/dark mode, theme color, text size and app lock remain available. Reminder delivery also depends on system notification and battery settings.

## Updates and license

Updates are built through GitHub Actions in your own repository. Updates must use the same application ID and signing key.

Original project: https://github.com/TNT-Likely/BeeCount . The original license is available in About.


## Finance and complete backups

Open Assets → Finance for reimbursements, money lent/borrowed, instalments, mortgages and investments. Reimbursements support linked existing expenses and partial receipts. Principal movements are excluded from income, expense and budgets; interest and realized investment gains/losses are counted separately. Fixed-rate monthly schedules support equal payments or equal principal, sequential payments and early settlement. Investment cost uses weighted averaging; purchases include fees and sales use net proceeds. Prices, balances and cash flows are entered manually and remain offline.

Turn off the cash-flow switch when registering an existing position. Use the finance detail screen to undo the latest event or delete a record. Prepaid accounts support top-ups by transfer and spending by expense. The homepage calendar icon shows daily totals and transactions.

Data management offers a complete database JSON backup including finance plans, events, links and flags. Restore replaces the local database; export your existing database first. Attachment files and application preferences are excluded. Export/import attachment ZIPs separately. CSV and configuration YAML do not preserve the full financial relationships.
