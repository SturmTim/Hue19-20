package tsturm18.pos.todoapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class Picker {
    public Picker() {
    }

    public void pickDate(TextView textView, Context context){
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

                        LocalDate localDate = LocalDate.of(year,monthOfYear+1,dayOfMonth);

                        textView.setText(localDate.format(formatter));

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();

    }

    public void pickTime(TextView textView, Context context){
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

                        LocalTime localTime = LocalTime.of(hourOfDay,minute);

                        textView.setText(localTime.format(formatter));

                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }
}
