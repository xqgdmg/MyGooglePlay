package com.abc.myappstore.manager;

import com.abc.myappstore.bean.ItemInfoBean;
import com.abc.myappstore.conf.Constants;
import com.abc.myappstore.factory.ThreadPoolProxyFactory;
import com.abc.myappstore.utils.CommonUtils;
import com.abc.myappstore.utils.FileUtils;
import com.abc.myappstore.utils.HttpUtils;
import com.abc.myappstore.utils.UIUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建者     Chris
 * 创建时间   2016/7/12 09:40
 * 描述	      下载管理器,负责谷歌市场的下载功能
 * 描述	      定义了所有的状态
 * 描述	      记录状态的变化->因为DownLoadManager处理下载逻辑-->得知状态的变化
 * 描述	      变成被观察者,需要时刻的发布最新的消息给观察者
 *
 */
public class DownLoadManager {
    public static final int STATE_UNDOWNLOAD      = 0;//未下载
    public static final int STATE_DOWNLOADING     = 1;//下载中
    public static final int STATE_PAUSEDOWNLOAD   = 2;//暂停下载
    public static final int STATE_WAITINGDOWNLOAD = 3;//等待下载
    public static final int STATE_DOWNLOADFAILED  = 4;//下载失败
    public static final int STATE_DOWNLOADED      = 5;//下载完成
    public static final int STATE_INSTALLED       = 6;//已安装

    private static DownLoadManager instance;
    private Map<String, DownLoadInfo> mCacheDownLoadInfoMap = new HashMap<>();

    private DownLoadManager() {

    }

    public static DownLoadManager getInstance() {
        if (instance == null) {
            synchronized (DownLoadManager.class) {
                if (instance == null) {
                    instance = new DownLoadManager();
                }
            }
        }
        return instance;
    }

    /**
     * @param downLoadInfo
     * @des 开始异步下载apk
     * @des 用户点击了下载按钮, 当前可以记录5种状态的变化(未下载, 等待下载, 下载中, 下载失败, 下载完成)
     * @called 用户点击了下载按钮的时候
     */
    public void downLoad(DownLoadInfo downLoadInfo) {


        mCacheDownLoadInfoMap.put(downLoadInfo.packageName, downLoadInfo);

        /*=============== 当前状态:未下载 ===============*/
        downLoadInfo.curState = STATE_UNDOWNLOAD;
        notifyObservers(downLoadInfo);
        /*#######################################*/


        /*
            预先把状态设置为等待中
         */
        /*=============== 当前状态:等待中 ===============*/
        downLoadInfo.curState = STATE_WAITINGDOWNLOAD;
        notifyObservers(downLoadInfo);
        /*#######################################*/



        /*
        一个任务提交给线程池执行之后有两种情况
            1.任务被执行-->状态应该会被切换为 下载中
            2.任务等待中-->状态应该是等待中-->但是提交给线程池之后,没有相关的回调告诉我,任务的最新状态
         */

        //异步下载
        DownLoadTask task = new DownLoadTask(downLoadInfo);
        downLoadInfo.task = task;
        ThreadPoolProxyFactory.createDownloadThreadPoolProxy().submit(task);

    }


    class DownLoadTask implements Runnable {
        DownLoadInfo downLoadInfo;

        public DownLoadTask(DownLoadInfo downLoadInfo) {
            this.downLoadInfo = downLoadInfo;
        }

