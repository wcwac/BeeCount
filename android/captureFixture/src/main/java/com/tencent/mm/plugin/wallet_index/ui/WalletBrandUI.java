package com.tencent.mm.plugin.wallet_index.ui;
public final class WalletBrandUI extends io.github.wcwac.reference.fixture.FixtureActivity {
    @Override protected String[] lines(){String amount=getIntent().getStringExtra("fixture_amount");if(amount==null||!amount.matches("[1-9][0-9]{0,3}\\.[0-9]{2}"))amount="12.30";return new String[]{"微信支付","支付成功","实验小卖部","￥"+amount};}
}
