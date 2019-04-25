package com.jw.autonest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.jw.autonest.util.SpfUtil;

public class MainActivity extends AppCompatActivity {

    private Switch sbtn;
    private Switch sbtn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sbtn = findViewById(R.id.switch1);
        sbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpfUtil.getInstance(MainActivity.this).putData("auto",isChecked);

            }
        });
        sbtn2 = findViewById(R.id.switch2);
        sbtn2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpfUtil.getInstance(MainActivity.this).putData("auto2",isChecked);

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        sbtn.setChecked(SpfUtil.getInstance(this).getData("auto"));
        sbtn.setChecked(SpfUtil.getInstance(this).getData("auto2"));
    }
}
