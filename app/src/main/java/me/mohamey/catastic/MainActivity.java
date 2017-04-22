package me.mohamey.catastic;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;
    private static final String TAG = "MainActivity";
    protected CookieManager cookieManager = new CookieManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set cookie handler
        CookieHandler.setDefault(cookieManager);

        final TextInputLayout numberWrapper = (TextInputLayout) findViewById(R.id.numberWrapper);
        final TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);

        numberWrapper.setHint("Phone Number");
        passwordWrapper.setHint("Password");

        Button loginButton = (Button) findViewById(R.id.button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Log.i(TAG, "Click Received!");
                // Hide the keyboard
                hideKeyboard();

                String number = null, password = null;
                // Get input data
                try{
                    number = numberWrapper.getEditText().getText().toString();
                    password = passwordWrapper.getEditText().getText().toString();
                }catch (NullPointerException e){
                    Log.e(TAG, e.toString());
                }

                // Make sure values aren't null
                if (number == null || password == null){
                    Log.e(TAG, "Username or password was null");
                    return;
                }

                if (number.length() != 10){
                    Log.d(TAG, String.format("Number is %s", number));
                    numberWrapper.setError("Not a valid Number!");
                } else if (!validatePassword(password)) {
                    Log.d(TAG, String.format("Password is %s", password));
                    passwordWrapper.setError("Not a valid Password!");
                } else{
                    numberWrapper.setErrorEnabled(false);
                    passwordWrapper.setErrorEnabled(false);
                    login(number, password);
                }


            }
        });
    }

    public boolean validatePassword(String password) {
        return password.length() > 0;
    }

    public void login(String username, String password){
        Log.d(TAG, "Logging in!");
        LoginObject obj = new LoginObject(username, password);
        try{
            LoginResult res = new Login().execute(obj).get();
            Toast.makeText(getApplicationContext(), res.getMessage(), Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            Log.e(TAG, e.toString());
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null){
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private class LoginResult {
        private boolean result;
        private String message;

        public void setMessage(String message) {
            this.message = message;
        }

        public void setResult(boolean result) {
            this.result = result;
        }

        public String getMessage() {
            return message;
        }

        public boolean isResult() {
            return result;
        }
    }

    private class LoginObject {
        private String number;
        private String password;

        public LoginObject (String number, String password){
            this.number = number;
            this.password = password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getPassword() {
            return password;
        }

        public String getNumber() {
            return number;
        }
    }

    private class Login extends AsyncTask<LoginObject, Void, LoginResult>{
        protected LoginResult doInBackground(LoginObject... loginObjects){
            LoginResult loginResult = new LoginResult();

            try{
                if (loginObjects.length != 1){
                    throw new Exception("Number of Login Objects passed to AsycnTask did not equal 1");
                }

                LoginObject loginObject = loginObjects[0];
                String url = getResources().getString(R.string.login_endpoint);
                URL obj = new URL(url);
                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                Log.d(TAG, "Opening HTTP Connection");

                // Configure Request method
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);
                con.setDoInput(true);
                Log.d(TAG, "Set Request properties");

                // Create the JSON Object to Post
                JSONObject payload = new JSONObject();
                payload.put("msisdn", loginObject.getNumber());
                payload.put("pin", loginObject.getPassword());
                Log.d(TAG, "Created JSON Object");

                // Get the output bytestream
                OutputStream os = con.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();
                Log.d(TAG, "Wrote JSON to output stream");

                // Read the response
                InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                String res = IOUtils.toString(in, encoding);

                if (res.length() == 0){
                    throw new Exception("Empty Response from server");
                }

                CookieStore cookieStore = cookieManager.getCookieStore();
                List<HttpCookie> cookieList = cookieStore.getCookies();

                Log.d(TAG, "Retrieved Response");

                if (res.contains("Logged in as")){
                    loginResult.setResult(true);
                    loginResult.setMessage("Successfully Logged in");
                } else{
                    loginResult.setResult(false);
                    loginResult.setMessage("Log in failed, check username password combination");
                }

            } catch(Exception e){
                loginResult.setResult(false);
                loginResult.setMessage("Something went wrong.");
                Log.e(TAG, e.toString());
            }

            Log.d(TAG, "Returning result");
            return loginResult;

        }
    }
}