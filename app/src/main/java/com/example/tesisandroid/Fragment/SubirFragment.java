package com.example.tesisandroid.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
        btnUploadFile.setOnClickListener(v -> new UploadTask().execute(fileUri));

        return view;
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Selecciona un archivo"), PICK_FILE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                txtFileName.setText(getFileName(fileUri));
                btnUploadFile.setEnabled(true);
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (getContext() != null && uri != null) {
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {  // Verifica si el índice es válido
                        result = cursor.getString(index);
                    } else {
                        result = uri.getLastPathSegment();  // Usa el nombre de la URI como alternativa
                    }
                }
            }
        }
        return result;
    }
    private void escribirParametro(DataOutputStream writer, String boundary, String key, String value) throws Exception {
        writer.writeBytes("--" + boundary + "\r\n");
        writer.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
        writer.writeBytes(value + "\r\n");
    }
    private class UploadTask extends AsyncTask<Uri, Integer, String> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Uri... uris) {
            Uri fileUri = uris[0];
            String serverURL = MainActivity.BASE_URL + "documentos/subir";
            String boundary = "*****" + System.currentTimeMillis() + "*****";

            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
                String fileName = getFileName(fileUri);

                HttpURLConnection connection = (HttpURLConnection) new URL(serverURL).openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream outputStream = connection.getOutputStream();
                DataOutputStream writer = new DataOutputStream(outputStream);

                // 🔹 Agregar parámetros obligatorios al request
                escribirParametro(writer, boundary, "nombre", "Cotización Infozonal");
                escribirParametro(writer, boundary, "idCategoria", "1");
                escribirParametro(writer, boundary, "cliente", "Empresa XYZ");
                escribirParametro(writer, boundary, "fecha_emision", "2024-03-27");
                escribirParametro(writer, boundary, "importe_total", "500.00");

                // 🔹 Adjuntar archivo PDF
                writer.writeBytes("--" + boundary + "\r\n");
                writer.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n");
                writer.writeBytes("Content-Type: application/pdf\r\n\r\n");

                // Escribir contenido del archivo
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    writer.write(buffer, 0, bytesRead);
                }
                writer.writeBytes("\r\n--" + boundary + "--\r\n");

                writer.flush();
                writer.close();
                inputStream.close();

                // 🔹 Obtener respuesta del servidor
                InputStream responseStream = connection.getInputStream();
                byte[] responseBuffer = new byte[1024];
                int responseLength = responseStream.read(responseBuffer);
                responseStream.close();

                return new String(responseBuffer, 0, responseLength);

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

