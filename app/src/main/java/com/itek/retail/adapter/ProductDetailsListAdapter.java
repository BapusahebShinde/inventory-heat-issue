package com.itek.retail.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.databinding.ListDetailsProductBinding;
import com.itek.retail.model.LabelValues;

import java.util.List;

public class ProductDetailsListAdapter extends RecyclerView.Adapter<ProductDetailsListAdapter.MyViewHolder> {

    private final List<List<LabelValues>> listDetails;
    private final Context context;

    public ProductDetailsListAdapter(@NonNull Context context, @NonNull List<List<LabelValues>> listDetails) {
        this.context = context;
        this.listDetails = listDetails;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListDetailsProductBinding binding = ListDetailsProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        List<LabelValues> listLblVals = listDetails.get(position);
        setupLayout(holder.llVals,listLblVals);

//        holder.productName.setText(item.getLabel());
//        holder.productValue.setText(item.getValue());
    }

    private void setupLayout(final LinearLayout ll,final List<LabelValues> list){
        ll.removeAllViews();
        int margin = context.getResources().getDimensionPixelSize(R.dimen.dp_5);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //llParams.setMargins(margin,margin,margin,margin);
        ll.setBackgroundResource(R.drawable.border_top_right);
        for(LabelValues lv:list) {
            TextView txtVal = new TextView(context);
            txtVal.setTextAppearance(context, R.style.TextStyleSubHeader);
            txtVal.setBackgroundResource(R.drawable.border_bottom);
            txtVal.setText(lv.getValue());
            txtVal.setPadding(margin,margin,margin,margin);
            txtVal.setLayoutParams(llParams);
            txtVal.setMaxLines(1);
            txtVal.setSingleLine(true);
            txtVal.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            txtVal.setMarqueeRepeatLimit(-1);
            txtVal.setSelected(true);
            ll.addView(txtVal);
        }
    }

    @Override
    public int getItemCount() {
        return listDetails != null ? listDetails.size() : 0;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llVals;
//        TextView productName;
//        TextView productValue;

        MyViewHolder(ListDetailsProductBinding binding) {
            super(binding.getRoot());
            llVals = binding.llProductVals;
//            productName = binding.tvLabel;
//            productValue = binding.tvValue;
//
//            productName.setSelected(true);
//            productValue.setSelected(true);
        }
    }
}
