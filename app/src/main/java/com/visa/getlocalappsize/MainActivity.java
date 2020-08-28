package com.visa.getlocalappsize;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;


public class MainActivity extends Activity implements AppInfoCallbackListener{
    private TextView tv;
    private CommonUtils commonUtils;
    public final String TAG = "visa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
    }

    private void initData() {
        commonUtils = CommonUtils.getInstance();
        commonUtils.setAppInfoCallbackListener(this);
    }


    private void initView() {
        tv = findViewById(R.id.tv);
    }

    @Override
    public void OnAppInfoCallback(String data, boolean isGetAppSizeAuthority) {
        List<AppInfo> myAppInfos = new Gson().fromJson(data,new TypeToken<List<AppInfo>>(){}.getType());

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < myAppInfos.size(); i++) {
            sb.append(myAppInfos.get(i).appName + "\r\n\t\t" + myAppInfos.get(i).packageName + "\r\n\t\t\t\t" + myAppInfos.get(i).appSize+ "\r\n");
            sb.append("============" + "\r\n");
        }
        tv.setText(sb.toString());
    }

    public void Click(View view) {
        switch (view.getId()) {
            case R.id.btn_getSize:
                if (commonUtils.isGetAccess(this)) {
                    commonUtils.GetInstallAppInfo(this);
                }else {
                    Toast.makeText(this, "请授权", Toast.LENGTH_SHORT).show();
                    commonUtils.getApplicationSizeAccess(this);
                }
                break;
        }
    }
}
