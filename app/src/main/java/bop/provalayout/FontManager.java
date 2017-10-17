package bop.provalayout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Adm on 14/09/2017.
 */

public class FontManager {

    static Global gbl = new Global();

    public static final String ROOT = "fonts/",
                        FONTAWESOME = ROOT + "fontawesome-webfont.ttf";

    public static Typeface getTypeface(Context context, String font) {
        try{
            return Typeface.createFromAsset(context.getAssets(), font);
        }
        catch(Exception e){
            gbl.myLog("FontManager --> Errore ["+e.toString()+"]");
            return null;
        }

    }

    public static void markAsIconContainer(View v, Typeface typeface, float textSize, final int textColor) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                markAsIconContainer(child, typeface,textSize,textColor);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(typeface);
            ((TextView) v).setTextSize(textSize);
            ((TextView) v).setTextColor(textColor);

           }
    }

    // Utile per il calcolo della larchezza dell'ACTIONLAYOUT del MENUITEM
    // Calcola la dimensione del menu contando i menuitem: quelli che ho marcato con ORDER > 100 popolano l'OVERFLOW --> non li conto
    // Se vi sono elementi nell'overflow --> devo aumentare di 1 la SIZE poichè devo tenere presente che c'è l'icona del MORE
    private static int getMenuSize(Menu menu){
        int size=0;
        boolean hasOverflow = false;
        for(int i=0;i<menu.size();i++){
            MenuItem mi = menu.getItem(i);
            if(mi.getOrder()<100)
                size++;
            else
                hasOverflow = true;

        }
        if (hasOverflow) size++;
        return size;

    }

    public static void manageMenuItem(Menu menu, int screen_width_px, final Context ctx, final Activity act) {
        try {
            int menu_size = getMenuSize(menu);
            final int icon_width = (int) Math.ceil((double) (screen_width_px / menu_size));
            Typeface tf = FontManager.getTypeface(ctx, FontManager.FONTAWESOME);

            //double margin = icon_width*0.4;
            //int text_size_px = icon_width - (int)margin;
            //int text_size_dp = (int) gbl.convertPixelsToDp(text_size_px,ctx);
            //if (text_size_px > 56 ) text_size_px = 56;

            //gbl.myLog("screen_width_px["+screen_width_px+"] menu_size["+menu_size+"] icon_width["+icon_width+"] margin["+margin+"] text_size_px["+text_size_px+"] text_size_dp["+text_size_dp+"]");

            for (int i = 0; i < menu_size; i++) {

                final MenuItem mi = menu.getItem(i);

                final RelativeLayout rl = (RelativeLayout) mi.getActionView();

                if (rl != null) {
                    rl.setMinimumWidth(icon_width);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(icon_width, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rl.setLayoutParams(lp);

                    final TextView tv = (TextView) rl.findViewById(R.id.tv_menu_item);

                    if (mi.getOrder() < 100) {
                        rl.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(View v) {
                                try {

                                    act.onOptionsItemSelected(mi);
                                    final int current_text_color = tv.getCurrentTextColor();
                                    tv.setTextColor(ContextCompat.getColor(ctx,R.color.mi_icon_selected));

                                    new CountDownTimer(500, 100) {
                                        public void onTick(long millisUntilFinished) {
                                        }

                                        public void onFinish() {
                                            tv.setTextColor(current_text_color);
                                            //rl.setBackgroundColor(Color.TRANSPARENT);
                                        }

                                    }.start();
                                } catch (Exception e) {
                                    gbl.myLog("Errore FontManager --> onClick [" + e.toString() + "]");
                                }
                            }
                        });
                    }
                    tv.setText(mi.getTitleCondensed());

                    //tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,text_size_dp);
                    //tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,text_size_px);

                    tv.setTextColor(Color.BLACK);
                    tv.setTypeface(tf);


                }

            }
        }
        catch(Exception e){gbl.myLog("Errore in manageMenuItem["+e.toString()+"]");}
    }

}