        @Override
        public void run() {

            try {

                 /*=============== 当前状态:下载中 ===============*/
                downLoadInfo.curState = STATE_DOWNLOADING;
                notifyObservers(downLoadInfo);
                /*#######################################*/
                File saveApk = new File(downLoadInfo.savePath);
                long initRange = saveApk.length();

                //②进度的初始化值
                downLoadInfo.progress = (int) initRange;


                //真正的发起下载请求
                OkHttpClient okHttpClient = new OkHttpClient();
                //http://localhost:8080/GooglePlayServer/download?
                //name=app/com.itheima.www/com.itheima.www.apk&range=0
                String url = Constants.URLS.DOWNLOADBASEURL;

                // 拼接参数
                Map<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("name", downLoadInfo.downLoadUrl);
                paramsMap.put("range", initRange);//先不考虑断点 ①1.请求参数

                String urlParamsByMap = HttpUtils.getUrlParamsByMap(paramsMap);
                url = url + urlParamsByMap;


                Request request = new Request.Builder().get().url(url).build();
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    //得到数据-->输入流
                    InputStream in = response.body().byteStream();
                    //处理数据-->写入文件
                    File apkFile = new File(downLoadInfo.savePath);

                    int len;
                    byte[] buffer = new byte[1024];
                    OutputStream out = new FileOutputStream(apkFile, true);//③文件的写入以追加的方式去写
                    boolean isPause = false;
                    while ((len = in.read(buffer)) != -1) {

                        if (downLoadInfo.curState == STATE_PAUSEDOWNLOAD) {
                            isPause = true;
                            break;
                        }

                        out.write(buffer, 0, len);
                        downLoadInfo.progress += len;

                         /*=============== 当前状态:下载中 ===============*/
                        downLoadInfo.curState = STATE_DOWNLOADING;
                        notifyObservers(downLoadInfo);
                         /*#######################################*/
                        //读完主动的break
                        //避免OkHttp断点下载的时候出现的异常
                        if (saveApk.length() == downLoadInfo.max) {
                            break;
                        }
                    }

                    if (isPause) {//暂停之后来到这个地方

                    } else {
                        //下载完成了
                     /*=============== 当前状态:下载完成 ===============*/
                        downLoadInfo.curState = STATE_DOWNLOADED;
                        notifyObservers(downLoadInfo);
                    /*#######################################*/
                    }


                } else {
                    //响应失败
                      /*=============== 当前状态:下载失败 ===============*/
                    downLoadInfo.curState = STATE_DOWNLOADFAILED;
                    notifyObservers(downLoadInfo);
                   /*#######################################*/
                }

            } catch (IOException e) {
                e.printStackTrace();
                //请求错误
                  /*=============== 当前状态:下载失败 ===============*/
                downLoadInfo.curState = STATE_DOWNLOADFAILED;
                notifyObservers(downLoadInfo);
                   /*#######################################*/
            }
        }
    }

    /**
     * @param itemInfoBean 任意应用相关的信息
     * @return
     * @des 得到任意应用当前的状态
     */
    public DownLoadInfo getDownLoadInfo(ItemInfoBean itemInfoBean) {
        /*
        未下载
        下载中
        暂停下载
        等待下载
        下载失败
        下载完成

         */
        DownLoadInfo downLoadInfo = new DownLoadInfo();
        //常规赋值
        String dir = FileUtils.getDir("apk");
        String fileName = itemInfoBean.packageName + ".apk";
        File saveFile = new File(dir, fileName);


        downLoadInfo.downLoadUrl = itemInfoBean.downloadUrl;
        downLoadInfo.savePath = saveFile.getAbsolutePath();
        downLoadInfo.packageName = itemInfoBean.packageName;
        downLoadInfo.max = itemInfoBean.size;
        downLoadInfo.progress = 0;


        //状态的赋值
        //优先判断-->已安装
        if (CommonUtils.isInstalled(UIUtils.getContext(), itemInfoBean.packageName)) {
            downLoadInfo.curState = STATE_INSTALLED;
            return downLoadInfo;
        }

        //在判断-->是否下载完成
        if (saveFile.exists() && saveFile.length() == itemInfoBean.size) {
            downLoadInfo.curState = STATE_DOWNLOADED;
            return downLoadInfo;
        }


        //未下载,等待中,下载中,下载完成,下载失败
        if (mCacheDownLoadInfoMap.containsKey(itemInfoBean.packageName)) {
            downLoadInfo = mCacheDownLoadInfoMap.get(itemInfoBean.packageName);
            return downLoadInfo;
        }

        downLoadInfo.curState = STATE_UNDOWNLOAD;//默认就是未下载
        return downLoadInfo;
    }

    /*--------------- 自己实现观察者设计模式 ---------------*/

    //1.定义接口以及接口方法
    public interface DownLoadInfoObserver {
        void onDownLoadInfoChanged(DownLoadInfo downLoadInfo);
    }

    //2.定义集合存储接口对象
    public List<DownLoadInfoObserver> observers = new ArrayList<>();

    //3.针对集合做一些常规的操作
    //添加观察者到观察者集合中
    public synchronized void addObserver(DownLoadInfoObserver o) {
        if (o == null)
            throw new NullPointerException();
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    //从观察者集合中移除观察者
    public synchronized void deleteObserver(DownLoadInfoObserver o) {
        observers.remove(o);
    }

    //通知所有的观察者消息已经发生改变
    public void notifyObservers(DownLoadInfo downLoadInfo) {
        for (DownLoadInfoObserver o : observers) {
            o.onDownLoadInfoChanged(downLoadInfo);
        }
    }

    /**
     * @param downLoadInfo
     * @des 暂停下载
     * @called apk正在下载, 如果点击了暂停操作
     */
    public void pauseDownLoad(DownLoadInfo downLoadInfo) {
        /*############### 当前的状态:暂停下载 ###############*/
        downLoadInfo.curState = STATE_PAUSEDOWNLOAD;
        notifyObservers(downLoadInfo);
        /*#######################################*/
    }

    /**
     * @param downLoadInfo
     * @des 取消下载
     * @call 如果当前是等待状态, 点击了取消操作的时候
     */
    public void cancelDownLoad(DownLoadInfo downLoadInfo) {
         /*############### 当前的状态:未下载###############*/
        downLoadInfo.curState = STATE_UNDOWNLOAD;
        notifyObservers(downLoadInfo);
        /*#######################################*/
        //取出downLoadInfo所对应的任务
        Runnable task = downLoadInfo.task;
        ThreadPoolProxyFactory.createDownloadThreadPoolProxy().remove(task);
    }
}
