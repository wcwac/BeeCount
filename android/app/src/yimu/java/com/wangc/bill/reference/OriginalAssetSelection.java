package com.wangc.bill.reference;

/**
 * Recovered AddBillActivity.d1/G0/f1 rules. All asset IDs here are original semantic
 * IDs; localRowId is only the experimental accounts foreign key. This does not
 * implement AutoAddLayout.k0's separate text/merchant matching chain.
 */
public final class OriginalAssetSelection {
    public static final long INITIAL_DEFAULT_ASSET = 0;
    public static final long INITIAL_LAST_ASSET = 0;
    public static final long MANUAL_NO_ACCOUNT = 0;
    public static final long AUTOMATIC_NO_ACCOUNT = -1;

    public static final class AssetRef {
        public final long localRowId, semanticId;
        public AssetRef(long localRowId, long semanticId) {
            if (localRowId <= 0) throw new IllegalArgumentException("账户本地主键无效");
            this.localRowId = localRowId; this.semanticId = semanticId;
        }
    }

    /** Existing v2 mappings have a known local FK but no recoverable raw ID. */
    public static final class Mapping {
        public final boolean exists, rawKnown;
        public final long rawSemanticId;
        public final Long localRowId;
        private Mapping(boolean exists, boolean rawKnown, long rawSemanticId, Long localRowId) {
            if (localRowId != null && localRowId <= 0) throw new IllegalArgumentException("账户本地主键无效");
            this.exists=exists; this.rawKnown=rawKnown; this.rawSemanticId=rawSemanticId; this.localRowId=localRowId;
        }
        public static Mapping missing() { return new Mapping(false,false,0,null); }
        public static Mapping unrecovered(Long localRowId) { return new Mapping(true,false,0,localRowId); }
        public static Mapping known(long rawSemanticId,Long localRowId) { return new Mapping(true,true,rawSemanticId,localRowId); }
    }

    public interface Lookup {
        /** Original f.P: current user only, no hide/type/showBook filter. */
        AssetRef unscoped(long rawSemanticId);
        /** Original f.Q: showBook for the application's current book. */
        AssetRef inCurrentBook(long rawSemanticId);
        /** Original f.R: showBook for the editor's selected book. */
        AssetRef inSelectedBook(long rawSemanticId);
        /** Original a0.I2: last internal Bill row with assetId>0 in selected book. */
        Long latestPositiveBillAssetInSelectedBook();
    }

    public final AssetRef selected;
    public final boolean categoryLinkEnabled;
    public OriginalAssetSelection(AssetRef selected, boolean categoryLinkEnabled) {
        this.selected=selected; this.categoryLinkEnabled=categoryLinkEnabled;
    }

    /**
     * d1 initializes only when o==null. A missing positive default does not itself
     * disable G0. The final latest-Bill fallback is gated by lastAssetId>0.
     */
    public OriginalAssetSelection initializeManual(long defaultAsset, long lastAssetId, Lookup lookup) {
        AssetRef account=selected;
        boolean linked=categoryLinkEnabled;
        if (account == null) {
            if (defaultAsset > 0) account=lookup.unscoped(defaultAsset);
            else if (defaultAsset < 0) linked=false;
            if (account == null) account=lookup.inCurrentBook(lastAssetId);
            else linked=false;
        }
        if (account == null && lastAssetId > 0) {
            Long previous=lookup.latestPositiveBillAssetInSelectedBook();
            if (previous != null) account=lookup.unscoped(previous);
        }
        return new OriginalAssetSelection(account,linked);
    }

    /**
     * G0 exact-pair lookup. Missing and explicit -1 both leave o unchanged, but
     * Mapping preserves their distinct persistence states. Raw 0 still calls R.
     */
    public OriginalAssetSelection applyManualCategory(boolean editingBill, boolean modeT,
            Mapping mapping, Lookup lookup) {
        if (editingBill || !categoryLinkEnabled || modeT || !mapping.exists || !mapping.rawKnown
                || mapping.rawSemanticId == -1) return this;
        AssetRef account=lookup.inSelectedBook(mapping.rawSemanticId);
        return account == null ? this : new OriginalAssetSelection(account,categoryLinkEnabled);
    }

    /** f1 disables G0 even when the temporary -1 picker item clears the account. */
    public OriginalAssetSelection manualPickerResult(AssetRef account) {
        return new OriginalAssetSelection(account == null || account.semanticId == -1 ? null : account,false);
    }

    /** e1 tests only showBook. AutoAdd.r0 does not have this clearing rule. */
    public OriginalAssetSelection manualBookChanged(boolean selectedAccountAllowsNewBook) {
        return selected == null || selectedAccountAllowsNewBook ? this
            : new OriginalAssetSelection(null,categoryLinkEnabled);
    }

    /** New manual Bill keeps constructor assetId=0 when no account is selected. */
    public long manualSavedRawId() { return selected == null ? MANUAL_NO_ACCOUNT : selected.semanticId; }
    /** AutoAdd.S -> u1.m explicitly writes -1 when no account is selected. */
    public long automaticSavedRawId() { return selected == null ? AUTOMATIC_NO_ACCOUNT : selected.semanticId; }
}
