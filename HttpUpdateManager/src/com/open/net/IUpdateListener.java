package com.open.net;

import android.content.Context;

public interface IUpdateListener {

    /**
     * Callback to update the downloaded file
     * @param context
     * @param rootPath The directory where the downloaded file located
     * @param fileNumbers How many files are contained in the directory
     * @return true if updated successfully, false if update failed.
     */
    public abstract boolean onUpdate(Context context, String rootPath, int fileNumbers);
}

