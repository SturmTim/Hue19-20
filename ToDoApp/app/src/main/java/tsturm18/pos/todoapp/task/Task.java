package tsturm18.pos.todoapp.task;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task implements Parcelable {

    public String title;
    public String dateTime;
    public String details;
    public boolean isOver;
    public boolean isDone = false;

    int taskId = -1;

    String location = "";

    public Task(String title, String dateTime, String details, boolean isDone) {
        this.title = title;
        this.dateTime = dateTime;
        this.details = details;
        this.isDone = isDone;
        setIsOver();
    }

    public Task(String title, String dateTime, String details) {
        this.title = title;
        this.dateTime = dateTime;
        this.details = details;

        setIsOver();
    }

    protected Task(Parcel in) {
        title = in.readString();
        dateTime = in.readString();
        details = in.readString();
        isDone = in.readByte() != 0;
        taskId = in.readInt();
        location = in.readString();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getDetails() {
        return details;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setIsOver(){
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime ldtDue = LocalDateTime.parse(dateTime, formatter);
        isOver= ldtDue.isBefore(ldt);
    }

    public boolean getIsOver(){
        return isOver;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public @NotNull String toString() {
        return title + ";" + dateTime + ";" + details + ";" + isDone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getTitle());
        dest.writeString(getDateTime());
        dest.writeString(getDetails());
        dest.writeBoolean(isDone());
        dest.writeInt(getTaskId());
        dest.writeString(getLocation());
    }
}
