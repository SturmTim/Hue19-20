package tsturm18.pos.todoapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends BaseAdapter implements Filterable {

    private final int listViewItemLayoutId;
    private List<Task> tasks;
    private List<Task> finishedTasks;
    Context context;
    private final LayoutInflater inflater;

    private boolean isFiltered;

    public TaskAdapter(Context context, int listViewItemLayoutId, List<Task> tasks, List<Task> finishedTasks) {
        this.listViewItemLayoutId = listViewItemLayoutId;

        this.tasks = tasks;
        this.finishedTasks = finishedTasks;

        this.context = context;
        inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = (convertView==null) ? inflater.inflate(this.listViewItemLayoutId,null) : convertView;

        if(tasks.get(position).getIsOver()){
            view.setBackgroundColor(context.getColor(R.color.over));
        }else {
            view.setBackgroundColor(context.getColor(R.color.textBackground));
        }

        TextView text = view.findViewById(R.id.titel);
        text.setText(tasks.get(position).getTitle());

        TextView dateTime = view.findViewById(R.id.dateTime);
        dateTime.setText(tasks.get(position).getDateTime());

        TextView detail = view.findViewById(R.id.detail);
        detail.setText(tasks.get(position).getDetails());

        CheckBox checkBox = view.findViewById(R.id.isFinished);
        checkBox.setChecked(tasks.get(position).isDone());
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tasks.get(position).isDone()){
                    tasks.get(position).setDone(false);
                }else{
                    tasks.get(position).setDone(true);
                    if (isFiltered){
                        finishedTasks.add(tasks.remove(position));
                        notifyDataSetChanged();
                    }

                }
            }
        });

        return view;
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                List<Task> filteredTasks = new ArrayList<>();

                if(constraint.equals("hide")){

                    for (Task task: tasks) {
                        if (!task.isDone){
                            filteredTasks.add(task);
                        }else{
                            finishedTasks.add(task);
                        }
                    }
                    isFiltered = true;

                }else{
                    filteredTasks.addAll(tasks);
                    filteredTasks.addAll(finishedTasks);
                    finishedTasks.clear();
                    isFiltered = false;
                }


                results.count = filteredTasks.size();
                results.values = filteredTasks;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                tasks.clear();
                tasks.addAll((List<Task>) results.values);
                notifyDataSetChanged();
            }
        };
        return filter;
    }
}
