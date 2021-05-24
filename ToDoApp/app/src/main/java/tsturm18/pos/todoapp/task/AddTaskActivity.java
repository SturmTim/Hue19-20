package tsturm18.pos.todoapp.task;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import tsturm18.pos.todoapp.InternetConnection;
import tsturm18.pos.todoapp.Picker;
import tsturm18.pos.todoapp.R;

public class AddTaskActivity extends AppCompatActivity {

    Picker picker = new Picker();
    TextView editDate;
    TextView editTime;

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        editDate = findViewById(R.id.editDate);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        editDate.setText(LocalDate.now().format(dateFormatter));

        editTime = findViewById(R.id.editTime);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        editTime.setText(LocalTime.now().format(timeFormatter));

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

    }

    public void clickDate(View view){
        picker.pickDate(editDate, AddTaskActivity.this);
    }

    public void clickTime(View view){
        picker.pickTime(editTime, AddTaskActivity.this);
    }

    public void cancel(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void add(View view){
        EditText title = findViewById(R.id.editTitle);
        EditText detail = findViewById(R.id.editDetail);
        if (title.getText().toString().isEmpty() || detail.getText().toString().isEmpty()){
            Toast.makeText(this,"Title and details cannot be empty",Toast.LENGTH_LONG).show();
        }else {
            Task task = new Task(title.getText().toString(),editDate.getText().toString() + " " + editTime.getText().toString(),detail.getText().toString());
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,locationListener);
            }
            Thread thread = new Thread(() -> task.setLocation(getLocation()));
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Intent intent =new Intent();
            intent.putExtra("addedTask", task);

            setResult(RESULT_OK,intent);

            finish();
        }
    }

    public String getLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);

        String provider = locationManager.getBestProvider(criteria, false);

        Location location;

        String longitude = "";
        String latitude = "";
        String address = "";

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            try {
                location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    longitude = String.valueOf(location.getLongitude());
                    latitude = String.valueOf(location.getLatitude());

                    InternetConnection internetConnection = new InternetConnection();
                    InternetConnection.Response response = internetConnection.get("https://eu1.locationiq.com/v1/reverse.php?key=pk.7684f487d4c793c6cdfd80b9348f60bd&lat="+latitude+"&lon="+longitude+"&format=json");

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getInputStream()));
                    JsonObject json = new JsonParser().parse(bufferedReader.readLine()).getAsJsonObject();

                    JsonObject addressJson = json.getAsJsonObject("address");

                    address = addressJson.get("country").getAsString() + " " +
                            addressJson.get("state").getAsString() + " " +
                            addressJson.get("postcode").getAsString() + " " +
                            addressJson.get("village").getAsString() + " " +
                            addressJson.get("road").getAsString() + " " +
                            addressJson.get("house_number").getAsString();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (longitude.equals("") || latitude.equals("") || address.equals("")){
            return "No valid Location";
        }
        return address + " Longitude = " + longitude + " Latitude = " + latitude;
    }


}