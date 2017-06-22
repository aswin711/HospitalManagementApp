package com.synnefx.cqms.event.ui.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.ui.MainActivity;

/**
 * Created by cedex on 5/17/2017.
 */

public class NavigationDrawerListAdapter extends BaseAdapter {

    String[] menus;
    int[] icons;
    Context context;
    private static LayoutInflater inflater =null;

    private ItemClickListener itemClickListener=null;

    public NavigationDrawerListAdapter(String[] menus, int[] icons, MainActivity mainActivity,ItemClickListener itemClickListener) {
        this.menus = menus;
        this.icons = icons;
        this.context = mainActivity;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getCount() {
        return menus.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public class Holder{
        TextView menu;
        ImageView icon;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
            rowView = inflater.inflate(R.layout.list_navigation_drawer,null);
            holder.menu = (TextView) rowView.findViewById(R.id.list_naviagation_drawer_menu);
            holder.icon = (ImageView) rowView.findViewById(R.id.list_naviagation_drawer_icon);
        holder.menu.setText(menus[position]);
        Picasso.with(context).load(icons[position]).into(holder.icon);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onClick(position);
                //Toast.makeText(context, ""+position, Toast.LENGTH_SHORT).show();
            }
        });

        return rowView;
    }

    public interface ItemClickListener{
        void onClick(int position);
    }
}
