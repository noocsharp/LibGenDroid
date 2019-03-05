package com.noocsharp.libgendroid;

import android.app.Activity;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

    EditText edittext;
    ListView bookList;
    ArrayList<BookEntry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        entries = new ArrayList<>();

        edittext = findViewById(R.id.editText);
        bookList = findViewById(R.id.bookList);

        edittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    hideKeyboard();
                    doSearchTask(((EditText) view).getText().toString());
                }
                return false;
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void doSearchTask(String term) {
        try {
            entries = new HandleSearchTask().execute(term).get();
        } catch (InterruptedException e) {
            entries = new ArrayList<>();
            e.printStackTrace();
        } catch (ExecutionException e) {
            entries = new ArrayList<>();
            e.printStackTrace();
        }
        BookAdapter adapter = new BookAdapter(this, entries);
        bookList.setAdapter(adapter);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();

        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                    entry.setYear(parseYear(elements.get(4).text()));
                    entry.setPages(parsePages(elements.get(5).text()));
                    entry.setLanguage(elements.get(6).text());
                    entry.setSize(elements.get(7).text());
                    entry.setExtension(elements.get(8).text());

                    entry.addMirror(elements.get(9).children().first().attributes().get("href"));
                    entry.addMirror(elements.get(10).children().first().attributes().get("href"));
                    entry.addMirror(elements.get(11).children().first().attributes().get("href"));
                    entry.addMirror(elements.get(12).children().first().attributes().get("href"));
                    entry.addMirror(elements.get(13).children().first().attributes().get("href"));

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
            Log.i(TAG, "parsePages: " + pages);
            if (pages.equals(""))
                return -1;
            else if (pages.indexOf(' ') != -1)
                return Integer.parseInt(pages.substring(0, pages.indexOf(' ')));
            else
                return Integer.parseInt(pages);
        }

        private int parseYear(String year) {
            try {
                return Integer.parseInt(year);
            } catch (Exception e) {

                return -1;
            }
        }
    }
}
