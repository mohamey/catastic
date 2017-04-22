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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextInputLayout usernameWrapper = (TextInputLayout) findViewById(R.id.usernameWrapper);
        final TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);

        usernameWrapper.setHint("Username");
        passwordWrapper.setHint("Password");

        Button loginButton = (Button) findViewById(R.id.button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Log.i(TAG, "Click Received!");
                // Hide the keyboard
                hideKeyboard();

                String username = null, password = null;
                // Get input data
                try{
                    username = usernameWrapper.getEditText().getText().toString();
                    password = passwordWrapper.getEditText().getText().toString();
                }catch (NullPointerException e){
                    Log.e(TAG, e.toString());
                }

                // Make sure values aren't null
                if (username == null || password == null){
                    Log.e(TAG, "Username or password was null");
                    return;
                }

                if (!validateEmail(username)){
                    Log.d(TAG, String.format("Username is %s", username));
                    usernameWrapper.setError("Not a valid Email Address!");
                } else if (!validatePassword(password)) {
                    Log.d(TAG, String.format("Password is %s", password));
                    passwordWrapper.setError("Not a valid Password!");
                } else{
                    usernameWrapper.setErrorEnabled(false);
                    passwordWrapper.setErrorEnabled(false);
                    login(username, password);
                }


            }
        });
    }

    public boolean validateEmail(String email){
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean validatePassword(String password) {
        return password.length() > 5;
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
        private String username;
        private String password;

        public LoginObject (String username, String password){
            this.username = username;
            this.password = password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public String getUsername() {
            return username;
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
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
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
                payload.put("username", loginObject.getUsername());
                payload.put("password", loginObject.getPassword());
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

                JSONObject response = new JSONObject(res);
                Log.d(TAG, "Retrieved JSON Response");

                loginResult.setResult(response.getBoolean("result"));
                loginResult.setMessage(response.getString("message"));

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