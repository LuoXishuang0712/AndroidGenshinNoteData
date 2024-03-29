package com.luoxishuang.genshinnotedata;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class calcResin extends AppCompatActivity {
    private Integer max_resin = 160;
    private Integer time_pre_resin = 8 * 60; // in sec
    final int[] input = new int[]{-1};
    private int time = 0;
    // todo : 将获取数据到展示的延迟添加到time中
    private Handler refreshHandler;
    private Runnable refreshTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calc_resin);

        Integer present = getIntent().getIntExtra("present", -1);  // present resin
        Integer rest = getIntent().getIntExtra("rest", -1);  // rest time

        if(present == -1 || rest == -1){
            Toast.makeText(
                    getApplicationContext(),
                    "数值传递异常，请重试！",
                    Toast.LENGTH_LONG
            ).show();
            finish();
        }

//        Log.d("resin calculator", "present : " + present.toString() + " rest : " + rest.toString());

        ((Button) findViewById(R.id.custom_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText ipt = findViewById(R.id.custom_text);
                Integer inputNum;
                try{
                    inputNum = Integer.parseInt(String.valueOf(ipt.getText()));
                } catch (NumberFormatException e){
                    Toast.makeText(
                            getApplicationContext(),
                            "请输入规范的数字！",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                if(inputNum<0 || inputNum>160){
                    Toast.makeText(
                            getApplicationContext(),
                            "请输入有效数字！",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                input[0] = inputNum;
                refreshTask(rest, present, false);
            }
        });

        updateListData(present, rest);

        refreshHandler = new Handler();
        refreshTask = (Runnable) () -> {
            refreshTask(rest, present);
            refreshHandler.postDelayed(refreshTask, 1000);
        };

        refreshHandler.postDelayed(refreshTask, 0);

    }

    public void refreshTask(int rest, int present){
        refreshTask(rest, present, true);
    }

    public void refreshTask(int rest, int present, boolean mode){
        int timeCnt = time;
        int nextOne = calcToBase(present, rest, present + 1);
        int thisRest = rest;
        int thisPresent = present;
        if(mode){
            time = ++timeCnt;
        }
        if(timeCnt > rest) {  // 计时大于剩余时间（已恢复满）
            updateListData(max_resin, 0);
            Log.d("refreshTask", "Reach max resin");
            return;
        }
        thisPresent += (timeCnt + (time_pre_resin - nextOne)) / time_pre_resin;  // 将时间恢复到上一个树脂恢复的时间点，从而能用最大时间直接整除
        thisRest -= timeCnt;
//        Log.d("resin refreshTask", "present : " + thisPresent + " rest : " + thisRest);
        if(mode){
            updateListData(thisPresent, thisRest);
        }
        else{
            updateCalcData(thisPresent, thisRest);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && refreshHandler != null && refreshTask != null){
            // 取消定时器
            refreshHandler.removeCallbacks(refreshTask);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateListData(int present, int rest){
        ((TextView) findViewById(R.id.current_resin_data)).setText(String.format("%d/%d", present, max_resin));
        ((TextView) findViewById(R.id.current_resin_time)).setText(sec2str(rest));
        ((TextView) findViewById(R.id.resin_to_20)).setText(sec2str(calcToBase(present, rest, 20)));
        ((TextView) findViewById(R.id.resin_to_40)).setText(sec2str(calcToBase(present, rest, 40)));
        ((TextView) findViewById(R.id.resin_to_90)).setText(sec2str(calcToBase(present, rest, 90)));
        ((TextView) findViewById(R.id.resin_to_next_20)).setText(sec2str(calcToBase(present, rest, calcBase(present, 20))));
        ((TextView) findViewById(R.id.resin_to_next_40)).setText(sec2str(calcToBase(present, rest, calcBase(present, 40))));
        ((TextView) findViewById(R.id.resin_to_next_1)).setText(sec2str(calcToBase(present, rest, present + 1)));

        updateCalcData(present, rest);
    }

    private void updateCalcData(int present, int rest){
        int inputNum = input[0];
        if(inputNum != -1){
            ((TextView) findViewById(R.id.custom_ans)).setText(sec2str(calcToBase(present, rest, inputNum)));
        }
    }

    private String sec2str(int sec){
        String ans = new String();
        ans = String.format("%d秒",sec%60) + ans;
        sec /= 60;
        if(sec<=0){
            return ans;
        }
        ans = String.format("%d分",sec%60) + ans;
        sec /= 60;
        if(sec<=0){
            return ans;
        }
        ans = String.format("%d时",sec%24) + ans;
        sec /= 24;
        if(sec<=0){
            return ans;
        }
        ans = String.format("%d日",sec) + ans;
        return ans;
    }

    private Integer calcBase(Integer present, Integer times){
        if(present.equals(max_resin)){
            return max_resin;
        }
        return (((present) / times) + 1) * times;
    }

    private Integer calcToBase(Integer present, Integer rest, Integer base) {
        if (present >= base || rest == 0) {
            return 0;
        }
        int thisTime = rest - (max_resin - present - 1) * time_pre_resin;
        int toBase = base - present;
        return (toBase - 1) * time_pre_resin + thisTime;
    }
}
