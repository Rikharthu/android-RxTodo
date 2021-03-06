package com.treehouse.android.rxjavaworkshop;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxAdapterView;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

    private static final String LIST = "list";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();


    Toolbar toolbar;
    Spinner spinner;

    EditText addInput;

    RecyclerView recyclerView;
    TodoAdapter adapter;

    TodoList list;

    CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // retrieve from Saved State or SharedPreferences if we are starting fresh
        if (savedInstanceState != null) {
            list = new TodoList(savedInstanceState.getString(LIST));
        } else {
            list = new TodoList(getSharedPreferences("data", Context.MODE_PRIVATE).getString(LIST, null));
        }


        // setup the Adapter, this contains a callback when an item is checked/unchecked
        adapter = new TodoAdapter(this, list);


        // setup the list with the adapter
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        // setup adding new items to the list
        addInput = (EditText) findViewById(R.id.add_todo_input);
        findViewById(R.id.add_todo_container).requestFocus(); // ensures the edittext isn't focused when entering the Activity
        // Use Rx binding
        // Similar to OnClickListener()
        subscriptions.add(
                RxView.clicks(findViewById(R.id.btn_add_todo))
                        // what item to emit
                        .map(new Func1<Void, String>() {
                            // for every click on btn_add_todo emit an item
                            // which is mapped to be a string from addInput edit text
                            @Override
                            public String call(Void aVoid) {
                                String item = addInput.getText().toString();
                                Log.d(LOG_TAG, "mapping. return=" + item);
                                return item;
                            }
                        })
                        // filter which items to emit
                        .filter(new Func1<String, Boolean>() {
                            @Override
                            public Boolean call(String s) {
                                // if it's true => emit an item
                                // false - don't emit
                                boolean emit = !TextUtils.isEmpty(s.trim());

                                Log.d(LOG_TAG, "filtering. emit=" + emit);
                                return emit;
                            }
                        })
                        // what happens when an item is emitted
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String s) {
                                Log.d(LOG_TAG, "adding item");
                                // update our list
                                list.add(new Todo(s, false));

                                // clear input, remove focus, and hide keyboard
                                addInput.setText("");
                                findViewById(R.id.add_todo_container).requestFocus();
                                dismissKeyboard();
                            }
                        }));


        // setup the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // setup the filter in the toolbar
        spinner = (Spinner) toolbar.findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"All", "Incomplete", "Completed"}));

        // combine latest changes to spinner values and notes list
        subscriptions.add(
                Observable.combineLatest(
                        RxAdapterView.itemSelections(spinner).skip(1),
                        list.asObservable(),
                        new Func2<Integer, TodoList, List<Todo>>() {
                            @Override
                            public List<Todo> call(Integer integer, TodoList todoList) {
                                switch (integer) {
                                    case FilterPositions.COMPLETE:
                                        return list.getComplete();
                                    case FilterPositions.INCOMPLETE:
                                        return list.getIncomplete();
                                    default:
                                        return list.getAll();
                                }
                            }
                        }
                ).subscribe(adapter));


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(LIST, list.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        editor.putString(LIST, list.toString());
        editor.apply();

        // unsubscribe from all subscriptions
        subscriptions.unsubscribe();
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(addInput.getWindowToken(), 0);
    }
}
