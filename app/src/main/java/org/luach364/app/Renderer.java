package org.luach364.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
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

/** מצייר את מסך הנעילה ישירות מהנתונים — בלי דפדפן ובלי צילום מסך. */
public final class Renderer {
    private Renderer() {}

    // =====================================================================
    //  ערכת עיצוב — כל הצבעים במקום אחד. כדי לשנות עיצוב משנים רק כאן.
    //  צבע = 0xAARRGGBB  (AA = שקיפות: FF אטום, 80 חצי, 00 שקוף)
    // =====================================================================
    public static final class Theme {
        // חוגה
        int halo, diskIn, diskOut;                       // הילה סביב החוגה, הדיסק הפנימי (מרכז → שוליים)
        int daySeg, daySeg2, nightSeg, nightSeg2;        // מקטעי היום/הלילה בטבעת (לסירוגין)
        int curDay, curNight;                            // החלק הנוכחי
        int segLine, segTxtDay, segTxtNight;             // קווי הפרדה ומספרי החלקים
        int ring, riseDot, setDot, dotRim;               // טבעת זהב, נקודות זריחה/שקיעה
        int tickBig, tickSmall, hourMain, hourOther;     // שנתות ו-24 השעות
        int cPart, cKind, cGate;                         // טקסט מרכזי
        boolean icons;                                   // שמש וירח בתוך החוגה
        int sun, moon;
        // מחוגים
        int hHour, hHourEdge, hMin, hMinHead, hMinEdge, cap, capRim, capDot;
        // כרטיס
        int cardTop, cardBottom, cardStroke, cardShadow;
        int tGreg, tTitle, tSub, tBody, tInfo, tSun, tNote, tFeast;
        boolean textShadow, ornament;
        Typeface titleTf;
    }

    /** בהיר — "זכוכית": טבעת פסטל, דיסק כחול שקוף, כרטיס שמנת. */
    public static Theme light() {
        Theme t = new Theme();
        t.halo = 0x55FFFFFF; t.diskIn = 0xD84A8DB8; t.diskOut = 0xE02C6390;
        t.daySeg = 0xF2F7EEDB; t.daySeg2 = 0xF2F1E6CC; t.nightSeg = 0xEBD6EAF3; t.nightSeg2 = 0xEBC7E4E8;
        t.curDay = 0xFFF0C766; t.curNight = 0xFF7DB7E2;
        t.segLine = 0xAAFFFFFF; t.segTxtDay = 0xFF2A4466; t.segTxtNight = 0xFF1E3A5F;
        t.ring = 0xFFD4A537; t.riseDot = 0xFFEBBE4C; t.setDot = 0xFFEBBE4C; t.dotRim = 0xFFFFFFFF;
        t.tickBig = 0xFFF2C14E; t.tickSmall = 0x99FFFFFF; t.hourMain = 0xFFFFFFFF; t.hourOther = 0xEEFFFFFF;
        t.cPart = 0xFFFFFFFF; t.cKind = 0xFFF2C14E; t.cGate = 0xFFFFFFFF;
        t.icons = true; t.sun = 0xFFF6C343; t.moon = 0xFFE3EEF6;
        t.hHour = 0xFFFFFFFF; t.hHourEdge = 0x66244A6E; t.hMin = 0xFFE2B33C; t.hMinHead = 0xFFF2C14E; t.hMinEdge = 0x66553A00;
        t.cap = 0xFFE2B33C; t.capRim = 0xFFFFFFFF; t.capDot = 0xFF1E3A5F;
        t.cardTop = 0xF5FCF7EE; t.cardBottom = 0xEADCEEF6; t.cardStroke = 0xFFD4A537; t.cardShadow = 0x40000000;
        t.tGreg = 0xFF2B3F5C; t.tTitle = 0xFF14325A; t.tSub = 0xFFB07A12; t.tBody = 0xFF22406A;
        t.tInfo = 0xFF2B4B70; t.tSun = 0xFF14325A; t.tNote = 0xFF6A7F99; t.tFeast = 0xFFB0650A;
        t.textShadow = false; t.ornament = true; t.titleTf = SANS_B;
        return t;
    }

