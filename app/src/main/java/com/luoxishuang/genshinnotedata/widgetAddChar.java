package com.luoxishuang.genshinnotedata;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class widgetAddChar extends AppCompatActivity {
    ListView charList = null;
    int listCell = 0;

    userDBHandler udbh = MainActivity.udbh;
    dataDBHandler ddbh = MainActivity.ddbh;
    widgetDBHandler wdbh = MainActivity.wdbh;

    public ConnectivityManager mConnectivity ;

    Integer actID;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_char_select);
        mConnectivity = (ConnectivityManager)getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);

        Intent passed = getIntent();
        this.actID = passed.getIntExtra("widgetID", -1);

        initList();

    }

    private void initList(){
        charList = findViewById(R.id.widgetCharSelect);
        listCell = R.layout.char_listview_cell;

        List<Map<String, String>> data = udbh.getDB();

        String key[] = new String[]{"nickname","level","region","uid"};
        int target[] = new int[]{R.id.nickname,R.id.level,R.id.region,R.id.uid};
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        SimpleAdapter sa = new SimpleAdapter(this, list, listCell, key, target);
        charList.setAdapter(sa);

        charList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String itemID = ((HashMap<String,String>) sa.getItem(i)).get("id");
//                 Log.d("charListSelect",String.format("Get ID = %s",itemID));
                Log.d("widgetAddChar", "received widget id = " + actID + " , char ID = " + itemID);
                wdbh.updateWidget(actID, Integer.parseInt(itemID));
                Toast.makeText(getApplicationContext(), "添加完成！",Toast.LENGTH_LONG).show();
                new widgetUpdateMethod().update(getApplicationContext());
                finish();
            }
        });

        for(int i=0;i<data.size();i++){
            int finalI = i;
            requestOB reOB = new requestOB() {
                @Override
                public void onDataChanged(Object rdata) throws JSONException {
                    HashMap<String,String> map = new HashMap<String,String>();
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
//                    Log.d("InitList",String.format("GetData : %s", map.toString()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            list.add(map);
                            sa.notifyDataSetChanged();
                        }
                    });
                }
            };
            if(mConnectivity.getActiveNetworkInfo() != null) {
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
            else{
                Toast.makeText(
                        getApplicationContext(),
                        "当前无网络！显示缓存信息",
                        Toast.LENGTH_LONG
                ).show();
                HashMap<String,String> map = new HashMap<String,String>();
                Map<String, Object> dbData = ddbh.getCharID(data.get(i).get("id"));
                map.put("id",data.get(finalI).get("id"));
                map.put("nickname", (String) dbData.get("nickname"));
                map.put("level","冒险等阶 : " + dbData.get("level"));
                map.put("region", (String) dbData.get("regionName"));
                map.put("uid","uid : " + dbData.get("uid"));
                list.add(map);
                sa.notifyDataSetChanged();
            }
        }
    }

}
