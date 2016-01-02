package cn457.keylessentry;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by synycboom on 12/29/2015 AD.
 */
public class CustomKeyListAdapter extends BaseAdapter {
    private static int checkedId;

    private ViewHolder mViewHolder;
    private LayoutInflater mInflater;
    private List<Key> mKeys;
    private Key mKey;
    private boolean isLocal;
    private static Set<Integer> checkedPos;


    public CustomKeyListAdapter(Activity activity, List<Key> mKeys, boolean isLocal) {
        mInflater = (LayoutInflater) activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        this.mKeys = mKeys;
        checkedPos = new HashSet<Integer>();
        this.isLocal = isLocal;
    }


    @Override
    public int getCount() {
        return mKeys.size();
    }

    @Override
    public Object getItem(int position) {
        return mKeys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.keys, parent, false);
            mViewHolder = new ViewHolder();
            mViewHolder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.checked_text_key);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mKey = mKeys.get(position);

        if(isLocal) mViewHolder.checkedTextView.setText( mKey.getName() + " : " + mKey.getKey());
        else mViewHolder.checkedTextView.setText(mKey.getKey());

        mViewHolder.checkedTextView.setChecked(mKey.getIsChecked());
        //Note that clickable have to be false otherwise onItemClick in SearchingActivity will not work !!!
        mViewHolder.checkedTextView.setClickable(false);

        return convertView;
    }

    public boolean getIsChecked(int position){
        return ((Key) getItem(position)).getIsChecked();
    }

    public Set<Integer> getCheckedPosition(){
        return checkedPos;
    }

    public void setChecked(int position){
        if( ((Key) getItem(position)).getIsChecked() ){
            checkedPos.remove(position);
            ((Key) getItem(position)).setChecked(false);
            return;
        }
        checkedPos.add(position);
        ((Key) getItem(position)).setChecked(true);
    }

    private static class ViewHolder {
        CheckedTextView checkedTextView;
    }
}
