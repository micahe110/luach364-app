package org.luach364.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/** מצייר את רקע מסך הנעילה ישירות מהנתונים — בלי דפדפן ובלי צילום מסך. */
public final class Renderer {
    private Renderer() {}

    public static final class Opts {
        public Bitmap bg;              // תמונת הרקע של המשתמש (או null)
        public float topPct = 0.30f;   // מאיפה מתחיל התוכן (מתחת לשעון של סמסונג)
        public boolean dial = true;    // להציג את שעון חנוך
        public String place = "";
        // פלט: מיקום החוגה, כדי לצייר עליה את המחוגים בזמן אמת
        public float dialCx, dialCy, dialHalf;
    }

    private static final Typeface SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
    private static final Typeface SERIF_B = Typeface.create(Typeface.SERIF, Typeface.BOLD);
    private static final Typeface SANS = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    private static final Typeface SANS_B = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);

    private static final class Line {
        final String t; final float size; final int color; final Typeface tf; final float gapBefore;
        Line(String t, float size, int color, Typeface tf, float gapBefore) {
            this.t = t; this.size = size; this.color = color; this.tf = tf; this.gapBefore = gapBefore;
        }
    }

    public static Bitmap render(int W, int H, Luach.State s, Opts o) {
        Bitmap bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        float u = W / 1080f;
        drawBackground(c, W, H, o.bg);

        // ----- שורות הכרטיס -----
        Luach.Z z = s.z;
        List<Line> lines = new ArrayList<>();
        int gwd = s.greg.getDayOfWeek().getValue() % 7;
        lines.add(new Line(Luach.WEEKDAYS[gwd] + ", " + s.greg.getDayOfMonth() + " ב" + Luach.GREG[s.greg.getMonthValue() - 1]
                + " " + s.greg.getYear(), 38 * u, 0xFFFFFFFF, SANS, 0));
        if (z.ic) {
            lines.add(new Line("שבוע העיבור · יום " + Luach.gem(z.icDay), 70 * u, 0xFFF3E3A6, SERIF_B, 8 * u));
            lines.add(new Line(Luach.WEEKDAYS[z.wd] + " · כל הכהנים עם גמול", 36 * u, 0xFFE8D48B, SANS, 6 * u));
        } else {
            lines.add(new Line(Luach.gem(z.day) + " לחודש " + Luach.MONTH_ORD[z.month - 1], 76 * u, 0xFFF3E3A6, SERIF_B, 8 * u));
            String yr = "שנה " + Luach.gem(z.year) + " במחזור";
            if (z.year == 1) yr += " · אות גמול";
            if (z.year == 4) yr += " · אות שכניה";
            lines.add(new Line(Luach.WEEKDAYS[z.wd] + " · " + yr, 36 * u, 0xFFE8D48B, SANS, 6 * u));
            String course = (z.wd == 6 && !z.cin.equals(z.cout))
                    ? "משמרת " + z.cout + " יוצאת · " + z.cin + " נכנסת" : "משמרת " + z.cin;
            lines.add(new Line(course, 36 * u, 0xFFDCE4F2, SANS, 4 * u));
            if (z.feast != null) lines.add(new Line(z.feast, 44 * u, 0xFFF6C343, SERIF_B, 8 * u));
            if (z.countLabel != null) lines.add(new Line("היום " + z.countN + " " + z.countLabel, 34 * u, 0xFFDCE4F2, SANS, 4 * u));
            if (z.shmita) lines.add(new Line("שנת השמיטה", 30 * u, 0xFFB9C6E2, SANS, 4 * u));
        }
        String moon = z.isX ? "X — מילוא סכמטי" : (z.isD ? "דוקה — ליל הסהר הראשון" : "X בעוד " + z.toX + " ימים");
        lines.add(new Line("שער " + s.gate + " · יום " + s.dp + " חלקים · לילה " + s.np + " · " + moon, 30 * u, 0xFFC9D4EA, SANS, 8 * u));
        if (s.beforeSunrise) lines.add(new Line("לפני הזריחה — עדיין היום המקראי של אתמול", 28 * u, 0xFFB9C6E2, SANS, 4 * u));
        lines.add(new Line("זריחה " + Luach.hhmm(s.rise) + "   ·   שקיעה " + Luach.hhmm(s.set), 38 * u, 0xFFFFFFFF, SANS_B, 12 * u));
        if (o.place != null && !o.place.isEmpty()) lines.add(new Line(o.place, 26 * u, 0xFF9FB2D6, SANS, 4 * u));

        float cw = W * 0.88f, pad = 34 * u;
        int tw = (int) (cw - 2 * pad);
        List<StaticLayout> lays = new ArrayList<>();
        float cardH = 2 * pad;
        for (Line l : lines) {
            StaticLayout sl = layout(l.t, l.size, l.color, l.tf, tw, 3 * u);
            lays.add(sl);
            cardH += sl.getHeight() + l.gapBefore;
        }

        // ----- פריסה -----
        float top = H * o.topPct, bottom = H * 0.94f, gap = 28 * u;
        float D = o.dial ? Math.min(W * 0.74f, H * 0.34f) : 0;
        if (o.dial && top + D + gap + cardH > bottom) {
            D = bottom - top - gap - cardH;
            if (D < W * 0.40f) D = 0;            // אין מקום — רק הכרטיס
        }
        float y = top;
        o.dialHalf = 0;
        if (D > 0) {
            drawDial(c, W / 2f, y + D / 2f, D / 2f, s, u);
            o.dialCx = W / 2f; o.dialCy = y + D / 2f; o.dialHalf = D / 2f;
            y += D + gap;
        }

        // ----- הכרטיס -----
        RectF card = new RectF((W - cw) / 2f, y, (W + cw) / 2f, y + cardH);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(0xB00E1830);
        c.drawRoundRect(card, 40 * u, 40 * u, fill);
        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE); stroke.setStrokeWidth(2.5f * u); stroke.setColor(0xC0C8A227);
        c.drawRoundRect(card, 40 * u, 40 * u, stroke);
        float ty = y + pad;
        for (int i = 0; i < lays.size(); i++) {
            ty += lines.get(i).gapBefore;
            c.save();
            c.translate(card.left + pad, ty);
            lays.get(i).draw(c);
            c.restore();
            ty += lays.get(i).getHeight();
        }
        return bmp;
    }

    // ===== רקע =====
    public static void drawBackground(Canvas c, int W, int H, Bitmap bg) {
        if (bg != null) {
            float sc = Math.max(W / (float) bg.getWidth(), H / (float) bg.getHeight());
            float bw = bg.getWidth() * sc, bh = bg.getHeight() * sc;
            RectF dst = new RectF((W - bw) / 2f, (H - bh) / 2f, (W + bw) / 2f, (H + bh) / 2f);
            Paint p = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
            c.drawBitmap(bg, null, dst, p);
        } else {
            Paint p = new Paint();
            p.setShader(new LinearGradient(0, 0, 0, H, new int[]{0xFF0B1430, 0xFF1C2C52, 0xFF3A2F5C},
                    new float[]{0f, 0.6f, 1f}, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, W, H, p);
        }
    }

    // ===== שעון חנוך (בלי מחוגים — תמונה סטטית) =====
    private static float ang(double min) { return (float) (((min % 1440) + 1440) % 1440 / 1440.0 * 360.0); }

    private static Path seg(float cx, float cy, float r1, float r2, float a1, float a2) {
        float sweep = ((a2 - a1) % 360 + 360) % 360;
        if (sweep < 0.01f) sweep = 359.99f;
        RectF out = new RectF(cx - r2, cy - r2, cx + r2, cy + r2);
        RectF in = new RectF(cx - r1, cy - r1, cx + r1, cy + r1);
        Path p = new Path();
        p.arcTo(out, a1 - 90, sweep, true);
        p.arcTo(in, a1 - 90 + sweep, -sweep, false);
        p.close();
        return p;
    }

    private static float px(float cx, float a, float r) { return cx + r * (float) Math.cos((a - 90) * Math.PI / 180); }
    private static float py(float cy, float a, float r) { return cy + r * (float) Math.sin((a - 90) * Math.PI / 180); }

    private static void drawDial(Canvas c, float cx, float cy, float half, Luach.State s, float u) {
        float R = half * 0.95f, RI = R * 0.78f, S = half * 2;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0x8C0B1430); c.drawCircle(cx, cy, R + S * 0.02f, p);
        p.setColor(0xDB0E1830); c.drawCircle(cx, cy, RI, p);

        double rise = s.riseMin, set = s.setMin, nl = 1440 - (set - rise);
        int dp = s.dp, np = s.np;
        p.setColor(0xFF16244A); c.drawPath(seg(cx, cy, RI, R, ang(set), ang(rise)), p);
        p.setColor(0xFFE8D48B); c.drawPath(seg(cx, cy, RI, R, ang(rise), ang(set)), p);
        // החלק הנוכחי
        if (s.isDay) {
            double a = rise + (set - rise) * (s.part - 1) / dp, b = rise + (set - rise) * s.part / dp;
            p.setColor(0xFFF6C343); c.drawPath(seg(cx, cy, RI, R, ang(a), ang(b)), p);
        } else {
            double a = set + nl * (s.part - 1) / np, b = set + nl * s.part / np;
            p.setColor(0xFF34508F); c.drawPath(seg(cx, cy, RI, R, ang(a), ang(b)), p);
        }
        Paint ln = new Paint(Paint.ANTI_ALIAS_FLAG); ln.setStrokeWidth(S * 0.004f);
        Paint tx = new Paint(Paint.ANTI_ALIAS_FLAG); tx.setTextAlign(Paint.Align.CENTER); tx.setTextSize(S * 0.040f);
        for (int k = 0; k < 2; k++) {
            boolean day = k == 0;
            double st = day ? rise : set, len = day ? set - rise : nl;
            int n = day ? dp : np;
            ln.setColor(day ? 0xFF131F3A : 0xFF2A3C66);
            tx.setColor(day ? 0xFF6B4E00 : 0xFF9FB2D6);
            tx.setTypeface(day ? SERIF_B : SERIF);
            for (int i = 0; i < n; i++) {
                double a = st + len * i / n, b = st + len * (i + 1) / n;
                float aa = ang(a), am = ang((a + b) / 2);
                c.drawLine(px(cx, aa, RI), py(cy, aa, RI), px(cx, aa, R), py(cy, aa, R), ln);
                float rr = (R + RI) / 2;
                c.drawText(String.valueOf(i + 1), px(cx, am, rr), py(cy, am, rr) + tx.getTextSize() * 0.36f, tx);
            }
        }
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG); ring.setStyle(Paint.Style.STROKE); ring.setColor(0xFFC8A227);
        ring.setStrokeWidth(S * 0.007f); c.drawCircle(cx, cy, R, ring);
        ring.setStrokeWidth(S * 0.004f); c.drawCircle(cx, cy, RI, ring);
        // זריחה ושקיעה
        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG); rim.setStyle(Paint.Style.STROKE); rim.setStrokeWidth(S * 0.003f);
        float[] marks = {ang(rise), ang(set)};
        int[] cols = {0xFFF6C343, 0xFFE07B39}, rims = {0xFFFFF3C4, 0xFFFFD2A8};
        for (int i = 0; i < 2; i++) {
            float x = px(cx, marks[i], R + S * 0.012f), y = py(cy, marks[i], R + S * 0.012f);
            dot.setColor(cols[i]); c.drawCircle(x, y, S * 0.016f, dot);
            rim.setColor(rims[i]); c.drawCircle(x, y, S * 0.016f, rim);
        }
        // שנתות הדקות
        Paint tk = new Paint(Paint.ANTI_ALIAS_FLAG);
        for (int k = 0; k < 60; k++) {
            boolean big = k % 5 == 0;
            tk.setColor(big ? 0xFFC8A227 : 0xFF5A6B85); tk.setStrokeWidth(S * (big ? 0.004f : 0.0025f));
            float a = k * 6, r1 = RI - S * 0.008f, r2 = RI - S * (big ? 0.032f : 0.018f);
            c.drawLine(px(cx, a, r1), py(cy, a, r1), px(cx, a, r2), py(cy, a, r2), tk);
        }
        // 24 השעות
        Paint hp = new Paint(Paint.ANTI_ALIAS_FLAG); hp.setTextAlign(Paint.Align.CENTER);
        for (int h = 0; h < 24; h++) {
            boolean main = h % 6 == 0;
            hp.setTextSize(S * (main ? 0.048f : 0.039f));
            hp.setTypeface(main ? SANS_B : SANS);
            hp.setColor(main ? 0xFFFFFFFF : 0xFFC9D4EA);
            float a = h * 15, r = RI * 0.80f;
            c.drawText(String.valueOf(h == 0 ? 24 : h), px(cx, a, r), py(cy, a, r) + hp.getTextSize() * 0.36f, hp);
        }
        // טקסט מרכזי
        StaticLayout l1 = layout("חלק " + s.part + " מתוך " + s.of + (s.isDay ? " ביום" : " בלילה"),
                S * 0.040f, 0xFFE8D48B, SANS, (int) (RI * 1.25f), 0);
        StaticLayout l2 = layout("שער " + s.gate, S * 0.036f, 0xFFDCE4F2, SANS, (int) (RI * 1.25f), 0);
        c.save(); c.translate(cx - RI * 0.625f, cy - RI * 0.30f - l1.getHeight() / 2f); l1.draw(c); c.restore();
        c.save(); c.translate(cx - RI * 0.625f, cy + RI * 0.30f - l2.getHeight() / 2f); l2.draw(c); c.restore();
        Paint cap = new Paint(Paint.ANTI_ALIAS_FLAG); cap.setColor(0xFFC8A227);
        c.drawCircle(cx, cy, S * 0.014f, cap);
    }

    // ===== מחוגים — מצוירים בכל שנייה מעל השכבה הקבועה =====
    public static void drawHands(Canvas c, float cx, float cy, float half, ZonedDateTime now) {
        float R = half * 0.95f, RI = R * 0.78f, S = half * 2;
        double mins = now.getMinute() + now.getSecond() / 60.0;
        float hourDeg = (float) ((now.getHour() * 60 + mins) / 4.0);   // 24 שעות = 360°
        float minDeg = (float) (mins * 6.0);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
        edge.setStyle(Paint.Style.STROKE); edge.setColor(0xFF0E1830); edge.setStrokeJoin(Paint.Join.ROUND);
        // מחוג 24 השעות — לבן ורחב
        float L = RI * 0.62f, w = S * 0.017f, tail = RI * 0.10f;
        Path hp = new Path();
        hp.moveTo(cx - w, cy + tail);
        hp.lineTo(cx - w * 0.55f, cy - L + w * 2);
        hp.lineTo(cx, cy - L);
        hp.lineTo(cx + w * 0.55f, cy - L + w * 2);
        hp.lineTo(cx + w, cy + tail);
        hp.close();
        c.save(); c.rotate(hourDeg, cx, cy);
        fill.setColor(0xFFFFFFFF); c.drawPath(hp, fill);
        edge.setStrokeWidth(S * 0.004f); c.drawPath(hp, edge);
        c.restore();
        // מחוג הדקות — דק, זהוב, עם חץ
        float Lm = RI * 0.95f, wm = S * 0.0065f, hl = S * 0.055f, hw = S * 0.024f, tm = RI * 0.16f;
        RectF shaft = new RectF(cx - wm, cy - Lm + hl * 0.8f, cx + wm, cy + tm);
        Path head = new Path();
        head.moveTo(cx, cy - Lm);
        head.lineTo(cx + hw, cy - Lm + hl);
        head.lineTo(cx, cy - Lm + hl * 0.75f);
        head.lineTo(cx - hw, cy - Lm + hl);
        head.close();
        c.save(); c.rotate(minDeg, cx, cy);
        edge.setStrokeWidth(S * 0.003f);
        fill.setColor(0xFFE8D48B); c.drawRoundRect(shaft, wm, wm, fill); c.drawRoundRect(shaft, wm, wm, edge);
        fill.setColor(0xFFF6C343); c.drawPath(head, fill); c.drawPath(head, edge);
        c.restore();
        fill.setColor(0xFFC8A227); c.drawCircle(cx, cy, S * 0.024f, fill);
        c.drawCircle(cx, cy, S * 0.024f, edge);
        fill.setColor(0xFF0E1830); c.drawCircle(cx, cy, S * 0.008f, fill);
    }

    static StaticLayout layout(String t, float size, int color, Typeface tf, int width, float shadow) {
        TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size); p.setColor(color); p.setTypeface(tf);
        if (shadow > 0) p.setShadowLayer(shadow, 0, shadow / 2, 0xAA000000);
        return StaticLayout.Builder.obtain(t, 0, t.length(), p, Math.max(1, width))
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setTextDirection(TextDirectionHeuristics.RTL)
                .setIncludePad(false)
                .build();
    }
}
