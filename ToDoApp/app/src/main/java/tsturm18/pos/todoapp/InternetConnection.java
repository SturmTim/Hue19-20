package tsturm18.pos.todoapp;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class InternetConnection {

    public boolean isNetworkAvailable(Activity activity){
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public Response post(String urlString, String data){

        final Response[] responses = new Response[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    URL url = new URL(urlString);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type","application/json");
                    httpURLConnection.setFixedLengthStreamingMode(data.getBytes().length);
                    httpURLConnection.getOutputStream().write(data.getBytes());
                    httpURLConnection.getOutputStream().flush();

                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode/100==4||responseCode/100==5){
                        responses[0] = new Response(httpURLConnection.getErrorStream(), responseCode);
                    }else {
                        responses[0] = new Response(httpURLConnection.getInputStream(), responseCode);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return responses[0];
    }

    public Response put(String urlString, String data){

        final Response[] responses = new Response[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    URL url = new URL(urlString);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestMethod("PUT");
                    httpURLConnection.setRequestProperty("Content-Type","application/json");
                    httpURLConnection.setFixedLengthStreamingMode(data.getBytes().length);
                    httpURLConnection.getOutputStream().write(data.getBytes());
                    httpURLConnection.getOutputStream().flush();

                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode/100==4||responseCode/100==5){
                        responses[0] = new Response(httpURLConnection.getErrorStream(), responseCode);
                    }else {
                        responses[0] = new Response(httpURLConnection.getInputStream(), responseCode);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return responses[0];
    }

    public Response delete(String urlString){
        final Response[] responses = new Response[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    URL url = new URL(urlString);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestMethod("DELETE");
                    httpURLConnection.setRequestProperty("Content-Type","application/json");

                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode/100==4||responseCode/100==5){
                        responses[0] = new Response(httpURLConnection.getErrorStream(), responseCode);
                    }else {
                        responses[0] = new Response(httpURLConnection.getInputStream(), responseCode);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return responses[0];
    }

    public Response get(String urlString){
        final Response[] responses = new Response[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    URL url = new URL(urlString);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("Content-Type","application/json");

                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode/100==4||responseCode/100==5){
                        responses[0] = new Response(httpURLConnection.getErrorStream(), responseCode);
                    }else {
                        responses[0] = new Response(httpURLConnection.getInputStream(), responseCode);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return responses[0];
    }

    public static class Response{
        InputStream inputStream;
        int responseCode;

        public Response(InputStream inputStream, int responseCode) {
            this.inputStream = inputStream;
            this.responseCode = responseCode;
        }

        public boolean startWith(int code){
            return responseCode/100 == code;
        }

        public void close(){
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
