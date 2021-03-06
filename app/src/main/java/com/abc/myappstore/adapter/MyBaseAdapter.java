package com.abc.myappstore.adapter;

import android.widget.BaseAdapter;

import java.util.List;

/**
 * 创建者     Chris
 * 创建时间   2016/7/6 15:34
 * 描述	     针对BaseAdapter中3个方法进行封装
 *
 */
public abstract class MyBaseAdapter<ITEMBEANTYPE> extends BaseAdapter {
    public List<ITEMBEANTYPE> mDataSource = null;

    public MyBaseAdapter(List<ITEMBEANTYPE> dataSource) {
        mDataSource = dataSource;
    }

    @Override
    public int getCount() {
        if (mDataSource != null) {
            return mDataSource.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mDataSource != null) {
            return mDataSource.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
