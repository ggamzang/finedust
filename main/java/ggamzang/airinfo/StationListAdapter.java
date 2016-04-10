package ggamzang.airinfo;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by user on 2016-01-06.
 */
public class StationListAdapter extends BaseAdapter{
    Context mContext                = null;
    ArrayList<StationInfo> mData    = null;
    LayoutInflater mLayoutInflater  = null;

    public StationListAdapter(Context mContext, ArrayList<StationInfo> mData) {
        this.mContext = mContext;
        this.mData = mData;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get( position );
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemLayout = mLayoutInflater.inflate(R.layout.list_view_item_station_layout, null);
        TextView tvStationName = (TextView)itemLayout.findViewById(R.id.tvStationName);
        TextView tvAddr = (TextView)itemLayout.findViewById(R.id.tvAddr);

        tvStationName.setText( mData.get( position ).getStationName() );
        tvAddr.setText( mData.get( position ).getAddr() );
        return itemLayout;
    }
}
