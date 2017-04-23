package me.mohamey.catastic;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class SendFact extends AppCompatActivity {
    private final String TAG = "SendFact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_fact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        new QueryApi().execute(100);
        findViewById(R.id.loading_panel).setVisibility(View.GONE);
        findViewById(R.id.catfact_textview).setVisibility(View.VISIBLE);
    }

    private class CatFactResponse{
        boolean success;
        ArrayList<String> facts;

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setFacts(ArrayList<String> facts) {
            this.facts = facts;
        }

        public ArrayList<String> getFacts() {
            return facts;
        }
    }

    private class QueryApi extends AsyncTask<Integer, Void, CatFactResponse>{
        @Override
        protected CatFactResponse doInBackground(Integer... integers){
            CatFactResponse result = new CatFactResponse();
            try{
                if (integers.length != 1 || integers[0] == 0){
                    throw new Exception("Invalid number of arguments provided");
                }

                int numFacts = integers[0];

                // Build Request
                String urlParams = String.format("?number=%d", numFacts);
                String url = getResources().getString(R.string.api_endpoint) + urlParams;
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                Log.d(TAG, "Opening HTTP Connection");

                // Configure Request method
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");

                Log.d(TAG, Integer.toString(con.getResponseCode()));
                Log.d(TAG, con.getResponseMessage());

                // Read the response
                InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                String res = IOUtils.toString(in, encoding);

                // Convert to POJO
                JSONObject responseObj = new JSONObject(res);
                result.setSuccess(responseObj.getBoolean("success"));
                if (result.isSuccess()){
                    JSONArray factArray = responseObj.getJSONArray("facts");
                    ArrayList<String> facts = new ArrayList<String>();

                    for (int i=0; i < factArray.length(); i++){
                        facts.add(factArray.getString(i));
                    }

                    result.setFacts(facts);
                } else {
                    result.setFacts(null);
                }

                Log.d(TAG, res);
            }catch (Exception e){
                result.setSuccess(false);
                result.setFacts(null);
                Log.e(TAG, e.toString());
            }

            return result;
        }
    }

}
