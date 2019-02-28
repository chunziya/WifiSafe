package com.administrator.wifisafe.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.administrator.wifisafe.R;
import com.administrator.wifisafe.bean.CheckBean;

import java.util.List;

/**
 * @author lesences  2018/5/26
 */
public class WifiCheckAdapter extends RecyclerView.Adapter<WifiCheckAdapter.CheckViewHolder> {
    private final LayoutInflater inflater;
    private final Context context;
    private final List<CheckBean> checkList;

    public WifiCheckAdapter(@NonNull Context context, @NonNull List<CheckBean> checkList) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.checkList = checkList;
    }

    @NonNull
    @Override
    public CheckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_check_layout, parent, false);
        return new CheckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckViewHolder holder, int position) {
        CheckBean checkBean = checkList.get(position);
        if (null == checkBean) {
            return;
        }
        holder.actvOption.setText(checkBean.getOptionStr());
        CheckBean.CheckState checkState = checkBean.getCheckState();
        Drawable drawable;
        int textColor;
        int textId;
        switch (checkState) {
            case SAFE:
                textId = R.string.wifi_security_safe;
                textColor = R.color.welcome_text_color;
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_safe);
                break;
            case WARN:
                textId = R.string.wifi_check_warn;
                textColor = R.color.default_color;
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_warn);
                break;
            case UNSAFE:
                textId = R.string.wifi_security_unsafe;
                textColor = R.color.unsafe_color;
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_unsafe);
                break;
            default:
            case CHECKING:
                textId = R.string.wifi_security_checking;
                textColor = R.color.welcome_text_color;
                drawable = getProgressDrawable(context);
                break;
        }
        holder.actvState.setTextColor(ContextCompat.getColor(context, textColor));
        holder.actvState.setText(textId);
        holder.acivState.setImageDrawable(drawable);
    }

    private Drawable getProgressDrawable(Context context) {
        CircularProgressDrawable progressDrawable = new CircularProgressDrawable(context);
        progressDrawable.start();
        return progressDrawable;
    }

    @Override
    public int getItemCount() {
        return checkList.size();
    }

    static class CheckViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView acivState;
        AppCompatTextView actvOption;
        AppCompatTextView actvState;


        CheckViewHolder(View itemView) {
            super(itemView);
            acivState = itemView.findViewById(R.id.aciv_state);
            actvOption = itemView.findViewById(R.id.actv_option);
            actvState = itemView.findViewById(R.id.actv_state);
        }
    }
}
