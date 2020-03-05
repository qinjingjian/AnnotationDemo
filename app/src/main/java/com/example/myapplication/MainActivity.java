package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button clickTest;
    private IShapeFactory factory;
    private TextView result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        factory = new IShapeFactory();
        clickTest = (Button) findViewById(R.id.click_test);
        result = (TextView) findViewById(R.id.show_result);
        clickTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = factory.create("Circle").draw();
                result.setText(data);
            }
        });
    }
}
