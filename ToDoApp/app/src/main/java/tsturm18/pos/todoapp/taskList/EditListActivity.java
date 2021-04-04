package tsturm18.pos.todoapp.taskList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import tsturm18.pos.todoapp.R;
import tsturm18.pos.todoapp.task.Task;

public class EditListActivity extends AppCompatActivity {


    TaskList originalTaskList;
    EditText title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        Intent intent = getIntent();
        originalTaskList = intent.getParcelableExtra("taskList");

        title = findViewById(R.id.listTitle);
        title.setText(originalTaskList.getName());
    }

    public void cancel(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void changeEdit(View view){
        TaskList taskList = new TaskList(title.getText().toString(),originalTaskList.getTasks());
        Intent intent =new Intent();
        intent.putExtra("changedTaskList", taskList);

        setResult(RESULT_OK,intent);
        finish();
    }




}