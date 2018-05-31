package com.example.wutthiphongthu.sim_c4050;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.example.wutthiphongthu.sim_c4050.R;
import com.zebra.adc.decoder.Barcode2DWithSoft;

public class MainActivity extends AppCompatActivity {
    Thread thread;
    TextView tv_scan_result;
    Barcode2DWithSoft mReader;
    boolean threadStop = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_scan_result = findViewById(R.id.tv_scan_result);

        if(Build.MODEL.equalsIgnoreCase("C4050-Q4")&&Build.DEVICE.equalsIgnoreCase("C4050-Q4")) {

            try {
                mReader = Barcode2DWithSoft.getInstance();
                new InitTaskScanner().execute();
            } catch (Exception ex) {
                Log.e("getInstanceError",ex.toString());
                return;
            }
        }else{
        }
    }

    //    Start Scan
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == 139) {
            if (event.getRepeatCount() == 0) {
                doDecode();
            }
        }
        return super.onKeyDown(keyCode, event);

    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 139) {
            mReader.stopScan();
        }
        return super.onKeyUp(keyCode, event);
    }


//สร้างScanner
    public class InitTaskScanner extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {

            boolean result = false;

            if (mReader != null) {
                result = mReader.open(MainActivity.this);
                if (result) {
                    mReader.setParameter(324, 1);
                    mReader.setParameter(300, 0); // Snapshot Aiming
                    mReader.setParameter(361, 0); // I mage Capture Illumination
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
            if (mReader != null) {
                mReader.setScanCallback(doc_scan);
            }

        }
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(MainActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("Loading...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

    }
    public Barcode2DWithSoft.ScanCallback doc_scan = new Barcode2DWithSoft.ScanCallback() {
        @Override
        public void onScanComplete(int i, int length, byte[] data) {
            if(data!=null&&data.length!=0) {
                String doc_no = new String(data).trim();
                tv_scan_result.setText(doc_no);
            }
        }
    };
    private void doDecode() {
        if (threadStop) {
            boolean bContinuous = false;
            int iBetween = 0;
            thread = new DecodeThread(bContinuous, iBetween);
            thread.start();
        }
        if (mReader != null) {

            mReader.stopScan();
            if(tv_scan_result.isFocused()) {
                mReader.setScanCallback(doc_scan);
            }
        }
    }
    private class DecodeThread extends Thread {
        private boolean isContinuous = false;
        private long sleepTime = 1000;

        public DecodeThread(boolean isContinuous, int sleep) {
            this.isContinuous = isContinuous;
            this.sleepTime = sleep;
        }

        @Override
        public void run() {
            super.run();
            do {
                mReader.scan();

                if (isContinuous) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (isContinuous && !threadStop);
        }

    }
//    End Scan
}

