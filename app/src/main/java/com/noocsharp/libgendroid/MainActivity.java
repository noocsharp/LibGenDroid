package com.noocsharp.libgendroid;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //EditText edittext = findViewById(R.id.editText);
    ListView bookList;
    ArrayList<BookEntry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bookList = findViewById(R.id.bookList);

        try {
            entries = new HandleSearchTask().execute("devil").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (entries != null) {
            for (BookEntry e : entries) {
                Log.i(TAG, e.toString());
            }
            ArrayAdapter<BookEntry> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entries);
            bookList.setAdapter(adapter);

        }
    }

    private class HandleSearchTask extends AsyncTask<String, Void, ArrayList<BookEntry>> {
        @Override
        protected ArrayList<BookEntry> doInBackground(String... strings) {
            ArrayList<BookEntry> bookList = new ArrayList<>();

            if (strings.length <= 0) return null;
            try {
                Document doc = Jsoup.connect("http://gen.lib.rus.ec/search.php?req=" + strings[0] + "&open=0&res=25&view=simple&phrase=1&column=def" + strings[0]).get();
                Elements rows = doc.getElementsByClass("c").first().children().first().children();

                boolean first = true;
                for (Element row : rows) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    Elements elements = row.children();

                    BookEntry entry = new BookEntry();

                    entry.setId(Integer.parseInt(elements.get(0).text()));
                    entry.setAuthor(elements.get(1).text());
                    entry.setTitle(parseTitle(elements.get(2).children().last().html()));
                    entry.setPublisher(elements.get(3).text());
                    entry.setYear(Integer.parseInt(elements.get(4).text()));
                    entry.setPages(parsePages(elements.get(5).text()));
                    entry.setLanguage(elements.get(6).text());
                    entry.setSize(elements.get(7).text());
                    entry.setExtension(elements.get(8).text());

                    Log.i(TAG, "ROW");
                    Log.i(TAG, entry.toString());
                    Log.i(TAG, row.html());

                    bookList.add(entry);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return bookList;
        }

        private String parseTitle(String title) {
            if (title.indexOf('<') != -1)
                return title.substring(0, title.indexOf('<'));
            else
                return title;
        }

        private int parsePages(String pages) {
            if (pages.indexOf(' ') != -1)
                return Integer.parseInt(pages.substring(0, pages.indexOf(' ')));
            else
                return Integer.parseInt(pages);
        }
    }
}
