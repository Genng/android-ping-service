package com.hwd.lc.hwdping;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyService extends Service {

    private static final String IP_ADDRESS = "183.196.130.125";
    private Process p;
    private NotifyView mToast;
    private String mMsg = "";
    private Handler handler = new Handler();


    public MyService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mToast = new NotifyView(this);

        checkTimeOut();

        new NetPingTask().execute(IP_ADDRESS, String.valueOf(Integer.MAX_VALUE));
    }

    private void checkTimeOut() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    String tmpMsg = mMsg;
                    SystemClock.sleep(3000);
                    if (tmpMsg.equals(mMsg)) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mToast.show("连接服务器超时");
                            }
                        });
                    }
                }
            }
        }.start();
    }

    private class NetPingTask extends AsyncTask<String, String, String> {

        private void ping(String paramString) {
            try {
                Log.i("hwd", "ping 执行");
                MyService.this.p = Runtime.getRuntime().exec(paramString);
                BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(MyService.this.p.getInputStream()), 8192);
                while (true) {
                    String str = localBufferedReader.readLine();
                    if (str == null) {
                        Log.i("hwd", "str 为null");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MyService.this, "请检查网络", Toast.LENGTH_SHORT).show();
                            }
                        });
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        }, 1000);
                        return;
                    }
                    publishProgress(str);
                }
            } catch (IOException localIOException) {
                Log.i("hwd", "io异常");
                localIOException.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String[] params) {
            String str = params[0];
            if ((str.contains(".")) && (str.length() > 3))
                try {
                    ping("ping -c " + params[1] + " " + str);
                } catch (Exception e) {
                    Log.e("Ping", "Error: " + e.getMessage());
                }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("hwd", "onPostExecute   -->" + s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String ms = values[0].replaceAll(".+from \\d+.+ttl=\\d+.+time=(.+).+ms", "$1");
            mMsg = "服务器延迟->" + ms;
            mToast.show(mMsg);
            Log.i("hwd", "onProgressUpdate    " + values[0]);
        }
    }
}
