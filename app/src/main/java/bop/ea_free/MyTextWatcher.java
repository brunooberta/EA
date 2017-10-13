package bop.ea_free;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/**
 * Created by Adm on 20/09/2017.
 */

public class MyTextWatcher implements TextWatcher {

    private TextView mTv;
    private TextInputLayout mTil;
    private Context mCtx = null;

    private boolean isChanged = false;

    public MyTextWatcher(TextView tv, TextInputLayout til, Context ctx) {

        mCtx = ctx;
        mTv = tv;
        mTil = til;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if(mTv.getText().length()>0)
            mTil.setError(null);
        else
            mTil.setError( mCtx.getString(R.string.err_fill_this_field));

    }

    @Override
    public void afterTextChanged(Editable s) {

        isChanged = true;
    }

    public boolean isChanged() {
        return isChanged;
    }
}
