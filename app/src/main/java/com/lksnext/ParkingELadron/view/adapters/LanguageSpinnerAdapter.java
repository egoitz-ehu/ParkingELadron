package com.lksnext.ParkingELadron.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.domain.LanguageItem;

import java.util.List;

public class LanguageSpinnerAdapter extends ArrayAdapter<LanguageItem> {
    public LanguageSpinnerAdapter(Context context, List<LanguageItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_language_spinner, parent, false);
        }
        LanguageItem item = getItem(position);
        ImageView imgFlag = convertView.findViewById(R.id.imgFlag);
        TextView tvLanguage = convertView.findViewById(R.id.tvLanguage);

        imgFlag.setImageResource(item.getFlagResId());
        tvLanguage.setText(item.getName());
        return convertView;
    }
}
