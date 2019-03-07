package com.noocsharp.libgendroid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements AsyncCallback {
    private static final String TAG = "MainActivity";

    private static final int READ_STORAGE_REQUEST = 100;

    EditText edittext;
    ListView bookList;
    ArrayList<BookEntry> entries;
    BookAdapter adapter;
    String currentMirror;

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

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BookEntry entry = adapter.getItem(i);
                currentMirror = entry.getMirrors().get(0);

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_STORAGE_REQUEST);
                } else {
                    doDownloadTask();
                }
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_STORAGE_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    doDownloadTask();
            }
        }
    }

    private void doDownloadTask() {
        new HandleDownloadTask(this).execute(currentMirror);
    }

    private void doSearchTask(String term) {
        new HandleSearchTask(this).execute(term);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();

        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void onDownloadComplete(String filename, boolean failed) {
        if (!failed)
            Toast.makeText(this, "Finished downloading " + filename + ".", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Failed to download " + filename + ".", Toast.LENGTH_LONG).show();
    }

    public void onSearchComplete(ArrayList<BookEntry> entries) {
        adapter = new BookAdapter(this, entries);
        bookList.setAdapter(adapter);
    }

    private class HandleSearchTask extends AsyncTask<String, Void, Void> {
        private AsyncCallback delegate;

        public HandleSearchTask(AsyncCallback delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Void doInBackground(String... strings) {
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
            final ArrayList<BookEntry> rBookList = bookList;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    delegate.onSearchComplete(rBookList);
                }
            });
            return null;
        }

        private String parseTitle(String title) {
            String newTitle;
            if (title.indexOf('<') != -1)
                newTitle = title.substring(0, title.indexOf('<'));
            else
                newTitle = title;

            return newTitle.replace("&amp;", "&");
        }

        private int parsePages(String pages) {
            Log.i(TAG, "parsePages: " + pages);
            if (pages.equals(""))
                return -1;
            else if (pages.indexOf('[') != -1)
                return Integer.parseInt(pages.substring(pages.indexOf('[') + 1, pages.indexOf(']')));
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

    private class HandleDownloadTask extends AsyncTask<String, Void, Boolean> {
        private AsyncCallback delegate;

        public HandleDownloadTask(AsyncCallback delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            InputStream input = null;
            FileOutputStream output = null;
            HttpURLConnection connection = null;
            String fileName = "";
            boolean failed = false;
            try {
                Document download = Jsoup.connect(strings[0]).get();
                Element head = download.getElementsByTag("tr").first();
                String fileURL = head.child(1).child(0).child(0).attributes().get("href");
                fileName = URLDecoder.decode(fileURL.substring(fileURL.lastIndexOf('/')+1), StandardCharsets.UTF_8.name());

                URL url = new URL(fileURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                Log.i(TAG, "Connected to " + fileURL);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                int fileLength = connection.getContentLength();

                input = connection.getInputStream();

                File outputFile = new File(Environment.getExternalStorageDirectory() + "/Download/" + fileName);
                outputFile.createNewFile();
                output = new FileOutputStream(outputFile);
                Log.i(TAG, "Path: " + Environment.getExternalStorageDirectory() + "/Download/" + fileName);

                byte[] data = new byte[4096];
                long bytesReceived = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                    bytesReceived += count;
                    Log.i(TAG, "Transfered " + bytesReceived + "bytes");
                }

            } catch (IOException e) {
                e.printStackTrace();
                failed = true;
            } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (connection != null) connection.disconnect();
            }
            if (failed) {
                Log.i(TAG, "Download of " + fileName + " failed.");
            }

            Log.i(TAG, "Completed successfully");

            final String rFilename = fileName;
            final boolean rFailed = failed;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    delegate.onDownloadComplete(rFilename, rFailed);
                }
            });
            return failed;
        }
    }
}
