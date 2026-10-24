package com.wangc.bill.reference.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;
import static org.junit.Assert.*;

/**
 * Expected strings below are fixed from the original APK's ARSC-mapped binary
 * XML raw AND typed string values, not the repaired project's source XML.
 * Provenance: original APK ARSC-mapped binary XML raw and typed string pools.
 * APK SHA-256: 4756d95730ce5e1fdd31f2f06d9636d29024e2f0a2a71b4f91c6c4405ca3e303.
 * The evidence displays LF as an escaped representation; Java \n below means
 * one actual U+000A. Do not generate these expectations from current res/layout.
 *
 * Only compiled resource and isolated TextView/EditText literal semantics are
 * checked. No original APK execution, feature-entry completeness, remote action,
 * page inflation, original pixels, or original app state is claimed.
 */
@RunWith(AndroidJUnit4.class)
public final class OriginalResourceLiteralDeviceTest {
    private static final String ANDROID = "http://schemas.android.com/apk/res/android";
    private static final String FUND_START = "提示：\n盈亏偏差：部分平台会将分红或手续费计入盈亏，设置该数值使累计盈亏与三方平台相同\n初次创建理财账户不会生成交易记录，如要生成，可将持仓份额设为0，再添加买入卖出记录";
    private static final String FUND_FOLLOW = "提示：\n打开“生成收益账单”后，只需编辑当前基金最后一条自动生成的收益账单，后续账单的分类、账本、标签、是否计入收支和预算，将自动沿用";
    private static final String EXPORT_WARNING = "提示：\n如要备份或转移数据，请使用云同步或数据备份功能，请勿通过账单导出备份数据";
    private static final String EXPORT_FILES = "Excel文件仅支持上传云端的附件导出，且有效期为12小时\nZip文件支持将账单和附件打包导出";

    private static final class Expected {
        final String layout, tag, attribute, value;
        final int ordinal, lineFeeds;
        Expected(String layout, int ordinal, String tag, String attribute, String value, int lineFeeds) {
            this.layout = layout; this.ordinal = ordinal; this.tag = tag;
            this.attribute = attribute; this.value = value; this.lineFeeds = lineFeeds;
        }
        String key() { return layout + ":" + ordinal + ":" + attribute; }
    }

