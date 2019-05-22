package com.example.heroesapi;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import Api.Heroes;
import URL.url;
import model.ImageResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etDesc;
    private Button btnSave;
    private ImageView img;
    String imagePath, imageName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        btnSave = findViewById(R.id.btnSave);
        img = findViewById(R.id.img);
//   loadFormURL();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Save();


            }


        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowseImage();
            }
        });


    }

    private void StrictMode() {
        android.os.StrictMode.ThreadPolicy threadPolicy = new android.os.StrictMode.ThreadPolicy.Builder().permitAll().build();
        android.os.StrictMode.setThreadPolicy(threadPolicy);

    }

    private void SaveImageOnly() {
        File file = new File(imagePath);

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-dat"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("imageFile", file.getName(), requestBody);

        Heroes heroes = url.getInstance().create(Heroes.class);
        Call<ImageResponse> responseBodyCall = heroes.uploadImage(body);
        StrictMode();
        try {
            Response<ImageResponse> imageResponseResponse = responseBodyCall.execute();
            imageName = imageResponseResponse.body().getFilename();
        } catch (IOException e) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }


//    private void loadFormURL() {
//
//        StrictMode();
//        try {
//            String imgURL ="https://softwarica.edu.np/wp-content/uploads/2019/02/Kiran-Rana.jpg";
//            URL url = new URL(imgURL);
//            img.setImageBitmap(BitmapFactory.decodeStream((InputStream)url.getContent()));
//
//
//        }
//        catch (IOException e){
//            Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
//        }
//    }


    private void Save() {
        SaveImageOnly();
        String name = etName.getText().toString();
        String desc = etDesc.getText().toString();
        Map<String,String> map = new HashMap<>();
        map.put("name",name);
        map.put("desc",desc);
        map.put("image",imageName);

        Heroes heroes = url.getInstance().create(Heroes.class);

        Call<Void> heroesCall = heroes.addHero(name, desc);

        heroesCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (!response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Code", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "Successfully added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = new Intent(this,HeroesActivity.class);
        startActivity(intent);
        finish();

    }

    private void BrowseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Select an Image", Toast.LENGTH_LONG).show();
            }
        }
        Uri uri = data.getData();
        imagePath = getRealPathFromUri(uri);
        previewImage(imagePath);
    }


    private String getRealPathFromUri(Uri uri) {

        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(colIndex);
        cursor.close();
        return result;
    }

    private void previewImage(String imagePath) {
        File imgfile = new File(imagePath);
        if (imgfile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgfile.getAbsolutePath());
            img.setImageBitmap(myBitmap);
        }

    }


}



