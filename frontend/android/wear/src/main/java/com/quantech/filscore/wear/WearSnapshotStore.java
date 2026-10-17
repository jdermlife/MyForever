package com.quantech.filscore.wear;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

final class WearSnapshotStore {
    private WearSnapshotStore() {
    }

    static boolean save(Context context, byte[] payload) {
        try {
            JSONObject snapshot = new JSONObject(new String(payload, StandardCharsets.UTF_8));
            SharedPreferences.Editor editor = context
                    .getSharedPreferences("wear_snapshot", Context.MODE_PRIVATE)
                    .edit()
                    .clear();

            JSONObject financial = snapshot.optJSONObject("financialHealth");
            putNumber(editor, "financial_health", financial, "value");
            JSONArray groups = financial == null ? null : financial.optJSONArray("groups");
            if (groups != null) {
                for (int index = 0; index < groups.length(); index++) {
                    JSONObject group = groups.optJSONObject(index);
                    if (group == null) continue;
                    String id = group.optString("id");
                    if ("foundation".equals(id) || "control".equals(id) || "future".equals(id)) {
                        putNumber(editor, "financial_" + id, group, "value");
                    }
                }
            }

            JSONObject credit = snapshot.optJSONObject("creditHealth");
            putNumber(editor, "credit_score", credit, "creditScore");
            putNumber(editor, "credit_bureau_score", credit, "creditBureauScore");
            putNumber(editor, "non_starter_score", credit, "nonStarterScore");
            putNumber(editor, "social_score", credit, "socialScore");
            putNumber(editor, "psychometric_score", credit, "psychometricScore");

            JSONObject wealth = snapshot.optJSONObject("wealthBalance");
            putNumber(editor, "wealth_positioning", wealth, "netWorthPositioning");
            putNumber(editor, "wealth_behaviour", wealth, "wealthBehaviour");
            putNumber(editor, "wealth_foundation", wealth, "wealthFoundation");
            putNumber(editor, "wealth_authenticity", wealth, "wealthAuthenticity");

            JSONObject budget = snapshot.optJSONObject("budget");
            putNumber(editor, "budget_utilization", budget, "utilizationPercent");
            putNumber(editor, "budget_amount", budget, "budgetAmount");
            putNumber(editor, "utilized_amount", budget, "utilizedAmount");
            putNumber(editor, "remaining_amount", budget, "remainingAmount");
            putNumber(editor, "overflow_percent", budget, "overflowPercent");
            putNumber(editor, "income", budget, "income");
            putNumber(editor, "expenses", budget, "expenses");

            JSONObject bills = snapshot.optJSONObject("bills");
            putNumber(editor, "bills_outstanding", bills, "outstanding");
            editor.putString("updated_at", snapshot.optString("updatedAt"));
            editor.apply();
            return true;
        } catch (JSONException error) {
            return false;
        }
    }

    private static void putNumber(
            SharedPreferences.Editor editor,
            String preferenceKey,
            JSONObject section,
            String field
    ) {
        if (section != null && section.has(field) && !section.isNull(field)) {
            editor.putFloat(preferenceKey, (float) section.optDouble(field));
        }
    }
}