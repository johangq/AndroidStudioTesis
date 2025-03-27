package com.example.tesisandroid.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.tesisandroid.MainActivity;
import com.example.tesisandroid.R;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class SubirFragment extends Fragment {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private TextView txtFileName;
    private Button btnUploadFile;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subir, container, false);

        txtFileName = view.findViewById(R.id.txtFileName);
        Button btnSelectFile = view.findViewById(R.id.btnSelectFile);
        btnUploadFile = view.findViewById(R.id.btnUploadFile);
        progressBar = view.findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
        btnUploadFile.setEnabled(false);

        btnSelectFile.setOnClickListener(v -> selectFile());
        btnUploadFile.setOnClickListener(v -> uploadFile());

        return view;
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Selecciona un archivo"), PICK_FILE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                String selectedPath = fileUri.getPath();
                txtFileName.setText(selectedPath);
                btnUploadFile.setEnabled(true);
            }
        }
    }

    private void uploadFile() {
        if (fileUri == null) {
            Toast.makeText(getContext(), "Selecciona un archivo primero", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(Objects.requireNonNull(fileUri.getPath()));
        if (!file.exists()) {
            Toast.makeText(getContext(), "No se pudo encontrar el archivo", Toast.LENGTH_SHORT).show();
            return;
        }

        new UploadTask().execute(file);
    }

    private class UploadTask extends AsyncTask<File, Integer, String> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(File... files) {
            File file = files[0];
            String serverURL = MainActivity.BASE_URL + "documentos/subir";
            String boundary = "*****";
            String lineEnd = "\r\n";
            String twoHyphens = "--";

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(serverURL).openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"archivo\"; filename=\"" + file.getName() + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                outputStream.writeBytes(lineEnd);

                FileInputStream fileInputStream = new FileInputStream(file);
                int bytesAvailable = fileInputStream.available();
                int bufferSize = Math.min(bytesAvailable, 1024);
                byte[] buffer = new byte[bufferSize];

                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer, 0, bufferSize)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, 1024);
                }

                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK ? "Archivo subido exitosamente" : "Error en la subida";
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
        }
    }
}