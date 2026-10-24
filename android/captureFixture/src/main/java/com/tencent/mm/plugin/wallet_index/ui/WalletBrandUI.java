package com.tencent.mm.plugin.wallet_index.ui;
/** Project-owned synthetic WeChat page; fixture_detail exercises the real detail parser. */
public final class WalletBrandUI extends io.github.wcwac.reference.fixture.FixtureActivity {
    @Override protected String[] lines() {
        String amount = getIntent().getStringExtra("fixture_amount");
        if (amount == null || !amount.matches("[1-9][0-9]{0,3}\\.[0-9]{2}")) amount = "12.30";
        if (getIntent().getBooleanExtra("fixture_detail", false)) {
            // p.recoveredPageKind sees detail, p.v reads predecessor merchant,
            // signed amount and the exact following transaction-time item.
            return new String[]{"账单详情", "功能验收同一商户", "-" + amount,
                    "交易时间", "2025-06-15 12:34:56", "付款方式", "零钱"};
        }
        return new String[]{"微信支付", "支付成功", "实验小卖部", "￥" + amount};
    }
}
