package it.mm.supportlibrary.ui.component;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;

import androidx.core.content.ContextCompat;

import it.mm.supportlibrary.R;
import it.mm.supportlibrary.core.AndroidUtilities;
import it.mm.supportlibrary.databinding.LoadingLayoutBinding;

/**
 * Created by moxiaomo
 * on 2021/3/13
 */
public class LoadingDialog extends Dialog {

    private Context context;
    private int backgroundDrawable;
    private String message;
    private boolean canCancel;
    private LoadingLayoutBinding binding;
    private Handler applicationHandler;

    public LoadingDialog(Handler applicationHandler, Context context, String message, boolean canCancel, int backgroundDrawable) {
        super(context, R.style.LoadingDialog);
        this.applicationHandler = applicationHandler;
        this.message = message;
        this.canCancel = canCancel;
        this.context = context;
        this.backgroundDrawable = backgroundDrawable;
    }

    public void setMessage(String message) {
        this.message = message;
        handler.sendEmptyMessage(0);
    }

    @Override
    public void show() {
        AndroidUtilities.runOnUIThread(this.applicationHandler, super::show);
    }

    @Override
    public void dismiss() {
        AndroidUtilities.runOnUIThread(this.applicationHandler, super::dismiss);
    }

    @SuppressLint("HandlerLeak")
    private
    Handler handler = new Handler () {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                binding.tvMessage.setText(message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LoadingLayoutBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
//        setCancelable(canCancel);
        setCanceledOnTouchOutside(canCancel);
        binding.linearLayout.setBackground(ContextCompat.getDrawable(context, backgroundDrawable));
        binding.tvMessage.setText(message);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}