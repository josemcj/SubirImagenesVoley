package com.josebv.subirimagenes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditarCuentaActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99 ;

    ShapeableImageView siImgUser;
    Button btnCamara, btnGaleria, btnActualizar;
    TextInputLayout etNombre;
    String idUsuario;
    String URI_USER = "http://192.168.137.1:3000/api/usuario/";
    String URI_IMG_USER = "http://192.168.137.1:3000/static/images/users/";
    String URI_PATCH_USER = "http://192.168.137.1:3000/api/editar/";
    RequestQueue requestQueue;
    Bitmap nuevaImagen;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_cuenta);

        requestQueue = Volley.newRequestQueue(EditarCuentaActivity.this);
        progressDialog = new ProgressDialog(EditarCuentaActivity.this);
        progressDialog.setMessage("Guardando");
        idUsuario = getIntent().getStringExtra("idUser");

        siImgUser = findViewById(R.id.siImgUser);
        etNombre = findViewById(R.id.etNombre);
        btnCamara = findViewById(R.id.btnCamara);
        btnGaleria = findViewById(R.id.btnGaleria);
        btnActualizar = findViewById(R.id.btnActualizar);

        cargarDatos();

        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ( CheckPermission() ) {
                    Intent camara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camara, 0);
                }
            }
        });

        btnGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ( CheckPermission() ) {
                    Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galeria, 1);
                }
            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                actualizarDatos(nuevaImagen);
            }
        });

        // Boton de retorno
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Boton de retorno
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 0: {
                // Tomar foto
                if (resultCode == RESULT_OK) {
                    nuevaImagen = (Bitmap) data.getExtras().get("data");

                    // Colocar foto en la imagen de la activity
                    siImgUser.setImageBitmap(nuevaImagen);
                }
            }
            break;

            case 1: {
                // Seleccionar de la galeria
                if (resultCode == RESULT_OK) {

                    try {
                        Uri imageUri = data.getData();
                        nuevaImagen = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        siImgUser.setImageBitmap(nuevaImagen);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }

    }

    protected void actualizarDatos(Bitmap nuevaImg) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        nuevaImg.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        String nuevaImagen = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        String nuevoNombre = etNombre.getEditText().getText().toString();

        StringRequest stringRequest = new StringRequest(Request.Method.PATCH, URI_PATCH_USER + idUsuario,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {

                            JSONObject res = new JSONObject(response);
                            String message = res.getString("message");
                            Integer code = Integer.parseInt( res.getString("code") );

                            if (code == 200) {
                                finish();
                            }

                            Toast.makeText(EditarCuentaActivity.this, message, Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) { e.printStackTrace(); }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(EditarCuentaActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        )
        {
            protected Map<String, String> getParams() {

                Map<String, String> datos = new HashMap<>();

                datos.put("nombre", nuevoNombre);
                datos.put("imagen", nuevaImagen);

                return datos;
            }
        };

        requestQueue.add(stringRequest);
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
                                        .into(siImgUser);
                                etNombre.getEditText().setText( usuario.getString("nombre") );
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

    public boolean CheckPermission() {
        if (ContextCompat.checkSelfPermission(EditarCuentaActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(EditarCuentaActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(EditarCuentaActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(EditarCuentaActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(EditarCuentaActivity.this,
                    Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(EditarCuentaActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(EditarCuentaActivity.this)
                        .setTitle("Permisos necesarios")
                        .setMessage("Por favor, acepta los permisos para acceder a la cámara y al contenido multimedia.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(EditarCuentaActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_LOCATION);


                                startActivity(new Intent(EditarCuentaActivity
                                        .this, EditarCuentaActivity.class));
                                EditarCuentaActivity.this.overridePendingTransition(0, 0);
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(EditarCuentaActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {

            return true;

        }
    }
}