package tsturm18.pos.todoapp.task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import tsturm18.pos.todoapp.R;
import tsturm18.pos.todoapp.SettingActivity;
import tsturm18.pos.todoapp.taskList.TaskList;

public class TaskActivity extends AppCompatActivity {

    List<Task> fullTaskList = new ArrayList<>();
    List<Task> finishedTasks = new ArrayList<>();
    TaskList taskList;

    private static boolean allowedToWrite = false;
    private static boolean allowedToRead = false;

    private ListView taskView;

    private TaskAdapter taskAdapter;

    private static final int ADD_ACTIVITY_REQUEST_CODE = 0;
    private final static int SETTING_PREFERENCE = 1;
    private static final int Edit_ACTIVITY_REQUEST_CODE = 2;

    private SharedPreferences pref;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;

    int changedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        Intent intent = getIntent();
        taskList = intent.getParcelableExtra("tasks");

        TextView textView = findViewById(R.id.listName);
        textView.setText(taskList.getName());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        taskView = findViewById(R.id.taskList);

        taskAdapter = new TaskAdapter(this,R.layout.task_layout, fullTaskList, finishedTasks);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesChangeListener = this::preferenceChanged;
        pref.registerOnSharedPreferenceChangeListener( preferencesChangeListener );

        fullTaskList.addAll(taskList.getTasks());
        for (int i = 0; i < fullTaskList.size(); i++){
            fullTaskList.get(i).setIsOver();
        }
        taskView.setAdapter(taskAdapter);

        show(pref.getBoolean("hideDone",false));
        darkMode(pref.getBoolean("darkActivate",false));

        registerForContextMenu(taskView);
    }

    @Override
    public void finish() {
        returnResult();
        super.finish();
    }

    private void preferenceChanged(SharedPreferences sharedPrefs , String key) {
        if (key.equals("hideDone")){
            boolean hideFinished = sharedPrefs.getBoolean(key, false);
            show(hideFinished);
        }else if(key.equals("darkActivate")){
            boolean darkActivate = sharedPrefs.getBoolean(key,false);
            darkMode(darkActivate);
        }

    }

    public void show(boolean hideFinished){
        if (hideFinished){
            taskAdapter.getFilter().filter("hide");
        }else{
            taskAdapter.getFilter().filter("show");
        }
    }

    public void darkMode(boolean darkActivate){
        if (darkActivate){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.addTask:
                addNewNote();
                break;
            case R.id.settings:
                useSettings();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void addNewNote(){
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivityForResult(intent, ADD_ACTIVITY_REQUEST_CODE);
    }

    private void useSettings(){
        Intent intent = new Intent(this,
                SettingActivity.class);
        startActivityForResult(intent,SETTING_PREFERENCE);
    }

    int viewId;

    @Override
    public void onCreateContextMenu(ContextMenu menu, @NotNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        viewId= v.getId();
        if (viewId == R.id.taskList) {
            getMenuInflater().inflate(R.menu.context_tasks, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.delete:
                deleteItem(info.position);
                break;
            case R.id.edit:
                editItem(info.position);
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void deleteItem(int position){
        Task task = fullTaskList.remove(position);
        taskView.invalidateViews();

        Snackbar undoBar = Snackbar.make(findViewById(R.id.layout),task.getTitle() + " was deleted",30000);
        undoBar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullTaskList.add(task);
                returnResult();
                taskView.invalidateViews();
            }
        });
        returnResult();
        undoBar.show();
    }

    public void editItem(int position){
        changedPosition=position;
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("task",fullTaskList.get(position));
        startActivityForResult(intent, Edit_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Task task = data.getParcelableExtra("addedTask");
                task.setIsOver();
                fullTaskList.add(task);
            }
        }
        else if (requestCode == Edit_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                Task task = data.getParcelableExtra(("changedTask"));
                task.setIsOver();
                fullTaskList.set(changedPosition,task);
            }
        }
        returnResult();
        taskView.invalidateViews();
    }

    public void returnResult(){
        List<Task> temp = new ArrayList<>();
        temp.addAll(fullTaskList);
        temp.addAll(finishedTasks);
        taskList.setTasks(temp);

        Intent intent = new Intent();
        intent.putExtra("tasks",taskList);

        setResult(RESULT_OK,intent);
    }
}