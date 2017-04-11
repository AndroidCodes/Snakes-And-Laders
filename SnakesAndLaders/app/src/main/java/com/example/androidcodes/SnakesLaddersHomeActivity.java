package com.example.androidcodes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SnakesLaddersHomeActivity extends Activity implements View.OnClickListener {

    private Bundle bundle = null;

    private Dialog number_of_players;

//    Header hdr;

    private Activity activity = SnakesLaddersHomeActivity.this;

    private FloatingActionButton fab_actions;

    private int change_background_according_focus = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snakesladders_home);

       /* hdr = (Header) findViewById(R.id.view);
        hdr.tv_temp.setOnClickListener(this);*/
        fab_actions = (FloatingActionButton) findViewById(R.id.fab_actions);
        findViewById(R.id.tv_play).setOnClickListener(this);
      /*  findViewById(R.id.main_layout).setBackground(new BitmapDrawable(Bitmap.createBitmap(fastblur(BitmapFactory.decodeResource(getResources(),
                R.drawable.snack_activity_background), 35))));*/
    }

    @Override
    public void onClick(View view) {

        Intent intent;

        switch (view.getId()) {

          /*  case R.id.tv_temp:

                Snackbar.make(view, "Header", Snackbar.LENGTH_SHORT).show();

                return;
*/
            case R.id.tv_play:

                change_background_according_focus = R.id.tv_play;

                number_of_players = new Dialog(activity);
                number_of_players.requestWindowFeature(Window.FEATURE_NO_TITLE);
                number_of_players.setContentView(R.layout.layout_play_option_dialog);
                number_of_players.setTitle("Select PLayers");
                number_of_players.setCancelable(true);

                number_of_players.findViewById(R.id.tv_singlePlayer).setOnClickListener(this);
                number_of_players.findViewById(R.id.tv_multiPlayer).setOnClickListener(this);

                number_of_players.show();

                return;

            case R.id.tv_singlePlayer:

                bundle = new Bundle();
                intent = new Intent(SnakesLaddersHomeActivity.this, SnakesLaddersActivity.class);
                bundle.putString(getString(R.string.singlePlayerKey), getString(R.string.yes));
                intent.putExtras(bundle);

                number_of_players.dismiss();

                startActivity(intent);

                return;

            case R.id.tv_multiPlayer:

                bundle = new Bundle();
                intent = new Intent(SnakesLaddersHomeActivity.this, SnakesLaddersActivity.class);
                bundle.putString(getString(R.string.singlePlayerKey), getString(R.string.no));
                intent.putExtras(bundle);

                number_of_players.dismiss();

                startActivity(intent);

                return;

            case R.id.fab_actions:

                Display scales_of_display = activity.getWindowManager().getDefaultDisplay();
                Point display_size = new Point();
                scales_of_display.getSize(display_size);

                LinearLayout ll_poppup = (LinearLayout) activity.findViewById(R.id.ll_popup);
                LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                View layout_popup_window = inflater.inflate(R.layout.pop_up_window, ll_poppup);

                int[] location = new int[2];
                fab_actions.getLocationOnScreen(location);
                Point fab_actions_location = new Point();
                fab_actions_location.x = location[0];
                fab_actions_location.y = location[1];

                PopupWindow pop_window = new PopupWindow();
                pop_window.setContentView(layout_popup_window);
                pop_window.setWidth(display_size.x);
                pop_window.setHeight((display_size.y) / 8);
                pop_window.setFocusable(true);
                pop_window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                pop_window.setOutsideTouchable(true);
                pop_window.showAtLocation(layout_popup_window, Gravity.NO_GRAVITY, fab_actions_location.x, fab_actions_location.y - (fab_actions.getHeight() * 2) / 3);
                pop_window.setAnimationStyle(R.anim.popup_animation);
                pop_window.setOnDismissListener(new PopupWindow.OnDismissListener() {

                    @Override
                    public void onDismiss() {

                    }
                });

                return;

            default:

                return;

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        TextView tv_focuss_off = (TextView) findViewById(R.id.tv_focus_off);

        if (!hasFocus) {
            if (change_background_according_focus == R.id.tv_play) {
                change_background_according_focus = 0;
                tv_focuss_off.setVisibility(View.VISIBLE);
            } else {
                tv_focuss_off.setVisibility(View.VISIBLE);
                tv_focuss_off.setBackgroundColor(Color.parseColor("#50000000"));
            }
        } else {
            tv_focuss_off.setVisibility(View.GONE);
        }
    }

    public Bitmap fastblur(Bitmap sentBitmap, int radius) {

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
}
