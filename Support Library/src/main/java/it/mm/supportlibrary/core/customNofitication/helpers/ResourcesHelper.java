package it.mm.supportlibrary.core.customNofitication.helpers;

import android.content.Context;

import androidx.annotation.NonNull;

public class ResourcesHelper {
    public static String getStringResourceByKey(@NonNull Context context,@NonNull String resourceKey) {
        int resId = context.getResources().getIdentifier(resourceKey, "string", context.getPackageName());
        return context.getResources().getString(resId);
    }
}
