package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.json.JSONObject;
import org.json.JSONArray;

/** Durable host user operations. These keys never pretend to be original semantic IDs. */
public final class AccountingOperations {
    public static final String TABLE = "accounting_operations";
    private static final long MAX_MINOR = 99999999999999L;
    private final ReferenceStore store;
    public AccountingOperations(ReferenceStore store) { this.store = store; }

    /** Called by schema9 within SQLiteOpenHelper's create/upgrade transaction. */
    public static void installV9Schema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE accounting_operations(operation_id TEXT PRIMARY KEY,entry_kind TEXT NOT NULL CHECK(entry_kind IN ('transfer','lend')),entry_id INTEGER NOT NULL CHECK(entry_id>0),owner_semantic_id INTEGER NOT NULL,semantic_id INTEGER NOT NULL,book_id INTEGER NOT NULL REFERENCES books(id),payload TEXT NOT NULL,created INTEGER NOT NULL,UNIQUE(entry_kind,entry_id))");
        db.execSQL("CREATE INDEX accounting_operations_book ON accounting_operations(book_id,created)");
        db.execSQL("ALTER TABLE transactions ADD COLUMN manual_operation_id TEXT");
        db.execSQL("CREATE UNIQUE INDEX transactions_manual_operation ON transactions(manual_operation_id) WHERE manual_operation_id IS NOT NULL");
        db.execSQL("ALTER TABLE candidates ADD COLUMN capture_key TEXT");
        db.execSQL("ALTER TABLE candidates ADD COLUMN evidence_kind TEXT");
        db.execSQL("ALTER TABLE candidates ADD COLUMN capture_window_key TEXT");
        db.execSQL("CREATE UNIQUE INDEX candidates_pending_session ON candidates(package_name,capture_window_key) WHERE capture_window_key IS NOT NULL AND state='PENDING'");
        db.execSQL("CREATE UNIQUE INDEX candidates_strong_identity ON candidates(package_name,capture_key) WHERE capture_key IS NOT NULL AND evidence_kind='PAYMENT_ID'");
        db.execSQL("CREATE UNIQUE INDEX candidates_pending_window ON candidates(package_name,capture_key) WHERE capture_key IS NOT NULL AND evidence_kind='WINDOW_RECEIPT' AND state='PENDING'");
    }

    /** The UUID belongs to a new manual draft, never to a candidate or an existing Bill edit. */
    public long saveBill(JSONObject value,String operationId) {
        JSONObject snapshot=Json.copy(value);AccountingTargets.requireResolved(store,snapshot,null);String id=uuid(operationId);
        return AccountingCommitScope.run(store,()->{
            rejectOtherDomain(id);
            JSONObject prior=store.one("SELECT id FROM transactions WHERE manual_operation_id=?",id);
            if(prior!=null)return prior.optLong("id");
            if(store.one("SELECT operation_id FROM accounting_operations WHERE operation_id=?",id)!=null)
                throw new IllegalArgumentException("此保存标识已用于转账或还款");
            long row=store.saveTransaction(snapshot,0,null);
            ContentValues receipt=new ContentValues();receipt.put("manual_operation_id",id);
            AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("transactions",receipt,
                    "id=? AND manual_operation_id IS NULL",new String[]{""+row}));
            return row;
        });
    }

    /** New manual transfer/credit repayment. One UUID survives retries and rotation. */
    public JSONObject saveTransfer(JSONObject value, String operationId, boolean repayment) {
        JSONObject snapshot = Json.copy(value);
        AccountingTargets.requireResolved(store,snapshot,null);
        String id = uuid(operationId);
        return AccountingCommitScope.run(store, () -> {
            rejectOtherDomain(id);
            JSONObject previous = store.one("SELECT * FROM accounting_operations WHERE operation_id=?", id);
            if (previous != null) {
                if (!previous.optString("entry_kind").equals(repayment ? "lend" : "transfer"))
                    throw new IllegalArgumentException("此保存标识已用于另一种操作");
                return Json.parse(previous.optString("payload"));
            }
            if(store.one("SELECT id FROM transactions WHERE manual_operation_id=?",id)!=null)
                throw new IllegalArgumentException("此保存标识已用于普通账单");
            JSONObject book = store.book(snapshot.optLong("book_id"));
            if (book == null || book.optInt("archived") != 0 || !"CNY".equals(book.optString("currency"))
                    || book.isNull("owner_semantic_id") || book.optInt("owner_semantic_id") != ReferenceStore.LOCAL_OFFLINE_OWNER)
                throw new IllegalArgumentException("请选择当前离线身份的人民币账本");
            JSONObject from = selected(snapshot.optLong("account_id"));
            JSONObject to = selected(snapshot.optLong("to_account_id"));
            if (from.optLong("id") == to.optLong("id")) throw new IllegalArgumentException("请选择不同的转出与转入账户");
            if (repayment && to.optInt("asset_type") != 2) throw new IllegalArgumentException("还款目标必须是信用账户");
            long amount = snapshot.optLong("amount"), fee = snapshot.optLong("fee");
            if (amount <= 0 || amount > MAX_MINOR || fee < -MAX_MINOR || fee > MAX_MINOR)
                throw new IllegalArgumentException("请输入有效金额与手续费");
            long time = snapshot.optLong("time");
            if (time <= 0 || time > 8640000000000000L) throw new IllegalArgumentException("记账时间无效");
            int mode = store.context().getSharedPreferences("reference_settings", android.content.Context.MODE_PRIVATE)
                    .getInt("original_transfer_mode", 0);
            if(snapshot.has("transfer_mode"))mode=snapshot.optInt("transfer_mode");
            if(mode!=0&&mode!=1)throw new IllegalArgumentException("转账金额模式无效");
            if (mode == 1 && fee > 0 && amount <= fee) throw new IllegalArgumentException("到账金额必须大于零");
            int owner = ReferenceStore.LOCAL_OFFLINE_OWNER;
            // Explicit account membership wins. Otherwise a manual draft's selected
            // book is its context; the app's global current book may be different.
            java.util.function.LongFunction<JSONObject> feeBooks = raw -> {
                JSONObject asset=store.assetRepository().p(raw,owner);
                for(Long semantic:store.assetRepository().showBook(asset.optLong("id"))){
                    JSONObject assigned=store.bookRepository().byIdentity(owner,semantic);
                    if(assigned!=null)return assigned;
                }
                return book;
            };
            if (fee != 0) {
                JSONObject feeBook = feeBooks.apply(from.optLong("semantic_id"));
                if (feeBook == null || !"CNY".equals(feeBook.optString("currency")))
                    throw new IllegalArgumentException("手续费账本币种与账户不一致");
            }
            OriginalFinancialEffects effects = new OriginalFinancialEffects(store, owner, book.optInt("owner_semantic_id"));
            String feeText = BigDecimal.valueOf(fee, 2).toPlainString();
            double number = BigDecimal.valueOf(amount, 2).doubleValue();
            long row, semantic, feeRow = 0;
            String entryKind = repayment ? "lend" : "transfer";
            if (repayment) {
                OriginalAutoCreditRepaymentCommands commands = new OriginalAutoCreditRepaymentCommands(store, owner,
                        book.optInt("owner_semantic_id"), new OriginalLendActions(store, owner), raw -> feeBooks.apply(raw).optLong("semantic_id"));
                OriginalAutoCreditRepaymentCommands.Result saved = commands.save(from.optLong("semantic_id"), to.optLong("semantic_id"),
                        number, feeText, mode, snapshot.optString("remark"), time,
                        bill -> OriginalTransferConfirmation.projectFee(store, bill),
                        new OriginalAutoCreditRepaymentCommands.Effects() {
                            public void warningNonPositiveArrival() { throw new IllegalArgumentException("到账金额必须大于零"); }
                            public void repaymentChanged(OriginalLendRecord lend, boolean ok) { effects.lendChanged(lend, ok); }
                        }, null);
                if (!saved.saved) throw new IllegalStateException("还款未能完整保存，可安全重试");
                row = saved.lend.localRowId; semantic=saved.lend.lendId;
                if (saved.interestBill != null) feeRow = saved.interestBill.localRowId;
            } else {
                OriginalAutoTransferCommands commands = new OriginalAutoTransferCommands(store, owner, book.optInt("owner_semantic_id"),
                        new OriginalTransferActions(store, owner, effects::transferChanged), raw -> feeBooks.apply(raw).optLong("semantic_id"));
                OriginalAutoTransferCommands.Result saved = commands.save(from.optLong("semantic_id"), to.optLong("semantic_id"),
                        number, feeText, mode, snapshot.optString("remark"), time,
                        bill -> OriginalTransferConfirmation.projectFee(store, bill),
                        () -> { throw new IllegalArgumentException("到账金额必须大于零"); }, null);
                if (!saved.saved) throw new IllegalStateException("转账未能完整保存，可安全重试");
                row = saved.transfer.localRowId; semantic=saved.transfer.transferId;
                if (saved.feeBill != null) feeRow = saved.feeBill.localRowId;
            }
            JSONObject result = typedDto(entryKind, row, book.optLong("id"), amount, fee, time,
                    snapshot.optString("remark"), from.optLong("id"), to.optLong("id"), feeRow);
            Json.put(result, "operation_id", id);
            Json.put(result,"entry_owner_semantic_id",owner);Json.put(result,"entry_semantic_id",semantic);
            Json.put(result,"attachments",snapshot.optString("attachments","[]"));
            Json.put(result,"tag_ids",snapshot.optJSONArray("tag_ids")==null?new JSONArray():snapshot.optJSONArray("tag_ids"));
            ContentValues receipt = new ContentValues();
            receipt.put("operation_id", id); receipt.put("entry_kind", entryKind); receipt.put("entry_id", row);
            receipt.put("owner_semantic_id",owner);receipt.put("semantic_id",semantic);
            receipt.put("book_id", book.optLong("id")); receipt.put("payload", result.toString());
            receipt.put("created", System.currentTimeMillis());
            if (store.getWritableDatabase().insertOrThrow(TABLE, null, receipt) == -1)
                throw new IllegalStateException("保存凭据失败，可安全重试");
            return result;
        });
    }

    private void rejectOtherDomain(String operationId) {
        if(store.one("SELECT operation_id FROM accounting_financial_operations WHERE operation_id=?",operationId)!=null
                ||store.one("SELECT operation_id FROM accounting_entry_edits WHERE operation_id=?",operationId)!=null)
            throw new IllegalArgumentException("此保存标识已用于其他记账或编辑操作");
    }

    private JSONObject selected(long localId) {
        JSONObject account = store.assetRepository().byRowId(localId);
        if (account == null || !AssetRepository.ORIGINAL.equals(account.optString("asset_origin"))
                || account.isNull("owner_semantic_id") || account.optInt("owner_semantic_id") != ReferenceStore.LOCAL_OFFLINE_OWNER
                || account.optInt("archived") != 0 || account.optInt("asset_type") == 9)
            throw new IllegalArgumentException("请选择可用的当前离线账户");
        if (!account.isNull("asset_currency") && !account.optString("asset_currency").isEmpty())
            throw new IllegalArgumentException("跨币种账户操作尚未接入，请选择本币账户");
        return account;
    }

    public String upsertCapturedCandidate(JSONObject payload, String pkg, long book, String key, String evidence) {
        if (pkg == null || pkg.isEmpty() || pkg.length() > 300 || store.book(book) == null)
            throw new IllegalArgumentException("采集来源或账本无效");
        boolean strong = "PAYMENT_ID".equals(evidence), weak = "WINDOW_RECEIPT".equals(evidence);
        String identity = (strong || weak) && key != null && !key.isEmpty() ? key : null;
        if (identity != null && identity.length() > 1024) throw new IllegalArgumentException("采集标识过长");
        JSONObject snapshot = Json.copy(payload);
        String windowKey=snapshot.isNull("capture_window_key")?null:snapshot.optString("capture_window_key");
        if(windowKey!=null&&(windowKey.isEmpty()||windowKey.length()>1024))throw new IllegalArgumentException("采集会话标识无效");
        if (weak) Json.put(snapshot, "capture_requires_confirmation", true);
        return AccountingCommitScope.run(store, () -> {
            JSONObject previous = identity == null ? null : store.one("SELECT * FROM candidates WHERE package_name=? AND capture_key=? AND evidence_kind=?"
                    + (strong ? "" : " AND state='PENDING'") + " ORDER BY created DESC LIMIT 1", pkg, identity, evidence);
            JSONObject window=windowKey==null?null:store.one("SELECT * FROM candidates WHERE package_name=? AND capture_window_key=? AND state='PENDING' LIMIT 1",pkg,windowKey);
            String attachWindow=windowKey;
            if(previous==null&&window!=null) {
                boolean incompatibleStrong=strong&&identity!=null&&"PAYMENT_ID".equals(window.optString("evidence_kind"))&&!identity.equals(window.optString("capture_key"));
                if(incompatibleStrong)attachWindow=null;else previous=window;
            }
            if(previous!=null&&strong&&window!=null&&!previous.optString("id").equals(window.optString("id"))
                    &&!"PAYMENT_ID".equals(window.optString("evidence_kind"))) {
                // A real payment ID proves this still-pending weak observation was already handled.
                // Preserve the payload for history, rather than leave a second actionable bill.
                JSONObject duplicate=Json.parse(window.optString("payload"));
                Json.put(duplicate,"capture_duplicate_hint",true);Json.put(duplicate,"capture_requires_confirmation",true);
                Json.put(duplicate,"capture_resolved_candidate_id",previous.optString("id"));
                ContentValues dismissed=new ContentValues();dismissed.put("state","DISMISSED");dismissed.put("payload",duplicate.toString());
                AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("candidates",dismissed,"id=? AND state='PENDING'",new String[]{window.optString("id")}));
            }
            if (previous != null) {
                if ("PENDING".equals(previous.optString("state"))) {
                    JSONObject existing = Json.parse(previous.optString("payload"));
                    Iterator<String> keys = snapshot.keys();
                    while (keys.hasNext()) {
                        String field = keys.next();
                        Object old = existing.opt(field);
                        if (old == null || old == JSONObject.NULL || old instanceof String && ((String) old).isEmpty())
                            Json.put(existing, field, snapshot.opt(field));
                    }
                    for(String flag:new String[]{"capture_requires_confirmation","capture_account_requires_confirmation","capture_duplicate_hint"})
                        if(snapshot.optBoolean(flag))Json.put(existing,flag,true);
                    JSONArray conflicts=existing.optJSONArray("capture_conflict_fields");
                    java.util.Set<String> fields=new java.util.LinkedHashSet<>();
                    if(conflicts!=null)for(int i=0;i<conflicts.length();i++)fields.add(conflicts.optString(i));
                    for(String field:new String[]{"amount","kind","currency"}) {
                        Object old=Json.parse(previous.optString("payload")).opt(field),next=snapshot.opt(field);
                        if(old!=null&&old!=JSONObject.NULL&&next!=null&&next!=JSONObject.NULL&&!sameValue(old,next))fields.add(field);
                    }
                    if(!fields.isEmpty()){JSONArray all=new JSONArray();for(String field:fields)all.put(field);Json.put(existing,"capture_conflict_fields",all);Json.put(existing,"capture_requires_confirmation",true);}
                    if (weak) Json.put(existing, "capture_requires_confirmation", true);
                    ContentValues update = new ContentValues(); update.put("payload", existing.toString());
                    if(strong&&identity!=null){update.put("capture_key",identity);update.put("evidence_kind","PAYMENT_ID");Json.put(existing,"capture_identity_kind","PAYMENT_ID");update.put("payload",existing.toString());}
                    if(attachWindow!=null&&previous.isNull("capture_window_key"))update.put("capture_window_key",attachWindow);
                    AccountingCommitScope.requireChanged(store, store.getWritableDatabase().update("candidates", update,
                            "id=? AND state='PENDING'", new String[]{previous.optString("id")}));
                } else {
                    // A handled payment remains idempotent, but conflicting new evidence is visible.
                    JSONObject handled=Json.parse(previous.optString("payload"));
                    java.util.Set<String> fields=new java.util.LinkedHashSet<>();
                    JSONArray oldFields=handled.optJSONArray("capture_conflict_fields");
                    if(oldFields!=null)for(int i=0;i<oldFields.length();i++)fields.add(oldFields.optString(i));
                    for(String field:new String[]{"amount","kind","currency"}) {
                        Object old=handled.opt(field),next=snapshot.opt(field);
                        if(old!=null&&old!=JSONObject.NULL&&next!=null&&next!=JSONObject.NULL&&!sameValue(old,next))fields.add(field);
                    }
                    for(String flag:new String[]{"capture_requires_confirmation","capture_account_requires_confirmation","capture_duplicate_hint"})
                        if(snapshot.optBoolean(flag))Json.put(handled,flag,true);
                    if(!fields.isEmpty()){JSONArray all=new JSONArray();for(String field:fields)all.put(field);Json.put(handled,"capture_conflict_fields",all);Json.put(handled,"capture_requires_confirmation",true);}
                    ContentValues metadata=new ContentValues();metadata.put("payload",handled.toString());
                    AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("candidates",metadata,"id=?",new String[]{previous.optString("id")}));
                }
                return previous.optString("id");
            }
            if (store.scalar("SELECT COUNT(*) n FROM candidates WHERE state='PENDING'") >= 1000)
                throw new IllegalStateException("待确认列表已满");
            String id = UUID.randomUUID().toString();
            ContentValues v = new ContentValues(); v.put("id", id); v.put("payload", snapshot.toString());
            v.put("package_name", pkg); v.put("book_id", book); v.put("created", System.currentTimeMillis());
            if (identity != null) { v.put("capture_key", identity); v.put("evidence_kind", evidence); }
            if(attachWindow!=null)v.put("capture_window_key",attachWindow);
            if (store.getWritableDatabase().insertOrThrow("candidates", null, v) == -1)
                throw new IllegalStateException("采集记录未能持久保存");
            return id;
        });
    }

    public JSONObject captureIdentity(String key) {
        if (key == null || key.isEmpty()) return null;
        return store.one("SELECT id,state,evidence_kind,package_name FROM candidates WHERE"
                + " (capture_key=? AND (state='PENDING' OR evidence_kind='PAYMENT_ID')) OR (capture_window_key=? AND state='PENDING')"
                + " ORDER BY CASE state WHEN 'PENDING' THEN 0 ELSE 1 END,created DESC LIMIT 1", key,key);
    }

    /** Typed rows use entry_key; their numeric id must never be passed to Bill edit/delete. */
    public List<JSONObject> entries(long book, long start, long end, String search, boolean trash) {
        List<JSONObject> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (JSONObject source : store.transactions(book, start, end, search, trash)) {
            JSONObject bill = Json.copy(source);
            Json.put(bill, "entry_kind", "bill"); Json.put(bill, "entry_id", bill.optLong("id"));
            Json.put(bill, "entry_key", "bill:" + bill.optLong("id")); if(seen.add(bill.optString("entry_key")))result.add(bill);
        }
        for(JSONObject row:store.query("SELECT entry_kind,entry_id FROM accounting_operations WHERE book_id=?",""+book)) {
            String kind=row.optString("entry_kind");long id=row.optLong("entry_id");
            JSONObject value=store.accountingEntry(kind,id);
            if((value.optInt("deleted")!=0)==trash)append(result,seen,value,start,end,search);
        }
        for(JSONObject candidate:store.query("SELECT c.id,c.save_kind FROM candidates c WHERE c.book_id=? AND c.state='SAVED' AND c.save_kind IN ('transfer','lend')",""+book)) {
            String kind=candidate.optString("save_kind");
            JSONObject receipt=store.one("SELECT "+kind+"_row_id FROM candidate_"+kind+"_receipts WHERE candidate_id=?",candidate.optString("id"));
            if(receipt==null)continue;
            String raw=kind.equals("transfer")?"original_transfers":"original_lends";
            if(store.one("SELECT id FROM "+raw+" WHERE id=?",""+receipt.optLong(kind+"_row_id"))==null)continue;
            JSONObject value=store.accountingEntry(kind,receipt.optLong(kind+"_row_id"));
            if((value.optInt("deleted")!=0)==trash)append(result,seen,value,start,end,search);
        }
        // Loan/borrow rows use the same typed identity as candidate lend receipts.
        // C applies its own search before the shared dedupe/time filter.
        for(JSONObject loan:AccountingFinancialFlows.entries(store,book,start,end,search,trash))
            append(result,seen,loan,start,end,null);
        result.sort(Comparator.comparingLong((JSONObject item) -> item.optLong("time")).reversed()
                .thenComparing(item -> item.optString("entry_key")));
        return result.size()>2000?new ArrayList<>(result.subList(0,2000)):result;
    }

    private static void append(List<JSONObject> output, Set<String> seen, JSONObject row, long start, long end, String search) {
        if (row.optLong("time") < start || row.optLong("time") >= end) return;
        String query = search == null ? "" : search.trim().toLowerCase(java.util.Locale.ROOT);
        if (!query.isEmpty() && !(row.optString("remark") + " " + row.optString("financial_type")).toLowerCase(java.util.Locale.ROOT).contains(query)) return;
        if (seen.add(row.optString("entry_key"))) output.add(Json.copy(row));
    }

    private static JSONObject typedDto(String kind, long id, long book, long amount, long fee, long time,
            String remark, long from, long to, long feeBill) {
        return Json.object("entry_kind", kind, "entry_id", id, "entry_key", kind + ":" + id, "id", id,
                "book_id", book, "kind", "transfer", "financial_type", kind.equals("lend") ? "original_lend" : "original_transfer",
                "amount", amount, "currency", "CNY", "fee", fee, "time", time, "remark", remark == null ? "" : remark,
                "account_id", from, "from_account_id", from, "to_account_id", to, "fee_bill_id", feeBill == 0 ? null : feeBill,
                "deleted", 0, "settled", 1, "merchant", "", "discount", 0, "not_total", 1, "not_budget", 1,
                "category_name", kind.equals("lend") ? "信用还款" : "转账", "icon", "ic_other", "tags", "", "attachments", "[]");
    }

    public static void validateRestored(ReferenceStore store) {
        synchronized (store) {
            for(JSONObject bill:store.query("SELECT id,manual_operation_id FROM transactions WHERE manual_operation_id IS NOT NULL")) {
                String id=uuid(bill.optString("manual_operation_id"));
                if(store.one("SELECT operation_id FROM accounting_operations WHERE operation_id=?",id)!=null)
                    throw new IllegalArgumentException("同一保存标识不能同时指向普通账单与转账还款");
            }
            for (JSONObject receipt : store.query("SELECT * FROM accounting_operations")) {
                uuid(receipt.optString("operation_id"));
                String kind = receipt.optString("entry_kind");
                long row = receipt.optLong("entry_id");
                JSONObject payload = Json.parse(receipt.optString("payload"));
                if (!(kind.equals("transfer") || kind.equals("lend")) || row <= 0 || store.book(receipt.optLong("book_id")) == null
                        || !kind.equals(payload.optString("entry_kind")) || row != payload.optLong("entry_id")
                        || !((kind + ":" + row).equals(payload.optString("entry_key")))
                        || receipt.optLong("book_id") != payload.optLong("book_id"))
                    throw new IllegalArgumentException("记账操作凭据无效");
                if(receipt.optInt("owner_semantic_id")!=ReferenceStore.LOCAL_OFFLINE_OWNER
                        ||payload.optLong("entry_owner_semantic_id",Long.MIN_VALUE)!=receipt.optLong("owner_semantic_id")
                        ||payload.optLong("entry_semantic_id",Long.MIN_VALUE)!=receipt.optLong("semantic_id"))throw new IllegalArgumentException("记账操作身份无效");
                if (kind.equals("transfer")) {
                    OriginalTransferRecord raw = new OriginalTransferRepository(store).byLocalRowId(row);
                    if (raw == null || raw.userId != receipt.optInt("owner_semantic_id") ||raw.transferId!=receipt.optLong("semantic_id")) throw new IllegalArgumentException("转账凭据关联无效");
                } else {
                    OriginalLendRecord raw = new OriginalLendRepository(store).byLocalRowId(row);
                    if (raw == null || raw.userId != receipt.optInt("owner_semantic_id") ||raw.lendId!=receipt.optLong("semantic_id")) throw new IllegalArgumentException("还款凭据关联无效");
                }
            }
            for (JSONObject candidate : store.query("SELECT capture_key,evidence_kind,capture_window_key FROM candidates")) {
                if(!candidate.isNull("capture_window_key")&&(candidate.optString("capture_window_key").isEmpty()||candidate.optString("capture_window_key").length()>1024))throw new IllegalArgumentException("采集会话标识无效");
                boolean keyed = !candidate.isNull("capture_key");
                String evidence = candidate.optString("evidence_kind");
                if (keyed && (candidate.optString("capture_key").isEmpty() || candidate.optString("capture_key").length() > 1024
                        || !("PAYMENT_ID".equals(evidence) || "WINDOW_RECEIPT".equals(evidence)))
                        || !keyed && !candidate.isNull("evidence_kind")) throw new IllegalArgumentException("采集身份凭据无效");
            }
        }
    }

    private static boolean sameValue(Object first,Object second) {
        if(first instanceof Number&&second instanceof Number) {
            try{return new BigDecimal(first.toString()).compareTo(new BigDecimal(second.toString()))==0;}
            catch(NumberFormatException invalid){return false;}
        }
        return first.equals(second);
    }

    private static String uuid(String id) {
        if (id == null) throw new IllegalArgumentException("保存操作标识缺失");
        try { if (!UUID.fromString(id).toString().equals(id)) throw new IllegalArgumentException("保存操作标识无效"); }
        catch (IllegalArgumentException invalid) { throw new IllegalArgumentException("保存操作标识无效", invalid); }
        return id;
    }
}
