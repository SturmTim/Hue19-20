package tsturm18.pos.todoapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import tsturm18.pos.todoapp.task.Task;
import tsturm18.pos.todoapp.taskList.TaskList;

public class CloudManager {

    InternetConnection internetConnection = new InternetConnection();

    TaskList lastChangedList;
    Task lastChangedTask;
    User user;

    public CloudManager(User user) {
        this.user = user;
    }

    public TaskList getLastChangedList() {
        return lastChangedList;
    }

    public Task getLastChangedTask() {
        return lastChangedTask;
    }

    public User getUser() {
        return user;
    }

    public List<TaskList> loadFromCloud(){
        List<TaskList> taskLists = new ArrayList<>();
        InternetConnection.Response listResponse = internetConnection.get("http://sickinger-solutions.at/notesserver/todolists.php?username=" + user.getUsername() + "&password=" + user.getPassword());
        boolean success = listResponse.startWith(2);
        if (success){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(listResponse.inputStream));
                JsonArray json = new JsonParser().parse(bufferedReader.readLine()).getAsJsonArray();
                listResponse.close();
                for (int i = 0; i < json.size(); i++){
                    JsonObject jsonObject = json.get(i).getAsJsonObject();
                    int id = jsonObject.get("id").getAsInt();
                    String title = jsonObject.get("name").getAsString();
                    TaskList taskList = new TaskList(title);
                    taskList.setListId(id);
                    taskLists.add(taskList);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        InternetConnection.Response response = internetConnection.get("http://sickinger-solutions.at/notesserver/todo.php?username=" + user.getUsername() + "&password=" + user.getPassword());

        success = response.startWith(2);
        if (success){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.inputStream));
                JsonArray json = new JsonParser().parse(bufferedReader.readLine()).getAsJsonArray();
                response.close();
                for (int i = 0; i < json.size(); i++){
                    JsonObject jsonObject = json.get(i).getAsJsonObject();

                    int id = jsonObject.get("id").getAsInt();
                    String title = jsonObject.get("title").getAsString();
                    String details = jsonObject.get("description").getAsString();
                    String dateTime = jsonObject.get("dueDate").getAsString();
                    DateTimeFormatter cloudFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.parse(dateTime,cloudFormat);
                    DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                    dateTime = localDateTime.format(myFormat);
                    boolean done;
                    done = !jsonObject.get("state").getAsString().equals("OPEN");
                    Task task = new Task(title,dateTime,details,done);
                    task.setTaskId(id);
                    task.setLocation(jsonObject.get("additionalData").getAsString());
                    int listId = jsonObject.get("todoListId").getAsInt();
                    for (TaskList taskList:taskLists) {
                        if (taskList.getListId() == listId){
                            taskList.getTasks().add(task);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return taskLists;
    }

    public void addList(TaskList taskList){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name",taskList.getName());
            jsonObject.put("additionalData","");
            InternetConnection.Response response = internetConnection.post("http://sickinger-solutions.at/notesserver/todolists.php?username=" + user.getUsername() +"&password=" + user.getPassword(),jsonObject.toString());
            boolean success = response.startWith(2);
            if (success){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.inputStream));
                JsonObject json = new JsonParser().parse(bufferedReader.readLine()).getAsJsonObject();
                taskList.setListId(json.get("id").getAsInt());
                lastChangedList = taskList;
            }
            response.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public void editList(TaskList taskList){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name",taskList.getName());
            jsonObject.put("additionalData","");
            InternetConnection.Response response = internetConnection.put("http://sickinger-solutions.at/notesserver/todolists.php?id=" + taskList.getListId() + "&username=" + user.getUsername() +"&password=" + user.getPassword(),jsonObject.toString());

            response.close();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void deleteList(TaskList taskList){
        InternetConnection.Response response = internetConnection.delete("http://sickinger-solutions.at/notesserver/todolists.php?id=" + taskList.getListId() +"&username="+ user.getUsername() +"&password=" + user.getPassword());
        response.close();
    }

    public void addTask(TaskList taskList, Task task){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("todoListId",taskList.getListId());
            jsonObject.put("title",task.getTitle());
            jsonObject.put("description",task.getDetails());
            jsonObject.put("dueDate",task.getDateTime());
            if (task.isDone()){
                jsonObject.put("state","CLOSED");
            }else {
                jsonObject.put("state","OPEN");
            }
            jsonObject.put("additionalData",task.getLocation() + " ");

            InternetConnection.Response response = internetConnection.post("http://sickinger-solutions.at/notesserver/todo.php?username="+ user.getUsername() +"&password=" + user.getPassword(),jsonObject.toString());
            boolean success = response.startWith(2);
            if (success){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.inputStream));
                JsonObject json = new JsonParser().parse(bufferedReader.readLine()).getAsJsonObject();
                task.setTaskId(json.get("id").getAsInt());
                lastChangedTask = task;
            }
            response.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public void editTask(TaskList taskList, Task task){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("todoListId",taskList.getListId());
            jsonObject.put("title",task.getTitle());
            jsonObject.put("description",task.getDetails());
            jsonObject.put("dueDate",task.getDateTime());
            if (task.isDone()){
                jsonObject.put("state","CLOSED");
            }else {
                jsonObject.put("state","OPEN");
            }
            jsonObject.put("additionalData",task.getLocation());

            InternetConnection.Response response = internetConnection.put("http://sickinger-solutions.at/notesserver/todo.php?id=" + task.getTaskId() +"&username=" + user.getUsername() + "&password=" + user.getPassword(),jsonObject.toString());
            response.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void deleteTask(Task task){
        InternetConnection.Response response = internetConnection.delete("http://sickinger-solutions.at/notesserver/todo.php?id=" + task.getTaskId() + "&username=" + user.getUsername() + "&password=" + user.getPassword());
        response.close();
    }
}
