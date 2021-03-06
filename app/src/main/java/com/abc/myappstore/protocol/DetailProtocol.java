package com.abc.myappstore.protocol;

import android.support.annotation.NonNull;

import com.abc.myappstore.base.BaseProtocol;
import com.abc.myappstore.bean.ItemInfoBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建者     Chris
 * 创建时间   2016/7/11 14:25
 * 描述	      ${TODO}
 *
 */
public class DetailProtocol extends BaseProtocol<ItemInfoBean> {
    //http://localhost:8080/GooglePlayServer/detail?packageName=com.itheima.www
    //http://localhost:8080/GooglePlayServer/detail?index=0
    String mPackageName;

    public DetailProtocol(String packageName) {
        mPackageName = packageName;
    }

    @Override
    public String getInterfaceKey() {
        return "detail";
    }

  /*  @Override
    public ItemInfoBean parseJsonString(String resultJsonString) {
        Gson gson = new Gson();
        return gson.fromJson(resultJsonString, ItemInfoBean.class);
    }*/

    @NonNull
    @Override
    public Map<String, Object> getParamsMap(int index) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("packageName", mPackageName);
        return paramsMap;

    }

    @NonNull
    @Override
    public String generateKey(int index) {
        return getInterfaceKey() + "." + mPackageName;
    }
}
