package com.lasselindh.tools;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class LassePermission extends AppCompatActivity {
    public static final int DENIED = 0;
    public static final int GRANTED = 1;
    public static final int ALWAYS_DENIED = -1;
    private static final int REQUEST_OVERLAY = 1000;
    private static PermissionListener mPermissionListener;
    public interface PermissionListener {
        void onCheckCompleted(int result);
    }

    private static OverlayListener mOverlayListener;
    public interface OverlayListener {
        void onCheckCompleted(boolean result);
    }

    private String[] mPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if(getIntent().getBooleanExtra("overlay", false)) {
            if(canDrawOverlays(this)) {
                mOverlayListener.onCheckCompleted(true);
                finish();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
            }
        } else {
            CheckPermissionApp(getIntent().getStringArrayExtra("permission"));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_OVERLAY: {
                if(canDrawOverlays(this)) {
                    mOverlayListener.onCheckCompleted(true);
                    finish();
                } else {
                    mOverlayListener.onCheckCompleted(false);
                    finish();
                }
                break;
            }

        }
    }

    public boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        else if (Build.VERSION.SDK_INT >= 26) {
            return Settings.canDrawOverlays(context);
        } else {
            if (Settings.canDrawOverlays(context))
                return true;
            return false;
        }
    }


    private void CheckPermissionApp(String[] pers) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissions = pers;
            int permissionTotal = 0;
            for(int i=0 ; i<mPermissions.length ; i++) {
                if(checkSelfPermission(mPermissions[i]) == PackageManager.PERMISSION_DENIED) {
                    permissionTotal++;
                    if((shouldShowRequestPermissionRationale(mPermissions[i]))) {
                        // 두번이상 거부를 한 경우
                    }
                }
            }

            if(permissionTotal == 0) {
                // 권한설정 다 되어 있음
                mPermissionListener.onCheckCompleted(GRANTED);
                finish();
            } else {
                requestPermissions(mPermissions, 0);
            }
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != 0) {
            return;
        }

        boolean isDenied = false;
        for(int i=0 ; i<grantResults.length ; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                // 하나라도 거부된 것이 있는지 확인
                isDenied = true;
            }
        }

        if(isDenied) {
            for(int i=0 ; i<mPermissions.length ; i++) {
                if((shouldShowRequestPermissionRationale(mPermissions[i]))) {
                    // 거부를 한번 누른 경우
                    mPermissionListener.onCheckCompleted(DENIED);
                } else {
                    // 다시보지 않기를 누르고 항상 거부를 한 경우
                    mPermissionListener.onCheckCompleted(ALWAYS_DENIED);
                }
            }

            finish();
        } else {
            // 권한을 다 받은 상태
            mPermissionListener.onCheckCompleted(GRANTED);
            finish();
        }
    }

    public static void getPermission(Context context, String[] permissions, PermissionListener listener) {
        mPermissionListener = listener;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPermissionListener.onCheckCompleted(1);
        } else {
            Intent intent = new Intent(context, LassePermission.class);
            intent.putExtra("overlay", false);
            intent.putExtra("permission", permissions);
            context.startActivity(intent);
        }
    }


    public static void getOverlay(Context context, OverlayListener listener) {
        mOverlayListener = listener;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mOverlayListener.onCheckCompleted(true);
        } else {
            if (Settings.canDrawOverlays(context)) {
                mOverlayListener.onCheckCompleted(true);
            } else {
                Intent intent = new Intent(context, LassePermission.class);
                intent.putExtra("overlay", true);
                context.startActivity(intent);
            }
        }
    }
}