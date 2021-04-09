package com.sdtv.player;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 作者：admin016
 * 日期时间: 2021/4/9 10:04
 * 内容描述:
 */
public class Demo5Activity extends AppCompatActivity {

    private Button btnPlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btnPlay = findViewById(R.id.btn_play);
    }
}
