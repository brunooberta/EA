package bop.provalayout;

/**
 * Created by Adm on 19/05/2017.
 */

public class WayPointType {

    public String mLat="", mLon="", mEle="",mName="",mCmt="",mDesc="",mSym="", mId="";

    public WayPointType() {
    }
    public WayPointType(String lat, String lon, String ele, String name, String cmt, String desc, String sym) {

        mLat=lat;
        mLon=lon;
        mEle=ele;
        mName=name;
        mCmt=cmt;
        mDesc=desc;
        mSym=sym;
    }

    public WayPointType(String id, String lat, String lon, String ele, String name, String cmt, String desc, String sym) {
        mId= id;
        mLat=lat;
        mLon=lon;
        mEle=ele;
        mName=name;
        mCmt=cmt;
        mDesc=desc;
        mSym=sym;
    }
}
