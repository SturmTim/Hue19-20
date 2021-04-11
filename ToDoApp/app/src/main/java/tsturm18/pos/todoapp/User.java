package tsturm18.pos.todoapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class User implements Parcelable{

    private final String username;
    private final String password;
    InternetConnection internetConnection = new InternetConnection();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    //sickinger-solutions.at/notesserver/register.php

    protected User(Parcel in) {
        username = in.readString();
        password = in.readString();
    }

    public boolean signUp(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username",username);
            jsonObject.put("password", password);
            jsonObject.put("name", "x");

            InternetConnection.Response response = internetConnection.post("http://sickinger-solutions.at/notesserver/register.php?username="+ username +"&password=" + password, jsonObject.toString());
            boolean success = response.startWith(2);
            response.close();
            return success;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean correctLogin(){
        InternetConnection.Response response = internetConnection.post(
                "http://sickinger-solutions.at/notesserver/todolists.php?username="+ username +"&password=" + password,
                ""
        );
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.inputStream));
            JsonObject json = new JsonParser().parse(bufferedReader.readLine()).getAsJsonObject();
            response.close();
            System.out.println(json.toString());
            return !json.get("message").getAsString().equals("username or password not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public boolean validPassword(){
        String pattern = "^[A-Za-z0-9]{1,}$";
        return password.matches(pattern);
    }

    public boolean validUsername(){
        String pattern = "^[A-Za-z0-9]{1,}$";
        return username.matches(pattern);
    }
}
