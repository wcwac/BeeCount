package com.wangc.bill.reference;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public final class CsvCodecTest {
    @Test public void quotesAndMultilineRoundTrip(){List<List<String>> rows=Arrays.asList(Arrays.asList("金额","备注","空"),Arrays.asList("12.30","咖啡,\"两杯\"\n第二行",""));assertEquals(rows,CsvCodec.parse(CsvCodec.write(rows)));}
    @Test public void acceptsCrLfAndUtf8Bom(){assertEquals("备注",CsvCodec.parse("\uFEFF金额,备注\r\n12.30,午饭\r\n").get(0).get(1));}
    @Test public void invalidQuotesFailBeforeImport(){MoneyTest.reject(()->CsvCodec.parse("a,\"broken"));MoneyTest.reject(()->CsvCodec.parse("a,\"closed\"evil"));}
}
