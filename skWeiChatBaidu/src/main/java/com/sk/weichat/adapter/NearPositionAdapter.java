package com.sk.weichat.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.map.MapHelper;
import com.sk.weichat.util.SkinUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearPositionAdapter extends RecyclerView.Adapter<NearPositionAdapter.NearPositionViewHodler> {
    List<MapHelper.Place> places = new ArrayList<>();
    Map<String, MapHelper.Place> placesCheck = new HashMap<>(1);

    private OnRecyclerItemClickListener monItemClickListener;
    private Context mContext;

    public NearPositionAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<MapHelper.Place> placesList) {
        places.clear();
        this.places = placesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NearPositionViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.near_position_adapter, parent, false);
        return new NearPositionViewHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NearPositionViewHodler holder, int position) {
        MapHelper.Place placePosition = places.get(position);
        if (placePosition != null) {
            holder.map_name_tv.setText(placePosition.getName());
            holder.map_dateils_tv.setText(placePosition.getAddress());
            holder.cb_position.setVisibility(View.GONE);
            holder.cb_position.setChecked(false);
            holder.cb_position.setButtonDrawable(null);
            ColorStateList tabColor = SkinUtils.getSkin(mContext).getTabColorState();
            if (placesCheck.size() != 0) {
                if (placesCheck.containsKey(places.get(position).getName())) {
                    holder.cb_position.setChecked(true);
                    holder.cb_position.setVisibility(View.VISIBLE);
                    Drawable drawable = mContext.getResources().getDrawable(R.drawable.sel_check_wx2);
                    drawable = DrawableCompat.wrap(drawable);
                    DrawableCompat.setTintList(drawable, tabColor);
                    holder.cb_position.setButtonDrawable(drawable);
                }
            } else {
                if (position == 0) {// 以上条件不满足，默认选中第一个
                    holder.cb_position.setChecked(true);
                    holder.cb_position.setVisibility(View.VISIBLE);
                    Drawable drawable = mContext.getResources().getDrawable(R.drawable.sel_check_wx2);
                    drawable = DrawableCompat.wrap(drawable);
                    DrawableCompat.setTintList(drawable, tabColor);
                    holder.cb_position.setButtonDrawable(drawable);
                }
            }

            holder.item_ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    placesCheck.clear();
                    if (monItemClickListener != null) {
                        placesCheck.put(placePosition.getName(), placePosition);
                        notifyDataSetChanged();
                        monItemClickListener.onItemClick(position, placePosition);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setRecyclerItemClickListener(OnRecyclerItemClickListener listener) {
        monItemClickListener = listener;
    }

    public interface OnRecyclerItemClickListener {
        //RecyclerView的点击事件，将信息回调给view
        void onItemClick(int Position, MapHelper.Place dataBeanList);
    }

    class NearPositionViewHodler extends RecyclerView.ViewHolder {
        RelativeLayout item_ll;
        TextView map_name_tv;
        TextView map_dateils_tv;
        CheckBox cb_position;

        NearPositionViewHodler(@NonNull View itemView) {
            super(itemView);
            item_ll = itemView.findViewById(R.id.item_ll);
            map_dateils_tv = itemView.findViewById(R.id.map_dateils_tv);
            map_name_tv = itemView.findViewById(R.id.map_name_tv);
            cb_position = itemView.findViewById(R.id.cb_position);
        }
    }
}
