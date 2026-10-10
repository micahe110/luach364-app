package org.luach364.app;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZonedDateTime;

public class MainActivity extends Activity {
    static final int PICK_BG = 7;

    static final String[] CITY = {"אשקלון","ירושלים","תל אביב","חיפה","באר שבע","אילת","מותאם אישית"};
    static final String[][] COORD = {{"31.668","34.574"},{"31.778","35.235"},{"32.080","34.781"},
            {"32.794","34.990"},{"31.252","34.791"},{"29.558","34.952"}};

    PreviewView preview;
    TextView info;
    EditText latE, lonE;
    Spinner city;
    SharedPreferences p;
    boolean firstSpinnerEvent = true;

    int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    /** תצוגה מקדימה חיה — אותו ציור כמו ברקע, עם מחוגים שזזים. */
    static class PreviewView extends View {
        Bitmap layer, bg; String key = ""; Renderer.Opts opts; long bgVer = -1;
        final int W, H;
        PreviewView(Context c) {
            super(c);
            int[] sz = Updater.screenSize(c);
            W = sz[0]; H = sz[1];
        }
        @Override protected void onMeasure(int wSpec, int hSpec) {
            int w = MeasureSpec.getSize(wSpec);
            int h = (int) (w * 0.62f * H / W);
            setMeasuredDimension(w, h);
        }
        void invalidateLayer() { key = ""; invalidate(); }
        @Override protected void onDraw(Canvas c) {
            Context ctx = getContext();
            long ver = Updater.prefs(ctx).getLong("bgVer", 0);
            if (ver != bgVer) { bg = Updater.loadBg(ctx, W, H); bgVer = ver; key = ""; }
            ZonedDateTime now = ZonedDateTime.now();
            Luach.State s = Updater.state(ctx, now);
            String k = s.key() + "|" + Updater.cfgKey(ctx);
            if (layer == null || !k.equals(key)) {
                opts = Updater.opts(ctx, bg);
                layer = Renderer.render(W, H, s, opts);
                key = k;
            }
            float sc = getHeight() / (float) H;
            c.save();
            c.translate((getWidth() - W * sc) / 2f, 0);
            c.scale(sc, sc);
            c.drawBitmap(layer, 0, 0, null);
            if (opts.dialHalf > 0) Renderer.drawHands(c, opts.dialCx, opts.dialCy, opts.dialHalf, now, opts.theme);
            c.restore();
            postInvalidateDelayed(1000 - (System.currentTimeMillis() % 1000) + 10);
        }
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        p = Updater.prefs(this);
        final I18n.L L = I18n.get(p.getString("lang", "he"));

