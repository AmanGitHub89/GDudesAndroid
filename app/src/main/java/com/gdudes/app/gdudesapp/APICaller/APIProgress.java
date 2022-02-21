package com.gdudes.app.gdudesapp.APICaller;

import android.app.ProgressDialog;
import android.content.Context;

public class APIProgress {
    public ProgressDialog progressDialog;
    private Context context;
    private String Message;
    public Boolean CanBeDismissed;

    public APIProgress(Context vcontext, String vMessage, Boolean vCanBeDismissed) {
        context = vcontext;
        Message = vMessage;
        CanBeDismissed = vCanBeDismissed;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(Message);
        progressDialog.setCancelable(CanBeDismissed);
    }
}
