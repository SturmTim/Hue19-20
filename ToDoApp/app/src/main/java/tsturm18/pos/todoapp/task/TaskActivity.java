package tsturm18.pos.todoapp.task;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import tsturm18.pos.todoapp.CloudManager;
import tsturm18.pos.todoapp.InternetConnection;
import tsturm18.pos.todoapp.R;
import tsturm18.pos.todoapp.SettingActivity;
import tsturm18.pos.todoapp.User;
import tsturm18.pos.todoapp.taskList.TaskList;

public class TaskActivity extends AppCompatActivity {

    List<Task> fullTaskList = new ArrayList<>();
    List<Task> finishedTasks = new ArrayList<>();
    TaskList taskList;

    private ListView taskView;

    private TaskAdapter taskAdapter;

    private static final int ADD_ACTIVITY_REQUEST_CODE = 0;
    private final static int SETTING_PREFERENCE = 1;
    private static final int Edit_ACTIVITY_REQUEST_CODE = 2;

    private SharedPreferences pref;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;

    int changedPosition;

    User currentUser;
    CloudManager cloudManager;


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

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesChangeListener = this::preferenceChanged;
        pref.registerOnSharedPreferenceChangeListener(preferencesChangeListener);

        if (pref.getBoolean("firstTime", true)) {
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "You can change this in your settings", Toast.LENGTH_LONG).show();
                }
            }).launch(Manifest.permission.ACCESS_FINE_LOCATION);
            pref.edit().putBoolean("firstTime", false).apply();
        }

        fullTaskList.addAll(taskList.getTasks());
        for (int i = 0; i < fullTaskList.size(); i++) {
            fullTaskList.get(i).setIsOver();
        }
        currentUser = new User(pref.getString("username", ""), pref.getString("password", ""));
        cloudManager = new CloudManager(currentUser);

        taskAdapter = new TaskAdapter(this, R.layout.task_layout, fullTaskList, finishedTasks, cloudManager, taskList);

        taskView.setAdapter(taskAdapter);

        show(pref.getBoolean("hideDone", false));
        darkMode(pref.getBoolean("darkActivate", false));

        registerForContextMenu(taskView);

        /*if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null)
            {

            }
            else if (extras.getBoolean("NotiClick"))
            {
                System.out.println("test");
                editItem(extras.getInt("position"));
            }

        }*/
    }

    @Override
    public void finish() {
        returnResult();
        super.finish();
    }

    private void preferenceChanged(SharedPreferences sharedPrefs, String key) {
        if (key.equals("hideDone")) {
            boolean hideFinished = sharedPrefs.getBoolean(key, false);
            show(hideFinished);
        } else if (key.equals("darkActivate")) {
            boolean darkActivate = sharedPrefs.getBoolean(key, false);
            darkMode(darkActivate);
        }

    }

    public void show(boolean hideFinished) {
        if (hideFinished) {
            taskAdapter.getFilter().filter("hide");
        } else {
            taskAdapter.getFilter().filter("show");
        }
    }

    public void darkMode(boolean darkActivate) {
        if (darkActivate) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        menu.findItem(R.id.account).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.addTask:
                addNewNote();
                break;
            case R.id.settings:
                useSettings();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void addNewNote() {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivityForResult(intent, ADD_ACTIVITY_REQUEST_CODE);
    }

    private void useSettings() {
        Intent intent = new Intent(this,
                SettingActivity.class);
        startActivityForResult(intent, SETTING_PREFERENCE);
    }

    int viewId;

    @Override
    public void onCreateContextMenu(ContextMenu menu, @NotNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        viewId = v.getId();
        if (viewId == R.id.taskList) {
            getMenuInflater().inflate(R.menu.context_tasks, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                deleteItem(info.position);
                break;
            case R.id.edit:
                editItem(info.position);
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void deleteItem(int position) {
        Task task = fullTaskList.remove(position);
        taskView.invalidateViews();
        if (currentUser.validUsername() && new InternetConnection().isNetworkAvailable(this)) {
            cloudManager.deleteTask(task);
        }
        Snackbar undoBar = Snackbar.make(findViewById(R.id.layout), task.getTitle() + " was deleted", 30000);
        undoBar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullTaskList.add(task);
                if (currentUser.validUsername() && new InternetConnection().isNetworkAvailable(TaskActivity.this)) {
                    cloudManager.addTask(taskList, task);
                }
                returnResult();
                taskView.invalidateViews();
            }
        });
        returnResult();
        undoBar.show();
    }

    public void editItem(int position) {
        changedPosition = position;
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("task", fullTaskList.get(position));
        startActivityForResult(intent, Edit_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Task task = data.getParcelableExtra("addedTask");
                if (currentUser.validUsername() && new InternetConnection().isNetworkAvailable(this)) {
                    cloudManager.addTask(taskList, task);
                    task = cloudManager.getLastChangedTask();
                }
                fullTaskList.add(task);

                /*Intent intent = new Intent(this, TaskActivity.class);
                intent.putExtra("NotiClick",true);
                TaskList taskList = new TaskList(this.taskList.getName(),fullTaskList);
                taskList.setListId(this.taskList.getListId());
                intent.putExtra("tasks",taskList);
                intent.putExtra("position",fullTaskList.indexOf(task));

                PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, Intent.FILL_IN_ACTION);

                Notification.Builder builder = new Notification.Builder(this,"TODO")
                        .setContentTitle("YourTitle")
                        .setContentText("YourDescription")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true);
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(task.getTaskId(), builder.build());*/


            }
        } else if (requestCode == Edit_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Task task = data.getParcelableExtra(("changedTask"));
                if (currentUser.validUsername() && new InternetConnection().isNetworkAvailable(this)) {
                    cloudManager.editTask(taskList, task);
                }
                fullTaskList.set(changedPosition, task);
            }
        }
        returnResult();
        taskView.invalidateViews();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    public void returnResult() {
        List<Task> temp = new ArrayList<>();
        temp.addAll(fullTaskList);
        temp.addAll(finishedTasks);
        taskList.setTasks(temp);

        Intent intent = new Intent();
        intent.putExtra("tasks", taskList);

        setResult(RESULT_OK, intent);
    }
}