    // Ordinals count every START_TAG from root=0, exactly as the original binary
    // XML reader did. Attribute identity remains checked independently of text.
    private static final Expected[] ORIGINAL = {
        new Expected("activity_add_fund", 45, "TextView", "text", FUND_START, 2),
        new Expected("activity_add_fund_info_total", 47, "TextView", "text", FUND_FOLLOW, 1),
        new Expected("activity_add_monetary_fund", 45, "TextView", "text", FUND_FOLLOW, 1),
        new Expected("activity_add_stock_info", 44, "TextView", "text", FUND_FOLLOW, 1),
        new Expected("activity_asset_fixed_deposit", 39, "TextView", "text", "提示：\n定存不会影响当前账户金额，到期后将自动生成一笔收益账单\n如要计入总资产，可手动将定存金额加入账户余额中", 2),
        new Expected("activity_asset_income_bind", 22, "TextView", "text", "提示：\n编辑当前账户最后一条自动生成的收益账单，后续账单的分类、账本、标签、是否计入收支和预算，将自动沿用\n点击「余额宝」，在页面左上角查看绑定的货币型基金\n点击「零钱通」，在「七日年化收益率」下方查看绑定的货币型基金", 3),
        new Expected("activity_backup_guide", 3, "TextView", "text", "我们建议您至少开启「云备份」或「WebDAV备份」其中一项，以防止恢复出厂设置、手机损坏遗失而导致数据丢失。\n部分「换机助手」迁移数据时，不支持「金融类」数据的转移，如果您只开启了「本地备份」，请勿轻易将手机恢复出厂设置。", 1),
        new Expected("activity_export_bill", 24, "TextView", "text", EXPORT_WARNING, 1),
        new Expected("activity_export_bill", 25, "TextView", "text", EXPORT_FILES, 1),
        new Expected("activity_export_choice_bill", 16, "TextView", "text", EXPORT_WARNING, 1),
        new Expected("activity_export_choice_bill", 17, "TextView", "text", EXPORT_FILES, 1),
        new Expected("activity_export_lend", 28, "TextView", "text", EXPORT_FILES, 1),
        new Expected("activity_export_transfer", 28, "TextView", "text", EXPORT_FILES, 1),
        new Expected("activity_report", 4, "TextView", "text", "邮件发送至：553847323@qq.com\n　邮件标题：【版权投诉】+主题名称+用户名称\n　邮件正文：投诉主题名称\n　　　　　　投诉用户名称\n　　　　　　主体信息\n　　　　　　简单说明\n　　　附件：相关授权文书、版权证明等。", 6),
        new Expected("activity_speech", 8, "TextView", "text", "“支付宝打车花了15元”，“昨天晚饭20元”\n“支付宝转入微信钱包15元”", 1),
        new Expected("activity_template", 12, "TextView", "text", "提示：\n1.日期，收支类型，金额列必填\n2.收支类型仅支持“支出”和“收入”\n3.复制金额和日期数据后，建议统一格式化一下金额和日期列数据\n4.多个标签请用空格隔开\n5.建议使用Office编辑文档", 5),
        // Preserve the source's missing final quote in this first hint.
        new Expected("activity_transparent", 23, "EditText", "hint", "试试“工资卡收入100元”、“昨天打车20元”\n“支付宝转到微信15元”、“长按可再记", 1),
        new Expected("dialog_add_bill", 12, "EditText", "hint", "试试“工资卡收入10元”、“昨天打车20元”\n“支付宝转到微信15元”、“长按可再记”", 1),
        new Expected("dialog_cancellation", 3, "TextView", "text", "该操作不是退出登录，将永久删除当前账户且无法使用。相关的服务器数据和本地数据将被清除，无法找回。\n如您确定注销，请输入以下确认码：", 1),
        new Expected("dialog_data_sync_all", 2, "TextView", "text", "必须仔细阅读：\n此操作会清除云备份并重新上传本地数据，如果当前本地没有数据或数据有误，千万不要执行此操作", 1),
        new Expected("dialog_data_sync_all", 3, "TextView", "text", "本地账单数量：0\n云备份账单数量：0", 1),
        new Expected("dialog_data_sync_download", 3, "TextView", "text", "本地账单数量：0\n云备份账单数量：0", 1),
        new Expected("dialog_samsung_tip", 2, "TextView", "text", "三星系统更新至最新版本后，会默认开启“高耗电限制”选项，您需要手动关闭后，再重新配置自动记账：\n电池-后台控制-右上角更多设置-关闭高耗电限制", 1),
        new Expected("dialog_screenshot_tip", 5, "TextView", "text", "1.开启自动记账后，截图识别才能生效；\n2.该功能通过第三方AI进行识别，图片数据我们不做任何备份存储；\n3.当前会员每日可识别50次，非会员不支持试用，会根据成本动态调整", 2),
        new Expected("dialog_theme_share_type", 9, "TextView", "text", "分享后我们将进行审核，以下主题可能会被拒绝哦：\n1.含有版权且需要授权的ip形象或图片\n2.涉及暴力、色情、政治等元素的主题\n3.风格杂乱，不符合我们审美标准的主题", 3),
        new Expected("fragment_my", 9, "TextView", "text", "坚持记账的第21天\n已记录1875条账单", 1),
        new Expected("layout_guide_home_one", 1, "TextView", "text", "点击进行智能记账\n长按进行语音记账", 1)
    };

    @Before public void cloudOnly() {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
    }

