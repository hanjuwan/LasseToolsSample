package com.lasselindh.tools;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;


public class LasseTools {
    private static LasseTools instance;
    private WebView mWebView;
    private Context mContext;
    private HashMap<String, Object> activityStack;
    private static Toast mCToast;
    private String file_name;

    public LasseTools() {
        activityStack = new HashMap<>();
    }

    public static LasseTools getInstance() {
        if (instance == null) {
            instance = new LasseTools();
        }

        return instance;
    }

    public void init(Context context) {
        mContext = context;
        if(!BuildConfig.DEBUG) {
            return;
        }
        if(Build.VERSION.SDK_INT >= 19) {
            mWebView = new WebView(mContext);
            mWebView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            mWebView.setPadding(0, 0, 0, 0);
            mWebView.setInitialScale(1);
            mWebView.addJavascriptInterface(new CustomJavascriptInterface(mContext), this.getClass().getSimpleName());
            WebView.setWebContentsDebuggingEnabled(true);
            mWebView.setHorizontalScrollBarEnabled(false);
            mWebView.setVerticalScrollBarEnabled(false);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setDatabaseEnabled(true);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setUseWideViewPort(true);;
            mWebView.getSettings().setDomStorageEnabled(true);
            mWebView.setWebViewClient(new WebViewClient());
            mWebView.setWebChromeClient(new WebChromeClient());
//            mWebView.loadUrl("file:///android_asset/index.html");

            String content =
//                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
                    "<html><head><title>LasseTools</title>"+
                            "</head></html>";
            mWebView.loadDataWithBaseURL("", content, "text/html", "UTF-8", "http://lasselindh.tistory.com/");
        }
    }

    /**
     * 캡쳐하거나 메서드를 실행할 화면을 등록
     * @param name
     */
    public void setScreen(Object name) {
        if(!BuildConfig.DEBUG) {
            return;
        }
        activityStack.put(name.getClass().getSimpleName(), name);
    }

