package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.Collections;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real owner/semantic book records and ordered showBook rows; no name or PK inference. */
@RunWith(AndroidJUnit4.class)
public final class OriginalAssetBookResolverDeviceTest {
    private static final String DATABASE="reference_original_asset_book_resolver_test.sqlite";
    private Context context; private ReferenceStore store; private SharedPreferences preferences;
    private boolean hadBook; private long oldBook;
    @Before public void open(){context=ApplicationProvider.getApplicationContext();context.deleteDatabase(DATABASE);store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();preferences=context.getSharedPreferences("reference_settings",Context.MODE_PRIVATE);hadBook=preferences.contains("current_book");oldBook=preferences.getLong("current_book",1);}
    @After public void close(){if(store!=null)store.close();if(preferences!=null){SharedPreferences.Editor edit=preferences.edit();if(hadBook)edit.putLong("current_book",oldBook);else edit.remove("current_book");edit.commit();}if(context!=null)context.deleteDatabase(DATABASE);}
    private long book(int owner,long semantic,int type,boolean hidden,boolean archived,String shares){ContentValues v=new ContentValues();v.put("name","合成手续费账本 "+owner+":"+semantic);v.put("created",1);v.put("book_origin",BookRepository.RECOVERED_LOCAL);v.put("owner_semantic_id",owner);v.put("semantic_id",semantic);v.put("book_type",type);v.put("hide",hidden?1:0);v.put("archived",archived?1:0);v.put("share_users",shares);return store.getWritableDatabase().insertOrThrow("books",null,v);}
    private long asset(int owner,long semantic,long legacy,Long... show){AssetRepository.Definition d=new AssetRepository.Definition("合成转出资产",1,null);d.legacyBookSemanticId=legacy;d.showBook.addAll(Arrays.asList(show));return store.assetRepository().insertRecovered(owner,semantic,1,0,d);}
    private void select(long row){assertTrue(preferences.edit().putLong("current_book",row).commit());}
    private void identity(OriginalAssetBookResolver.Resolution actual,long row,int owner,long semantic,OriginalAssetBookResolver.Route route){assertEquals(row,actual.rowId);assertEquals(owner,actual.ownerSemanticId);assertEquals(semantic,actual.semanticId);assertEquals(route,actual.route);}
    private void missing(Runnable action){try{action.run();fail("A missing original book must not invent an identity");}catch(IllegalStateException expected){assertFalse(expected.getMessage().isEmpty());}}

    @Test public void orderedAllowlistSkipsMissingOrForeignIdentitiesAndKeepsHiddenArchivedBooks(){book(8,9001,2,false,false,"[]");long first=book(7,9002,2,true,true,"[]"),later=book(7,9003,1,false,false,"[]");asset(7,101,9003,9001L,999999L,9002L,9003L);select(later);OriginalAssetBookResolver resolver=new OriginalAssetBookResolver(store,7);identity(resolver.resolveIdentity(101),first,7,9002,OriginalAssetBookResolver.Route.SHOW_BOOK);assertEquals(9002,resolver.resolve(101));assertNotEquals(first,resolver.resolve(101));}

    @Test public void emptyAllowlistUsesTheGlobalLocalRowButReturnsItsSemanticId(){long system=book(7,701,1,false,false,"[]"),selected=book(7,702,2,false,false,"[]");asset(7,101,701);select(selected);identity(new OriginalAssetBookResolver(store,7).resolveIdentity(101),selected,7,702,OriginalAssetBookResolver.Route.CURRENT_BOOK);assertNotEquals(system,selected);assertNotEquals(selected,702);assertEquals(selected,preferences.getLong("current_book",-1));}

