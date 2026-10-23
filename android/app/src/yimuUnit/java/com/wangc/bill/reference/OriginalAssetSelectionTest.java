package com.wangc.bill.reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public final class OriginalAssetSelectionTest {
    private static OriginalAssetSelection.AssetRef asset(long row,long semantic) {
        return new OriginalAssetSelection.AssetRef(row,semantic);
    }
    private static final class Lookup implements OriginalAssetSelection.Lookup {
        final Map<Long,OriginalAssetSelection.AssetRef> p=new HashMap<>(),q=new HashMap<>(),r=new HashMap<>();
        final List<String> calls=new ArrayList<>();
        Long latest;
        public OriginalAssetSelection.AssetRef unscoped(long id) { calls.add("P:"+id);return p.get(id); }
        public OriginalAssetSelection.AssetRef inCurrentBook(long id) { calls.add("Q:"+id);return q.get(id); }
        public OriginalAssetSelection.AssetRef inSelectedBook(long id) { calls.add("R:"+id);return r.get(id); }
        public Long latestPositiveBillAssetInSelectedBook() { calls.add("I2");return latest; }
    }

    @Test public void categoryModeStillLooksUpRawZeroAsRecentAndKeepsCategoryLinkEnabled() {
        Lookup lookup=new Lookup();lookup.q.put(0L,asset(40,0));
        OriginalAssetSelection result=new OriginalAssetSelection(null,true).initializeManual(0,0,lookup);
        assertEquals(40,result.selected.localRowId);assertEquals(0,result.selected.semanticId);
        assertTrue(result.categoryLinkEnabled);assertEquals(Arrays.asList("Q:0"),lookup.calls);
    }

    @Test public void recentBillModeDisablesCategoryLinkButDoesNotMeanForcedNoAccount() {
        Lookup lookup=new Lookup();lookup.q.put(-1L,asset(40,-1));
        OriginalAssetSelection result=new OriginalAssetSelection(null,true).initializeManual(-1,-1,lookup);
        assertEquals(40,result.selected.localRowId);assertFalse(result.categoryLinkEnabled);
        assertEquals(Arrays.asList("Q:-1"),lookup.calls);
    }

    @Test public void positiveDefaultUsesUnscopedLookupAndDisablesTheCategoryOverride() {
        Lookup lookup=new Lookup();lookup.p.put(777L,asset(3,777));lookup.q.put(5L,asset(4,5));
        OriginalAssetSelection result=new OriginalAssetSelection(null,true).initializeManual(777,5,lookup);
        assertEquals(777,result.selected.semanticId);assertFalse(result.categoryLinkEnabled);
        assertEquals(Arrays.asList("P:777"),lookup.calls);
    }

    @Test public void missingPositiveDefaultMayUseRecentWithoutDisablingCategoryOverride() {
        Lookup lookup=new Lookup();lookup.q.put(5L,asset(4,5));
        OriginalAssetSelection result=new OriginalAssetSelection(null,true).initializeManual(777,5,lookup);
        assertEquals(5,result.selected.semanticId);assertTrue(result.categoryLinkEnabled);
        assertEquals(Arrays.asList("P:777","Q:5"),lookup.calls);
    }

    @Test public void finalFallbackUsesSelectedBooksLatestBillThenUnscopedAssetLookup() {
        Lookup lookup=new Lookup();lookup.latest=18L;lookup.p.put(18L,asset(6,18));
        OriginalAssetSelection result=new OriginalAssetSelection(null,true).initializeManual(0,9,lookup);
        assertEquals(18,result.selected.semanticId);assertTrue(result.categoryLinkEnabled);
        assertEquals(Arrays.asList("Q:9","I2","P:18"),lookup.calls);
    }

    @Test public void nonpositiveRecentIdDoesNotInventAFirstVisibleOrLatestBillDefault() {
        Lookup lookup=new Lookup();lookup.latest=18L;lookup.p.put(18L,asset(6,18));
        OriginalAssetSelection result=new OriginalAssetSelection(null,true).initializeManual(0,0,lookup);
        assertNull(result.selected);assertEquals(Arrays.asList("Q:0"),lookup.calls);
        assertEquals(0,result.manualSavedRawId());assertEquals(-1,result.automaticSavedRawId());
    }

    @Test public void existingAccountAndItsLinkFlagSurviveInitializationWithoutAnyLookups() {
        Lookup lookup=new Lookup();OriginalAssetSelection.AssetRef original=asset(50,700);
        OriginalAssetSelection result=new OriginalAssetSelection(original,false).initializeManual(999,888,lookup);
        assertSame(original,result.selected);assertFalse(result.categoryLinkEnabled);assertTrue(lookup.calls.isEmpty());
    }

    @Test public void missingAndExplicitMinusOneCategoryMappingsBothKeepCurrentButRemainDistinctData() {
        Lookup lookup=new Lookup();OriginalAssetSelection selected=new OriginalAssetSelection(asset(40,55),true);
        OriginalAssetSelection.Mapping missing=OriginalAssetSelection.Mapping.missing();
        OriginalAssetSelection.Mapping explicit=OriginalAssetSelection.Mapping.known(-1,null);
        assertFalse(missing.exists);assertTrue(explicit.exists);assertTrue(explicit.rawKnown);
        assertSame(selected,selected.applyManualCategory(false,false,missing,lookup));
        assertSame(selected,selected.applyManualCategory(false,false,explicit,lookup));assertTrue(lookup.calls.isEmpty());
    }

    @Test public void exactCategoryMappingRawZeroUsesSelectedBookLookupAndCanSelectAnActualZeroAsset() {
        Lookup lookup=new Lookup();lookup.r.put(0L,asset(82,0));
        OriginalAssetSelection result=new OriginalAssetSelection(asset(40,55),true)
            .applyManualCategory(false,false,OriginalAssetSelection.Mapping.known(0,null),lookup);
        assertEquals(82,result.selected.localRowId);assertEquals(0,result.selected.semanticId);
        assertEquals(Arrays.asList("R:0"),lookup.calls);
    }

    @Test public void absentOrBookExcludedMappingTargetDoesNotClearThePreviouslySelectedAccount() {
        Lookup lookup=new Lookup();OriginalAssetSelection selected=new OriginalAssetSelection(asset(40,55),true);
        assertSame(selected,selected.applyManualCategory(false,false,OriginalAssetSelection.Mapping.known(99,null),lookup));
        assertEquals(Arrays.asList("R:99"),lookup.calls);
    }

    @Test public void editingManualOverrideAndModeTDoNotApplyTheCategoryAccountMapping() {
        Lookup lookup=new Lookup();lookup.r.put(11L,asset(2,11));
        OriginalAssetSelection.Mapping mapping=OriginalAssetSelection.Mapping.known(11,2L);
        OriginalAssetSelection linked=new OriginalAssetSelection(asset(40,55),true);
        assertSame(linked,linked.applyManualCategory(true,false,mapping,lookup));
        assertSame(linked,linked.applyManualCategory(false,true,mapping,lookup));
        OriginalAssetSelection overridden=new OriginalAssetSelection(linked.selected,false);
        assertSame(overridden,overridden.applyManualCategory(false,false,mapping,lookup));assertTrue(lookup.calls.isEmpty());
    }

    @Test public void legacyMappingRetainsPresenceAndLocalFkWithoutPretendingItsRawIdIsKnown() {
        Lookup lookup=new Lookup();OriginalAssetSelection selected=new OriginalAssetSelection(asset(40,55),true);
        OriginalAssetSelection.Mapping mapping=OriginalAssetSelection.Mapping.unrecovered(8L);
        assertTrue(mapping.exists);assertFalse(mapping.rawKnown);assertEquals(Long.valueOf(8),mapping.localRowId);
        assertSame(selected,selected.applyManualCategory(false,false,mapping,lookup));assertTrue(lookup.calls.isEmpty());
    }

    @Test public void choosingNoAccountDisablesLaterCategoryOverridesAndPreservesDifferentSaveSentinels() {
        OriginalAssetSelection result=new OriginalAssetSelection(asset(40,55),true).manualPickerResult(null);
        assertNull(result.selected);assertFalse(result.categoryLinkEnabled);
        assertEquals(0,result.manualSavedRawId());assertEquals(-1,result.automaticSavedRawId());
        assertNull(new OriginalAssetSelection(asset(40,55),true).manualPickerResult(asset(99,-1)).selected);
    }

    @Test public void manualBookChangeClearsOnlyAnExcludedSelectionAndDoesNotChangeItsOverrideFlag() {
        OriginalAssetSelection selected=new OriginalAssetSelection(asset(40,55),false);
        assertSame(selected,selected.manualBookChanged(true));
        OriginalAssetSelection cleared=selected.manualBookChanged(false);
        assertNull(cleared.selected);assertFalse(cleared.categoryLinkEnabled);
        OriginalAssetSelection original=new OriginalAssetSelection(asset(81,902),true);
        assertEquals(902,original.manualSavedRawId());assertEquals(902,original.automaticSavedRawId());
    }
}