    /** כהה — העיצוב הקודם: לילה כחול-כהה וזהב. */
    public static Theme dark() {
        Theme t = new Theme();
        t.halo = 0x8C0B1430; t.diskIn = 0xDB16244A; t.diskOut = 0xDB0E1830;
        t.daySeg = 0xFFE8D48B; t.daySeg2 = 0xFFE8D48B; t.nightSeg = 0xFF16244A; t.nightSeg2 = 0xFF16244A;
        t.curDay = 0xFFF6C343; t.curNight = 0xFF34508F;
        t.segLine = 0xFF2A3C66; t.segTxtDay = 0xFF6B4E00; t.segTxtNight = 0xFF9FB2D6;
        t.ring = 0xFFC8A227; t.riseDot = 0xFFF6C343; t.setDot = 0xFFE07B39; t.dotRim = 0xFFFFF3C4;
        t.tickBig = 0xFFC8A227; t.tickSmall = 0xFF5A6B85; t.hourMain = 0xFFFFFFFF; t.hourOther = 0xFFC9D4EA;
        t.cPart = 0xFFE8D48B; t.cKind = 0xFFE8D48B; t.cGate = 0xFFDCE4F2;
        t.icons = false; t.sun = 0xFFF6C343; t.moon = 0xFFDCE4F2;
        t.hHour = 0xFFFFFFFF; t.hHourEdge = 0xFF0E1830; t.hMin = 0xFFE8D48B; t.hMinHead = 0xFFF6C343; t.hMinEdge = 0xFF0E1830;
        t.cap = 0xFFC8A227; t.capRim = 0xFF0E1830; t.capDot = 0xFF0E1830;
        t.cardTop = 0xB00E1830; t.cardBottom = 0xB00E1830; t.cardStroke = 0xC0C8A227; t.cardShadow = 0;
        t.tGreg = 0xFFFFFFFF; t.tTitle = 0xFFF3E3A6; t.tSub = 0xFFE8D48B; t.tBody = 0xFFDCE4F2;
        t.tInfo = 0xFFC9D4EA; t.tSun = 0xFFFFFFFF; t.tNote = 0xFF9FB2D6; t.tFeast = 0xFFF6C343;
        t.textShadow = true; t.ornament = false; t.titleTf = SERIF_B;
        return t;
    }

    public static final class Opts {
        public Bitmap bg;              // תמונת הרקע של המשתמש (או null)
        public float topPct = 0.30f;   // מאיפה מתחיל התוכן (מתחת לשעון של סמסונג)
        public boolean dial = true;    // להציג את שעון חנוך
        public String place = "";
        public Theme theme = light();
        public I18n.L lang = I18n.get("he");
        // פלט: מיקום החוגה, כדי לצייר עליה את המחוגים בזמן אמת
        public float dialCx, dialCy, dialHalf;
    }

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
        Theme t = o.theme;
        float u = W / 1080f;
        drawBackground(c, W, H, o.bg);

