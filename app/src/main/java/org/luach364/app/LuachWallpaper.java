package org.luach364.app;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.time.ZonedDateTime;

/**
 * רקע חי: מצויר מחדש בכל שנייה כשהמסך דולק.
 * במסך הנעילה — שעון חנוך עם מחוגים וכרטיס התאריך. במסך הבית — רק תמונת הרקע (אלא אם בחרת אחרת).
 * השכבה הקבועה (רקע + חוגה + כרטיס) נבנית מחדש רק כשמשהו בחישוב משתנה; בכל שנייה מצוירים רק המחוגים.
 */
public class LuachWallpaper extends WallpaperService {

    @Override
    public Engine onCreateEngine() { return new LuachEngine(); }

    class LuachEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Runnable tick = this::drawFrame;
        private boolean visible;
        private float xOffset = 0.5f;
        private Bitmap bg, layer;
        private boolean bgDirty = true;
        private String layerKey = "";
        private Renderer.Opts layerOpts;
        private int bgW, bgH;
        // פתיחת נעילה / כיבוי מסך — לעבור מיד בין תצוגת הנעילה לתצוגת הבית
        private final BroadcastReceiver screenRcv = new BroadcastReceiver() {
            @Override public void onReceive(Context ctx, Intent i) { if (visible) drawFrame(); }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            Updater.prefs(LuachWallpaper.this).registerOnSharedPreferenceChangeListener(this);
            IntentFilter f = new IntentFilter();
            f.addAction(Intent.ACTION_USER_PRESENT);
            f.addAction(Intent.ACTION_SCREEN_OFF);
            f.addAction(Intent.ACTION_SCREEN_ON);
            if (Build.VERSION.SDK_INT >= 33) registerReceiver(screenRcv, f, Context.RECEIVER_EXPORTED);
            else registerReceiver(screenRcv, f);
        }

        @Override
        public void onDestroy() {
            handler.removeCallbacks(tick);
            Updater.prefs(LuachWallpaper.this).unregisterOnSharedPreferenceChangeListener(this);
            try { unregisterReceiver(screenRcv); } catch (Exception ignored) { }
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean v) {
            visible = v;
            if (v) drawFrame(); else handler.removeCallbacks(tick);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            layerKey = "";
            drawFrame();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            visible = false;
            handler.removeCallbacks(tick);
            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onOffsetsChanged(float xOff, float yOff, float xStep, float yStep, int xPx, int yPx) {
            if (Math.abs(xOff - xOffset) > 0.001f) { xOffset = xOff; drawFrame(); }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences p, String key) {
            layerKey = "";
            if ("bgVer".equals(key)) bgDirty = true;
            if (visible) drawFrame();
        }

        private void drawFrame() {
            handler.removeCallbacks(tick);
            SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) paint(c);
            } catch (Exception e) {
                Log.e("Luach364", "draw failed", e);
            } finally {
                if (c != null) {
                    try { holder.unlockCanvasAndPost(c); } catch (Exception ignored) { }
                }
            }
            if (visible) {
                long now = System.currentTimeMillis();
                handler.postDelayed(tick, 1000 - (now % 1000) + 10);   // בתחילת כל שנייה
            }
        }

        private void paint(Canvas c) {
            int W = c.getWidth(), H = c.getHeight();
            int vw = Math.min(W, Updater.screenSize(LuachWallpaper.this)[0]);   // רוחב המסך הנראה
            float left = (W - vw) * xOffset;

            if (bgDirty || bgW != vw || bgH != H) {
                if (bg != null) bg.recycle();
                bg = Updater.loadBg(LuachWallpaper.this, vw, H);
                bgW = vw; bgH = H; bgDirty = false;
                layerKey = "";
            }

            KeyguardManager km = getSystemService(KeyguardManager.class);
            boolean locked = km != null && km.isKeyguardLocked();
            boolean show = locked || isPreview() || Updater.prefs(LuachWallpaper.this).getBoolean("home", false);

            c.drawColor(Color.BLACK);
            if (!show) {                                   // מסך הבית: רק התמונה
                c.save(); c.translate(left, 0);
                Renderer.drawBackground(c, vw, H, bg);
                c.restore();
                return;
            }

            ZonedDateTime now = ZonedDateTime.now();
            Luach.State s = Updater.state(LuachWallpaper.this, now);
            String key = s.key() + "|" + Updater.cfgKey(LuachWallpaper.this) + "|" + vw + "x" + H;
            if (layer == null || !key.equals(layerKey)) {
                if (layer != null) layer.recycle();
                layerOpts = Updater.opts(LuachWallpaper.this, bg);
                layer = Renderer.render(vw, H, s, layerOpts);
                layerKey = key;
            }
            c.drawBitmap(layer, left, 0, null);
            if (layerOpts.dialHalf > 0) {
                c.save(); c.translate(left, 0);
                Renderer.drawHands(c, layerOpts.dialCx, layerOpts.dialCy, layerOpts.dialHalf, now, layerOpts.theme);
                c.restore();
            }
        }
    }
}
