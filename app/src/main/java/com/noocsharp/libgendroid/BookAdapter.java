package com.noocsharp.libgendroid;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends ArrayAdapter<BookEntry> {
    private static final String TAG = "BookAdapter";
    private Context context;
    private List<BookEntry> bookList;

    public BookAdapter(@NonNull Context context, @NonNull ArrayList<BookEntry> objects) {
        super(context, 0, objects);

        this.context = context;
        this.bookList = objects;
    }

    @Override
    public int getCount() {
        return bookList.size();
    }

    @Override
    public BookEntry getItem(int i) {
        return bookList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        BookEntry entry = bookList.get(i);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.bookentry, null);
        }

        TextView title = view.findViewById(R.id.title);
        TextView author = view.findViewById(R.id.author);
        TextView pages = view.findViewById(R.id.pages);

        title.setText(entry.getTitle());
        author.setText(entry.getAuthor());
        pages.setText(String.valueOf(entry.getPages()));

        return view;
    }
}
