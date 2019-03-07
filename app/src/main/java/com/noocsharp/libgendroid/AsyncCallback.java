package com.noocsharp.libgendroid;

import java.util.ArrayList;

public interface AsyncCallback {
    public void onDownloadComplete(String filename, boolean failed);
    public void onSearchComplete(ArrayList<BookEntry> entries);
}
