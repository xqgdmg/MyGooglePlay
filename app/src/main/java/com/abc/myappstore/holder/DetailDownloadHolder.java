package com.abc.myappstore.holder;

import android.view.View;
import android.widget.Button;

import com.abc.myappstore.R;
import com.abc.myappstore.base.BaseHolder;
import com.abc.myappstore.bean.ItemInfoBean;
import com.abc.myappstore.manager.DownLoadInfo;
import com.abc.myappstore.manager.DownLoadManager;
import com.abc.myappstore.utils.CommonUtils;
import com.abc.myappstore.utils.PrintDownLoadInfo;
import com.abc.myappstore.utils.UIUtils;
import com.abc.myappstore.views.ProgressBtn;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 创建者     Chris
 * 创建时间   2016/7/11 15:09
 * 描述	      观察者,监听DownLoadInfo的变化
 *
 */
public class DetailDownloadHolder extends BaseHolder<ItemInfoBean> implements DownLoadManager.DownLoadInfoObserver {

    @InjectView(R.id.app_detail_download_btn_favo)
    Button mAppDetailDownloadBtnFavo;
    @InjectView(R.id.app_detail_download_btn_share)
    Button mAppDetailDownloadBtnShare;

    @InjectView(R.id.app_detail_download_btn_download)
    ProgressBtn mAppDetailDownloadBtnDownload;
    private ItemInfoBean mItemInfoBean;

    @Override
    public View initHolderViewAndFindViews() {
        View holderView = View.inflate(UIUtils.getContext(), R.layout.item_detail_download, null);
        //找孩子
        ButterKnife.inject(this, holderView);
        return holderView;
    }

    @Override
    public void refreshHolderView(ItemInfoBean data) {
        mItemInfoBean = data;

        /*--------------- 2.根据不同的状态给用户提示(修改下载按钮ui) ---------------*/
        //curState-->downLoadInfo
        DownLoadInfo downLoadInfo = DownLoadManager.getInstance().getDownLoadInfo(data);
        refreshProgressBtnUI(downLoadInfo);
    }

    private void refreshProgressBtnUI(DownLoadInfo downLoadInfo) {
        int curState = downLoadInfo.curState;
        /*
        状态(编程记录)  	|  给用户的提示(ui展现)
        ----------------|-----------------------
        未下载			|下载
        下载中			|显示进度条
        暂停下载			|继续下载
        等待下载			|等待中...
        下载失败 		|重试
        下载完成 		|安装
        已安装 			|打开

         */
        mAppDetailDownloadBtnDownload.setBackgroundResource(R.drawable.selector_app_detail_bottom_normal);
        switch (curState) {
            case DownLoadManager.STATE_UNDOWNLOAD://未下载
                mAppDetailDownloadBtnDownload.setText("下载");
                break;
            case DownLoadManager.STATE_DOWNLOADING://下载中
                mAppDetailDownloadBtnDownload.setProgressEnable(true);
                mAppDetailDownloadBtnDownload.setBackgroundResource(R.drawable.selector_app_detail_bottom_downloading);
                int index = (int) (downLoadInfo.progress * 1.0f / downLoadInfo.max * 100 + .5f);
                mAppDetailDownloadBtnDownload.setText(index + "%");
                mAppDetailDownloadBtnDownload.setMax(downLoadInfo.max);
                mAppDetailDownloadBtnDownload.setProgress(downLoadInfo.progress);
                break;
            case DownLoadManager.STATE_PAUSEDOWNLOAD://暂停下载
                mAppDetailDownloadBtnDownload.setText("继续下载");
                break;
            case DownLoadManager.STATE_WAITINGDOWNLOAD://等待中
                mAppDetailDownloadBtnDownload.setText("等待中...");
                break;
            case DownLoadManager.STATE_DOWNLOADFAILED://下载失败
                mAppDetailDownloadBtnDownload.setText("重试");
                break;
            case DownLoadManager.STATE_DOWNLOADED://下载完成
                mAppDetailDownloadBtnDownload.setText("安装");
                mAppDetailDownloadBtnDownload.setProgressEnable(false);
                break;
            case DownLoadManager.STATE_INSTALLED://已安装
                mAppDetailDownloadBtnDownload.setText("打开");
                break;

            default:
                break;
        }
    }

