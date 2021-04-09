package com.sdtv.player;

import android.app.AppComponentFactory;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 作者：admin016
 * 日期时间: 2021/3/31 14:11
 * 内容描述:
 */
public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        button1 = findViewById(R.id.btn_one);
        button2 = findViewById(R.id.btn_two);
        button3 = findViewById(R.id.btn_three);
        button4 = findViewById(R.id.btn_four);
        button5 = findViewById(R.id.btn_five);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_one:
                Intent intent = new Intent(HomePageActivity.this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_two:
                Intent intent1 = new Intent(HomePageActivity.this,Demo2Activity.class);
                startActivity(intent1);
                break;
            case R.id.btn_three:
                Intent intent2 = new Intent(HomePageActivity.this,Demo3Activity.class);
                startActivity(intent2);
                break;
            case R.id.btn_four:
                Intent intent3 = new Intent(HomePageActivity.this,Demo4Activity.class);
                startActivity(intent3);
                break;
            case R.id.btn_five:
                Intent intent4 = new Intent(HomePageActivity.this,Demo5Activity.class);
                startActivity(intent4);
                break;
        }
    }
}
