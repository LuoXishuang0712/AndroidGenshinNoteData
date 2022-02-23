package com.luoxishuang.genshinnotedata;

import android.os.Bundle;
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
    private Integer time_pre_resin = 8 * 60; //in sec

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calc_resin);

        Integer present = getIntent().getIntExtra("present", -1);
        Integer rest = getIntent().getIntExtra("rest", -1);

        if(present == -1 || rest == -1){
            Toast.makeText(
                    getApplicationContext(),
                    "数值传递异常，请重试！",
                    Toast.LENGTH_LONG
            ).show();
            finish();
        }

        ((TextView) findViewById(R.id.current_resin_data)).setText(String.format("%d/%d", present, max_resin));
        ((TextView) findViewById(R.id.current_resin_time)).setText(sec2str(rest));
        ((TextView) findViewById(R.id.resin_to_20)).setText(sec2str(calcToBase(present, rest, 20)));
        ((TextView) findViewById(R.id.resin_to_40)).setText(sec2str(calcToBase(present, rest, 40)));
        ((TextView) findViewById(R.id.resin_to_90)).setText(sec2str(calcToBase(present, rest, 90)));
        ((TextView) findViewById(R.id.resin_to_next_20)).setText(sec2str(calcToBase(present, rest, calcBase(present, 20))));
        ((TextView) findViewById(R.id.resin_to_next_40)).setText(sec2str(calcToBase(present, rest, calcBase(present, 40))));
        ((TextView) findViewById(R.id.resin_to_next_1)).setText(sec2str(calcToBase(present, rest, present + 1)));


        ((Button) findViewById(R.id.custom_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText ipt = findViewById(R.id.custom_text);
                Integer inputNum;
                try{
                    inputNum = Integer.parseInt(String.valueOf(ipt.getText()));
                } catch (java.lang.NumberFormatException e){
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
                ((TextView) findViewById(R.id.custom_ans)).setText(sec2str(calcToBase(present, rest, inputNum)));
            }
        });
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
