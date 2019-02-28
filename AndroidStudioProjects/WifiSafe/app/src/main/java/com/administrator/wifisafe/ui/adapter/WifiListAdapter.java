package com.administrator.wifisafe.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.administrator.wifisafe.R;
import com.administrator.wifisafe.bean.WifiBean;

import java.util.List;

/**
 * @author lesences  2018/5/26
 */
public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.WifiViewHolder> {
    private final List<WifiBean> wifiBeanList;
    private final LayoutInflater inflater;
    private final Context context;

    public WifiListAdapter(@NonNull Context context, @NonNull List<WifiBean> wifiBeanList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.wifiBeanList = wifiBeanList;
    }


    @NonNull
    @Override
    public WifiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_wifi_layout, parent, false);
        return new WifiViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull WifiViewHolder holder, int position) {
        WifiBean wifiBean = wifiBeanList.get(position);
        if (null == wifiBean) {
            return;
        }
        holder.acivLevel.setImageResource(wifiBean.getIconId());
        holder.actvSsid.setText(wifiBean.getSSID());
        String capaStr;
        if (wifiBean.isConnect()) {
            capaStr = context.getString(R.string.connected);
        } else {
            capaStr = wifiBean.getCapabilities();
            if (TextUtils.isEmpty(capaStr)) {
                capaStr = context.getString(R.string.open_wifi);
            } else {
                capaStr = context.getString(R.string.capa_wifi, capaStr);
            }
        }
        holder.actvCapa.setText(capaStr);
    }

    @Override
    public int getItemCount() {
        return wifiBeanList.size();
    }

    static class WifiViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView acivLevel;
        AppCompatTextView actvSsid;
        AppCompatTextView actvCapa;

        WifiViewHolder(View itemView) {
            super(itemView);
            acivLevel = itemView.findViewById(R.id.aciv_level);
            actvSsid = itemView.findViewById(R.id.actv_ssid);
            actvCapa = itemView.findViewById(R.id.actv_capa);
        }
    }
}
