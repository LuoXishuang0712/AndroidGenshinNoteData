package com.luoxishuang.genshinnotedata;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddCharacter extends AppCompatActivity {

    userDBHandler udbh = MainActivity.udbh;
    dataDBHandler ddbh = MainActivity.ddbh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_character);

        WebView mainWebView = findViewById(R.id.mainWebView);
        Button next = findViewById(R.id.NextStep);
        Button refresh = findViewById(R.id.Refresh);
        TextView info = findViewById(R.id.NextInfo);

        Bundle passedData = this.getIntent().getExtras();
        Boolean FromMain = passedData.getBoolean("FromMain");

        info.setText("请在米游社完成登录\n完成后点击右侧按键跳转下一步");  //please login at the mihoyo bbs \n and then press the button right go next

        mainWebView.loadUrl("https://bbs.mihoyo.com/ys/");
//        CookieSyncManager.createInstance(self);
        CookieManager.getInstance().removeAllCookie();
        WebView.setWebContentsDebuggingEnabled(false);  //Turn off before release
        mainWebView.clearCache(true);
        mainWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mainWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mainWebView.getSettings().setDomStorageEnabled(true);
        mainWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        final Integer[] step = {0};
        final String[] ret_data = new String[1];

        refresh.setOnClickListener(view -> mainWebView.reload());

        next.setOnClickListener(view -> {
            switch (step[0]){
                case 0:
                    mainWebView.loadUrl("https://user.mihoyo.com/");
                    info.setText("请在账号中心完成登录\n完成后点击右侧案件获取cookies");  //please login in the user center \n then press the right button to get cookies.
                    step[0]++;
                    break;
                case 1:
                    mainWebView.evaluateJavascript("javascript:(function(){return document.cookie;})()", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            ret_data[0] = s;
                        }
                    });
                    info.setText("获取成功！");
                    next.setText("完成");
                    step[0]++;
                    break;
                case 2:
                    requestOB reOB = new requestOB() {
                        @Override
                        public void onDataChanged(Object data) throws JSONException {
                            JSONObject retData = (JSONObject) data;
                            if(retData.getInt("retcode") != 0){
                                Looper.prepare();
                                Toast.makeText(
                                        getApplicationContext(),
                                        "cookies无效，请尝试重新获取",
                                        Toast.LENGTH_LONG
                                ).show();
                                Looper.loop();
                            }
                            else{
                                JSONArray charArray = retData.getJSONObject("data").getJSONArray("list");
                                if(charArray.length()>1){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                            Looper.prepare();
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "发现多个角色，请选择需要添加的角色。",
                                                    Toast.LENGTH_LONG
                                            ).show();
//                                            Looper.loop();

                                            initList(charArray, ret_data[0]);
                                        }
                                    });
                                }
                                else if(charArray.length()==1){
                                    // add character into database
                                    Integer retID = udbh.insertRetId(
                                            ret_data[0],
                                            "0",
                                            charArray.getJSONObject(0).getString("game_uid"),
                                            charArray.getJSONObject(0).getString("region")
                                    );
                                    ddbh.updateInfo(
                                            retID,
                                            charArray.getJSONObject(0).getString("game_uid"),
                                            genshinData.chinese_decode(charArray.getJSONObject(0).getString("nickname")),
                                            charArray.getJSONObject(0).getInt("level"),
                                            genshinData.chinese_decode(charArray.getJSONObject(0).getString("region_name"))
                                    );
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "添加成功！",
                                            Toast.LENGTH_LONG
                                    ).show();
                                    finish();
                                }
                                else{
                                    Log.w("CharArray", String.format("GetErrorCharCount: %d", charArray.length()));
                                }
                            }
                        }
                    };
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            try {
                                genshinData.validCookie(ret_data[0],reOB);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Looper.loop();
                        }
                    }).start();
                    break;

            }
        });

    }

    private void initList(JSONArray data, String cookies){
        setContentView(R.layout.multi_char_selector);
        ListView charSelector = findViewById(R.id.charSelector);
//        Log.i("AddCharacter", charSelector == null?"True":"False");
        int listCell = R.layout.char_listview_cell;
        String key[] = new String[]{"nickname","level","region","uid"};
        int target[] = new int[]{R.id.nickname,R.id.level,R.id.region,R.id.uid};
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        SimpleAdapter sa = null;
        try {
            for (int i = 0; i < data.length(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("nickname", genshinData.chinese_decode(data.getJSONObject(i).getString("nickname")));
                map.put("level", String.format("冒险等阶 %d", data.getJSONObject(i).getInt("level")));
                map.put("region", genshinData.chinese_decode(data.getJSONObject(i).getString("region_name")));
                map.put("uid", String.format("UID %d", data.getJSONObject(i).getInt("game_uid")));
                list.add(map);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        sa = new SimpleAdapter(AddCharacter.this, list, listCell, key, target);
        Log.i("AddCharacter", sa.toString());
        Log.i("AddCharacter", charSelector.toString());
        charSelector.setAdapter(sa);
        charSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Integer retID = udbh.insertRetId(
                            cookies,
                            String.valueOf(i),
                            data.getJSONObject(i).getString("game_uid"),
                            data.getJSONObject(i).getString("region")
                    );
                    ddbh.updateInfo(
                            retID,
                            data.getJSONObject(0).getString("game_uid"),
                            genshinData.chinese_decode(data.getJSONObject(0).getString("nickname")),
                            data.getJSONObject(0).getInt("level"),
                            genshinData.chinese_decode(data.getJSONObject(0).getString("region_name"))
                    );
                    Toast.makeText(
                            getApplicationContext(),
                            "添加成功！",
                            Toast.LENGTH_LONG
                    ).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(
                            getApplicationContext(),
                            "添加失败！请检查日志",
                            Toast.LENGTH_LONG
                    ).show();
                }
                finish();
            }
        });
    }
}
