package com.example.android.recyclerplusview;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private String[] data = {"Apple", "Banana", "Orange", "Watermelon",
            "Pear", "Grape", "Pineapple", "Strawberry", "Cherry", "Mango"};

    private RecyclerPlusView mRecyclerPlusView;
    private List<String> mData = new ArrayList<>();
    InitAdapter initAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerPlusView = findViewById(R.id.recycler_plus_view);
        mRecyclerPlusView.setLayoutManager(new LinearLayoutManager(this));
        initAdapter = new InitAdapter(mData, this);
        mRecyclerPlusView.setAdapter(initAdapter);
        mRecyclerPlusView.setPullListener(new RecyclerPlusView.PullListener() {
            @Override
            public void refresh() {
                loadData();
            }
        });
    }

    private void loadData() {
        Observable.create(new ObservableOnSubscribe<String[]>() {
            @Override
            public void subscribe(ObservableEmitter<String[]> emitter) throws Exception {
                SystemClock.sleep(2000);
                emitter.onNext(new String[new Random().nextInt(2)]);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String[]>() {
                    @Override
                    public void accept(String[] strings) throws Exception {
                        mData.addAll(Arrays.asList(strings));
                        initAdapter.notifyItemChanged(mData.size() - strings.length, strings.length);
                        mRecyclerPlusView.onRefreshComplete(strings.length > 0);
                    }
                });
    }

    private static class InitAdapter extends RecyclerView.Adapter<InitAdapter.Holder> {

        private LayoutInflater inflater;
        private List<String> list;

        InitAdapter(List<String> list, Context context) {
            this.list = list;
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_view, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.textView.setText(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            private TextView textView;

            Holder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.tv_item);
            }
        }
    }
}
