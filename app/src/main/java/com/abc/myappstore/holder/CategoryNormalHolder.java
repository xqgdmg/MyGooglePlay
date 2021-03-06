package com.abc.myappstore.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abc.myappstore.R;
import com.abc.myappstore.base.BaseHolder;
import com.abc.myappstore.bean.CategoryInfoBean;
import com.abc.myappstore.conf.Constants;
import com.abc.myappstore.utils.UIUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 创建者     Chris
 * 创建时间   2016/7/11 08:50
 * 描述	      ${TODO}
 *
 */
public class CategoryNormalHolder extends BaseHolder<CategoryInfoBean> {


    @InjectView(R.id.item_category_icon_1)
    ImageView    mItemCategoryIcon1;
    @InjectView(R.id.item_category_name_1)
    TextView     mItemCategoryName1;
    @InjectView(R.id.item_category_item_1)
    LinearLayout mItemCategoryItem1;
    @InjectView(R.id.item_category_icon_2)
    ImageView    mItemCategoryIcon2;
    @InjectView(R.id.item_category_name_2)
    TextView     mItemCategoryName2;
    @InjectView(R.id.item_category_item_2)
    LinearLayout mItemCategoryItem2;
    @InjectView(R.id.item_category_icon_3)
    ImageView    mItemCategoryIcon3;
    @InjectView(R.id.item_category_name_3)
    TextView     mItemCategoryName3;
    @InjectView(R.id.item_category_item_3)
    LinearLayout mItemCategoryItem3;

    @Override
    public View initHolderViewAndFindViews() {
        View holderView = View.inflate(UIUtils.getContext(), R.layout.item_category_normal, null);
        //找孩子
        ButterKnife.inject(this, holderView);
        return holderView;
    }

    @Override
    public void refreshHolderView(CategoryInfoBean data) {
        setGridData(data.name1, data.url1, mItemCategoryName1, mItemCategoryIcon1);
        setGridData(data.name2, data.url2, mItemCategoryName2, mItemCategoryIcon2);
        setGridData(data.name3, data.url3, mItemCategoryName3, mItemCategoryIcon3);
    }

    public void setGridData(final String name, String url, TextView tvName, ImageView ivIcon) {
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(url)) {
            ViewParent parent = tvName.getParent();
            ((ViewGroup)parent).setVisibility(View.INVISIBLE);

        } else {
            tvName.setText(name);
            ImageLoader.getInstance().displayImage(Constants.URLS.IMGBASEURL + url, ivIcon);

            ViewParent parent = tvName.getParent();
            ((ViewGroup)parent).setVisibility(View.VISIBLE);

            //设置每一个格子的点击事件
            ((ViewGroup)parent).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(UIUtils.getContext(), name, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
