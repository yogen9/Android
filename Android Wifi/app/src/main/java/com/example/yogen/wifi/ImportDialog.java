package com.example.yogen.wifi;

/**
 * Created by Yogen on 27-06-2017.
 */

import android.app.Activity;
import android.app.AlertDialog;

public class ImportDialog {
    final CharSequence[] items = { "Take Photo From Gallery",
            "Take Photo From Camera" };
    Activity activity;
    AlertDialog dialog;
    AlertDialog.Builder builder;
    String detailProvader;

    public ImportDialog(Activity a, String detailProvader) {
        this.activity = a;
        this.detailProvader = detailProvader;
        builder = new AlertDialog.Builder(a);
    }

    public void showDialog() {

        builder.setTitle("Wifi Provider Details");
        builder.setMessage(detailProvader);

        AlertDialog alert = builder.create();
        alert.show();
    }
}

