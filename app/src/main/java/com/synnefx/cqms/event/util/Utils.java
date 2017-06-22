package com.synnefx.cqms.event.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputLayout;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synnefx.cqms.event.R;

import timber.log.Timber;

public class Utils {

    private static Gson gson;

    private Utils() {
    }


    public static Gson getGsonParser() {
        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            // gsonBuilder.registerTypeAdapter(Filters.class, new DeserializerFilters());
            gson = gsonBuilder.create();
        }
        return gson;
    }

    /**
     * Generate top layer progress indicator.
     *
     * @param context    activity context
     * @param cancelable can be progress layer canceled
     * @return dialog
     */
    public static ProgressDialog generateProgressDialog(Context context, boolean cancelable) {
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.ProgressTheme);
        progressDialog.setMessage(context.getString(R.string.loading));
        progressDialog.setCancelable(cancelable);
        return progressDialog;
    }


    /**
     * Method converts iso date string to better readable form.
     *
     * @param isoDate input iso date. Example: "2016-04-13 13:21:04".
     * @return processed date string.
     */
    public static String parseDate(String isoDate) {
        try {
            String[] parts = isoDate.split("-");

            String year = parts[0];
            String month = parts[1];

            String dayTemp = parts[2];
            String[] parts2 = dayTemp.split(" ");
            String day = parts2[0].trim();
            if (day.length() > 2)
                throw new RuntimeException("String with day number unexpected length.");

            return day + "." + month + "." + year;
        } catch (Exception e) {
            Timber.e(e, "Parsing order date created failed.");
            return isoDate;
        }
    }


    public static int dpToPx(Context context, int dp) {
        return Math.round(dp * getPixelScaleFactor(context));
    }

    public static int pxToDp(Context context, int px) {
        return Math.round(px / getPixelScaleFactor(context));
    }

    private static float getPixelScaleFactor(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT;
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable)
            return ((BitmapDrawable) drawable).getBitmap();

        // We ask for the bounds if they have been set as they would be most
        // correct, then we check we are  > 0
        final int width = !drawable.getBounds().isEmpty() ? drawable.getBounds().width() : drawable.getIntrinsicWidth();

        final int height = !drawable.getBounds().isEmpty() ? drawable.getBounds().height() : drawable.getIntrinsicHeight();

        // Now we check we are > 0
        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width, height <= 0 ? 1 : height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    /**
     * Check if textInputLayout contains editText view. If so, then set text value to the view.
     *
     * @param textInputLayout wrapper for the editText view where the text value should be set.
     * @param text            text value to display.
     */
    public static void setTextToInputLayout(TextInputLayout textInputLayout, String text) {
        if (textInputLayout != null && textInputLayout.getEditText() != null) {
            textInputLayout.getEditText().setText(text);
        } else {
            Timber.e("Setting text to null input wrapper, or without editText");
        }
    }

    /**
     * Check if textInputLayout contains editText view. If so, then return text value of the view.
     *
     * @param textInputLayout wrapper for the editText view.
     * @return text value of the editText view.
     */
    public static String getTextFromInputLayout(TextInputLayout textInputLayout) {
        if (textInputLayout != null && textInputLayout.getEditText() != null) {
            return textInputLayout.getEditText().getText().toString();
        } else {
            return null;
        }
    }


    /**
     * Method checks if text input layout exist and contains some value.
     * If layout is empty, then show error value under the textInputLayout.
     *
     * @param textInputLayout textInputFiled for check.
     * @param errorValue      value displayed when ext input is empty.
     * @return true if everything ok.
     */
    public static boolean checkTextInputLayoutValueRequirement(TextInputLayout textInputLayout, String errorValue) {
        if (textInputLayout != null && textInputLayout.getEditText() != null) {
            String text = Utils.getTextFromInputLayout(textInputLayout);
            if (text == null || text.isEmpty()) {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError(errorValue);
                Timber.d("Input field %s missing text.", textInputLayout.getHint());
                return false;
            } else {
                textInputLayout.setErrorEnabled(false);
                Timber.d("Input field: %s OK.", textInputLayout.getHint());
                return true;
            }
        } else {
            Timber.e(new RuntimeException(), "Checking null input field during order send.");
            return false;
        }
    }
}
