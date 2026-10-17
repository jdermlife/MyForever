package com.quantech.filscore.wear;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

final class WearDashboardView extends View {
    private static final int PAGE_COUNT = 5;
    private static final int NAVY = Color.rgb(7, 27, 51);
    private static final int PANEL = Color.rgb(12, 43, 72);
    private static final int BLUE = Color.rgb(59, 130, 246);
    private static final int GREEN = Color.rgb(34, 197, 94);
    private static final int RED = Color.rgb(239, 68, 68);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(165, 190, 211);

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Snapshot snapshot;
    private int page;
    private float touchDownX;

    WearDashboardView(Context context) {
        super(context);
        snapshot = Snapshot.read(context);
        setBackgroundColor(NAVY);
        setContentDescription("FILScore Wear dashboard. Swipe left or right to change pages.");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float scale = Math.min(width, height) / 240f;

        canvas.save();
        canvas.scale(scale, scale);
        float logicalWidth = width / scale;
        float logicalHeight = height / scale;

        drawHeader(canvas, logicalWidth);
        switch (page) {
            case 0:
                drawScoreRings(canvas, logicalWidth, logicalHeight);
                break;
            case 1:
                drawWealthRadar(canvas, logicalWidth, logicalHeight);
                break;
            case 2:
                drawBudget(canvas, logicalWidth, logicalHeight);
                break;
            case 3:
                drawHealthGauge(canvas, logicalWidth, logicalHeight);
                break;
            default:
                drawBillCalendar(canvas, logicalWidth, logicalHeight);
                break;
        }
        drawPageDots(canvas, logicalWidth, logicalHeight);
        canvas.restore();
    }

    private void drawHeader(Canvas canvas, float width) {
        text(canvas, "FILScore", width / 2f, 25f, 12f, WHITE, Paint.Align.CENTER, true);
        String[] labels = {"HEALTH", "WEALTH", "BUDGET", "POSITION", "BILLS"};
        text(canvas, labels[page], width / 2f, 40f, 7f, MUTED, Paint.Align.CENTER, true);
    }