    @OnClick(R.id.app_detail_download_btn_download)
    public void clickDownDownBtn(View view) {
        /*--------------- 3.根据不同的状态触发不同的操作(调用不同的方法) ---------------*/
        //curState-->downLoadInfo
        DownLoadInfo downLoadInfo = DownLoadManager.getInstance().getDownLoadInfo(mItemInfoBean);
        int curState = downLoadInfo.curState;
        /*

            状态(编程记录)   | 用户行为(触发操作)
            --------------- | -----------------
            未下载			| 去下载
            下载中			| 暂停下载
            暂停下载			| 断点继续下载
            等待下载			| 取消下载
            下载失败 		| 重试下载
            下载完成 		| 安装应用
            已安装 			| 打开应用
         */
        switch (curState) {
            case DownLoadManager.STATE_UNDOWNLOAD://未下载
                downLoad(downLoadInfo);
                break;
            case DownLoadManager.STATE_DOWNLOADING://下载中
                pauseDownLoad(downLoadInfo);
                break;
            case DownLoadManager.STATE_PAUSEDOWNLOAD://暂停下载
                downLoad(downLoadInfo);
                break;
            case DownLoadManager.STATE_WAITINGDOWNLOAD://等待中
                cancelDownLoad(downLoadInfo);
                break;
            case DownLoadManager.STATE_DOWNLOADFAILED://下载失败
                downLoad(downLoadInfo);
                break;
            case DownLoadManager.STATE_DOWNLOADED://下载完成
                installApk(downLoadInfo);
                break;
            case DownLoadManager.STATE_INSTALLED://已安装
                openApk(downLoadInfo);
                break;

            default:
                break;
        }
    }

    /**
     * 打开apk
     *
     * @param downLoadInfo
     */
    private void openApk(DownLoadInfo downLoadInfo) {
        CommonUtils.openApp(UIUtils.getContext(), downLoadInfo.packageName);
    }

    /**
     * 安装apk
     *
     * @param downLoadInfo
     */
    private void installApk(DownLoadInfo downLoadInfo) {
        File apkFile = new File(downLoadInfo.savePath);
        CommonUtils.installApp(UIUtils.getContext(), apkFile);
    }

    /**
     * 取消下载
     *
     * @param downLoadInfo
     */
    private void cancelDownLoad(DownLoadInfo downLoadInfo) {
        DownLoadManager.getInstance().cancelDownLoad(downLoadInfo);
    }

    /**
     * 暂停下载
     *
     * @param downLoadInfo
     */
    private void pauseDownLoad(DownLoadInfo downLoadInfo) {
        DownLoadManager.getInstance().pauseDownLoad(downLoadInfo);
    }

    /**
     * 开始下载,继续下载,重试下载
     *
     * @param downLoadInfo
     */
    private void downLoad(DownLoadInfo downLoadInfo) {
     /*   Toast.makeText(UIUtils.getContext(), "开始下载", Toast.LENGTH_SHORT).show();
        DownLoadInfo downLoadInfo = new DownLoadInfo();
        String dir = FileUtils.getDir("apk");
        String fileName = mItemInfoBean.packageName + ".apk";
        File saveFile = new File(dir, fileName);

        //downLoadInfo的赋值
        downLoadInfo.downLoadUrl = mItemInfoBean.downloadUrl;
        downLoadInfo.savePath = saveFile.getAbsolutePath();*/

        DownLoadManager.getInstance().downLoad(downLoadInfo);
    }

    /**
     * 接收到downLoadInfo信息的改变
     * 接收消息方法所在的线程有发布消息所在的线程决定
     *
     * @param downLoadInfo
     */
    @Override
    public void onDownLoadInfoChanged(final DownLoadInfo downLoadInfo) {

        if(!downLoadInfo.packageName.equals(mItemInfoBean.packageName)){
            return;
        }

        PrintDownLoadInfo.printDownLoadInfo(downLoadInfo);
        //更新ui
        UIUtils.getMainThreadHanlder().post(new Runnable() {
            @Override
            public void run() {
                refreshProgressBtnUI(downLoadInfo);
            }
        });
    }
}
