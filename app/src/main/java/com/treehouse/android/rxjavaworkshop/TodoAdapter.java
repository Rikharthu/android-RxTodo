package com.treehouse.android.rxjavaworkshop;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoHolder> implements Action1<List<Todo>> {

    private static final String LOG_TAG = TodoAdapter.class.getSimpleName();
    LayoutInflater inflater;

    TodoCompletedChangeListener todoChangeListener;

    List<Todo> data = new ArrayList<>();

    public TodoAdapter(Activity activity, TodoCompletedChangeListener listener) {
        inflater = LayoutInflater.from(activity);
        todoChangeListener = listener;
    }

    @Override
    public TodoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TodoHolder(inflater.inflate(R.layout.item_todo, parent, false));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(TodoHolder holder, int position) {
        final Todo todo = data.get(position);
        holder.checkbox.setText(todo.description);

        // ensure existing listener is nulled out, setting the value causes a check changed listener callback
        holder.checkbox.setOnCheckedChangeListener(null);

        // set the current value, then setup the listener
        holder.checkbox.setChecked(todo.isCompleted);
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                todoChangeListener.onTodoCompletedChanged(todo);
            }
        });
    }

    @Override
    public void call(List<Todo> todos) {
        Log.d(LOG_TAG,"call()");
        data=todos;
        notifyDataSetChanged();
    }

    public class TodoHolder extends RecyclerView.ViewHolder {

        public CheckBox checkbox;

        public TodoHolder(View itemView) {
            super(itemView);
            checkbox = (CheckBox) itemView;
        }
    }
}