package com.hwd.lc.hwdping;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service {

    private String mIPAddr;
    private Process p;
    private String mMsg = "";
    private NotifyView mNotifyView;
    private Handler handler = new Handler();
    private static final int CONNECT_TIME_OUT = 10000;
    private static final int READ_TIME_OUT = 10000;
    private static final long CHECK_NET_INTERVAL = 3000L;
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


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
        init();
    }

    private void init() {
        mNotifyView = new NotifyView(this);
        executorService.submit(getIPAddress);
    }

    Runnable getIPAddress = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL("https://raw.githubusercontent.com/Eidon0725/config_properties/master/ecgf.properties");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(CONNECT_TIME_OUT);
                conn.setReadTimeout(READ_TIME_OUT);
                conn.setRequestMethod("GET");
                InputStream inputStream = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                mIPAddr = baos.toString().replaceAll(".*ECGF_NET_ADDRESS.*= *((\\d{1,3}\\.){3}\\d{1,3})\\n?.*", "$1");

                executorService.submit(checkTimeout);
                executorService.submit(pingServer);
            } catch (Exception e) {
                Toast.makeText(MyService.this, "pls check yr net is available", Toast.LENGTH_SHORT).show();
            }
        }
    };

    Runnable pingServer = new Runnable() {

        @Override
        public void run() {
            try {
                MyService.this.p = Runtime.getRuntime().exec("ping " + mIPAddr);
                BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(MyService.this.p.getInputStream()), 8192);
                while (true) {
                    String str = localBufferedReader.readLine();
                    if (str == null) {
                        handler.post(showNoNet);
                        SystemClock.sleep(1000);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        return;
                    }
                    handler.post(updateNotifyView(str));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable updateNotifyView(final String str) {
        return new Runnable() {
            @Override
            public void run() {
                String ms = str.replaceAll(".+from.+ttl=\\d+.+time=(.+).+ms", "$1");
                mMsg = mIPAddr + "--> " + ms + "ms";
                mNotifyView.show(mMsg);
            }
        };
    }


    Runnable showNoNet = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MyService.this, "pls check yr net", Toast.LENGTH_SHORT).show();
        }
    };

    Runnable checkTimeout = new Runnable() {
        @Override
        public void run() {
            for (; ; ) {
                String tmpMsg = mMsg;
                SystemClock.sleep(CHECK_NET_INTERVAL);
                if (tmpMsg.equals(mMsg)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mNotifyView.show("connect " + mIPAddr + " timeout");
                        }
                    });
                }
            }
        }
    };

}