        ScrollView sv = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(L.rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        root.setPadding(dp(16), dp(16), dp(16), dp(32));
        sv.addView(root);

        TextView title = new TextView(this);
        title.setText(L.uiTitle);
        title.setTextSize(24); title.setGravity(Gravity.CENTER); title.setTextColor(0xFFE8D48B);
        root.addView(title);

        // בחירת שפה
        root.addView(label(L.uiLang));
        Spinner lang = new Spinner(this);
        lang.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, I18n.NAMES));
        int cur = 0;
        for (int i = 0; i < I18n.CODES.length; i++) if (I18n.CODES[i].equals(L.code)) cur = i;
        lang.setSelection(cur);
        final int curLang = cur;
        lang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
                if (pos == curLang) return;
                p.edit().putString("lang", I18n.CODES[pos]).apply();
                recreate();
            }
            @Override public void onNothingSelected(AdapterView<?> a) { }
        });
        root.addView(lang);

        TextView hint = new TextView(this);
        hint.setText(L.uiHint);
        hint.setGravity(Gravity.CENTER); hint.setTextColor(0xFF9FB2D6); hint.setPadding(0, dp(4), 0, dp(10));
        root.addView(hint);

        preview = new PreviewView(this);
        root.addView(preview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        info = new TextView(this);
        info.setTextColor(Color.WHITE); info.setTextSize(15); info.setPadding(0, dp(10), 0, dp(6));
        info.setGravity(Gravity.CENTER);
        root.addView(info);

        Button set = button(L.uiSet);
        set.setOnClickListener(v -> {
            Intent i = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, LuachWallpaper.class));
            try {
                startActivity(i);
            } catch (Exception e) {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
            }
        });
        root.addView(set);

        Button pick = button(L.uiPick);
        pick.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(i, PICK_BG);
        });
        root.addView(pick);

        Button clear = button(L.uiNoBg);
        clear.setOnClickListener(v -> {
            //noinspection ResultOfMethodCallIgnored
            Updater.bgFile(this).delete();
            p.edit().putLong("bgVer", System.currentTimeMillis()).apply();
            refresh();
        });
        root.addView(clear);

        CheckBox dial = new CheckBox(this);
        dial.setText(L.uiDial);
        dial.setTextColor(Color.WHITE);
        dial.setChecked(p.getBoolean("dial", true));
        dial.setOnCheckedChangeListener((v, on) -> { p.edit().putBoolean("dial", on).apply(); refresh(); });
        root.addView(dial);

        CheckBox dark = new CheckBox(this);
        dark.setText(L.uiDark);
        dark.setTextColor(Color.WHITE);
        dark.setChecked("dark".equals(p.getString("theme", "light")));
        dark.setOnCheckedChangeListener((v, on) -> { p.edit().putString("theme", on ? "dark" : "light").apply(); refresh(); });
        root.addView(dark);

        CheckBox home = new CheckBox(this);
        home.setText(L.uiHome);
        home.setTextColor(Color.WHITE);
        home.setChecked(p.getBoolean("home", false));
        home.setOnCheckedChangeListener((v, on) -> p.edit().putBoolean("home", on).apply());
        root.addView(home);

        root.addView(label(L.uiHeight));
        SeekBar top = new SeekBar(this);
        top.setMax(45);
        top.setProgress(p.getInt("top", 30) - 10);
        top.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int v, boolean user) { }
            @Override public void onStartTrackingTouch(SeekBar s) { }
            @Override public void onStopTrackingTouch(SeekBar s) {
                p.edit().putInt("top", s.getProgress() + 10).apply(); refresh();
            }
        });
        root.addView(top);

        root.addView(label(L.uiLoc));
        city = new Spinner(this);
        city.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, L.city));
        root.addView(city);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        latE = num(p.getString("lat", "31.668"));
        lonE = num(p.getString("lon", "34.574"));
        row.addView(latE, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(lonE, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(row);
        Button saveLoc = button(L.uiSaveLoc);
        saveLoc.setOnClickListener(v -> {
            try {
                Double.parseDouble(latE.getText().toString().trim());
                Double.parseDouble(lonE.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, L.uiBadLoc, Toast.LENGTH_SHORT).show();
                return;
            }
            int sel = city.getSelectedItemPosition();
            p.edit().putString("lat", latE.getText().toString().trim())
                    .putString("lon", lonE.getText().toString().trim())
                    .putString("place", sel < COORD.length ? L.city[sel] : "")
                    .putInt("city", sel).apply();
            refresh();
            Toast.makeText(this, L.uiSaved, Toast.LENGTH_SHORT).show();
        });
        root.addView(saveLoc);
        city.setSelection(p.getInt("city", 0));
        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
                if (firstSpinnerEvent) { firstSpinnerEvent = false; return; }
                if (pos < COORD.length) { latE.setText(COORD[pos][0]); lonE.setText(COORD[pos][1]); }
            }
            @Override public void onNothingSelected(AdapterView<?> a) { }
        });

        setContentView(sv);
        refresh();
    }

    Button button(String t) {
        Button b = new Button(this);
        b.setText(t);
        b.setAllCaps(false);
        return b;
    }

    TextView label(String t) {
        TextView tv = new TextView(this);
        tv.setText(t); tv.setTextColor(0xFFDCE4F2); tv.setPadding(0, dp(14), 0, dp(4));
        return tv;
    }

    EditText num(String v) {
        EditText e = new EditText(this);
        e.setText(v);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        e.setTextColor(Color.WHITE);
        e.setGravity(Gravity.CENTER);
        return e;
    }

    void refresh() {
        I18n.L L = I18n.get(p.getString("lang", "he"));
        Luach.State s = Updater.state(this, ZonedDateTime.now());
        info.setText(I18n.title(L, s.z) + "\n" + I18n.courseLine(L, s.z) + "\n" + I18n.sunLine(L, s));
        preview.invalidateLayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preview != null) refresh();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != PICK_BG || res != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        File out = Updater.bgFile(this);
        try (InputStream in = getContentResolver().openInputStream(uri); OutputStream os = new FileOutputStream(out)) {
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
        } catch (Exception e) {
            Toast.makeText(this, I18n.get(p.getString("lang", "he")).uiBadImg, Toast.LENGTH_LONG).show();
            return;
        }
        p.edit().putLong("bgVer", System.currentTimeMillis()).apply();
        refresh();
    }
}
