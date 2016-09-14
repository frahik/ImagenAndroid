package com.example.frahi.imagen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    // Declarar Elementos de Interfaz
    Button buttonFoto, buttonUrl, buttonOpen;
    ImageView imageFoto;
    EditText etUrl;

    // Variables de ayuda para el resultado de Imagenes en local
    private final int IDRESULT = 100;
    private static int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inflar Elementos de Interfaz
        buttonFoto = (Button)findViewById(R.id.btnTomarFoto);
        buttonOpen = (Button)findViewById(R.id.btnOpen);
        buttonUrl = (Button)findViewById(R.id.btnOpenUrl);
        etUrl = (EditText)findViewById(R.id.etUrl);
        imageFoto = (ImageView)findViewById(R.id.ivImagen);

        //Evento de presionar el boton para TOMAR FOTOS
        buttonFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamar un INTENT predefinido externo para capturar la IMAGEN
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Procesar el RESULTADO
                startActivityForResult(intent, IDRESULT);
            }
        });

        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamar un INTENT predefinido externo para capturar la IMAGEN de una GALERIA
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                // Procesar el RESULTADO
                startActivityForResult(intent,SELECT_PICTURE);;
            }
        });

        buttonUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Recuperar la URL escrita en el EDITEXT
                String url = etUrl.getText().toString();
                if (url.length()>0){
                    //Llamar una Tarea ASINCRONA para descargar y mostrar la imagen (Requerido desde Android 3)
                    new DownloadImageTask().execute(url);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IDRESULT){
            //Si la respuesta proviene del primer boton (TOMAR FOTO) se ejecuta esto:
            if(resultCode == RESULT_OK){
                //Si se tomo la foto, se ejecuta esto:
                //Convertir los datos en un mapa de bits
                Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                //Mostrar la imagen en el IMAGEVIEW
                imageFoto.setImageBitmap(bitmap);
            }
        }else if(requestCode == SELECT_PICTURE){
            //Si la respuesta proviene del segundo boton(ABRIR FOTO [DE GALERIA]) se ejecuta esto:
            //Se recupera la URI de la imagen y se hace un input de los datos
            Uri selectedImage = data.getData();
            InputStream is;
            try {
                //Se intenta resolver la localización de la imagen y mandarla a buffer
                is = getContentResolver().openInputStream(selectedImage);
                BufferedInputStream bis = new BufferedInputStream(is);
                //Convertir los datos en un mapa de bits
                Bitmap bitmap = BitmapFactory.decodeStream(bis);
                //Mostrar la imagen en el IMAGEVIEW
                imageFoto.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {}
        }
    }

    class DownloadImageTask extends AsyncTask<String, Void, Drawable>{
        /*
         * ESTA CLASE FUNCIONA PARA DESCARGAR UNA IMAGEN DE INTERNET; Funciona de manera Asincrona.
         */
        protected Drawable doInBackground(String... urls){
            //Log.d("DEBUG", "drawable");
            return downloadImage(urls[0]);
        }

        protected void onPostExecute(Drawable imagen){
            //Terminada la descarga se realiza el POSTEXECUTE que muestra la imagen el el IMAGEVIEW
            imageFoto.setImageDrawable(imagen);
        }

        /**
         * Lo siguiente devuelve una imagen desde una URL
         * @return Una imagen
         */
        private Drawable downloadImage(String imageUrl){
            try{
                //Se transforma el string recuperado en una URL
                URL url = new URL(imageUrl);
                //Se recuperan los datos de la url
                InputStream is = (InputStream)url.getContent();
                //Se transforman los datos en un DRAWABLE, que sera la imagen a mostrar (Y se retorna)
                return Drawable.createFromStream(is, "src");
            }
            catch (MalformedURLException e){
                //Excepciones requeridas
                e.printStackTrace();
                return null;
            }
            catch (IOException e){
                //Excepciones requeridas
                e.printStackTrace();
                return null;
            }
        }
    }
}