    @Test public void allTwentySevenCompiledLiteralAttributesMatchTheOriginalRawAndTypedLineFeeds() throws Exception {
        assertEquals(27, ORIGINAL.length);
        Set<String> keys = new HashSet<>(), layouts = new HashSet<>();
        int[] observedAttributes = {0}, observedLineFeeds = {0};
        for (Expected expected : ORIGINAL) {
            assertTrue("Duplicate fixed source expectation " + expected.key(), keys.add(expected.key()));
            layouts.add(expected.layout);
            withAttribute(expected, parser -> {
                CharSequence actual = compiledText(parser, expected);
                assertLiteral(expected, actual);
                observedAttributes[0]++;
                observedLineFeeds[0] += lineFeeds(actual);
            });
        }
        assertEquals(24, layouts.size());
        assertEquals(27, observedAttributes[0]);
        assertEquals(43, observedLineFeeds[0]);
    }

    @Test public void isolatedRealTextViewsAndEditTextHintsKeepNewlinesAndFollowingDigits() {
        // Isolated widgets consume actual compiled AttributeSets. No complex
        // original page, constructor, or business initialization is executed.
        for (String key : new String[]{"dialog_samsung_tip:2:text", "activity_template:12:text",
                "activity_transparent:23:hint", "dialog_add_bill:12:hint"}) {
            Expected expected = find(key);
            AtomicReference<TextView> actualView = new AtomicReference<>();
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                try {
                    withAttribute(expected, parser -> {
                        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
                        TextView view = expected.tag.equals("EditText") ? new EditText(context, parser) : new TextView(context, parser);
                        actualView.set(view);
                        assertLiteral(expected, expected.attribute.equals("hint") ? view.getHint() : view.getText());
                    });
                } catch (Exception failure) { throw new AssertionError("Failed compiled widget literal " + expected.key(), failure); }
            });
            assertNotNull(actualView.get());
        }
    }

    private interface LiteralCheck { void check(XmlResourceParser parser) throws Exception; }
    private static void withAttribute(Expected expected, LiteralCheck check) throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Resources resources = context.getResources();
        int id = resources.getIdentifier(expected.layout, "layout", context.getPackageName());
        assertNotEquals("Missing compiled layout " + expected.layout, 0, id);
        XmlResourceParser parser = resources.getLayout(id);
        try {
            int ordinal = -1;
            for (int event = parser.next(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
                if (event != XmlPullParser.START_TAG) continue;
                if (++ordinal != expected.ordinal) continue;
                assertEquals("Original element identity " + expected.key(), expected.tag, parser.getName());
                assertNotNull("Missing literal attribute " + expected.key(), parser.getAttributeValue(ANDROID, expected.attribute));
                check.check(parser);
                return;
            }
            fail("Missing original element ordinal " + expected.key());
        } finally { parser.close(); }
    }
    private static CharSequence compiledText(XmlResourceParser parser, Expected expected) {
        Resources resources = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources();
        int attribute = expected.attribute.equals("hint") ? android.R.attr.hint : android.R.attr.text;
        TypedArray values = resources.obtainAttributes(parser, new int[]{attribute});
        try { return values.getText(0); } finally { values.recycle(); }
    }
    private static void assertLiteral(Expected expected, CharSequence actual) {
        assertNotNull("No compiled text " + expected.key(), actual);
        assertEquals("Compiled literal must preserve source characters " + expected.key(), expected.value, actual.toString());
        assertEquals("U+000A count " + expected.key(), expected.lineFeeds, lineFeeds(actual));
        assertFalse("Legacy literal escape survived " + expected.key(), actual.toString().contains("\\10"));
        assertFalse("A carriage return replaced LF " + expected.key(), actual.toString().contains("\r"));
    }
    private static int lineFeeds(CharSequence value) {
        int result = 0;
        for (int i = 0; i < value.length(); i++) if (value.charAt(i) == '\n') result++;
        return result;
    }
    private static Expected find(String key) {
        for (Expected expected : ORIGINAL) if (expected.key().equals(key)) return expected;
        throw new AssertionError("Unknown fixed resource expectation " + key);
    }
}
