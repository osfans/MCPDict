package com.osfans.mcpdict.custom;

import android.content.Context;
import android.graphics.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import java.util.Locale;
import java.util.function.Consumer;

/** Native HSV palette; no web view or external picker dependency. */
public final class SchemeColorPicker {
    public static int parse(String value) {
        try { return Color.parseColor(value); } catch (RuntimeException ignored) { return 0xffb71c1c; }
    }
    public static void showOverall(Context context, String title, CustomSchemes.Scheme scheme, String group, Runnable saved) {
        String key = group == null ? "" : group;
        show(context, title, scheme.colors.getOrDefault(key, ""), group == null ? "沿用音典原色" : "跟隨上級顏色", value -> {
            Consumer<Boolean> apply = reset -> {
                CustomSchemes.applyOverallColor(scheme, group, value, reset);
                CustomSchemes.replaceCurrent(scheme);
                com.osfans.mcpdict.Util.Pref.putBool(com.osfans.mcpdict.R.string.pref_key_custom_language_color_by_scheme, true);
                saved.run();
            };
            if (!CustomSchemes.hasColorOverrides(scheme, group)) { apply.accept(false); return; }
            new AlertDialog.Builder(context).setTitle("套用整體顏色")
                    .setMessage("部分語言或子分區已單獨設色。要保留，還是清除單獨配色並統一？")
                    .setNegativeButton("取消", null)
                    .setNeutralButton("保留單獨配色", (d, w) -> apply.accept(false))
                    .setPositiveButton(value.isEmpty() ? "全部恢復繼承" : "全部統一", (d, w) -> apply.accept(true)).show();
        });
    }
    public static void show(Context context, String title, String initial, Consumer<String> result) {
        show(context, title, initial, "清除自訂顏色", result);
    }
    public static void show(Context context, String title, String initial, String resetLabel, Consumer<String> result) {
        LinearLayout body = new LinearLayout(context);
        body.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(context, 20);
        body.setPadding(pad, dp(context, 8), pad, 0);
        Palette palette = new Palette(context, parse(initial));
        body.addView(palette, new LinearLayout.LayoutParams(-1, dp(context, 200)));
        TextView hueLabel = new TextView(context); hueLabel.setText("色相"); body.addView(hueLabel);
        SeekBar hue = new SeekBar(context); hue.setMax(360); hue.setProgress(Math.round(palette.hsv[0])); body.addView(hue);
        EditText hex = new EditText(context); hex.setSingleLine(); hex.setHint("#RRGGBB"); body.addView(hex);
        View preview = new View(context); body.addView(preview, new LinearLayout.LayoutParams(-1, dp(context, 28)));
        final boolean[] syncing = {false};
        Runnable update = () -> {
            syncing[0] = true;
            hex.setText(String.format(Locale.ROOT, "#%06X", Color.HSVToColor(palette.hsv) & 0xffffff));
            preview.setBackgroundColor(Color.HSVToColor(palette.hsv));
            syncing[0] = false;
        };
        palette.changed = update;
        hue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int value, boolean user) {
                if (user) { palette.hsv[0] = value; palette.invalidate(); update.run(); }
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });
        hex.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!syncing[0] && s.toString().matches("#[0-9a-fA-F]{6}")) {
                    Color.colorToHSV(Color.parseColor(s.toString()), palette.hsv);
                    hue.setProgress(Math.round(palette.hsv[0]));
                    preview.setBackgroundColor(Color.HSVToColor(palette.hsv)); palette.invalidate();
                }
            }
            public void afterTextChanged(Editable value) {}
        });
        update.run();
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(title).setView(body)
                .setNegativeButton("取消", null).setNeutralButton(resetLabel, (d, w) -> result.accept(""))
                .setPositiveButton("確定", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = hex.getText().toString().trim().toUpperCase(Locale.ROOT);
            if (!value.matches("#[0-9A-F]{6}")) { hex.setError("請填 #RRGGBB"); return; }
            result.accept(value); dialog.dismiss();
        }));
        dialog.show();
    }
    static int dp(Context context, int value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    private static final class Palette extends View {
        final float[] hsv = new float[3];
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Runnable changed;
        Palette(Context context, int color) { super(context); Color.colorToHSV(color, hsv); setContentDescription("飽和度與明度調色盤"); }
        protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            paint.setShader(new LinearGradient(0,0,w,0,Color.WHITE,Color.HSVToColor(new float[]{hsv[0],1,1}), Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,paint);
            paint.setShader(new LinearGradient(0,0,0,h,Color.TRANSPARENT,Color.BLACK,Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,paint); paint.setShader(null);
            float x = hsv[1]*w, y = (1-hsv[2])*h;
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(4); paint.setColor(Color.BLACK); canvas.drawCircle(x,y,dp(getContext(),7),paint);
            paint.setStrokeWidth(2); paint.setColor(Color.WHITE); canvas.drawCircle(x,y,dp(getContext(),7),paint); paint.setStyle(Paint.Style.FILL);
        }
        public boolean onTouchEvent(android.view.MotionEvent event) {
            if (event.getActionMasked()==MotionEvent.ACTION_DOWN || event.getActionMasked()==MotionEvent.ACTION_MOVE) {
                getParent().requestDisallowInterceptTouchEvent(true);
                hsv[1]=Math.max(0,Math.min(1,event.getX()/Math.max(1,getWidth())));
                hsv[2]=1-Math.max(0,Math.min(1,event.getY()/Math.max(1,getHeight())));
                invalidate(); if(changed!=null) changed.run(); return true;
            }
            if(event.getActionMasked()==MotionEvent.ACTION_UP) { performClick(); return true; }
            return true;
        }
        public boolean performClick() { super.performClick(); return true; }
    }
}
