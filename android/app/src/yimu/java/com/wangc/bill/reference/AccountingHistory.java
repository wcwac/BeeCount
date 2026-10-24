package com.wangc.bill.reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/** Keyset paging over owned Bill/Transfer/Lend identities; only one page is hydrated. */
public final class AccountingHistory {
    public static final int PAGE_SIZE = 100;
    public enum Family { ALL, LOANS, BORROWS, REIMBURSEMENT }
    public static final class Cursor {
        public final long time, id;
        public final String kind;
        private Cursor(long time, String kind, long id) { this.time=time; this.kind=kind; this.id=id; }
    }
    public static final class Page {
        public final List<JSONObject> rows;
        public final Cursor next;
        public final boolean more;
        private Page(List<JSONObject> rows, Cursor next, boolean more) { this.rows=rows; this.next=next; this.more=more; }
    }
    public static final class Filter {
        public long book, start=0, end=Long.MAX_VALUE, account, parentLoan;
        public String search="";
        public boolean trash, attachments;
        public Family family=Family.ALL;
        public Filter copy() {
            Filter result=new Filter();result.book=book;result.start=start;result.end=end;result.account=account;result.parentLoan=parentLoan;
            result.search=search;result.trash=trash;result.attachments=attachments;result.family=family;return result;
        }
    }
    private AccountingHistory() {}

    /* Priority follows accountingEntry: financial loan, manual typed, then saved candidate.
       An immutable receipt is never treated as a raw model's current book/account ID. */
    private static final String OWNERS =
            "WITH owners AS ("
            +"SELECT 'lend' kind,entity_row_id id,book_id,0 priority,parent_lend_row_id parent_loan FROM accounting_financial_operations WHERE entity_kind='lend' AND action IN('loan','borrow','collection','repayment')"
            +" UNION ALL SELECT entry_kind,entry_id,book_id,1,0 FROM accounting_operations"
            +" UNION ALL SELECT 'transfer',r.transfer_row_id,c.book_id,2,0 FROM candidate_transfer_receipts r JOIN candidates c ON c.id=r.candidate_id WHERE c.state='SAVED' AND c.save_kind='transfer'"
            +" UNION ALL SELECT 'lend',r.lend_row_id,c.book_id,2,0 FROM candidate_lend_receipts r JOIN candidates c ON c.id=r.candidate_id WHERE c.state='SAVED' AND c.save_kind='lend'),"
            +" owned AS (SELECT o.* FROM owners o WHERE NOT EXISTS(SELECT 1 FROM owners p WHERE p.kind=o.kind AND p.id=o.id AND p.priority<o.priority)),"
            +" refs AS ("
            +"SELECT 'bill' entry_kind,t.id entry_id,t.book_id,t.time,t.deleted,COALESCE(t.remark,'')||' '||COALESCE(t.merchant,'')||' '||COALESCE(c.name,'') search_text,CASE WHEN length(trim(COALESCE(t.attachments,'')))>2 THEN 1 ELSE 0 END has_files,3 source_priority,t.account_id,t.to_account_id,0 asset_owner,-1 from_asset,-1 to_asset,0 typed,t.financial_type,0 lend_type,t.bill_reimbursement reimbursement,0 parent_loan FROM transactions t LEFT JOIN categories c ON c.id=t.category_id"
            +" UNION ALL SELECT 'transfer',t.id,o.book_id,t.time,t.original_delete_flag,COALESCE(t.remark,'')||' 转账 original_transfer',1,o.priority,0,0,t.owner_semantic_id,t.from_asset_semantic_id,t.to_asset_semantic_id,1,'original_transfer',0,0,0 FROM original_transfers t JOIN owned o ON o.kind='transfer' AND o.id=t.id"
            +" UNION ALL SELECT 'lend',l.id,o.book_id,l.out_time,l.original_delete_flag,COALESCE(l.remark,'')||' '||CASE WHEN o.priority=0 THEN CASE l.type WHEN 1 THEN '借出 lend' WHEN 2 THEN '收回借款 lend' WHEN 3 THEN '借入 borrow' ELSE '归还借款 borrow' END ELSE '信用还款 original_lend' END,1,o.priority,0,0,l.owner_semantic_id,l.repayment_asset_semantic_id,l.asset_semantic_id,1,CASE WHEN o.priority=0 THEN CASE WHEN l.type IN(1,2) THEN 'lend' ELSE 'borrow' END ELSE 'original_lend' END,l.type,0,COALESCE(o.parent_loan,0) FROM original_lends l JOIN owned o ON o.kind='lend' AND o.id=l.id) ";

