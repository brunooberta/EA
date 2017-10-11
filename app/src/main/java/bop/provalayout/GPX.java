package bop.provalayout;

import android.location.Location;

import java.util.List;

/**
 * Created by Adm on 19/05/2017.
 */

public class GPX {

    public List<Location> m_location_list;
    public List<WayPointType> m_wayPoint_list;
    private boolean isDataOk = true;

    public boolean isDataOk() {return isDataOk;}
    public void setDataOk(boolean dataOk) {isDataOk = dataOk;}


    public GPX() {
    }

    public GPX(List<Location> location_list, List<WayPointType> wayPoint_list) {
        m_location_list = location_list;
        m_wayPoint_list = wayPoint_list;
    }
}
