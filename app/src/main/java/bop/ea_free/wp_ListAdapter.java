package bop.ea_free;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class wp_ListAdapter extends BaseAdapter {

    private Context mContext = null;
    private ArrayList<WayPointType> mLstItemArray = new ArrayList<>();

    public ArrayList<Integer> getItemsCheckedArray() {
        return mItemsCheckedArray;
    }

    private ArrayList<Integer> mItemsCheckedArray = new ArrayList<>(); // contiene la lista degli indici degli elementi selezionati

    public wp_ListAdapter(ArrayList<WayPointType> lstItemArray, Context context){
        mContext = context;
        mLstItemArray = lstItemArray;
    }

    @Override
    public int getCount() {
        return mLstItemArray.size();
    }

    @Override
    public Object getItem(int position) {
        return mLstItemArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.wp_list_row_layout, parent, false);

            WayPointType lstItem = mLstItemArray.get(position);

            TextView tv_wp_name = (TextView) convertView.findViewById(R.id.tv_wp_name);
            ImageView img_wp_image = (ImageView) convertView.findViewById(R.id.img_wp_image);

            tv_wp_name.setFocusable(false);
            tv_wp_name.setFocusableInTouchMode(false);
            img_wp_image.setFocusable(false);
            img_wp_image.setFocusableInTouchMode(false);

            setBackground(convertView,position);

            // Gestione degli item selezionati
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean isSelected = manageSelection(position);

                    LinearLayout mainLayout = (LinearLayout)v.findViewById(R.id.wplist_mainlayout);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if(isSelected)
                            mainLayout.setBackground(mContext.getDrawable(R.drawable.list_bg_selected));
                        else
                            mainLayout.setBackground(mContext.getDrawable(R.drawable.list_bg_normal));
                    }
                }
            });

            tv_wp_name.setText(lstItem.mName);
            // Se non trovo l'icona giusta tra quelle disponibili --> metto quella di default
            try{
                img_wp_image.setImageResource(Integer.parseInt(lstItem.mSym));
            }
            catch(Exception ex){
                Log.w("MY_CHECK", "Errore in img_wp_image error[" + ex.toString() + "]");
                img_wp_image.setImageResource(R.mipmap.flag_map_marker_blue);
            }

            return convertView;
        }catch (Exception e){
            Log.w("MY_CHECK", "Errore in getView ["+e.toString()+"]");
            return convertView;
        }
    }

    // Se l'item index è selezionato --> lo deseleziono e ritorno FALSE, altrimenti lo selezione torno TRUE
    private boolean manageSelection(int index){
        try{
            for(int i=0;i<mItemsCheckedArray.size();i++){

                if(mItemsCheckedArray.get(i) == index){
                    mItemsCheckedArray.remove(i);
                    return false;
                }
            }

            mItemsCheckedArray.add(index);
            return true;

        }
        catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in manageSelection [" + e.toString() + "]");
            return false;
        }
    }

    private void setBackground(View v, int position){
        LinearLayout mainLayout = (LinearLayout)v.findViewById(R.id.wplist_mainlayout);
        for(int i=0;i<mItemsCheckedArray.size();i++){

            if(mItemsCheckedArray.get(i) == position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mainLayout.setBackground(mContext.getDrawable(R.drawable.list_bg_selected));
                }
            }
        }
    }
}
