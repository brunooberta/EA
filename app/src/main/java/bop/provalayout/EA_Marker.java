package bop.provalayout;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


/**
 * Created by Adm on 12/10/2017.
 */

public class EA_Marker extends Marker {

    private int m_id = -1;

    public EA_Marker(MapView mapView, int id) {
        super(mapView);
        m_id = id;
    }

    public int getId() {
        return m_id;
    }

    public void setId(int m_id) {
        this.m_id = m_id;
    }
}