    public static Page page(ReferenceStore store, Filter requested, Cursor after) {
        if(store==null||requested==null)throw new IllegalArgumentException("历史查询条件不能为空");
        Filter filter=requested.copy();
        if(filter.book<0||filter.account<0||filter.parentLoan<0||filter.end<=filter.start||filter.family==null)throw new IllegalArgumentException("历史查询范围无效");
        synchronized(store) {
            StringBuilder where=new StringBuilder(" WHERE deleted=? AND time>=? AND time<?");
            List<String> args=new ArrayList<>();args.add(filter.trash?"1":"0");args.add(""+filter.start);args.add(""+filter.end);
            if(filter.book>0){where.append(" AND book_id=?");args.add(""+filter.book);}
            if(filter.account>0){
                where.append(" AND ((typed=0 AND (account_id=? OR to_account_id=?)) OR (typed=1 AND EXISTS(SELECT 1 FROM accounts a WHERE a.id=? AND a.asset_origin='original' AND a.owner_semantic_id=asset_owner AND (a.semantic_id=from_asset OR a.semantic_id=to_asset))))");
                args.add(""+filter.account);args.add(""+filter.account);args.add(""+filter.account);
            }
            if(filter.parentLoan>0){where.append(" AND entry_kind='lend' AND parent_loan=?");args.add(""+filter.parentLoan);}
            switch(filter.family){
                case LOANS -> where.append(" AND ((entry_kind='lend' AND financial_type IN('lend','borrow') AND lend_type IN(1,3)) OR (entry_kind='bill' AND financial_type IN('lend','borrow')))");
                case BORROWS -> where.append(" AND ((entry_kind='lend' AND financial_type='borrow' AND lend_type=3) OR (entry_kind='bill' AND financial_type='borrow'))");
                case REIMBURSEMENT -> where.append(" AND entry_kind='bill' AND (financial_type='reimbursement' OR reimbursement<>0)");
                default -> { }
            }
            String query=filter.search==null?"":filter.search.trim().toLowerCase(Locale.ROOT);
            if(!query.isEmpty()){
                // Typed merchant/counterparty lives in opaque metadata. Filter it
                // after hydration, never require SQLite JSON1 or search JSON keys.
                where.append(" AND (typed=1 OR search_text LIKE ? ESCAPE '\\')");
                String like="%"+query.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";args.add(like);
            }
            if(filter.attachments)where.append(" AND has_files=1");
            if(after!=null){
                if(after.id<=0||!(after.kind.equals("bill")||after.kind.equals("lend")||after.kind.equals("transfer")))throw new IllegalArgumentException("历史分页位置无效");
                where.append(" AND (time<? OR (time=? AND entry_kind>?) OR (time=? AND entry_kind=? AND entry_id<?))");
                args.add(""+after.time);args.add(""+after.time);args.add(after.kind);args.add(""+after.time);args.add(after.kind);args.add(""+after.id);
            }
            List<JSONObject> keys=store.query(OWNERS+"SELECT entry_kind,entry_id,time,source_priority FROM refs"+where+" ORDER BY time DESC,entry_kind,entry_id DESC LIMIT "+(PAGE_SIZE+1),args.toArray(new String[0]));
            int available=Math.min(PAGE_SIZE,keys.size()),processed=0;
            List<JSONObject> rows=new ArrayList<>();Cursor cursor=null;
            for(int i=0;i<available;i++){
                JSONObject key=keys.get(i);String kind=key.optString("entry_kind");long id=key.optLong("entry_id");
                cursor=new Cursor(key.optLong("time"),kind,id);processed++;
                // Only the current page's opaque payloads are parsed. Empty typed
                // histories can yield a continuation page, never a false final empty list.
                if(filter.attachments&&!hasAttachments(store,key))continue;
                JSONObject row;
                if(kind.equals("bill")){
                    row=store.transaction(id);if(row==null)throw new IllegalStateException("历史账单在读取时已变化");
                    Json.put(row,"entry_kind","bill");Json.put(row,"entry_id",id);Json.put(row,"entry_key","bill:"+id);
                } else row=store.accountingEntry(kind,id);
                if(!matches(row,filter,query))continue;
                rows.add(row);
                if(filter.attachments&&rows.size()==25)break;
            }
            boolean more=processed<keys.size();
            if(!filter.attachments)decorateDailyTotals(store,rows,filter);
            return new Page(rows,cursor,more);
        }
    }

    private static boolean matches(JSONObject row,Filter filter,String query){
        if(filter.book>0&&row.optLong("book_id")!=filter.book||row.optLong("time")<filter.start||row.optLong("time")>=filter.end
                ||(row.optInt("deleted")!=0)!=filter.trash)throw new IllegalStateException("历史收据与实际账单范围不一致，请刷新后重试");
        if(filter.account>0&&row.optLong("account_id")!=filter.account&&row.optLong("to_account_id")!=filter.account)
            throw new IllegalStateException("历史账户身份与实际记录不一致");
        if(!query.isEmpty()){
            String text=row.optString("remark")+" "+row.optString("merchant")+" "+row.optString("counterparty")+" "+row.optString("category",row.optString("category_name"))+" "+row.optString("financial_type");
            if(!text.toLowerCase(Locale.ROOT).contains(query))return false;
        }
        if(filter.attachments&&Json.array(row.optString("attachments","[]")).length()==0)return false;
        return true;
    }

