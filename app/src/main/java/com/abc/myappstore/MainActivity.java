package com.abc.myappstore;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStripExtends;
import com.abc.myappstore.base.BaseFragment;
import com.abc.myappstore.factory.FragmentFactory;
import com.abc.myappstore.holder.MenuHolder;
import com.abc.myappstore.utils.LogUtils;
import com.abc.myappstore.utils.UIUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 创建者     Chris
 * 创建时间   2016/7/5 15:56
 * 描述	      ${TODO}$
 *
 */
public class MainActivity extends AppCompatActivity {


    @InjectView(R.id.main_tabs)
    PagerSlidingTabStripExtends mMainTabs;

    @InjectView(R.id.main_viewpager)
    ViewPager mMainViewpager;

    @InjectView(R.id.main_drawerlayout)
    DrawerLayout mMainDrawerlayout;
    @InjectView(R.id.main_left_menu)
    FrameLayout  mMainLeftMenu;

    private DrawerLayout          mDrawerLayout;
    private ActionBarDrawerToggle mTogglev7;
    private String[]              mMainTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initView();
        initActionBar();
        initActionBarDrawerToggle();
        initData();//创建adapter(FragmentStatePagerAdapter)-->setAdapter==>创建HomeFragment以及AppFragment
        initListener();
    }

    private void initListener() {
        final MyOnpageChangeListener mMyOnpageChangeListener = new MyOnpageChangeListener();
        mMainTabs.setOnPageChangeListener(mMyOnpageChangeListener);

        //监听ViewPager布局完成
        mMainViewpager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //手动选中第一页
                mMyOnpageChangeListener.onPageSelected(0);
                mMainViewpager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    class MyOnpageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            //触发加载数据
            //FragmentFactory.mCacheFragmentMap.get(position)-->BaseFragment-->loadingPager
            BaseFragment baseFragment = FragmentFactory.mCacheFragmentMap.get(position);
            baseFragment.mLoadingPager.triggerLoadData();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }


    private void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);

        //往mMainLeftMenu菜单里面添加一些内容
        MenuHolder menuHolder = new MenuHolder();
        mMainLeftMenu.addView(menuHolder.mHolderView);
    }


    private void initActionBar() {
        //1.得到actionbar的对象
        ActionBar actionBar = getSupportActionBar();

        //设置标题
        actionBar.setTitle("GooglePlay");
        //        actionBar.setSubtitle("副标题");

        //设置图标,默认显示的是icon
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setLogo(R.mipmap.ic_action_edit);

        //设置显示图标,默认是隐藏的
        actionBar.setDisplayShowHomeEnabled(false);//显示图标,默认是false,默认隐藏

        //设置icon和logo显示的优先级,默认是icon优先
        actionBar.setDisplayUseLogoEnabled(true);//默认是false,默认是icon优先

        //显示回退部分
        actionBar.setDisplayHomeAsUpEnabled(true);//默认是false,默认隐藏回退部分

    }

    private void initActionBarDrawerToggle() {
        mTogglev7 = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        //        android.support.v4.app.ActionBarDrawerToggle tooglev4 = new android.support.v4.app.ActionBarDrawerToggle(this,
        //                mDrawerLayout, R.mipmap.ic_drawer_am, R.string.open, R.string.close);

        //同步状态-->修改回退部分的ui展现
        mTogglev7.syncState();
        //        tooglev4.syncState();

        //DrawerLayout拖动的时候,ActionBarDrawerToggle跟着动
        mDrawerLayout.addDrawerListener(mTogglev7);
        //                mDrawerLayout.addDrawerListener(tooglev4);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //控制DrawerLayout的打开和关闭
                mTogglev7.onOptionsItemSelected(item);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initData() {
        //mViewPager 设置adapter
        //dataSets-->先用模拟的数据集
        mMainTitles = UIUtils.getStringArr(R.array.main_titles);

        //        MainPagerAdapter adapter = new MainPagerAdapter();
        //                MainFragmentPagerAdapter adapter = new MainFragmentPagerAdapter(getSupportFragmentManager());
        MainFragmentSatePagerAdapter adapter = new MainFragmentSatePagerAdapter(getSupportFragmentManager());
        mMainViewpager.setAdapter(adapter);

        // Bind the tabs to the ViewPager
        mMainTabs.setViewPager(mMainViewpager);
    }

    /*
    PagerAdapter-->page-->View
    FragmentPagerAdapter-->page-->Fragment
    FragmentStatePagerAdapter-->page-->Fragment
     */
    class MainPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            if (mMainTitles != null) {
                return mMainTitles.length;
            }
            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //view
            TextView tv = new TextView(UIUtils.getContext());
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.BLUE);
            //data
            String data = mMainTitles[position];

            //data+view
            tv.setText(data);
            //加入容器
            container.addView(tv);
            //返回视图
            return tv;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        /**
         * 必须要覆写getPageTitle方法,返回具体的title
         *
         * @param position
         * @return
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mMainTitles[position];
        }
    }

    class MainFragmentPagerAdapter extends FragmentPagerAdapter {

        public MainFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {//返回每一个页面所对应的fragment
            LogUtils.s("初始化-->" + mMainTitles[position]);

            //根据position生产不同的Fragment
            Fragment fragment = FragmentFactory.createFragment(position);
            return fragment;
        }

        @Override
        public int getCount() {
            if (mMainTitles != null) {
                return mMainTitles.length;
            }
            return 0;
        }

        /**
         * 覆写getPageTitle
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mMainTitles[position];
        }
    }

    class MainFragmentSatePagerAdapter extends FragmentStatePagerAdapter {
        public MainFragmentSatePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {//返回每一个页面所对应的fragment
            LogUtils.s("初始化-->" + mMainTitles[position]);
            //根据position生产不同的Fragment
            Fragment fragment = FragmentFactory.createFragment(position);
            return fragment;
        }

        @Override
        public int getCount() {
            if (mMainTitles != null) {
                return mMainTitles.length;
            }
            return 0;
        }

        /**
         * 覆写getPageTitle
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mMainTitles[position];
        }
    }
}
