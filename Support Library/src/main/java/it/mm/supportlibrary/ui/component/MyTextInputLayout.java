package it.mm.supportlibrary.ui.component;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import it.mm.supportlibrary.R;

/**
 * Created by Dott. Marco Mezzasalma on 23/04/2024.
 */

public class MyTextInputLayout extends TextInputLayout {
    private boolean required = false;
    TextInputEditText textInputEditText;
    MaterialAutoCompleteTextView materialAutoCompleteTextView;

    public MyTextInputLayout(Context context) {
        this(context, null);
    }

    public MyTextInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.MyTextInputLayout);
        required = ta.getBoolean(R.styleable.MyTextInputLayout_required, false);
        ta.recycle();

        this.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorPrimary)));
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.textInputEditText != null)
            this.textInputEditText.setEnabled(enabled);
        if (enabled) {
            this.setHelperTextEnabled(false);
        } else {
            this.setHelperTextEnabled(true);
            this.setHelperText("Non modificabile");
        }
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        this.setError(null);
    }

    public void setTextInputEditText(TextInputEditText textInputEditText, TextWatcher textWatcher) {
        this.textInputEditText = textInputEditText;
        TextInputLayout textInputLayout = this;
        if (textWatcher != null) {
            this.textInputEditText.addTextChangedListener(textWatcher);
        } else {
            this.textInputEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //do nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().isEmpty()) {
                        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
                        textInputLayout.setError(null);
                        textInputLayout.setErrorEnabled(false);
                    } else {
                        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    }
                }
            });
        }
    }

    public void setMaterialAutoCompleteTextView(MaterialAutoCompleteTextView materialAutoCompleteTextView, TextWatcher textWatcher) {
        this.materialAutoCompleteTextView = materialAutoCompleteTextView;
        TextInputLayout textInputLayout = this;
        if (textWatcher != null) {
            this.materialAutoCompleteTextView.addTextChangedListener(textWatcher);
        } else {
            this.materialAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //do nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().isEmpty()) {
                        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
                        textInputLayout.setError(null);
                    } else {
                        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_DROPDOWN_MENU);
                    }
                }
            });
        }
    }

    public boolean isValid() {
        if (required) {
            if (textInputEditText != null && textInputEditText.getText().toString().isEmpty()) {
                this.setError("Campo obbligatorio*");
                return false;
            } else if (materialAutoCompleteTextView != null && materialAutoCompleteTextView.getText().toString().isEmpty()) {
                this.setError("Campo obbligatorio*");
                return false;
            }
        }
        this.setError(null);
        return true;
    }

}
