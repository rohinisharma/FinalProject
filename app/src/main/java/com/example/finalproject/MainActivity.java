package com.example.finalproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
   private Button UploadBn, ChooseBn;
   private ImageView img;
   Bitmap bitmap;
   private final int IMG_REQUEST = 1;
   private String pathToImg;
   private static String apiKey = "cbe4ec0015644dc4b1594f756e32bfa7";
   private static String endpoint = "https://cs125-project.cognitiveservices.azure.com/vision/v2.1/describe";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UploadBn = findViewById(R.id.uploadBn);
        ChooseBn = findViewById(R.id.chooseBn);
        img = findViewById(R.id.image);
        ChooseBn.setOnClickListener(this);
        UploadBn.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chooseBn:
                selectImage();
                break;
            case R.id.uploadBn:
                uploadImage();
                break;
            case R.id.another:
                setContentView(R.layout.activity_main);
                UploadBn = findViewById(R.id.uploadBn);
                ChooseBn = findViewById(R.id.chooseBn);
                ChooseBn.setOnClickListener(this);
                UploadBn.setOnClickListener(this);
                img = findViewById(R.id.image);
        }
    }
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri path = data.getData();
            pathToImg = path.getPath();
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), path);
                bitmap = ImageDecoder.decodeBitmap(source);
                img.setImageBitmap(bitmap);
                img.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void uploadImage() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        final byte[] imgData = stream.toByteArray();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, endpoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    System.out.println(jsonObject);
                    try {
                        JSONObject desc = (JSONObject) jsonObject.get("description");
                        String cap = desc.getJSONArray("captions").getJSONObject(0).getString("text");
                        System.out.println(cap);
                        showCaption(cap);

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("it dont work");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error, please try again", Toast.LENGTH_SHORT);

            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("visualFeatures","Categories, Description, Color");
                return params;
            }
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Ocp-Apim-Subscription-Key", apiKey);
                headers.put("Content-Type", "application/octet-stream");
                return headers;
            }
            @Override
            public byte[] getBody() {
                return imgData;
            }
        };
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);
    }
    public void showCaption(String caption) {
        setContentView(R.layout.caption_display);
        TextView capText = findViewById(R.id.caption);
        Button another = findViewById(R.id.another);
        another.setOnClickListener(this);
        capText.setText(caption);
    }
}
