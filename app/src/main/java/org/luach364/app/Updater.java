package org.luach364.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.util.DisplayMetrics;
import android.view.Display;

import java.io.File;
import java.time.ZonedDateTime;

/** עזרים משותפים: הגדרות, תמונת רקע, גודל מסך. */
public final class Updater {
    private Updater() {}

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("luach", Context.MODE_PRIVATE);
    }

    public static double lat(Context c) { return Double.parseDouble(prefs(c).getString("lat", "31.668")); }
    public static double lon(Context c) { return Double.parseDouble(prefs(c).getString("lon", "34.574")); }

    public static Luach.State state(Context c, ZonedDateTime now) {
        return Luach.stateAt(now, lat(c), lon(c));
    }

    public static File bgFile(Context c) { return new File(c.getFilesDir(), "bg_image"); }

    public static int[] screenSize(Context c) {
        DisplayManager dm = c.getSystemService(DisplayManager.class);
        Display d = dm.getDisplay(Display.DEFAULT_DISPLAY);
        DisplayMetrics m = new DisplayMetrics();
        d.getRealMetrics(m);
        int w = Math.min(m.widthPixels, m.heightPixels), h = Math.max(m.widthPixels, m.heightPixels);
        return new int[]{w, h};
    }

    public static Bitmap loadBg(Context c, int W, int H) {
        File f = bgFile(c);
        if (!f.exists()) return null;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), o);
        int sample = 1;
        while (o.outWidth / (sample * 2) >= W && o.outHeight / (sample * 2) >= H) sample *= 2;
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = sample;
        return BitmapFactory.decodeFile(f.getAbsolutePath(), o2);
    }

    public static Renderer.Opts opts(Context c, Bitmap bg) {
        SharedPreferences p = prefs(c);
        Renderer.Opts o = new Renderer.Opts();
        o.topPct = p.getInt("top", 30) / 100f;
        o.dial = p.getBoolean("dial", true);
        o.place = p.getString("place", "אשקלון");
        o.bg = bg;
        return o;
    }

    /** כל מה שמשפיע על השכבה הקבועה מלבד הזמן. */
    public static String cfgKey(Context c) {
        SharedPreferences p = prefs(c);
        return p.getString("lat", "") + p.getString("lon", "") + p.getInt("top", 30) + p.getBoolean("dial", true)
                + p.getLong("bgVer", 0) + p.getString("place", "");
    }
}
