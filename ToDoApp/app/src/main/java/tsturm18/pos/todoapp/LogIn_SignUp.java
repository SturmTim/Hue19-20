package tsturm18.pos.todoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LogIn_SignUp extends AppCompatActivity {

    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        Intent intent = getIntent();
        User user = intent.getParcelableExtra("currentUser");

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        if(user.validUsername() && user.validPassword()){
            Button registerButton = findViewById(R.id.signUp);
            registerButton.setVisibility(View.GONE);
            Button logInButton = findViewById(R.id.logIn);
            logInButton.setText("Logout");

            logInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logOut();
                }
            });

            username.setClickable(false);
            username.setFocusable(false);
            username.setCursorVisible(false);
            username.setText(user.getUsername());

            password.setClickable(false);
            password.setFocusable(false);
            password.setCursorVisible(false);
            password.setText(user.getPassword());
        }


    }

    public void logOut(){
        User user = new User("","");
        Intent intent = new Intent();
        intent.putExtra("user",user);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void logIn(View view){
        User user = new User(username.getText().toString(),password.getText().toString());
        if (user.correctLogin()){
            Intent intent = new Intent();
            intent.putExtra("user",user);
            setResult(RESULT_OK,intent);
        }else{
            Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public void signUp(View view){
        User user = new User(username.getText().toString(),password.getText().toString());
        if (user.validPassword() && user.validUsername()){
            if (!user.signUp()){
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Not a valid Password or Username", Toast.LENGTH_SHORT).show();
        }
    }
}