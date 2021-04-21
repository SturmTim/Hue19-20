package tsturm18.pos.todoapp.taskList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import tsturm18.pos.todoapp.CloudManager;
import tsturm18.pos.todoapp.InternetConnection;
import tsturm18.pos.todoapp.LogIn_SignUp;
import tsturm18.pos.todoapp.R;
import tsturm18.pos.todoapp.SettingActivity;
import tsturm18.pos.todoapp.User;
import tsturm18.pos.todoapp.task.Task;

public class MainActivity extends AppCompatActivity {

    List<TaskList> taskList = new ArrayList<>();

    private ListView taskListView;

    private ListAdapter listAdapter;

    private static final int ADD_ACTIVITY_REQUEST_CODE = 0;
    private static final int SETTING_PREFERENCE = 1;
    private static final int Edit_ACTIVITY_REQUEST_CODE = 2;
    private static final int LOGIN_REGISTER = 3;

    private SharedPreferences pref;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;

    int changedPosition;

    User currentUser;
    CloudManager cloudManager;
    InternetConnection internetConnection = new InternetConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        taskListView = findViewById(R.id.taskList);

        listAdapter = new ListAdapter(this,R.layout.list_layout, taskList);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesChangeListener = this::preferenceChanged;
        pref.registerOnSharedPreferenceChangeListener( preferencesChangeListener );

        currentUser = new User(pref.getString("username", ""),pref.getString("password",""));
        cloudManager = new CloudManager(currentUser);

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

    public void saveNotes(){
        File file;
        try {
            PrintWriter outPrintWriter;
            file = new File("notes.json");
            OutputStream outputStream = openFileOutput(file.getName(), Context.MODE_PRIVATE);
            outPrintWriter = new PrintWriter(new OutputStreamWriter(outputStream));

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
            if (currentUser.validUsername() && internetConnection.isNetworkAvailable(this)){
                taskList.clear();
                Thread thread = new Thread(() -> taskList.addAll(cloudManager.loadFromCloud()));
                thread.start();
                thread.join();
            }else {
                FileInputStream fileInputStream;
                file = new File("notes.json");
                fileInputStream = openFileInput(file.getName());

                taskList.clear();
                try(BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(fileInputStream))){
                    String s = bufferedInputStream.readLine();
                    Gson gson = new Gson();
                    taskList.addAll(gson.fromJson(s,new TypeToken<List<TaskList>>(){}.getType()));
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException | InterruptedException e) {
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
            case  R.id.account:
                logIn();
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

    private void logIn(){
        if (new InternetConnection().isNetworkAvailable(this)){
            Intent intent = new Intent(this, LogIn_SignUp.class);
            intent.putExtra("currentUser",currentUser);
            startActivityForResult(intent,LOGIN_REGISTER);
        }else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
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
        if (currentUser.validUsername() && internetConnection.isNetworkAvailable(this)){
            for (Task task:list.getTasks()) {
                cloudManager.deleteTask(task);
            }
            cloudManager.deleteList(list);
        }
        taskListView.invalidateViews();

        Snackbar undoBar = Snackbar.make(findViewById(R.id.layout),list.getName() + " was deleted",30000);
        undoBar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser.validUsername() && internetConnection.isNetworkAvailable(MainActivity.this)){
                    cloudManager.addList(list);
                    list.setListId(cloudManager.getLastChangedList().getListId());
                    for (Task task:list.getTasks()) {
                        cloudManager.addTask(list,task);
                        task.setTaskId(cloudManager.getLastChangedTask().getTaskId());
                    }
                }
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
                if (currentUser.validUsername() && internetConnection.isNetworkAvailable(this)){
                    cloudManager.addList(list);
                    list.setListId(cloudManager.getLastChangedList().getListId());
                }
                taskList.add(list);
            }
        }
        else if (requestCode == Edit_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                TaskList list = data.getParcelableExtra(("changedTaskList"));
                if (currentUser.validUsername() && internetConnection.isNetworkAvailable(this)){
                    cloudManager.editList(list);
                }
                taskList.set(changedPosition,list);
            }
        }
        else if (requestCode == listAdapter.OPEN_TASK_LIST){
            if (resultCode == RESULT_OK){
                TaskList task = data.getParcelableExtra("tasks");
                taskList.set(listAdapter.lastClickedList,task);
            }
        }
        else if (requestCode == LOGIN_REGISTER){
            if (resultCode == RESULT_OK){
                currentUser = data.getParcelableExtra("user");
                pref.edit().putString("username",currentUser.getUsername()).apply();
                pref.edit().putString("password",currentUser.getPassword()).apply();
                cloudManager = new CloudManager(currentUser);
                loadNotes();
            }
        }

        taskListView.invalidateViews();
    }
}