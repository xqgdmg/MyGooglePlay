package com.abc.myappstore.protocol;

import com.google.gson.Gson;
import com.abc.myappstore.bean.HomeBean;
import com.abc.myappstore.conf.Constants;
import com.abc.myappstore.utils.HttpUtils;
import com.abc.myappstore.utils.LogUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建者     Chris
 * 创建时间   2016/7/8 16:25
 * 描述	      针对的是HomeFragment里面的网络请求
 * 描述	      把HomeFragment里面相关的网络请求的代码,移动过来了而已
 *
 */
public class HomeProtocolBackUp {
    /*
     1.如果方法签名的地方,抛出了异常,异常抛到哪里去了呢?
         方法的调用处
     2.方法体里面的异常,什么时候抛出去,什么时候自行try catch?
        什么时候抛出去-->方法调用出,需要根据具体异常处理具体逻辑的时候(方法调用出需要异常信息)
     */
    public HomeBean loadData() throws Exception {
        //1.创建okHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建请求对象
        //http://localhost:8080/GooglePlayServer/home?index=0
        String url = Constants.URLS.BASEURL + "home";

        //拼接参数
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("index", 0);

        String paramsMapStr = HttpUtils.getUrlParamsByMap(paramsMap);

        url = url + "?" + paramsMapStr;
        LogUtils.s(url);

        Request request = new Request.Builder().get().url(url).build();

        //3.发起请求-->同步
        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {//有响应
            String resultJsonString = response.body().string();
            //完成json的解析
            Gson gson = new Gson();
            HomeBean homeBean = gson.fromJson(resultJsonString, HomeBean.class);
            return homeBean;
        }
        return null;
    }
}
