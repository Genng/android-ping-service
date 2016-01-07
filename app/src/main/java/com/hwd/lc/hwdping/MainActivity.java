package com.hwd.lc.hwdping;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = new View(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        setContentView(view);
        startService(new Intent(this, MyService.class));
        finish();
    }

}