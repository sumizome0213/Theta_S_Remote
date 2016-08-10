package nittcprocon.thetasremote;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ThetaS_Shutter thetas_shutter = new ThetaS_Shutter();

        ImageButton shutter = (ImageButton)findViewById(R.id.shutter);
        assert shutter != null;
        shutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){ //シャッター切る

                thetas_shutter.shutter();

            }
        });

        ImageButton modevideo = (ImageButton)findViewById(R.id.modevideo);
        assert modevideo != null;
        modevideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //動画モード

                thetas_shutter.mode(true);

            }
        });

        ImageButton modecamera = (ImageButton)findViewById(R.id.modecamera);
        assert  modecamera != null;
        modecamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //写真モード

                thetas_shutter.mode(false);

            }
        });

        ImageButton rec = (ImageButton)findViewById(R.id.rec);
        assert rec != null;
        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //録画開始

                thetas_shutter.startcapture();

            }
        });

        ImageButton stop = (ImageButton)findViewById(R.id.stop);
        assert stop != null;
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //録画停止

                thetas_shutter.stopcapture();

            }
        });

        }
//    void alert(){
//        new AlertDialog.Builder(MainActivity.this)
//                .setTitle("title")
//                .setMessage("message")
//                .setPositiveButton("OK", null)
//                .show();
//    }




}

class ThetaS_Shutter{

    String connect(){
        String sessionID = "";
        String result;
        int count = 0;

        // sessionIDの取得
        do {
            result = run("{\"name\": \"camera.startSession\" ,\"parameters\": {}}");

            try {
                JSONObject js = new JSONObject(result);
                sessionID = js.getJSONObject("results").getString("sessionId");
            } catch (JSONException e){
                e.printStackTrace();
            }

            count++;

            Log.d("sessionID", sessionID);

        }while(sessionID == "" && count < 5 ); // 6回チャレンジして諦め

        /*if (count >= 5){
            MainActivity.alert();
        }*/

        return sessionID;
    }

    void disconnect(String sid){
        run("{\"name\": \"camera.closeSession\" ,\"parameters\":{\"sessionId\" :\"" + sid + "\"}}");
    }

    void shutter(){
        String sid;
        sid = connect();
        run("{\"name\": \"camera.takePicture\" ,\"parameters\": {\"sessionId\" :\"" + sid + "\"}}");
        disconnect(sid);
    }

    void startcapture(){
        String sid;
        sid = connect();
        run("{\"name\": \"camera._startCapture\" ,\"parameters\": {\"sessionId\" :\"" + sid + "\"}}");
        disconnect(sid);
    }

    void stopcapture(){
        String sid;
        sid = connect();
        run("{\"name\": \"camera._stopCapture\" ,\"parameters\": {\"sessionId\" :\"" + sid + "\"}}");
        disconnect(sid);
    }

    void mode(boolean mode){
        String sid;
        sid = connect();

        String value;
        if (mode){
            value = "_video";
        }else {
            value = "image";
        }

        option(sid, "captureMode", value);
        disconnect(sid);
    }

    public void option(String sid, String option_name, String option_value){
        run("{\"name\": \"camera.setOptions\" ,\"parameters\": {\"sessionId\" :\"" + sid + "\", \"options\": {\"" + option_name + "\": \"" + option_value + "\"}}}");
    }

    public synchronized String run(final String payload){
        final String[] result1 = {""};
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String result = null;

                // リクエストボディを作る
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json"), payload
                );

                // リクエストオブジェクトを作って
                Request request = new Request.Builder()
                        .url("http://192.168.1.1/osc/commands/execute")
                        .post(requestBody)
                        .build();

                // クライアントオブジェクトを作って
                OkHttpClient client = new OkHttpClient();



                // リクエストして結果を受け取って
                try {
                    Response response = client.newCall(request).execute();
                    result = response.body().string();
                    Log.d("result", result);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                // 返す
                result1[0] = result;
                return result;
            }

        }.execute();

        sleep(500); //500ms待つ
        return result1[0];
    }

    public synchronized void sleep(long msec)
    {
        try
        {
            wait(msec);
        }catch(InterruptedException e){}
    }



}


