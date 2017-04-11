package com.example.androidcodes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by hp on 1/18/2016.
 */
public class Header extends LinearLayout {

    TextView tv_temp;

    public Header(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflter.inflate(R.layout.layout_header, this);

        tv_temp = (TextView) findViewById(R.id.tv_temp);
    }
}
