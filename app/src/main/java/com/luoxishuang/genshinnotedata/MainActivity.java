package com.luoxishuang.genshinnotedata;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// todo : 外服hoyolab数据源增加

public class MainActivity extends AppCompatActivity {
    public static userDBHandler udbh;
    public static dataDBHandler ddbh;
    public static widgetDBHandler wdbh;

    public ConnectivityManager mConnectivity ;

    private Toolbar toolbar;

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    };


//    private AppBarConfiguration appBarConfiguration;
//    private ActivityMainBinding binding;

    customListView charList = null;
    int listCell = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        widgetUpdateService.invoke(getApplicationContext());

        toolbar = ((Toolbar) findViewById(R.id.toolbar_main_activity));
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.mainactivity_menu);

        mConnectivity = (ConnectivityManager)getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);

        udbh = new userDBHandler(getApplicationContext());
        udbh.init();
        ddbh = new dataDBHandler(getApplicationContext());
        ddbh.init();
        wdbh = new widgetDBHandler(getApplicationContext());
        wdbh.init();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.channel_id);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent AddChar = new Intent();
        AddChar.setClass(this, AddCharacter.class);
        AddChar.putExtra("FromMain", true);

        FloatingActionButton fabAdd = findViewById(R.id.addUser);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(AddChar);
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.toolbar_setting:
                        Log.d("toolbar", "setting pressed");
                }
                return false;
            }
        });

//        initList();

    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            initList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initList(){
        charList = findViewById(R.id.charlist);
        listCell = R.layout.char_listview_cell;

        List<Map<String, String>> data = udbh.getDB();

        String key[] = new String[]{"nickname","level","region","uid"};
        int target[] = new int[]{R.id.nickname,R.id.level,R.id.region,R.id.uid};
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        SimpleAdapter sa = new SimpleAdapter(this, list, listCell, key, target);
        charList.setAdapter(sa);

        charList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 String itemID = ((HashMap<String,String>) sa.getItem(i-1)).get("id");
//                 Log.d("charListSelect",String.format("Get ID = %s",itemID));
                 Intent InfoDetail = new Intent(MainActivity.this, InfoDetail.class);
                 InfoDetail.putExtra("id",itemID);
                 startActivity(InfoDetail);
            }
        });

        charList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String itemID = ((HashMap<String,String>) sa.getItem(i-1)).get("id");
                if(wdbh.getCharID(Integer.parseInt(itemID)).isEmpty()){
                    udbh.deleteRecord(itemID);
                    ddbh.deleteCharIDRecord(itemID);
                    initList();
                }
                else{
                    Toast.makeText(
                            getApplicationContext(),
                            "存在未解绑的小部件，请移除小部件后重试！",
                            Toast.LENGTH_LONG
                    ).show();
                }
                return false;
            }
        });

        charList.setonRefreshListener(new customListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initList();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                charList.onRefreshComplete();
                            }
                        });
                    }
                }).start();
            }
        });

        for(int i=0;i<data.size();i++) {  // load cache data
            HashMap<String, String> map = new HashMap<String, String>();
            Map<String, Object> dbData = ddbh.getCharID(data.get(i).get("id"));
            map.put("id", data.get(i).get("id"));
            map.put("nickname", (String) dbData.get("nickname"));
            map.put("level", "冒险等阶 : " + dbData.get("level"));
            map.put("region", (String) dbData.get("regionName"));
            map.put("uid", "uid : " + dbData.get("uid"));
            list.add(map);
        }
        sa.notifyDataSetChanged();

        for(int i=0;i<data.size();i++){
            int finalI = i;
            requestOB reOB = new requestOB() {
                @Override
                public void onDataChanged(Object rdata) throws JSONException {
                    HashMap<String,String> map = (HashMap<String, String>) list.get(finalI);
                    if(((JSONObject)rdata).getInt("retcode") != 0){
                        map.put("id",data.get(finalI).get("id"));
                        map.put("nickname","cookies已失效！");
                        map.put("level","Nan");
                        map.put("region","Nan");
                        map.put("uid",data.get(finalI).get("game_uid"));
                    }
                    else{
                        JSONObject charData = ((JSONObject) rdata)
                                .getJSONObject("data")
                                .getJSONArray("list")
                                .getJSONObject(Integer.parseInt(data.get(finalI).get("char_cnt")));
                        map.put("id",data.get(finalI).get("id"));
                        map.put("nickname",genshinData.chinese_decode(charData.getString("nickname")));
                        map.put("level","冒险等阶 : " + String.valueOf(charData.getInt("level")));
                        map.put("region",genshinData.chinese_decode(charData.getString("region_name")));
                        map.put("uid","uid : " + charData.getString("game_uid"));
                        ddbh.updateInfo(
                                Integer.parseInt(data.get(finalI).get("id")),
                                charData.getString("game_uid"),
                                genshinData.chinese_decode(charData.getString("nickname")),
                                charData.getInt("level"),
                                genshinData.chinese_decode(charData.getString("region_name"))
                        );
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sa.notifyDataSetChanged();
                        }
                    });
                }
            };
            if( mConnectivity.getActiveNetworkInfo() != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            genshinData.getUserInfo(
                                    data.get(finalI).get("cookies"),
                                    reOB
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        if(mConnectivity.getActiveNetworkInfo() == null){
            Toast.makeText(
                    getApplicationContext(),
                    "当前无网络！显示缓存信息",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return dispatchKeyEvent(event);
    }
}