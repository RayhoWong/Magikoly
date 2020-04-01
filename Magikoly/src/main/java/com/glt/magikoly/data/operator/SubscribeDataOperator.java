package com.glt.magikoly.data.operator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.glt.magikoly.data.CoreDataOperator;
import com.glt.magikoly.data.table.SubscribeTable;
import com.glt.magikoly.subscribe.billing.BillingOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * ┌───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐
 * │Esc│ │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8│ │ F9│F10│F11│F12│ │P/S│S L│P/B│ ┌┐    ┌┐    ┌┐
 * └───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘ └┘    └┘    └┘
 * ┌──┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───────┐┌───┬───┬───┐┌───┬───┬───┬───┐
 * │~`│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp ││Ins│Hom│PUp││N L│ / │ * │ - │
 * ├──┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤├───┼───┼───┤├───┼───┼───┼───┤
 * │Tab │ Q │ W │ E │ R │ T │ Y │ U │ I │ O │ P │{ [│} ]│ | \ ││Del│End│PDn││ 7 │ 8 │ 9 │   │
 * ├────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤└───┴───┴───┘├───┼───┼───┤ + │
 * │Caps │ A │ S │ D │ F │ G │ H │ J │ K │ L │: ;│" '│ Enter  │             │ 4 │ 5 │ 6 │   │
 * ├─────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤    ┌───┐    ├───┼───┼───┼───┤
 * │Shift  │ Z │ X │ C │ V │ B │ N │ M │< ,│> .│? /│  Shift   │    │ ↑ │    │ 1 │ 2 │ 3 │   │
 * ├────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤┌───┼───┼───┐├───┴───┼───┤ E││
 * │Ctrl│Ray │Alt │         Space         │ Alt│code│fuck│Ctrl││ ← │ ↓ │ → ││   0   │ . │←─┘│
 * └────┴────┴────┴───────────────────────┴────┴────┴────┴────┘└───┴───┴───┘└───────┴───┴───┘
 *
 * @author Rayhahah
 * @blog http://rayhahah.com
 * @time 2018/8/15
 * @tips 这个类是Object的子类
 * @fuction
 */
public class SubscribeDataOperator extends CoreDataOperator {
    public SubscribeDataOperator(Context context) {
        super(context);
    }

    public void addBillingOrder(BillingOrder orderDetails) {
        ContentValues values = new ContentValues();
        values.put(SubscribeTable.ID, orderDetails.orderId);
        values.put(SubscribeTable.SKU_ID, orderDetails.skuId);
        values.put(SubscribeTable.ORDER_ID, orderDetails.orderId);
        values.put(SubscribeTable.PURCHASE_TOKEN, orderDetails.purchaseToken);
        values.put(SubscribeTable.PURCHASE_TIME, orderDetails.purchaseTime);
        values.put(SubscribeTable.ENTRANCE, orderDetails.entrance);
        mManager.insert(SubscribeTable.TABLE_NAME, values, null);
    }

    public boolean queryOrderBySkuId(String skuId) {
        boolean result = false;
        Cursor cursor = mManager.query(SubscribeTable.TABLE_NAME, null, SubscribeTable.SKU_ID + "=?", new String[]{skuId}, null);
        if (cursor != null) {
            result = cursor.getCount() > 0;
        }
        return result;
    }

    public List<BillingOrder> queryAllBillingOrders() {
        List<BillingOrder> result = new ArrayList<>();
        Cursor cursor = mManager.query(SubscribeTable.TABLE_NAME, null, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    BillingOrder item = new BillingOrder();
                    cursor.getString(cursor.getColumnIndex(SubscribeTable.ID));
                    item.skuId = cursor.getString(cursor.getColumnIndex(SubscribeTable.SKU_ID));
                    item.orderId = cursor.getString(cursor.getColumnIndex(SubscribeTable.ORDER_ID));
                    item.purchaseToken = cursor.getString(cursor.getColumnIndex(SubscribeTable.PURCHASE_TOKEN));
                    item.purchaseTime = cursor.getLong(cursor.getColumnIndex(SubscribeTable.PURCHASE_TIME));
                    item.entrance = cursor.getInt(cursor.getColumnIndex(SubscribeTable.ENTRANCE));

                    result.add(item);
                }

            } finally {
                cursor.close();
            }
        }
        return result;
    }

    public void clearTable() {
        mManager.delete(SubscribeTable.TABLE_NAME, null, null);
    }
}
