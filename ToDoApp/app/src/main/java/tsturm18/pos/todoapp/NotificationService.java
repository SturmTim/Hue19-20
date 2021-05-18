package tsturm18.pos.todoapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import tsturm18.pos.todoapp.task.Task;
import tsturm18.pos.todoapp.taskList.TaskList;

public class NotificationService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Thread worker;
    List<TaskList> taskLists = new ArrayList<>();
    User currentUser;
    private SharedPreferences pref;
    CloudManager cloudManager;
    InternetConnection internetConnection = new InternetConnection();
    private String CHANNEL_ID = "TEST";

    @Override
    public void onCreate() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "TasksToDo";
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = new User(pref.getString("username", ""),pref.getString("password",""));
        cloudManager = new CloudManager(currentUser);
        worker = new Thread(this::doWork);
        super.onCreate();
    }

    private void doWork() {
        while(true) {
            File file;
            try {
                if (currentUser.validUsername()) {
                    taskLists.clear();
                    Thread thread = new Thread(() -> taskLists.addAll(cloudManager.loadFromCloud()));
                    thread.start();
                    thread.join();
                } else {
                    FileInputStream fileInputStream;
                    file = new File("notes.json");
                    fileInputStream = openFileInput(file.getName());

                    taskLists.clear();
                    try (BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(fileInputStream))) {
                        String s = bufferedInputStream.readLine();
                        Gson gson = new Gson();
                        taskLists.addAll(gson.fromJson(s, new TypeToken<List<TaskList>>() {
                        }.getType()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
            Notification summaryNotification = new Notification.Builder ( this , CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true)
                    .setGroup("DueNotifications")
                    .setGroupSummary(true)
                    .build();

            for (TaskList taskList : taskLists) {
                for (Task task : taskList.getTasks()) {
                    if (task.isOver && !task.isDone) {
                        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle("Task is Due:")
                                .setContentText(task.getTitle())
                                .setAutoCancel(true)
                                .setGroup("DueNotifications");
                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(task.getTaskId(), builder.build());
                    }
                }
            }
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(222, summaryNotification);
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!worker.isAlive()){
            worker.start();
        }
        return START_STICKY;

    }
}