    private static boolean hasAttachments(ReferenceStore store,JSONObject key){
        long id=key.optLong("entry_id");String kind=key.optString("entry_kind");int priority=key.optInt("source_priority");
        JSONObject value;
        if(kind.equals("bill")){value=store.one("SELECT attachments FROM transactions WHERE id=?",""+id);}
        else {
            JSONObject metadata;
            if(priority==0)metadata=store.one("SELECT payload FROM accounting_financial_operations WHERE entity_kind='lend' AND entity_row_id=? AND action IN('loan','borrow','collection','repayment')",""+id);
            else if(priority==1)metadata=store.one("SELECT payload FROM accounting_operations WHERE entry_kind=? AND entry_id=?",kind,""+id);
            else metadata=store.one("SELECT c.payload FROM candidates c JOIN candidate_"+kind+"_receipts r ON r.candidate_id=c.id WHERE r."+kind+"_row_id=?",""+id);
            if(metadata==null)throw new IllegalStateException("附件保存收据已经变化");
            value=Json.parse(metadata.optString("payload"));
        }
        return value!=null&&Json.array(value.optString("attachments","[]")).length()>0;
    }

    /** Page boundaries must not turn a partial day into a false daily subtotal. */
    private static void decorateDailyTotals(ReferenceStore store,List<JSONObject> rows,Filter filter){
        java.util.Map<String,JSONObject> cached=new java.util.HashMap<>();
        for(JSONObject row:rows){
            long time=row.optLong("time");java.util.Calendar first=java.util.Calendar.getInstance();first.setTimeInMillis(time);
            first.set(java.util.Calendar.HOUR_OF_DAY,0);first.set(java.util.Calendar.MINUTE,0);first.set(java.util.Calendar.SECOND,0);first.set(java.util.Calendar.MILLISECOND,0);
            long start=Math.max(filter.start,first.getTimeInMillis());first.add(java.util.Calendar.DATE,1);long end=Math.min(filter.end,first.getTimeInMillis());String key=row.optString("currency")+":"+start;
            JSONObject totals=cached.get(key);
            if(totals==null){
                StringBuilder sql=new StringBuilder("SELECT COALESCE(SUM(CASE WHEN t.kind='income' AND t.not_total=0 THEN t.amount ELSE 0 END),0) income,COALESCE(SUM(CASE WHEN t.not_total=0 THEN CASE WHEN t.kind='expense' THEN t.amount+t.fee ELSE t.fee END ELSE 0 END),0) pay FROM transactions t LEFT JOIN categories c ON c.id=t.category_id WHERE t.deleted=? AND t.time>=? AND t.time<? AND t.currency=?");
                List<String> args=new ArrayList<>();args.add(filter.trash?"1":"0");args.add(""+start);args.add(""+end);args.add(row.optString("currency","CNY"));
                if(filter.book>0){sql.append(" AND t.book_id=?");args.add(""+filter.book);}
                if(filter.account>0){sql.append(" AND (t.account_id=? OR t.to_account_id=?)");args.add(""+filter.account);args.add(""+filter.account);}
                if(filter.parentLoan>0)sql.append(" AND 0");
                if(filter.family==Family.LOANS)sql.append(" AND t.financial_type IN('lend','borrow')");
                else if(filter.family==Family.BORROWS)sql.append(" AND t.financial_type='borrow'");
                else if(filter.family==Family.REIMBURSEMENT)sql.append(" AND (t.financial_type='reimbursement' OR t.bill_reimbursement<>0)");
                String query=filter.search==null?"":filter.search.trim();
                if(!query.isEmpty()){
                    String like="%"+query.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";
                    sql.append(" AND (t.remark LIKE ? ESCAPE '\\' OR t.merchant LIKE ? ESCAPE '\\' OR c.name LIKE ? ESCAPE '\\')");args.add(like);args.add(like);args.add(like);
                }
                totals=store.one(sql.toString(),args.toArray(new String[0]));cached.put(key,totals);
            }
            Json.put(row,"history_day_income",totals.optLong("income"));Json.put(row,"history_day_pay",totals.optLong("pay"));
        }
    }

    /** Export/relationships may deliberately need every row. Never use this on the home page. */
    public static List<JSONObject> all(ReferenceStore store,Filter filter){
        List<JSONObject> result=new ArrayList<>();Cursor cursor=null;
        do {Page page=page(store,filter,cursor);result.addAll(page.rows);if(!page.more)break;cursor=page.next;}while(cursor!=null);
        return result;
    }
}