    /**
     * 개발전용 토스트
     * @param context
     * @param text
     */
    public static void DToast(Context context, String text) {
        if(!BuildConfig.DEBUG)
            return;

        if(mCToast != null && mCToast.getView().getVisibility() == View.VISIBLE) {
            mCToast.cancel();
        }

        String path = context.getClass().getName();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.lasse_toast, null);
        ((LinearLayout)view.findViewById(R.id.ctoast_layout)).setBackgroundColor(PreferenceManager.getDefaultSharedPreferences(context).getInt("color", Color.parseColor("#656D78")));
        TextView tv = (TextView)view.findViewById(R.id.tv_toast);
        tv.setText(text);
        Toast toast = new Toast(context);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 400);
        toast.setDuration(Toast.LENGTH_LONG);

        mCToast = toast;
        mCToast.show();
    }

    /**
     * 개발자모드 가져오기
     * @return
     */
    public boolean isDevModeEnabled() {
        if(Build.VERSION.SDK_INT == 16) {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED , 0) != 0;
        } else if (Build.VERSION.SDK_INT >= 17) {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0) != 0;
        } else return false;
    }

    /**
     * USB디버깅 모드 가져오기
     * @return
     */
    public boolean isUsbDebuggingEnabled() {
        return Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 0;
    }

    /**
     * 로그캣저장하기
     */

    @TargetApi(19)
    private void saveLog() {
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        LassePermission.getPermission(mContext, permissions, new LassePermission.PermissionListener() {
            @Override
            public void onCheckCompleted(int result) {
                if (result == 1) {
                    String ex_storage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
                    // Get Absolute Path in External Sdcard
                    file_name = "/" + System.currentTimeMillis() + ".log";

                    File logFile = new File(ex_storage + file_name); // log file name
                    int sizePerFile = 1024; // size in kilobytes
                    int rotationCount = 10; // file rotation count

                    String[] args = new String[]{"logcat",
                            "-v", "time",
                            "-f", logFile.getAbsolutePath(),
                            "-r", Integer.toString(sizePerFile),
                            "-n", Integer.toString(rotationCount),
                            "*:V"};

                    try {
                        Runtime.getRuntime().exec(args);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mWebView.evaluateJavascript("console.log('logcat저장이 완료되었습니다. 폰에서 Documents 폴더를 확인 또는 LasseTools.showLogcat() 을 쳐보세요')", null);
                }
            }
        });
    }

    /**
     * 특정 뷰만 캡쳐
     * @param View
     */
    public void captureView(View View) {
        View.buildDrawingCache();
        Bitmap captureView = View.getDrawingCache();
        FileOutputStream fos;

        String strFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        File folder = new File(strFolderPath);
        if(!folder.exists()) {
            folder.mkdirs();
        }

        String strFilePath = strFolderPath + "/" + System.currentTimeMillis() + ".png";
        File fileCacheItem = new File(strFilePath);

        try {
            fos = new FileOutputStream(fileCacheItem);
            captureView.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 액티비티 전체 캡쳐
     * @param context
     */
    String bitmapString;
    public void captureActivity(final Activity context, final boolean isSave) {
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        LassePermission.getPermission(context, permissions, new LassePermission.PermissionListener() {
            @Override
            public void onCheckCompleted(int result) {
                if(result != 1) return;
                if(context == null) return;
                View root = context.getWindow().getDecorView().getRootView();
                root.setDrawingCacheEnabled(true);
                root.buildDrawingCache();
                // 루트뷰의 캐시를 가져옴
                Bitmap screenshot = root.getDrawingCache();

                // get view coordinates
                int[] location = new int[2];
                root.getLocationInWindow(location);

                // 이미지를 자를 수 있으나 전체 화면을 캡쳐 하도록 함
                Bitmap bmp = Bitmap.createBitmap(screenshot, location[0], location[1], root.getWidth(), root.getHeight(), null, false);

                if(isSave) {
                    String strFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
                    File folder = new File(strFolderPath);
                    if(!folder.exists()) {
                        folder.mkdirs();
                    }

                    String strFilePath = strFolderPath + "/" + System.currentTimeMillis() + ".png";
                    File fileCacheItem = new File(strFilePath);
                    OutputStream out = null;
                    try {
                        fileCacheItem.createNewFile();
                        out = new FileOutputStream(fileCacheItem);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                        root.setDrawingCacheEnabled(false);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        try {
                            out.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    root.setDrawingCacheEnabled(false);
                    bitmapString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
                    String content =
                            "<html><head><title>LasseTools</title>"+
                                    "</head><body><img style=\"width:100%;\" src=\"data:image/png;base64," +bitmapString +"\"/></body></html>";
                    mWebView.loadDataWithBaseURL("", content, "text/html", "UTF-8", "http://lasselindh.tistory.com/");
                    mWebView.setVisibility(View.VISIBLE);
                }

                bitmapString = "";
            }
        });
    }

    @TargetApi(19)
    public class CustomJavascriptInterface {
        private Context mContext;
        public CustomJavascriptInterface(Context context) {
            mContext = context;
        }

//        @JavascriptInterface
//        public final void CToast(final String message) {
//            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
//        }

        /**
         * 폴더에 저장
         */
        @JavascriptInterface
        public final void saveScreen () {
            if(!activityStack.isEmpty()) {
                ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> info = am.getRunningTasks(1);
                ComponentName topActivity = info.get(0).topActivity;
                String topActivityName = topActivity.getShortClassName();
                int pos = topActivityName.lastIndexOf( "." );
                topActivityName = topActivityName.substring( pos + 1 );
                final Object mObject = activityStack.get(topActivityName);
                if(mObject != null)
                    captureActivity((Activity)mObject, true);
            }
        }

        /**
         * 디버깅 웹뷰로 가져오기
         */
        @JavascriptInterface
        public final void getScreen () {
            if(!activityStack.isEmpty()) {
                ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> info = am.getRunningTasks(1);
                ComponentName topActivity = info.get(0).topActivity;
                String topActivityName = topActivity.getShortClassName();
                int pos = topActivityName.lastIndexOf( "." );
                topActivityName = topActivityName.substring( pos + 1 );
                final Object mObject = activityStack.get(topActivityName);
                if(mObject != null)
                    captureActivity((Activity)mObject, false);
            }
        }

        /**
         * 단일 메서드 실행
         * @param activityName
         * @param methodName
         */
        @JavascriptInterface
        public final void runMethod(String activityName, String methodName) {
            if(!activityStack.isEmpty()) {
                final Object mObject = activityStack.get(activityName);
                final String mOkMethod = methodName;
                if(mObject!=null && mOkMethod != null) {
                    try {
                        Class<?> cls = mObject.getClass();
                        Method method = cls.getMethod(mOkMethod);
                        method.invoke(mObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 파라메터 하나짜리 메서드
         * @param activityName
         * @param methodName
         * @param param1
         * @param param1Type
         */
        @JavascriptInterface
        public final void runMethod(String activityName, String methodName, String param1, String param1Type) {
            if(!activityStack.isEmpty()) {
                final Object mObject = activityStack.get(activityName);
                final String mOkMethod = methodName;
                String paramString = "";
                boolean paramBoolean = false;
                int paramInt = -1;
                if(mObject!=null && mOkMethod != null) {
                    switch (param1Type) {
                        case "String": {
                            paramString = param1;
                            try {
                                Class<?> cls = mObject.getClass();
                                Method method = cls.getDeclaredMethod(mOkMethod, String.class);
                                method.invoke(mObject, paramString);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } break;
                        case "int": {
                            paramInt = Integer.parseInt(param1);
                            try {
                                Class<?> cls = mObject.getClass();
                                Method method = cls.getDeclaredMethod(mOkMethod, int.class);
                                method.invoke(mObject, paramInt);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } break;
                        case "boolean": {
                            paramBoolean = Boolean.parseBoolean(param1);
                            try {
                                Class<?> cls = mObject.getClass();
                                Method method = cls.getDeclaredMethod(mOkMethod, boolean.class);
                                method.invoke(mObject, paramBoolean);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } break;
                        default:
                            break;
                    }
                }
            }
        }


        /**
         * 로그캣을 저장
         */
        @JavascriptInterface
        public final void saveLogcat() {
            saveLog();
        }

        /**
         * 로그캣을 저장
         */
        @JavascriptInterface
        public final void showLogcat() {
            String[] permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            LassePermission.getPermission(mContext, permissions, new LassePermission.PermissionListener() {
                @Override
                public void onCheckCompleted(int result) {
                    if(result != 1) return;

                    if(file_name == null || file_name.equals("")) {
                        mWebView.evaluateJavascript("console.log('saveLogcat() 후에 실행해주세요.')", null);
                    } else {
                        String everything = "";
                        String ex_storage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(ex_storage + file_name));
                            try {
                                StringBuilder sb = new StringBuilder();
                                String line = br.readLine();

                                while (line != null) {
                                    sb.append(line);
                                    sb.append(System.lineSeparator());
                                    line = br.readLine();

                                    mWebView.evaluateJavascript("console.log('"+ line +"')", null);
                                }
                            } finally {
                                br.close();
                            }
                        } catch (Exception e) {

                        }
                    }

                }
            });
        }
    }
}
