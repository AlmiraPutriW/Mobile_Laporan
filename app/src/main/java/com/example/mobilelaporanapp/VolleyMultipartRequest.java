package com.example.mobilelaporanapp;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.*;
import java.util.*;

public abstract class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final String twoHyphens = "--";
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String lineEnd = "\r\n";

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> headers;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.headers = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Text parameters
            Map<String, String> params = getParams();
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    outputStream.write((twoHyphens + boundary + lineEnd).getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"" +
                            entry.getKey() + "\"" + lineEnd).getBytes());
                    outputStream.write((lineEnd).getBytes());
                    outputStream.write((entry.getValue() + lineEnd).getBytes());
                }
            }

            // File parameters
            Map<String, DataPart> data = getByteData();
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                    outputStream.write((twoHyphens + boundary + lineEnd).getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"" +
                            entry.getKey() + "\"; filename=\"" + entry.getValue().getFileName() + "\"" + lineEnd).getBytes());
                    outputStream.write(("Content-Type: " + entry.getValue().getType() + lineEnd).getBytes());
                    outputStream.write((lineEnd).getBytes());

                    ByteArrayInputStream fileInputStream = new ByteArrayInputStream(entry.getValue().getContent());
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.write(lineEnd.getBytes());
                }
            }

            outputStream.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    protected abstract Map<String, DataPart> getByteData() throws AuthFailureError;

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}
