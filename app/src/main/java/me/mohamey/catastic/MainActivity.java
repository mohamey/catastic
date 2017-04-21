package me.mohamey.catastic;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                // TODO
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
        Toast.makeText(getApplicationContext(), "Logging in!", Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null){
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}