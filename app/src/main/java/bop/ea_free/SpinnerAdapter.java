package bop.ea_free;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Adm on 26/06/2017.
 */


public class SpinnerAdapter extends ArrayAdapter<ListItem> {

    LayoutInflater inflater;
    ArrayList<ListItem> objects;
    ViewHolder holder = null;
    Context ctx = null;
    boolean is_wp_details = false;

    public SpinnerAdapter(Context context, int textViewResourceId, ArrayList<ListItem> objects,boolean is_wp_details) {
        super(context, textViewResourceId, objects);
        ctx = context;
        inflater = ((Activity) context).getLayoutInflater();
        this.objects = objects;
        this.is_wp_details = is_wp_details;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        ListItem listItem = objects.get(position);
        View row = convertView;


        if (null == row) {
            holder = new ViewHolder();
            row = inflater.inflate(R.layout.spinner_row_layout, parent, false);
            LinearLayout ll = (LinearLayout) row.findViewById(R.id.lay_spinner_row);
            holder.description = (TextView) row.findViewById(R.id.description);
            holder.imgThumb = (ImageView) row.findViewById(R.id.imgThumb);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(is_wp_details) {
                    ll.setBackgroundColor(ctx.getColor(R.color.share_style_back_color));
                    holder.description.setTextColor(ctx.getColor(R.color.black));
                }
            }


            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.description.setText(listItem.description);
        holder.imgThumb.setImageResource(listItem.imgThumb);

        return row;
    }

    static class ViewHolder {
        TextView description;
        ImageView imgThumb;
    }

}