    private void drawScoreRings(Canvas canvas, float width, float height) {
        float centerX = width / 2f;
        float centerY = height / 2f + 4f;
        float[] radii = {72f, 58f, 44f};
        int[] colors = {BLUE, GREEN, RED};
        Float[] values = {snapshot.filsScore, snapshot.financialHealth, snapshot.wealthScore};
        float[] maximums = {1000f, 1000f, 100f};

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(7f);
        for (int index = 0; index < radii.length; index++) {
            RectF ring = new RectF(
                    centerX - radii[index], centerY - radii[index],
                    centerX + radii[index], centerY + radii[index]
            );
            paint.setColor(Color.argb(58, 165, 190, 211));
            canvas.drawArc(ring, -90f, 360f, false, paint);
            paint.setColor(colors[index]);
            canvas.drawArc(ring, -90f, sweep(values[index], maximums[index]), false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        text(canvas, display(snapshot.filsScore, 0), centerX, centerY + 1f, 25f, WHITE, Paint.Align.CENTER, true);
        text(canvas, "CREDIT HEALTH", centerX, centerY + 18f, 7f, MUTED, Paint.Align.CENTER, true);
        drawSyncState(canvas, width, height);
    }

    private void drawWealthRadar(Canvas canvas, float width, float height) {
        float centerX = width / 2f;
        float centerY = height / 2f + 4f;
        float radius = 67f;
        int points = 5;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        for (int level = 1; level <= 4; level++) {
            paint.setColor(Color.argb(65, 165, 190, 211));
            canvas.drawPath(polygon(centerX, centerY, radius * level / 4f, points, null), paint);
        }

        for (int index = 0; index < points; index++) {
            double angle = -Math.PI / 2 + index * Math.PI * 2 / points;
            canvas.drawLine(
                    centerX, centerY,
                    centerX + (float) Math.cos(angle) * radius,
                    centerY + (float) Math.sin(angle) * radius,
                    paint
            );
        }

        float[] values = snapshot.wealthComponents();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(82, 34, 197, 94));
        canvas.drawPath(polygon(centerX, centerY, radius, points, values), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(GREEN);
        canvas.drawPath(polygon(centerX, centerY, radius, points, values), paint);

        String[] labels = {"LIQ", "CASH", "GOAL", "PROTECT", "DEBT"};
        for (int index = 0; index < points; index++) {
            double angle = -Math.PI / 2 + index * Math.PI * 2 / points;
            text(
                    canvas,
                    labels[index],
                    centerX + (float) Math.cos(angle) * (radius + 13f),
                    centerY + (float) Math.sin(angle) * (radius + 13f) + 3f,
                    6f,
                    MUTED,
                    Paint.Align.CENTER,
                    true
            );
        }
        drawSyncState(canvas, width, height);
    }

    private void drawBudget(Canvas canvas, float width, float height) {
        float left = 28f;
        float right = width - 28f;
        float top = 59f;
        roundRect(canvas, left, top, right, height - 30f, 9f, PANEL);
        text(canvas, "BUDGET UTILIZATION", left + 14f, top + 24f, 7f, MUTED, Paint.Align.LEFT, true);
        text(canvas, display(snapshot.budgetUtilization, 1) + percentSuffix(snapshot.budgetUtilization), right - 14f, top + 52f, 24f, GREEN, Paint.Align.RIGHT, true);

        float barTop = top + 72f;
        roundRect(canvas, left + 14f, barTop, right - 14f, barTop + 14f, 7f, Color.rgb(36, 65, 91));
        if (snapshot.budgetUtilization != null) {
            float ratio = clamp(snapshot.budgetUtilization / 100f);
            roundRect(canvas, left + 14f, barTop, left + 14f + (right - left - 28f) * ratio, barTop + 14f, 7f, GREEN);
        }

        text(canvas, "Income", left + 14f, top + 111f, 7f, MUTED, Paint.Align.LEFT, false);
        text(canvas, money(snapshot.income), right - 14f, top + 111f, 8f, WHITE, Paint.Align.RIGHT, true);
        text(canvas, "Expenses", left + 14f, top + 129f, 7f, MUTED, Paint.Align.LEFT, false);
        text(canvas, money(snapshot.expenses), right - 14f, top + 129f, 8f, WHITE, Paint.Align.RIGHT, true);
        drawSyncState(canvas, width, height);
    }

    private void drawHealthGauge(Canvas canvas, float width, float height) {
        float centerX = width / 2f;
        float centerY = height / 2f + 35f;
        RectF gauge = new RectF(centerX - 78f, centerY - 78f, centerX + 78f, centerY + 78f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeWidth(14f);
        paint.setColor(RED);
        canvas.drawArc(gauge, 150f, 80f, false, paint);
        paint.setColor(BLUE);
        canvas.drawArc(gauge, 230f, 50f, false, paint);
        paint.setColor(GREEN);
        canvas.drawArc(gauge, 280f, 80f, false, paint);

        if (snapshot.financialHealth != null) {
            float angle = 150f + 210f * clamp(snapshot.financialHealth / 1000f);
            double radians = Math.toRadians(angle);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(3f);
            paint.setColor(WHITE);
            canvas.drawLine(
                    centerX,
                    centerY,
                    centerX + (float) Math.cos(radians) * 58f,
                    centerY + (float) Math.sin(radians) * 58f,
                    paint
            );
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, 5f, paint);
        text(canvas, display(snapshot.financialHealth, 0), centerX, centerY + 31f, 25f, WHITE, Paint.Align.CENTER, true);
        text(canvas, "FINANCIAL HEALTH", centerX, centerY + 46f, 7f, MUTED, Paint.Align.CENTER, true);
        drawSyncState(canvas, width, height);
    }

    private void drawBillCalendar(Canvas canvas, float width, float height) {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        int startColumn = firstDay.getDayOfWeek().getValue() % 7;
        int days = today.lengthOfMonth();
        float left = 30f;
        float top = 58f;
        float cellWidth = (width - 60f) / 7f;
        float cellHeight = 18f;

        text(
                canvas,
                today.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()).toUpperCase(Locale.ROOT),
                width / 2f,
                top,
                10f,
                WHITE,
                Paint.Align.CENTER,
                true
        );
        String[] weekdays = {"S", "M", "T", "W", "T", "F", "S"};
        for (int index = 0; index < weekdays.length; index++) {
            text(canvas, weekdays[index], left + index * cellWidth + cellWidth / 2f, top + 20f, 6f, MUTED, Paint.Align.CENTER, true);
        }
        for (int day = 1; day <= days; day++) {
            int cell = startColumn + day - 1;
            int column = cell % 7;
            int row = cell / 7;
            float x = left + column * cellWidth + cellWidth / 2f;
            float y = top + 40f + row * cellHeight;
            if (day == today.getDayOfMonth()) {
                paint.setColor(BLUE);
                canvas.drawCircle(x, y - 3f, 8f, paint);
            }
            text(canvas, String.valueOf(day), x, y, 7f, WHITE, Paint.Align.CENTER, day == today.getDayOfMonth());
        }
        text(canvas, "Outstanding", left, height - 42f, 7f, MUTED, Paint.Align.LEFT, false);
        text(canvas, display(snapshot.billsOutstanding, 0), width - left, height - 42f, 9f, RED, Paint.Align.RIGHT, true);
        drawSyncState(canvas, width, height);
    }

    private void drawPageDots(Canvas canvas, float width, float height) {
        float startX = width / 2f - (PAGE_COUNT - 1) * 6f;
        for (int index = 0; index < PAGE_COUNT; index++) {
            paint.setColor(index == page ? WHITE : Color.argb(90, 255, 255, 255));
            canvas.drawCircle(startX + index * 12f, height - 13f, index == page ? 3f : 2f, paint);
        }
    }

    private void drawSyncState(Canvas canvas, float width, float height) {
        if (!snapshot.hasData) {
            text(canvas, "Awaiting phone sync", width / 2f, height - 27f, 7f, MUTED, Paint.Align.CENTER, false);
        }
    }

    private Path polygon(float centerX, float centerY, float radius, int points, float[] values) {
        Path path = new Path();
        for (int index = 0; index < points; index++) {
            float multiplier = values == null ? 1f : clamp(values[index] / 100f);
            double angle = -Math.PI / 2 + index * Math.PI * 2 / points;
            float x = centerX + (float) Math.cos(angle) * radius * multiplier;
            float y = centerY + (float) Math.sin(angle) * radius * multiplier;
            if (index == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        return path;
    }

    private void roundRect(Canvas canvas, float left, float top, float right, float bottom, float radius, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint);
    }

    private void text(Canvas canvas, String value, float x, float y, float size, int color, Paint.Align align, boolean bold) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTextAlign(align);
        paint.setTypeface(bold ? android.graphics.Typeface.DEFAULT_BOLD : android.graphics.Typeface.DEFAULT);
        canvas.drawText(value, x, y, paint);
    }

    private static float sweep(Float value, float maximum) {
        return value == null ? 0f : 360f * clamp(value / maximum);
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(value, 1f));
    }

    private static String display(Float value, int decimals) {
        if (value == null) {
            return "--";
        }
        return String.format(Locale.getDefault(), decimals == 0 ? "%.0f" : "%.1f", value);
    }

    private static String percentSuffix(Float value) {
        return value == null ? "" : "%";
    }

    private static String money(Float value) {
        return value == null ? "--" : "₱" + String.format(Locale.getDefault(), "%,.0f", value);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchDownX = event.getX();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float distance = event.getX() - touchDownX;
            if (Math.abs(distance) > 28f) {
                page = distance < 0
                        ? Math.min(page + 1, PAGE_COUNT - 1)
                        : Math.max(page - 1, 0);
                invalidate();
            }
            performClick();
            return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private static final class Snapshot {
        final Float filsScore;
        final Float financialHealth;
        final Float wealthScore;
        final Float budgetUtilization;
        final Float income;
        final Float expenses;
        final Float billsOutstanding;
        final Float liquidity;
        final Float cashFlow;
        final Float goalMomentum;
        final Float protection;
        final Float leverage;
        final boolean hasData;

        private Snapshot(SharedPreferences values) {
            filsScore = optional(values, "fils_score");
            financialHealth = optional(values, "financial_health");
            wealthScore = optional(values, "wealth_score");
            budgetUtilization = optional(values, "budget_utilization");
            income = optional(values, "income");
            expenses = optional(values, "expenses");
            billsOutstanding = optional(values, "bills_outstanding");
            liquidity = optional(values, "liquidity");
            cashFlow = optional(values, "cash_flow");
            goalMomentum = optional(values, "goal_momentum");
            protection = optional(values, "protection");
            leverage = optional(values, "leverage");
            hasData = !values.getAll().isEmpty();
        }

        static Snapshot read(Context context) {
            return new Snapshot(context.getSharedPreferences("wear_snapshot", Context.MODE_PRIVATE));
        }

        float[] wealthComponents() {
            return new float[]{
                    valueOrZero(liquidity),
                    valueOrZero(cashFlow),
                    valueOrZero(goalMomentum),
                    valueOrZero(protection),
                    valueOrZero(leverage)
            };
        }

        private static Float optional(SharedPreferences values, String key) {
            return values.contains(key) ? values.getFloat(key, 0f) : null;
        }

        private static float valueOrZero(Float value) {
            return value == null ? 0f : value;
        }
    }
}