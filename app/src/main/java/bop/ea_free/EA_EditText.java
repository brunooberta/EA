package bop.ea_free;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Adm on 02/10/2017.
 */

public class EA_EditText extends android.support.v7.widget.AppCompatEditText {

    private Global gbl = new Global();

    private String ic_color = "DARK";

    public EA_EditText(Context context) {
        super(context);
    }

    public EA_EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EA_EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void set_ic_color(String ic_color) {
        this.ic_color = ic_color;

    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if(focused)
            if(ic_color.equals("DARK"))
                this.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dialog_close_light, 0);
            else
                this.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dialog_close_dark, 0);
        else
            this.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int DRAWABLE_RIGHT = 2;
        if(this.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                int leftEdgeOfRightDrawable = this.getRight() - this.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                if (event.getRawX() >= leftEdgeOfRightDrawable) {
                    this.setText("");
                    return super.onTouchEvent(event);
                }
            }
        }
        return super.onTouchEvent(event);

    }




}
