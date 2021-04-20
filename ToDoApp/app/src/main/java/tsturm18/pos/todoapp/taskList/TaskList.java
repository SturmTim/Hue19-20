package tsturm18.pos.todoapp.taskList;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import tsturm18.pos.todoapp.task.Task;

public class TaskList implements Parcelable {

    String name;
    List<Task> tasks;

    int listId = -1;

    public TaskList(String name) {
        this.name = name;
        this.tasks = new ArrayList<>();
    }

    public TaskList(String name, List<Task> tasks) {
        this.name = name;
        this.tasks = tasks;
    }

    protected TaskList(Parcel in) {
        name = in.readString();
        tasks = in.createTypedArrayList(Task.CREATOR);
        listId = in.readInt();
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public int getListId() {
        return listId;
    }

    public static final Creator<TaskList> CREATOR = new Creator<TaskList>() {
        @Override
        public TaskList createFromParcel(Parcel in) {
            return new TaskList(in);
        }

        @Override
        public TaskList[] newArray(int size) {
            return new TaskList[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getName());
        dest.writeTypedList(getTasks());
        dest.writeInt(getListId());
    }
}
