package com.lasselindh.lassetoolssample;

import android.Manifest;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.lasselindh.tools.LassePermission;
import com.lasselindh.tools.LasseTools;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findViewById(R.id.btTest1).setOnClickListener(this);
        findViewById(R.id.btTest2).setOnClickListener(this);
        findViewById(R.id.btTest3).setOnClickListener(this);
        findViewById(R.id.btTest4).setOnClickListener(this);

        LasseTools.getInstance().init(this, BuildConfig.DEBUG);
        LasseTools.getInstance().setScreen(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btTest1: {
                test1();
                break;
            }

            case R.id.btTest2:{
                test2();
                break;
            }

            case R.id.btTest3: {
                test3();
                break;
            }

            case R.id.btTest4:{
                test4();
                break;
            }

            default:
                break;
        }
    }

    public void test1() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        LassePermission.getPermission(this, permissions, new LassePermission.PermissionListener() {
            @Override
            public void onRequestResult(boolean allGranted, ArrayList<String> deniedPermissions) {

                Toast.makeText(MainActivity.this, "getPermission Result : " + allGranted, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void test2() {
        LassePermission.getOverlay(this, new LassePermission.OverlayListener() {
            @Override
            public void onCheckCompleted(boolean result) {
                Toast.makeText(MainActivity.this, "getOverlay Result : " + result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void test3() {
        Toast.makeText(MainActivity.this, "isDevModeEnabled Result : " + LasseTools.getInstance().isDevModeEnabled(), Toast.LENGTH_SHORT).show();
    }

    public void test4() {
        Toast.makeText(MainActivity.this, "isUsbDebuggingEnabled Result : " + LasseTools.getInstance().isUsbDebuggingEnabled(), Toast.LENGTH_SHORT).show();
    }
}
