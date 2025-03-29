package com.example.tesisandroid.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.tesisandroid.MainActivity;
import com.example.tesisandroid.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class BuscarFragment extends Fragment {

    private EditText etBuscar;
    private Button btnBuscar;
    private LinearLayout layoutResultados;
    private RequestQueue requestQueue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar, container, false);

        // Inicializar elementos del layout
        etBuscar = view.findViewById(R.id.etBuscar);
        btnBuscar = view.findViewById(R.id.btnBuscar);
        layoutResultados = view.findViewById(R.id.layoutResultados);

        requestQueue = Volley.newRequestQueue(requireContext());

        btnBuscar.setOnClickListener(v -> buscarDocumento());

        return view;
    }

    private void buscarDocumento() {
        String query = etBuscar.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Ingresa un término de búsqueda", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = MainActivity.BASE_URL + "documentos/buscar";

        // Crear el JSON con el parámetro de búsqueda
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("parametroBusqueda", query);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        layoutResultados.removeAllViews(); // Limpiar resultados anteriores
                        boolean success = response.getBoolean("success");

                        if (success) {
                            JSONArray documentos = response.getJSONArray("documentos");

                            for (int i = 0; i < documentos.length(); i++) {
                                JSONObject doc = documentos.getJSONObject(i);
                                String archivo = doc.getString("archivo");
                                String contenido = doc.getString("contenido");

                                agregarResultado(archivo, contenido);
                            }
                        } else {
                            Toast.makeText(getContext(), "No se encontraron documentos", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void agregarResultado(String archivo, String contenido) {
        // Crear un nuevo TextView para el nombre del archivo
        TextView tvArchivo = new TextView(getContext());
        tvArchivo.setText("📄 " + archivo);
        tvArchivo.setTextSize(18);
        tvArchivo.setPadding(10, 10, 10, 5);

        // Crear otro TextView para el contenido
        TextView tvContenido = new TextView(getContext());
        tvContenido.setText(contenido);
        tvContenido.setTextSize(16);
        tvContenido.setPadding(10, 5, 10, 10);

        // Agregar al layout de resultados
        layoutResultados.addView(tvArchivo);
        layoutResultados.addView(tvContenido);
    }
}