        // ----- שורות הכרטיס (בשפה שנבחרה) -----
        Luach.Z z = s.z;
        I18n.L L = o.lang;
        rtl = L.rtl;
        List<Line> lines = new ArrayList<>();
        lines.add(new Line(I18n.gregLine(L, s.greg), 36 * u, t.tGreg, SANS, 0));
        boolean compact = L.code.equals("he") || L.code.equals("zh") || L.code.equals("ja");
        lines.add(new Line(I18n.title(L, z), (compact ? 74 : 58) * u, t.tTitle, t.titleTf, 6 * u));
        lines.add(new Line(I18n.subLine(L, z), 40 * u, t.tSub, SANS_B, 6 * u));
        if (!z.ic) {
            lines.add(new Line(I18n.courseLine(L, z), 36 * u, t.tBody, SANS, 6 * u));
            String feast = I18n.feast(L, z);
            if (feast != null) lines.add(new Line(feast, 44 * u, t.tFeast, SANS_B, 8 * u));
            String cnt = I18n.countLine(L, z);
            if (cnt != null) lines.add(new Line(cnt, 34 * u, t.tBody, SANS, 4 * u));
            if (z.shmita) lines.add(new Line(L.shmita, 30 * u, t.tNote, SANS, 4 * u));
        }
        lines.add(new Line(I18n.infoLine(L, s), 30 * u, t.tInfo, SANS, 6 * u));
        if (s.beforeSunrise) lines.add(new Line(L.beforeSunrise, 28 * u, t.tNote, SANS, 4 * u));
        lines.add(new Line(I18n.sunLine(L, s), 40 * u, t.tSun, SANS_B, 12 * u));
        if (o.place != null && !o.place.isEmpty()) lines.add(new Line(o.place, 26 * u, t.tNote, SANS, 2 * u));

        float cw = W * 0.86f, pad = 34 * u, orn = t.ornament ? 40 * u : 0;
        int tw = (int) (cw - 2 * pad);
        List<StaticLayout> lays = new ArrayList<>();
        float cardH = 2 * pad + orn;
        for (Line l : lines) {
            StaticLayout sl = layout(l.t, l.size, l.color, l.tf, tw, t.textShadow ? 3 * u : 0);
            lays.add(sl);
            cardH += sl.getHeight() + l.gapBefore;
        }

        // ----- פריסה -----
        float top = H * o.topPct, bottom = H * 0.90f, gap = 44 * u;
        float D = o.dial ? Math.min(W * 0.78f, H * 0.36f) : 0;
        if (o.dial && top + D + gap + cardH > bottom) {
            D = bottom - top - gap - cardH;
            if (D < W * 0.40f) D = 0;            // אין מקום — רק הכרטיס
        }
        float y = top;
        o.dialHalf = 0;
        if (D > 0) {
            drawDial(c, W / 2f, y + D / 2f, D / 2f, s, t, L);
            o.dialCx = W / 2f; o.dialCy = y + D / 2f; o.dialHalf = D / 2f;
            y += D + gap;
        }

