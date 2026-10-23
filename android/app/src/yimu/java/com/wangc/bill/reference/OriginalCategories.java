package com.wangc.bill.reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Original system category definitions, independent of local row IDs and the current user. */
public final class OriginalCategories {
    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 2;
    public static final int INCOME_PARENT_ID = 9;
    public static final int OTHER_PARENT_ID = 99;
    public static final int OTHER_INCOME_CHILD_ID = 911;

    private OriginalCategories() {}

    /** Constructor defaults from ParentCategory and ChildCategory; persistence supplies userId. */
    public abstract static class Seed {
        public final int categoryId;
        public final String categoryName;
        public final String iconUrl;
        public final String iconUrlNight = null;
        public final int categoryType = TYPE_SYSTEM;
        public final int positionWeight;
        public final boolean hide = false;
        public final List<Long> hideBook = Collections.emptyList();
        public final long updateTime = 0L;

        private Seed(int categoryId, String categoryName, String iconUrl) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.iconUrl = iconUrl;
            this.positionWeight = categoryId;
        }
    }

    public static final class ParentSeed extends Seed {
        private ParentSeed(int categoryId, String categoryName, String iconUrl) {
            super(categoryId, categoryName, iconUrl);
        }
    }

    public static final class ChildSeed extends Seed {
        public final int parentCategoryId;
        public final int parentChildCategoryId = 0;

        private ChildSeed(int categoryId, String categoryName, String iconUrl, int parentCategoryId) {
            super(categoryId, categoryName, iconUrl);
            this.parentCategoryId = parentCategoryId;
        }
    }

    // database.d.<clinit> and database.a.<clinit>, preserving original insertion order.
    private static final List<ParentSeed> PARENTS = Collections.unmodifiableList(Arrays.asList(
            new ParentSeed(1, "健康医疗", "ic_parent_medical"),
            new ParentSeed(2, "送礼人情", "ic_parent_gift"),
            new ParentSeed(3, "文化教育", "ic_parent_education"),
            new ParentSeed(4, "居家生活", "ic_parent_home"),
            new ParentSeed(5, "休闲娱乐", "ic_parent_game"),
            new ParentSeed(6, "出行交通", "ic_parent_traffic"),
            new ParentSeed(7, "食品餐饮", "ic_parent_restaurant"),
            new ParentSeed(8, "购物消费", "ic_parent_shopping"),
            new ParentSeed(9, "收入", "ic_parent_income"),
            new ParentSeed(99, "其他", "ic_parent_other")));
    private static final List<ChildSeed> CHILDREN = Collections.unmodifiableList(Arrays.asList(
            new ChildSeed(801, "装修装饰", "ic_shopping_renovation", 8),
            new ChildSeed(802, "办公用品", "ic_shopping_office", 8),
            new ChildSeed(803, "宠物用品", "ic_shopping_pets", 8),
            new ChildSeed(804, "服饰运动", "ic_shopping_motion", 8),
            new ChildSeed(805, "母婴玩具", "ic_shopping_baby", 8),
            new ChildSeed(806, "配饰腕表", "ic_shopping_watch", 8),
            new ChildSeed(807, "生活电器", "ic_shopping_electric", 8),
            new ChildSeed(808, "虚拟充值", "ic_shopping_recharge", 8),
            new ChildSeed(809, "手机数码", "ic_shopping_phone", 8),
            new ChildSeed(810, "个护美妆", "ic_shopping_makeup", 8),
            new ChildSeed(811, "日常家居", "ic_shopping_home", 8),
            new ChildSeed(701, "粮油调味", "ic_restaurant_foodstuff", 7),
            new ChildSeed(702, "请客吃饭", "ic_restaurant_treat", 7),
            new ChildSeed(703, "生鲜食品", "ic_restaurant_fresh", 7),
            new ChildSeed(704, "休闲零食", "ic_restaurant_snacks", 7),
            new ChildSeed(705, "饮料酒水", "ic_restaurant_wine", 7),
            new ChildSeed(706, "晚餐", "ic_restaurant_dinner", 7),
            new ChildSeed(707, "午餐", "ic_restaurant_lunch", 7),
            new ChildSeed(708, "早餐", "ic_restaurant_breakfast", 7),
            new ChildSeed(601, "保养修车", "ic_traffic_maintain", 6),
            new ChildSeed(602, "飞机", "ic_traffic_aircraft", 6),
            new ChildSeed(603, "火车", "ic_traffic_train", 6),
            new ChildSeed(604, "加油", "ic_traffic_oil", 6),
            new ChildSeed(605, "停车费", "ic_traffic_parking", 6),
            new ChildSeed(606, "公共交通", "ic_traffic_bus", 6),
            new ChildSeed(607, "打车", "ic_traffic_taxi", 6),
            new ChildSeed(501, "演出", "ic_game_show", 5),
            new ChildSeed(502, "酒吧", "ic_game_bar", 5),
            new ChildSeed(503, "棋牌桌游", "ic_game_chess", 5),
            new ChildSeed(504, "足浴按摩", "ic_game_foot_bath", 5),
            new ChildSeed(505, "运动健身", "ic_game_keep_fit", 5),
            new ChildSeed(506, "电影唱歌", "ic_game_film", 5),
            new ChildSeed(507, "旅游度假", "ic_game_travel", 5),
            new ChildSeed(401, "家政清洁", "ic_home_husekeeping", 4),
            new ChildSeed(402, "车位费", "ic_home_parking_lot", 4),
            new ChildSeed(403, "房租还贷", "ic_home_loan", 4),
            new ChildSeed(404, "物业费", "ic_home_property", 4),
            new ChildSeed(405, "燃气费", "ic_home_gas", 4),
            new ChildSeed(406, "水费", "ic_home_water", 4),
            new ChildSeed(407, "电费", "ic_home_electricity_fees", 4),
            new ChildSeed(408, "话费宽带", "ic_home_telephone_bill", 4),
            new ChildSeed(301, "培训考试", "ic_education_train", 3),
            new ChildSeed(302, "书报杂志", "ic_education_book", 3),
            new ChildSeed(303, "学费", "ic_education_tuition", 3),
            new ChildSeed(201, "打赏", "ic_gift_reward", 2),
            new ChildSeed(202, "红包", "ic_gift_red_paper", 2),
            new ChildSeed(203, "借出", "ic_gift_lend", 2),
            new ChildSeed(204, "礼物", "ic_gift_gift", 2),
            new ChildSeed(205, "孝敬长辈", "ic_gift_filial_piety", 2),
            new ChildSeed(101, "买药", "ic_medical_medicine", 1),
            new ChildSeed(102, "医院", "ic_medical_hospital", 1),
            new ChildSeed(103, "滋补保健", "ic_medical_healthcare", 1),
            new ChildSeed(901, "报销", "ic_income_reimbursement", 9),
            new ChildSeed(902, "补贴", "ic_income_subsidy", 9),
            new ChildSeed(903, "二手闲置", "ic_income_second_hand", 9),
            new ChildSeed(904, "工资", "ic_income_wages", 9),
            new ChildSeed(905, "兼职外快", "ic_income_part_time_job", 9),
            new ChildSeed(906, "奖金", "ic_income_bonus", 9),
            new ChildSeed(907, "借入", "ic_income_borrow", 9),
            new ChildSeed(908, "礼金人情", "ic_income_cash_gift", 9),
            new ChildSeed(909, "理财盈利", "ic_income_manager_money", 9),
            new ChildSeed(910, "中奖", "ic_income_winning", 9),
            new ChildSeed(911, "其他", "ic_income_other", 9),
            new ChildSeed(9901, "慈善捐助", "ic_other_charitable", 99),
            new ChildSeed(9902, "理财支出", "ic_r_manager_money", 99),
            new ChildSeed(9903, "罚款赔偿", "ic_other_fine", 99)));

    /** Use only when the current user has no system parent category, as in database.d.b(). */
    public static List<ParentSeed> parents() {
        return PARENTS;
    }

    /** Use only when the current user has no system child category, as in database.a.b(). */
    public static List<ChildSeed> children() {
        return CHILDREN;
    }

    public static ParentSeed parent(int categoryId) {
        for (ParentSeed seed : PARENTS) {
            if (seed.categoryId == categoryId) return seed;
        }
        return null;
    }

    public static ChildSeed child(int categoryId) {
        for (ChildSeed seed : CHILDREN) {
            if (seed.categoryId == categoryId) return seed;
        }
        return null;
    }

    /** Seed-only order of DAO y1.L/O: expense parents, positionWeight descending. */
    public static List<ParentSeed> expenseParents() {
        List<ParentSeed> result = new ArrayList<>();
        for (ParentSeed seed : PARENTS) {
            if (seed.categoryId != INCOME_PARENT_ID) result.add(seed);
        }
        sortDescending(result);
        return Collections.unmodifiableList(result);
    }

    /** Seed-only order of DAO m0.z/E: direct children, positionWeight descending. */
    public static List<ChildSeed> childrenOf(int parentCategoryId) {
        List<ChildSeed> result = new ArrayList<>();
        for (ChildSeed seed : CHILDREN) {
            if (seed.parentCategoryId == parentCategoryId) result.add(seed);
        }
        sortDescending(result);
        return Collections.unmodifiableList(result);
    }

    /** All original income seeds are top-level; custom nested income categories are separate rows. */
    public static List<ChildSeed> incomeChildren() {
        return childrenOf(INCOME_PARENT_ID);
    }

    /** DAO y1.O/m0.E/T use the per-book exclusion relation, without a legacy hide predicate. */
    public static boolean isVisibleInBook(List<Long> hiddenBookIds, long bookId) {
        return hiddenBookIds == null || !hiddenBookIds.contains(Long.valueOf(bookId));
    }

    private static <T extends Seed> void sortDescending(List<T> seeds) {
        Collections.sort(seeds, new Comparator<T>() {
            @Override public int compare(T left, T right) {
                return Integer.compare(right.positionWeight, left.positionWeight);
            }
        });
    }
}
