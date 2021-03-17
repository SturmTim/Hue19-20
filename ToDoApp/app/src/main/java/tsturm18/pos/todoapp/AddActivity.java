package tsturm18.pos.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AddActivity extends AppCompatActivity {

    Picker picker = new Picker();
    TextView editDate;
    TextView editTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        editDate = findViewById(R.id.editDate);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        editDate.setText(LocalDate.now().format(dateFormatter));

        editTime = findViewById(R.id.editTime);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        editTime.setText(LocalTime.now().format(timeFormatter));
    }

    public void clickDate(View view){
        picker.pickDate(editDate,AddActivity.this);
    }

    public void clickTime(View view){
        picker.pickTime(editTime,AddActivity.this);
    }

    public void cancel(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void add(View view){
        EditText title = findViewById(R.id.editTitle);
        EditText detail = findViewById(R.id.editDetail);

        Task task = new Task(title.getText().toString(),editDate.getText().toString() + " " + editTime.getText().toString(),detail.getText().toString());
        Intent intent =new Intent();
        intent.putExtra("addedTask", task);

        setResult(RESULT_OK,intent);

        finish();
    }
}