    @Test public void sharedForeignCurrentBookFallsBackToTheOwnersTypeOneBookRatherThanSemanticOne(){long own=book(7,745,1,true,true,"[]"),shared=book(8,801,2,false,false,"[7]");asset(7,101,801);select(shared);identity(new OriginalAssetBookResolver(store,7).resolveIdentity(101),own,7,745,OriginalAssetBookResolver.Route.OWN_SYSTEM_BOOK);assertEquals(shared,preferences.getLong("current_book",-1));}

    @Test public void missingOrUnsharedGlobalRowsUseTheApplicationCurrentBookFallback(){long own=book(7,745,1,false,false,"[]"),foreign=book(8,801,2,false,false,"[]");asset(7,101,801);OriginalAssetBookResolver resolver=new OriginalAssetBookResolver(store,7);select(foreign);identity(resolver.resolveIdentity(101),own,7,745,OriginalAssetBookResolver.Route.CURRENT_BOOK);select(999999);identity(resolver.resolveIdentity(101),own,7,745,OriginalAssetBookResolver.Route.CURRENT_BOOK);assertEquals(999999,preferences.getLong("current_book",-1));}

    @Test public void validShowBookReturnsBeforeAnAbsentCurrentOrSystemBookIsConsulted(){long chosen=book(7,765,2,false,false,"[]");asset(7,101,999,765L);select(999999);long before=store.scalar("SELECT COUNT(*) n FROM books");identity(new OriginalAssetBookResolver(store,7).resolveIdentity(101),chosen,7,765,OriginalAssetBookResolver.Route.SHOW_BOOK);assertNull(store.bookRepository().systemBook(7));assertEquals(before,store.scalar("SELECT COUNT(*) n FROM books"));assertEquals(999999,preferences.getLong("current_book",-1));}

    @Test public void absentOwnSystemBookFailsWithoutCreatingOrReassigningAnyBook(){long foreign=book(8,801,2,false,false,"[7]");asset(7,101,1);select(foreign);long before=store.scalar("SELECT COUNT(*) n FROM books");OriginalAssetBookResolver resolver=new OriginalAssetBookResolver(store,7);missing(()->resolver.resolve(101));select(999999);missing(()->resolver.resolve(101));assertNull(store.bookRepository().byIdentity(7,1));assertEquals(before,store.scalar("SELECT COUNT(*) n FROM books"));assertEquals(999999,preferences.getLong("current_book",-1));}

    @Test public void theSameResolverReflectsCurrentSelectionAndOrderedShowBookChanges(){long a=book(7,701,1,false,false,"[]"),b=book(7,702,2,false,false,"[]");long row=asset(7,101,999,701L,702L);OriginalAssetBookResolver resolver=new OriginalAssetBookResolver(store,7);select(b);assertEquals(701,resolver.resolve(101));store.assetRepository().setShowBook(row,Arrays.asList(702L,701L));assertEquals(702,resolver.resolve(101));store.assetRepository().setShowBook(row,Collections.emptyList());select(a);assertEquals(701,resolver.resolve(101));select(b);assertEquals(702,resolver.resolve(101));assertEquals(999,store.assetRepository().byRowId(row).optLong("legacy_book_semantic_id"));}

    @Test public void zeroSemanticIdentityIsValidButAnAssetLocalRowOrOtherOwnerIsNotAnIdentity(){long zero=book(7,0,2,false,false,"[]");long local=asset(7,0,999,0L);asset(8,818,0);select(999999);OriginalAssetBookResolver resolver=new OriginalAssetBookResolver(store,7);identity(resolver.resolveIdentity(0),zero,7,0,OriginalAssetBookResolver.Route.SHOW_BOOK);try{resolver.resolve(local);fail("A local asset PK was interpreted as its semantic ID");}catch(IllegalArgumentException expected){}try{resolver.resolve(818);fail("Another owner supplied the source asset");}catch(IllegalArgumentException expected){}long own=book(7,745,1,false,false,"[]");select(own);identity(resolver.resolveShowBook(null),own,7,745,OriginalAssetBookResolver.Route.CURRENT_BOOK);}
}
