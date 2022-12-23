package com.josebv.subirimagenes;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextInputLayout email, password;
    Button btnLogin;
    RequestQueue requestQueue;
    String URI_LOGIN = "http://TU_DOMINIO:3000/api/login";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Iniciando sesión");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String emailUsuario = email.getEditText().getText().toString();
                String passUsuario = password.getEditText().getText().toString();

                if ( validarCampos(emailUsuario, passUsuario) ) {
                    progressDialog.show();
                    login(emailUsuario, passUsuario);
                }
            }
        });
    }

    private Boolean validarCampos(String email, String password) {
        if ( email.isEmpty() || password.isEmpty() ) {
            Toast.makeText(this, "Debes llenar ambos campos", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void login(String emailUser, String passwordUser) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URI_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {

                            JSONObject res = new JSONObject(response);
                            String message = res.getString("message");
                            Integer code = Integer.parseInt( res.getString("code") );

                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (code == 200) {
                                JSONObject usuario = new JSONObject( res.getString("usuario") );

                                // Evitar que se pueda acceder a la activity una vez iniada la sesion
                                finish();

                                Intent abrirHome = new Intent(getApplicationContext(), HomeActivity.class);
                                abrirHome.putExtra("idUser", usuario.getString("_id"));
                                startActivity(abrirHome);
                            }
                        } catch (JSONException e) { e.printStackTrace(); }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        )

        {
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> datos = new HashMap<>();

                datos.put("email", emailUser);
                datos.put("contrasena", passwordUser);

                return datos;
            }
        };

        requestQueue.add(stringRequest);
    }
}
