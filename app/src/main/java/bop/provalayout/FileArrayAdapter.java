package bop.provalayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class FileArrayAdapter extends ArrayAdapter<Item>{

	private Context c;
	private int id;
	private List<Item>items;
	private Typeface iconFont;

	public FileArrayAdapter(Context context, int textViewResourceId, List<Item> objects) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
		iconFont = FontManager.getTypeface(c, FontManager.FONTAWESOME);
	}
	public Item getItem(int i)
	 {
		 return items.get(i);
	 }
	 @Override
       public View getView(int position, View convertView, ViewGroup parent) {
               View v = convertView;
               if (v == null) {
                   LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                   v = vi.inflate(id, null);
               }

               final Item o = items.get(position);

               if (o != null) {
                       TextView tv_filename = (TextView) v.findViewById(R.id.tv_filename);
                       TextView tv_info = (TextView) v.findViewById(R.id.tv_info);
                       TextView tv_date = (TextView) v.findViewById(R.id.tv_date);
				       TextView tv_file_icon = (TextView) v.findViewById(R.id.tv_file_icon);
				   		tv_file_icon.setText(o.getFileImage());
				   		FontManager.markAsIconContainer(tv_file_icon, iconFont,30, Color.BLACK);
                       
                       if(tv_filename!=null)
                       	tv_filename.setText(o.getName());
                       if(tv_info!=null)
                          	tv_info.setText(o.getData());
                       if(tv_date!=null)
                          	tv_date.setText(o.getDate());
                       
               }
               return v;
       }

}
