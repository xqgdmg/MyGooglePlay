package com.abc.myappstore.base;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.abc.myappstore.conf.Constants;
import com.abc.myappstore.utils.FileUtils;
import com.abc.myappstore.utils.HttpUtils;
import com.abc.myappstore.utils.IOUtils;
import com.abc.myappstore.utils.LogUtils;
import com.abc.myappstore.utils.UIUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建者     chris
 * 创建时间   2016/7/8 16:32
 * 描述	      针对所有的网络请求
 *
 */
public abstract class BaseProtocol<RESTYPE> {

    public static final String TAG = "BaseProtocol";

    /*
     1.如果方法签名的地方,抛出了异常,异常抛到哪里去了呢?
         方法的调用处
     2.方法体里面的异常,什么时候抛出去,什么时候自行try catch?
        什么时候抛出去-->方法调用出,需要根据具体异常处理具体逻辑的时候(方法调用出需要异常信息)
     */

    /**
     * 加载数据
     * 1.内存,内存有返回
     * 2.磁盘,磁盘有返回,存内存
     * 3.网络,网络有返回,存磁盘,存内存
     *
     * @param index
     * @return
     * @throws Exception
     */
    public RESTYPE loadData(int index) throws Exception {

        RESTYPE result = null;
        /*--------------- 1.内存,内存有返回 ---------------*/
        //得到内存缓存的存储结构
        MyApplication application = (MyApplication) UIUtils.getContext();//getApplicationContext
        Map<String, String> protocolCacheMap = application.mProtocolCacheMap;// 获取内存

        String key = generateKey(index);// 缓存的唯一索引

        if (protocolCacheMap.containsKey(key)) {
            String memCacheJsonString = protocolCacheMap.get(key);
            result = parseJsonString(memCacheJsonString);
            if (result != null) {
                LogUtils.i(TAG, "从内存加载了数据-->" + key);
                return result;
            }
        }

        /*--------------- 2.磁盘,磁盘有返回,存内存 ---------------*/
        //先本地,有返回
        result = loadDataFromLocal(index);
        if (result != null) {
            LogUtils.i(TAG, "从本地加载了数据-->" + getCacheFile(index).getAbsolutePath());
            return result;
        }
        /*--------------- 3.网络,网络有返回,存磁盘,存内存 ---------------*/
        //在网络,存本地
        return loadDataFromNet(index);
    }

    /**
     * @param index
     * @return
     * @des 生成缓存的唯一索引的key
     * @des 子类可以覆写该方法, 修改默认key的生成规则
     */
    @NonNull
    public String generateKey(int index) {
        return getInterfaceKey() + "." + index;
    }

    /**
     * 从本地加载数据
     *
     * @return
     */
    private RESTYPE loadDataFromLocal(int index) {
        BufferedReader reader = null;
        try {
            File cacheFile = getCacheFile(index);
            if (cacheFile.exists()) {//有缓存
                //判断缓存是否过期-->读取缓存的生成时间

                reader = new BufferedReader(new FileReader(cacheFile));
                // 读取第一行插入的缓存事件
                String insertTimeStr = reader.readLine();
                Long insertTime = Long.parseLong(insertTimeStr);

                if ((System.currentTimeMillis() - insertTime) < Constants.PROTOCOLTIMEOUT) {
                    //有效的缓存-->读取缓存内容
                    String diskCacheJsonString = reader.readLine();

                    //存内存
                    String key = generateKey(index);
                    MyApplication app = (MyApplication) UIUtils.getContext();
                    Map<String, String> protocolCacheMap = app.mProtocolCacheMap;
                    protocolCacheMap.put(key, diskCacheJsonString);
                    LogUtils.i(TAG, "保存本地数据到内存-->" + key);


                    //解析返回
                    RESTYPE result = parseJsonString(diskCacheJsonString);
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(reader);
        }
        return null;
    }

    @NonNull
    private File getCacheFile(int index) {
        String dir = FileUtils.getDir("json");//sdcard/Android/data/包目录/json
        String fileName = generateKey(index);// 缓存唯一命中
        return new File(dir, fileName);
    }

    /**
     * 从网络获取数据
     *
     * @param index
     * @return
     * @throws IOException
     */
    private RESTYPE loadDataFromNet(int index) throws IOException {
        //1.创建okHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建请求对象
        //http://localhost:8080/GooglePlayServer/home?index=0
        String url = Constants.URLS.BASEURL + getInterfaceKey();// 拼接地址

        //拼接get请求参数
        Map<String, Object> paramsMap = getParamsMap(index);

        String paramsMapStr = HttpUtils.getUrlParamsByMap(paramsMap);

        url = url + "?" + paramsMapStr;

        LogUtils.s(url);

        Request request = new Request.Builder().get().url(url).build();

        //3.发起请求-->同步
        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {//有响应
            String responseJsonString = response.body().string();

            BufferedWriter writer = null;
            try {
                //保存数据到本地
                File cacheFile = getCacheFile(index);
                // 第一行写入存入的时间
                writer = new BufferedWriter(new FileWriter(cacheFile));
                writer.write(System.currentTimeMillis() + "");

                writer.newLine();//换行
                writer.write(responseJsonString);// 写入缓存数据
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(writer);// 关闭流
            }

            //存内存
            MyApplication app = (MyApplication) UIUtils.getContext();
            Map<String, String> protocolCacheMap = app.mProtocolCacheMap;
            String key = generateKey(index);
            protocolCacheMap.put(key, responseJsonString);
            LogUtils.i(TAG, "保存网络数据到内存-->" + key);

            //完成json的解析
            RESTYPE t = parseJsonString(responseJsonString);
            return t;
        }
        return null;
    }

    /**
     * @param index
     * @return
     * @des 得到请求参数所对应的Map
     * @des 默认的参数是index, 子类可以覆写该方法, 传递不同的参数
     */
    @NonNull
    public Map<String, Object> getParamsMap(int index) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("index", index);
        return paramsMap;// 默认参数是index
    }


    /**
     * @return
     * @des 返回协议的关键字
     * @des 在BaseProtocol中不知道协议的关键字是什么, 交给子类
     * @des 子类是必须实现, 定义成为抽象方法, 交给子类具体实现
     */
    public abstract String getInterfaceKey();

    /**
     * @param resultJsonString
     * @return
     * @des 解析请求回来的jsonString
     * @des jsonString-->Bean
     * @des jsonString-->List Map
     * @des 在BaseProtocol不知道如何解析jsonString, 交给子类实现
     * @des 子类选择性实现
     */
    public RESTYPE parseJsonString(String resultJsonString) {
        RESTYPE result = null;
        Gson gson = new Gson();
        Type type = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        result = gson.fromJson(resultJsonString, type);
        return result;
    }
}
