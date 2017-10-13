package bop.ea_free;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;

import java.util.regex.Pattern;

/**
 * Created by Adm on 12/06/2017.
 */

public class My_EditText extends android.support.v7.widget.AppCompatEditText {

    private String _mode;

    public My_EditText(Context context, String defaultText, String mode, int maxLength, final android.support.v7.app.AlertDialog dlg  ) {
        super(context);
        switch (mode) {
            case "NUMERIC":
                this.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;

            case "ALPHANUMERIC":
                this.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }

        this.setText(defaultText);

        setFilter(maxLength, mode);

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    // Check if edittext is empty
                    if (TextUtils.isEmpty(s)) {
                        dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    }
                }catch(Exception e){

                    Log.w("MY_CHECK","ERRORE in addTextChangedListener["+ e.toString() +"]");
                }
            }
        });
    }

    public My_EditText(Context context, String mode, int maxLength ) {
        super(context);

        switch (mode) {
            case "NUMERIC":
                this.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;

            case "ALPHANUMERIC":
                this.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }

        setFilter(maxLength, mode);

    }


    // set the max number of digits the user can enter
    private void setFilter(int length, String mode) {
        Pattern pattern=null;

        switch (mode) {
            case "NUMERIC":
                pattern=Pattern.compile("[0-9]{0," + "}+((\\.[0-9]{0," + (9) + "})?)||(\\.)?");
                break;

            case "ALPHANUMERIC":
                pattern = Pattern.compile("[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_-[ ]]*");

                break;
        }

        final Pattern finalPattern = pattern;
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; ++i)
                {

                    if (!finalPattern.matcher(String.valueOf(source.charAt(i))).matches())
                    {
                        return "";
                    }
                }

                return null;
            }
        };

        this.setFilters(new InputFilter[]{filter,new InputFilter.LengthFilter(length)});

    }


}
