package tsturm18.pos.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class EditTaskActivity extends AppCompatActivity {

    Picker picker = new Picker();
    EditText title;
    EditText detail;
    TextView editDate;
    TextView editTime;
    Task originalTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        Intent intent = getIntent();
        originalTask = intent.getParcelableExtra("task");

        title = findViewById(R.id.editTitle);
        title.setText(originalTask.getTitle());

        detail = findViewById(R.id.editDetail);
        detail.setText(originalTask.getDetails());

        editDate = findViewById(R.id.editDate);
        editDate.setText(originalTask.getDateTime().split(" ")[0]);

        editTime = findViewById(R.id.editTime);
        editTime.setText(originalTask.getDateTime().split(" ")[1]);
    }

    public void clickDate(View view){
        picker.pickDate(editDate, EditTaskActivity.this);
    }

    public void clickTime(View view){
        picker.pickTime(editTime, EditTaskActivity.this);
    }

    public void cancel(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void changeEdit(View view){

        Task task = new Task(title.getText().toString(),editDate.getText().toString() + " " + editTime.getText().toString(),detail.getText().toString(),originalTask.isDone());
        Intent intent =new Intent();
        intent.putExtra("changedTask", task);

        setResult(RESULT_OK,intent);

        finish();
    }

}