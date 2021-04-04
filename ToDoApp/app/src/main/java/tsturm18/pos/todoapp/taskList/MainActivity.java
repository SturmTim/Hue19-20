package tsturm18.pos.todoapp.taskList;

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
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

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
import tsturm18.pos.todoapp.task.AddTaskActivity;
import tsturm18.pos.todoapp.task.EditTaskActivity;
import tsturm18.pos.todoapp.task.Task;

public class MainActivity extends AppCompatActivity {

    List<TaskList> taskList = new ArrayList<>();

    private ListView taskListView;

    private ListAdapter listAdapter;

    private static boolean allowedToWrite = false;
    private static boolean allowedToRead = false;

    private static final int ADD_ACTIVITY_REQUEST_CODE = 0;
    private final static int SETTING_PREFERENCE = 1;
    private static final int Edit_ACTIVITY_REQUEST_CODE = 2;

    private SharedPreferences pref;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;

    int changedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            checkPermissions();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        taskListView = findViewById(R.id.taskList);

        listAdapter = new ListAdapter(this,R.layout.list_layout, taskList);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesChangeListener = this::preferenceChanged;
        pref.registerOnSharedPreferenceChangeListener( preferencesChangeListener );

        loadNotes();

        darkMode(pref.getBoolean("darkActivate",false));

        registerForContextMenu(taskListView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNotes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveNotes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveNotes();
    }

    private static final int RQ_WRITE_STORAGE = 12345;
    private static final int RQ_READ_STORAGE = 54321;

    private void checkPermissions(){
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RQ_WRITE_STORAGE);
        } else {
            allowedToWrite = true;
        }
        if (checkSelfPermission (Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, RQ_READ_STORAGE);
        } else {
            allowedToRead = true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==RQ_WRITE_STORAGE) {
            allowedToWrite = grantResults.length <= 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (requestCode==RQ_READ_STORAGE) {
            allowedToRead = grantResults.length <= 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void saveNotes(){
        File file;
        try {
            PrintWriter outPrintWriter;
            if(allowedToRead && allowedToWrite){
                String folder =  getExternalFilesDir(null).getAbsolutePath();
                file = new File(folder + File.separator + "notes.json");
                outPrintWriter= new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
            }else{
                file = new File("notes.json");
                OutputStream outputStream = openFileOutput(file.getName(), Context.MODE_PRIVATE);
                outPrintWriter = new PrintWriter(new OutputStreamWriter(outputStream));
            }

            Gson gson = new Gson();

            String json =  gson.toJson(taskList);
            outPrintWriter.println(json);
            outPrintWriter.flush();
            outPrintWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadNotes(){
        File file;
        try {
            if(allowedToRead && allowedToWrite){
                String folder =  getExternalFilesDir(null).getAbsolutePath();
                file = new File(folder + File.separator + "notes.json");
            }else{
                file = new File("notes.json");
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            taskList.clear();
            try(BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(fileInputStream))){
                String s = bufferedInputStream.readLine();
                Gson gson = new Gson();
                taskList.addAll(gson.fromJson(s,new TypeToken<List<TaskList>>(){}.getType()));
            }catch(IOException e){
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        taskListView.setAdapter(listAdapter);
    }



    private void preferenceChanged(SharedPreferences sharedPrefs , String key) {
        if(key.equals("darkActivate")){
            boolean darkActivate = sharedPrefs.getBoolean(key,false);
            darkMode(darkActivate);
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
                addNewList();
                break;
            case R.id.settings:
                useSettings();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void addNewList(){
        Intent intent = new Intent(this, AddListActivity.class);
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
        TaskList list = taskList.remove(position);
        taskListView.invalidateViews();

        Snackbar undoBar = Snackbar.make(findViewById(R.id.layout),list.getName() + " was deleted",30000);
        undoBar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskList.add(list);
                taskListView.invalidateViews();
            }
        });
        undoBar.show();
    }

    public void editItem(int position){
        changedPosition=position;
        Intent intent = new Intent(this, EditListActivity.class);
        intent.putExtra("taskList",taskList.get(position));
        startActivityForResult(intent, Edit_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                TaskList list = data.getParcelableExtra("addedList");
                taskList.add(list);
            }
        }
        else if (requestCode == Edit_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                TaskList list = data.getParcelableExtra(("changedTaskList"));
                taskList.set(changedPosition,list);
            }
        }
        else if (requestCode == listAdapter.OPEN_TASK_LIST){
            if (resultCode == RESULT_OK){
                TaskList task = data.getParcelableExtra("tasks");
                taskList.set(listAdapter.lastClickedList,task);
            }
        }

        taskListView.invalidateViews();
    }
}