        // ----- הכרטיס -----
        RectF card = new RectF((W - cw) / 2f, y, (W + cw) / 2f, y + cardH);
        float rad = 44 * u;
        if (t.cardShadow != 0) {
            Paint sh = new Paint(Paint.ANTI_ALIAS_FLAG);
            sh.setColor(t.cardBottom | 0xFF000000);
            sh.setShadowLayer(26 * u, 0, 10 * u, t.cardShadow);
            c.drawRoundRect(card, rad, rad, sh);
        }
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setShader(new LinearGradient(0, card.top, 0, card.bottom, t.cardTop, t.cardBottom, Shader.TileMode.CLAMP));
        c.drawRoundRect(card, rad, rad, fill);
        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE); stroke.setStrokeWidth(3f * u); stroke.setColor(t.cardStroke);
        c.drawRoundRect(card, rad, rad, stroke);
        float ty = y + pad;
        for (int i = 0; i < lays.size(); i++) {
            ty += lines.get(i).gapBefore;
            c.save();
            c.translate(card.left + pad, ty);
            lays.get(i).draw(c);
            c.restore();
            ty += lays.get(i).getHeight();
        }
        if (t.ornament) drawOrnament(c, W / 2f, ty + orn * 0.55f, u, t.cardStroke);
        return bmp;
    }

    /** קישוט זהב בתחתית הכרטיס: קו — עלה — קו. */
    private static void drawOrnament(Canvas c, float cx, float cy, float u, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color); p.setStrokeWidth(2.4f * u);
        c.drawLine(cx - 150 * u, cy, cx - 30 * u, cy, p);
        c.drawLine(cx + 30 * u, cy, cx + 150 * u, cy, p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2.6f * u);
        for (int k = -1; k <= 1; k++) {                    // שלושה עלים קטנים
            Path leaf = new Path();
            leaf.moveTo(cx, cy + 8 * u);
            leaf.lineTo(cx + k * 16 * u - 7 * u, cy - 10 * u + Math.abs(k) * 6 * u);
            leaf.lineTo(cx + k * 16 * u, cy - 20 * u + Math.abs(k) * 8 * u);
            leaf.lineTo(cx + k * 16 * u + 7 * u, cy - 10 * u + Math.abs(k) * 6 * u);
            leaf.close();
            c.drawPath(leaf, p);
        }
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
            p.setShader(new LinearGradient(0, 0, 0, H, new int[]{0xFF3D6FB6, 0xFF4FA3D1, 0xFFBFE6EE},
                    new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, W, H, p);
        }
    }

    // ===== שעון חנוך (השכבה הקבועה) =====
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

    private static void drawDial(Canvas c, float cx, float cy, float half, Luach.State s, Theme t, I18n.L L) {
        float R = half * 0.95f, RI = R * 0.78f, S = half * 2;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(t.halo); c.drawCircle(cx, cy, R + S * 0.022f, p);

        double rise = s.riseMin, set = s.setMin, nl = 1440 - (set - rise);
        int dp = s.dp, np = s.np;
        // מקטעי הטבעת, לסירוגין בשני גוונים
        for (int k = 0; k < 2; k++) {
            boolean day = k == 0;
            double st = day ? rise : set, len = day ? set - rise : nl;
            int n = day ? dp : np;
            for (int i = 0; i < n; i++) {
                double a = st + len * i / n, b = st + len * (i + 1) / n;
                boolean cur = (day == s.isDay) && (i + 1 == s.part);
                int col = cur ? (day ? t.curDay : t.curNight)
                        : (day ? (i % 2 == 0 ? t.daySeg : t.daySeg2) : (i % 2 == 0 ? t.nightSeg : t.nightSeg2));
                p.setColor(col);
                c.drawPath(seg(cx, cy, RI, R, ang(a), ang(b)), p);
            }
        }
        // הדיסק הפנימי
        Paint disk = new Paint(Paint.ANTI_ALIAS_FLAG);
        disk.setShader(new RadialGradient(cx, cy - RI * 0.25f, RI * 1.1f, new int[]{t.diskIn, t.diskOut},
                new float[]{0f, 1f}, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, RI, disk);

        Paint ln = new Paint(Paint.ANTI_ALIAS_FLAG); ln.setStrokeWidth(S * 0.004f); ln.setColor(t.segLine);
        Paint tx = new Paint(Paint.ANTI_ALIAS_FLAG); tx.setTextAlign(Paint.Align.CENTER); tx.setTextSize(S * 0.044f);
        for (int k = 0; k < 2; k++) {
            boolean day = k == 0;
            double st = day ? rise : set, len = day ? set - rise : nl;
            int n = day ? dp : np;
            tx.setColor(day ? t.segTxtDay : t.segTxtNight);
            tx.setTypeface(SANS);
            for (int i = 0; i < n; i++) {
                double a = st + len * i / n, b = st + len * (i + 1) / n;
                float aa = ang(a), am = ang((a + b) / 2);
                c.drawLine(px(cx, aa, RI), py(cy, aa, RI), px(cx, aa, R), py(cy, aa, R), ln);
                float rr = (R + RI) / 2;
                c.drawText(String.valueOf(i + 1), px(cx, am, rr), py(cy, am, rr) + tx.getTextSize() * 0.36f, tx);
            }
        }
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG); ring.setStyle(Paint.Style.STROKE); ring.setColor(t.ring);
        ring.setStrokeWidth(S * 0.007f); c.drawCircle(cx, cy, R, ring);
        ring.setStrokeWidth(S * 0.005f); c.drawCircle(cx, cy, RI, ring);
        // זריחה ושקיעה
        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG); rim.setStyle(Paint.Style.STROKE); rim.setStrokeWidth(S * 0.004f);
        rim.setColor(t.dotRim);
        float[] marks = {ang(rise), ang(set)};
        int[] cols = {t.riseDot, t.setDot};
        for (int i = 0; i < 2; i++) {
            float x = px(cx, marks[i], R), y = py(cy, marks[i], R);
            dot.setColor(cols[i]); c.drawCircle(x, y, S * 0.020f, dot);
            c.drawCircle(x, y, S * 0.020f, rim);
        }
        // שנתות הדקות
        Paint tk = new Paint(Paint.ANTI_ALIAS_FLAG);
        for (int k = 0; k < 60; k++) {
            boolean big = k % 5 == 0;
            tk.setColor(big ? t.tickBig : t.tickSmall); tk.setStrokeWidth(S * (big ? 0.0045f : 0.0025f));
            float a = k * 6, r1 = RI - S * 0.008f, r2 = RI - S * (big ? 0.032f : 0.018f);
            c.drawLine(px(cx, a, r1), py(cy, a, r1), px(cx, a, r2), py(cy, a, r2), tk);
        }
        // 24 השעות
        Paint hp = new Paint(Paint.ANTI_ALIAS_FLAG); hp.setTextAlign(Paint.Align.CENTER);
        for (int h = 0; h < 24; h++) {
            boolean main = h % 6 == 0;
            hp.setTextSize(S * (main ? 0.056f : 0.040f));
            hp.setTypeface(main ? SANS_B : SANS);
            hp.setColor(main ? t.hourMain : t.hourOther);
            float a = h * 15, r = RI * 0.80f;
            c.drawText(String.valueOf(h == 0 ? 24 : h), px(cx, a, r), py(cy, a, r) + hp.getTextSize() * 0.36f, hp);
        }
        // שמש (ליד 6) וירח (ליד 18)
        if (t.icons) {
            drawSun(c, px(cx, 90, RI * 0.50f), py(cy, 90, RI * 0.50f), S * 0.034f, t.sun);
            drawMoon(c, px(cx, 270, RI * 0.50f), py(cy, 270, RI * 0.50f), S * 0.036f, t.moon, t.diskIn);
        }
        // טקסט מרכזי
        int w = (int) (RI * 1.2f);
        StaticLayout l1 = layout(I18n.partLine(L, s), S * 0.040f, t.cPart, SANS, w, 0);
        StaticLayout l2 = layout(s.isDay ? L.kindDay : L.kindNight, S * 0.040f, t.cKind, SANS_B, w, 0);
        StaticLayout l3 = layout(L.gate.replace("{n}", String.valueOf(s.gate)), S * 0.038f, t.cGate, SANS, w, 0);
        drawCentered(c, l1, cx, cy - RI * 0.40f, w);
        drawCentered(c, l2, cx, cy - RI * 0.24f, w);
        drawCentered(c, l3, cx, cy + RI * 0.36f, w);
    }

    private static void drawCentered(Canvas c, StaticLayout l, float cx, float cy, int w) {
        c.save(); c.translate(cx - w / 2f, cy - l.getHeight() / 2f); l.draw(c); c.restore();
    }

    private static void drawSun(Canvas c, float x, float y, float r, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        c.drawCircle(x, y, r * 0.55f, p);
        p.setStrokeWidth(r * 0.16f); p.setStrokeCap(Paint.Cap.ROUND);
        for (int k = 0; k < 8; k++) {
            float a = k * 45;
            c.drawLine(px(x, a, r * 0.80f), py(y, a, r * 0.80f), px(x, a, r * 1.15f), py(y, a, r * 1.15f), p);
        }
    }

    private static void drawMoon(Canvas c, float x, float y, float r, int color, int bgColor) {
        Path moon = new Path();
        moon.addCircle(x, y, r, Path.Direction.CW);
        Path cut = new Path();
        cut.addCircle(x + r * 0.45f, y - r * 0.25f, r * 0.85f, Path.Direction.CW);
        moon.op(cut, Path.Op.DIFFERENCE);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        c.drawPath(moon, p);
    }

    // ===== מחוגים — מצוירים בכל שנייה מעל השכבה הקבועה =====
    public static void drawHands(Canvas c, float cx, float cy, float half, ZonedDateTime now, Theme t) {
        float R = half * 0.95f, RI = R * 0.78f, S = half * 2;
        double mins = now.getMinute() + now.getSecond() / 60.0;
        float hourDeg = (float) ((now.getHour() * 60 + mins) / 4.0);   // 24 שעות = 360°
        float minDeg = (float) (mins * 6.0);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
        edge.setStyle(Paint.Style.STROKE); edge.setStrokeJoin(Paint.Join.ROUND);
        // מחוג 24 השעות — לבן ורחב
        float L = RI * 0.62f, w = S * 0.018f, tail = RI * 0.08f;
        Path hp = new Path();
        hp.moveTo(cx - w, cy + tail);
        hp.lineTo(cx - w * 0.55f, cy - L + w * 2);
        hp.lineTo(cx, cy - L);
        hp.lineTo(cx + w * 0.55f, cy - L + w * 2);
        hp.lineTo(cx + w, cy + tail);
        hp.close();
        c.save(); c.rotate(hourDeg, cx, cy);
        fill.setColor(t.hHour); c.drawPath(hp, fill);
        edge.setColor(t.hHourEdge); edge.setStrokeWidth(S * 0.004f); c.drawPath(hp, edge);
        c.restore();
        // מחוג הדקות — דק, זהוב, עם חץ
        float Lm = RI * 0.95f, wm = S * 0.0065f, hl = S * 0.060f, hw = S * 0.026f, tm = RI * 0.14f;
        RectF shaft = new RectF(cx - wm, cy - Lm + hl * 0.8f, cx + wm, cy + tm);
        Path head = new Path();
        head.moveTo(cx, cy - Lm);
        head.lineTo(cx + hw, cy - Lm + hl);
        head.lineTo(cx, cy - Lm + hl * 0.72f);
        head.lineTo(cx - hw, cy - Lm + hl);
        head.close();
        c.save(); c.rotate(minDeg, cx, cy);
        edge.setColor(t.hMinEdge); edge.setStrokeWidth(S * 0.003f);
        fill.setColor(t.hMin); c.drawRoundRect(shaft, wm, wm, fill); c.drawRoundRect(shaft, wm, wm, edge);
        fill.setColor(t.hMinHead); c.drawPath(head, fill); c.drawPath(head, edge);
        c.restore();
        // מרכז
        fill.setColor(t.cap); c.drawCircle(cx, cy, S * 0.026f, fill);
        edge.setColor(t.capRim); edge.setStrokeWidth(S * 0.005f); c.drawCircle(cx, cy, S * 0.026f, edge);
        fill.setColor(t.capDot); c.drawCircle(cx, cy, S * 0.009f, fill);
    }

    /** כיוון הטקסט: עברית מימין לשמאל, שאר השפות משמאל לימין. */
    static boolean rtl = true;

    static StaticLayout layout(String t, float size, int color, Typeface tf, int width, float shadow) {
        TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size); p.setColor(color); p.setTypeface(tf);
        if (shadow > 0) p.setShadowLayer(shadow, 0, shadow / 2, 0xAA000000);
        return StaticLayout.Builder.obtain(t, 0, t.length(), p, Math.max(1, width))
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setTextDirection(rtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR)
                .setIncludePad(false)
                .build();
    }
}
