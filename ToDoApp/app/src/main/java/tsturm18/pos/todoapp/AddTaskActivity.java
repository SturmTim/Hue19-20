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

public class AddTaskActivity extends AppCompatActivity {

    Picker picker = new Picker();
    TextView editDate;
    TextView editTime;

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

        Task task = new Task(title.getText().toString(),editDate.getText().toString() + " " + editTime.getText().toString(),detail.getText().toString());
        Intent intent =new Intent();
        intent.putExtra("addedTask", task);

        setResult(RESULT_OK,intent);

        finish();
    }
}