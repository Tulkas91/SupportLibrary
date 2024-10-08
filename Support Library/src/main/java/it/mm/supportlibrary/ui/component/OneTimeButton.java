package it.mm.supportlibrary.ui.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import it.mm.supportlibrary.R;
import it.mm.supportlibrary.core.AndroidUtilities;

/**
 * Created by Dott. Ing. Giovanni Accetta on 15/09/17.
 */
public class OneTimeButton extends AppCompatButton {
    private int timoeut;
    private CountDownTimer timer = null;

    public void startTimer(Context context) {
        if (timer != null && timoeut > 0) {
            setEnabled(false);
            setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            timer.start();
        }
    }

    public OneTimeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.OneTimeButton,
                0, 0);

        try {
            timoeut = a.getInteger(R.styleable.OneTimeButton_timeout, 0);
            setTimer();
        } finally {
            a.recycle();
        }
    }

    private void setTimer() {
        timer = new CountDownTimer(timoeut, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                long remainedSecs = millisUntilFinished / 1000;

                setText("Reinvia SMS tra " + checkDigit((remainedSecs / 60)) + ":" + checkDigit((remainedSecs % 60)));
            }

            @Override
            public void onFinish() {
                setCompoundDrawables(AndroidUtilities.getDrawable(getContext(), R.drawable.baseline_sms, R.color.colorPrimary), null, null, null);
                setText("Reinvia SMS");
                setTextColor(Color.WHITE);
                setEnabled(true);
            }
        };
    }

    public OneTimeButton(Context context) {
        super(context);
    }

    public OneTimeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public String checkDigit(long number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }
}
