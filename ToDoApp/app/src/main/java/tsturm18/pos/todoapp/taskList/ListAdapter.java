package tsturm18.pos.todoapp.taskList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import tsturm18.pos.todoapp.R;
import tsturm18.pos.todoapp.task.TaskActivity;

public class ListAdapter extends BaseAdapter {

    private final int listViewItemLayoutId;
    private final List<TaskList> taskList;
    Context context;
    private final LayoutInflater inflater;
    public final int OPEN_TASK_LIST = 9;
    public int lastClickedList;

    public ListAdapter(Context context, int listViewItemLayoutId, List<TaskList> taskList) {
        this.listViewItemLayoutId = listViewItemLayoutId;

        this.taskList = taskList;

        this.context = context;
        inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = (convertView==null) ? inflater.inflate(this.listViewItemLayoutId,null) : convertView;

        TextView listTitle = view.findViewById(R.id.listName);
        listTitle.setText(taskList.get(position).getName());

        ImageView image = view.findViewById(R.id.listDetail);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskActivity.class);
                intent.putExtra("tasks", taskList.get(position));
                lastClickedList = position;
                ((Activity) context).startActivityForResult(intent, OPEN_TASK_LIST);
            }
        });

        return view;
    }


}
