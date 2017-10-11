package bop.provalayout;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Adm on 26/06/2017.
 */


public class SpinnerAdapter extends ArrayAdapter<ListItem> {

    LayoutInflater inflater;
    ArrayList<ListItem> objects;
    ViewHolder holder = null;

    public SpinnerAdapter(Context context, int textViewResourceId, ArrayList<ListItem> objects) {
        super(context, textViewResourceId, objects);
        inflater = ((Activity) context).getLayoutInflater();
        this.objects = objects;
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
            holder.description = (TextView) row.findViewById(R.id.description);
            holder.imgThumb = (ImageView) row.findViewById(R.id.imgThumb);
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
