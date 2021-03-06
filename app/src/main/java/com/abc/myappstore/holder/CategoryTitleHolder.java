package com.abc.myappstore.holder;

import android.view.View;
import android.widget.TextView;

import com.abc.myappstore.base.BaseHolder;
import com.abc.myappstore.bean.CategoryInfoBean;
import com.abc.myappstore.utils.UIUtils;

/**
 * 创建者     Chris
 * 创建时间   2016/7/11 08:50
 * 描述	      ${TODO}
 *
 */
public class CategoryTitleHolder extends BaseHolder<CategoryInfoBean> {

    private TextView mTitleTv;

    /**
     * 决定holder所能提供的视图,以及找出孩子
     *
     * @return
     */
    @Override
    public View initHolderViewAndFindViews() {
        mTitleTv = new TextView(UIUtils.getContext());
        int padding = UIUtils.dip2Px(5);
        mTitleTv.setPadding(padding, padding, padding, padding);
        return mTitleTv;
    }

    /**
     * 视图和数据的绑定过程
     *
     * @param data
     */
    @Override
    public void refreshHolderView(CategoryInfoBean data) {
        mTitleTv.setText(data.title);
    }
}
