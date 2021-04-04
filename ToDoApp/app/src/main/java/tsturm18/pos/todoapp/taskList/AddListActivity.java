package tsturm18.pos.todoapp.taskList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import tsturm18.pos.todoapp.R;

public class AddListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);
    }

    public void cancel(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void add(View view){
        EditText title = findViewById(R.id.listTitle);

        TaskList taskList = new TaskList(title.getText().toString());
        Intent intent =new Intent();
        intent.putExtra("addedList", taskList);

        setResult(RESULT_OK,intent);

        finish();
    }
}