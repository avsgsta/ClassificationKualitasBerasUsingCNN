package com.example.kualitasberas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class PilihGalery extends AppCompatActivity {
    private String[] LABELS = {"Bagus", "Buruk"};
    Button btnPilihGaleri, btnDeteksi;
    TextView result;
    TextView textViewClassified;
    Bitmap bitmap;
    ImageView image;

    private String modelPath = "ml/model_8.tflite";
    private Interpreter tflite;
    private final int NUM_CLASSES = 2; // Masukkan jumlah kelas pada model Anda;
    private final int INPUT_SIZE = 224; // Sesuaikan dengan ukuran input model Anda

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_galery);

        btnPilihGaleri = findViewById(R.id.btnPilihGaleri);
        btnDeteksi = findViewById(R.id.btnDeteksi);
        result = findViewById(R.id.result);
        image =  findViewById(R.id.image);
        textViewClassified = findViewById(R.id.textViewClassified);

        try {
            tflite = new Interpreter(loadModelFile(getApplicationContext().getAssets(), "ml/model_8.tflite"));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal memuat model", Toast.LENGTH_SHORT).show();
            finish();
        }
        btnPilihGaleri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
            }
        });

        btnDeteksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap != null) {
                    runInference();
                } else {
                    Toast.makeText(PilihGalery.this, "Gambar belum diambil", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==10){
            if (data!=null){
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    image.setImageBitmap(bitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    // Add this method to convert Bitmap to float array
    private float[] bitmapToFloatArray(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        float[] floatValues = new float[width * height * 3];
        int pixel = 0;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                final int val = pixels[pixel++];
                floatValues[i * height * 3 + j * 3 + 0] = Color.red(val) / 255.0f;
                floatValues[i * height * 3 + j * 3 + 1] = Color.green(val) / 255.0f;
                floatValues[i * height * 3 + j * 3 + 2] = Color.blue(val) / 255.0f;
            }
        }

        return floatValues;
    }

    // Update runInference method to use the new method
    private void runInference() {
        try {
            // Resize gambar ke ukuran yang diharapkan oleh model
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

            // Convert Bitmap to float array
            float[] floatValues = bitmapToFloatArray(resizedBitmap);

            // Persiapkan input buffer
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(floatValues.length * 4);
            inputBuffer.order(ByteOrder.nativeOrder());
            inputBuffer.rewind();
            inputBuffer.asFloatBuffer().put(floatValues);

            // Persiapkan output buffer
            TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, NUM_CLASSES}, DataType.FLOAT32);

            // Jalankan model inference
            tflite.run(inputBuffer, outputBuffer.getBuffer());

            // Tampilkan hasil
            displayResult(outputBuffer.getFloatArray());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menjalankan inference", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayResult(float[] results) {
        // Display both labels and their percentages
        StringBuilder resultText = new StringBuilder("<html><body><b>Hasil:</b><br>");

        float maxPercentage = -1;
        int maxIndex = -1;

        for (int i = 0; i < results.length; i++) {
            // Konversi indeks kelas menjadi label yang sesuai
            String label = LABELS[i];

            // Tambahkan label dan persentase ke dalam StringBuilder
            resultText.append(label).append(": ").append(results[i] * 100).append("%<br>");

            // Cek apakah ini persentase terbesar yang ditemukan
            if (results[i] > maxPercentage) {
                maxPercentage = results[i];
                maxIndex = i;
            }
        }
        // Set text pada TextView (asumsikan resultkamera dan highestPercentageTextView adalah objek TextView)
        result.setText(Html.fromHtml(resultText.toString()));

        // Set text pada TextView untuk persentase tertinggi dan labelnya
        textViewClassified.setText("Classified As: " + "Kualitas "+ LABELS[maxIndex]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }
}