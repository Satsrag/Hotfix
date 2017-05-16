package com.zuga.hotfix.hotfix.Net;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.tencent.tinker.android.dex.util.FileUtils.readStream;

/**
 * @author saqrag
 * @version 1.0
 * @see null
 * 15/05/2017
 * @since 1.0
 **/

class HttpHandle {
    private Callback mCallback;

    HttpHandle(Callback callback) {
        mCallback = callback;
    }

    void cancel() {
        if (mCallback != null) {
            mCallback.onCancelled();
        }
        mCallback = null;
    }

    void get(String url, Params params) {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                if (params == null || params.length <= 0) return null;
                URL url;
                HttpURLConnection urlConn = null;
                try {
                    url = new URL(params[0]);
                    urlConn = (HttpURLConnection) url.openConnection();
                    urlConn.setConnectTimeout(5 * 1000);
                    urlConn.connect();
                    if (urlConn.getResponseCode() == 200) {
                        byte[] data = readStream(urlConn.getInputStream());
                        return new String(data, "utf-8");
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConn != null) {
                        urlConn.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (mCallback == null) return;
                if (TextUtils.isEmpty(result)) {
                    mCallback.onError(1);
                    return;
                }
                result = result.replaceAll("\\\\", "");
                mCallback.onSuccess(result);
            }
        }.execute(url + params.getParams());
    }

    void post() {

    }

    void down(String url, String savePath) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                InputStream input = null;
                OutputStream output = null;
                if (params == null || params.length <= 0) return null;
                final String URL = params[0];
                if (params.length < 2) return null;
                final String PATH = params[1];
                try {
                    URL downUrl = new URL(URL);
                    HttpURLConnection connection = (HttpURLConnection) downUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5 * 1000);
                    input = connection.getInputStream();
                    final int length = connection.getContentLength();
                    final File file = new File(PATH + ".down");
                    output = new FileOutputStream(file);
                    byte data[] = new byte[2048];
                    int progress = 0;
                    int count;
                    while ((count = input.read(data, 0, 1024)) != -1) {
                        output.write(data, 0, count);
                        progress += count;
                        publishProgress(progress);
                    }
                    output.flush();
                    output.close();
                    output = null;
                    input.close();
                    input = null;
                    final boolean b = file.renameTo(new File(PATH));
                    return b ? PATH : null;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mCallback != null) {
                        mCallback.onError(1);
                    }
                } finally {
                    try {
                        if (output != null) {
                            output.close();
                            output = null;
                        }
                        if (input != null) {
                            input.close();
                            input = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                if (values == null || values.length < 1) return;
                if (mCallback != null) {
                    mCallback.onProgressUpdate(values[0]);
                }
            }

            @Override
            protected void onPostExecute(String s) {
                if (mCallback == null) {
                    return;
                }
                if (TextUtils.isEmpty(s)) {
                    mCallback.onError(1);
                    return;
                }
                mCallback.onSuccess(s);
            }
        }.execute(url, savePath);
    }
}
