package cn457.keylessentry;

/**
 * Created by synycboom on 12/27/2015 AD.
 */
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.List;

public class CustomDeviceListAdapter extends BaseAdapter {
    private static int checkedId;

    private ViewHolder mViewHolder;
    private LayoutInflater mInflater;
    private List<Device> mDevices;
    private Device mDevice;

    public CustomDeviceListAdapter(Activity activity, List<Device> mDevices) {
        mInflater = (LayoutInflater) activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        this.mDevices = mDevices;
    }


    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.device_name, parent, false);
            mViewHolder = new ViewHolder();
            mViewHolder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.checked_text_device);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mDevice = mDevices.get(position);
        mViewHolder.checkedTextView.setText(mDevice.getDeviceName());
        mViewHolder.checkedTextView.setChecked(mDevice.getIsChecked());
        //Note that clickable have to be false otherwise onItemClick in SearchingActivity will not work !!!
        mViewHolder.checkedTextView.setClickable(false);

        return convertView;
    }

    public boolean getIsChecked(int position){
        return ((Device) getItem(position)).getIsChecked();
    }

    public int getCheckedPosition(){
        int pos = 0;
        for(Device dev: mDevices){
            if(dev.getIsChecked())
                return pos;
            pos++;
        }
        return -1;
    }

    public void setChecked(int position){
        for(int i = 0;i < mDevices.size(); i++)
            if(i == position)
                ((Device) getItem(i)).setChecked(true);
            else
                ((Device) getItem(i)).setChecked(false);
    }

    private static class ViewHolder {
        CheckedTextView checkedTextView;
    }
}