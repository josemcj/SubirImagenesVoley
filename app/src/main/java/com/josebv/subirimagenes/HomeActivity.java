package com.josebv.subirimagenes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    ShapeableImageView imgUser;
    TextView tvNameUser;
    RequestQueue requestQueue;
    String idUsuario;
    Button btnEditar;
    String URI_USER = "http://TU_DOMINIO:3000/api/usuario/";
    String URI_IMG_USER = "http://TU_DOMINIO:3000/static/images/users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        idUsuario = getIntent().getStringExtra("idUser");

        requestQueue = Volley.newRequestQueue(this);
        imgUser = findViewById(R.id.imgUser);
        tvNameUser = findViewById(R.id.tvNameUser);
        btnEditar = findViewById(R.id.btnEditar);

        cargarDatos();

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editarCuenta = new Intent(getApplicationContext(), EditarCuentaActivity.class);
                editarCuenta.putExtra("idUser", idUsuario);
                startActivity(editarCuenta);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Recarga los datos
        cargarDatos();
    }

    protected void cargarDatos() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URI_USER + idUsuario,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject res = new JSONObject(response);
                            Integer status = Integer.parseInt( res.getString("code") );

                            if (status == 200) {
                                JSONObject usuario = new JSONObject( res.getString("usuario") );

                                // Cargar imagen desde la API
                                Picasso.get()
                                        .load(URI_IMG_USER + usuario.getString("imagen"))
                                        .into(imgUser);
                                tvNameUser.setText( usuario.getString("nombre") );
                            }

                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestQueue.add(stringRequest);

    }
}
