package tsturm18.pos.todoapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Task> fullTaskList = new ArrayList<>();
    List<Task> finishedTasks = new ArrayList<>();

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
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        taskView = findViewById(R.id.taskList);

        taskAdapter = new TaskAdapter(this,R.layout.task_layout, fullTaskList, finishedTasks);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesChangeListener = ( sharedPrefs , key ) -> preferenceChanged(sharedPrefs, key);
        pref.registerOnSharedPreferenceChangeListener( preferencesChangeListener );

        loadNotes();

        show(pref.getBoolean("hideDone",false));
        darkMode(pref.getBoolean("darkActivate",false));

        registerForContextMenu(taskView);
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

    private void loadNotes(){
        File file = new File("notes.json");
        try {
            InputStream inputStream = openFileInput(file.getName());
            fullTaskList.clear();
            try(BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(inputStream))){
                String s = bufferedInputStream.readLine();
                Gson gson = new Gson();
                fullTaskList.addAll(gson.fromJson(s,new TypeToken<List<Task>>(){}.getType()));
            }catch(IOException e){
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        taskView.setAdapter(taskAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.saveAction:
                saveNotes();
                break;
            case R.id.newNote:
                addNewNote();
                break;
            case R.id.settings:
                useSettings();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void addNewNote(){
        Intent intent = new Intent(this, AddActivity.class);
        startActivityForResult(intent, ADD_ACTIVITY_REQUEST_CODE);
    }

    private void useSettings(){
        Intent intent = new Intent(this,
                SettingActivity.class);
        startActivityForResult(intent,SETTING_PREFERENCE);
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
        taskView.invalidateViews();
    }

    private void saveNotes(){
        File file = new File("notes.json");
        try {
            OutputStream outputStream = openFileOutput(file.getName(), Context.MODE_PRIVATE);
            PrintWriter outPrintWriter = new PrintWriter(new OutputStreamWriter(outputStream));
            Gson gson = new Gson();

            List<Task> allTasks = new ArrayList<>();
            allTasks.addAll(fullTaskList);
            allTasks.addAll(finishedTasks);

            String json =  gson.toJson(allTasks);
            outPrintWriter.println(json);

            outPrintWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
            fullTaskList.remove(position);
            taskView.invalidateViews();
    }

    public void editItem(int position){
        changedPosition=position;
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("task",fullTaskList.get(position));
        startActivityForResult(intent, Edit_ACTIVITY_REQUEST_CODE);
    }

}