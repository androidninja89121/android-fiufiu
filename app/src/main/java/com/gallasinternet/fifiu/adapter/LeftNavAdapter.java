package com.gallasinternet.fifiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gallasinternet.fifiu.R;
import com.gallasinternet.fifiu.model.MenuInfo;

import java.util.ArrayList;

/**
 * Created by Administrator on 8/19/2015.
 */
public class LeftNavAdapter extends BaseAdapter {

    private ArrayList<MenuInfo> items;
    private Context context;

    public LeftNavAdapter(Context context, ArrayList<MenuInfo> items){
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public MenuInfo getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MenuInfo f = getItem(i);
        view = LayoutInflater.from(context).inflate(R.layout.left_nav_item, null);

        ImageView imgvIcon = (ImageView)view.findViewById(R.id.imgvLeftNavItemIcon);
        TextView tvTitle = (TextView)view.findViewById(R.id.tvLeftNavItemTitle);
        RelativeLayout rlyNotify = (RelativeLayout)view.findViewById(R.id.rlyNavItemNotify);
        TextView tvNumber = (TextView)view.findViewById(R.id.tvLeftNavItemNumber);

        imgvIcon.setImageResource(f.getImage());
        tvTitle.setText(f.getTitle());

        if(f.getNotifys() == -1){
            rlyNotify.setVisibility(View.INVISIBLE);
        }else{
            rlyNotify.setVisibility(View.VISIBLE);
            tvNumber.setText(f.getNotifys() + "");
        }

        return view;
    }
}
