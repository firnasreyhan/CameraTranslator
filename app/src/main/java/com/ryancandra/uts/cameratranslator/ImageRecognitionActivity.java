package com.ryancandra.uts.cameratranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.ryancandra.uts.cameratranslator.api.Api;
import com.ryancandra.uts.cameratranslator.api.ApiClient;
import com.ryancandra.uts.cameratranslator.api.response.LanguageResponse;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageRecognitionActivity extends AppCompatActivity {
    private ImageView imageViewGambar;
    private Button buttonPilihGambar, buttonTerjemahkan, buttonHapus;
    private TextView textViewBahasaAsal, textViewTeksAsal, textViewHasilTerjemahan;
    private Spinner spinnerBahasaTerjemahan;
    private Toolbar toolbar;

    private Api api;
    private ProgressDialog progressDialog;
    private Translator translator;

    private ArrayList<LanguageResponse.LanguageModel> list;

    private String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.BlueTheme);
        setContentView(R.layout.activity_image_recognition);

        api = ApiClient.getClient();
        imageViewGambar = findViewById(R.id.imageViewGambar);
        buttonPilihGambar = findViewById(R.id.buttonPilihGambar);
        buttonTerjemahkan = findViewById(R.id.buttonTerjemahkan);
        buttonHapus = findViewById(R.id.buttonHapus);
        textViewBahasaAsal = findViewById(R.id.textViewBahasaAsal);
        textViewTeksAsal = findViewById(R.id.textViewTeksAsal);
        textViewHasilTerjemahan = findViewById(R.id.textViewHasilTerjemahan);
        spinnerBahasaTerjemahan = findViewById(R.id.spinnerBahasaTerjemahan);
        progressDialog = new ProgressDialog(this);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        spinnerBahasaTerjemahan.setEnabled(false);
        textViewHasilTerjemahan.setEnabled(false);

        buttonPilihGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ImageRecognitionActivity.this);
            }
        });

        progressDialog.setMessage("Tunggu Sebentar...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        api.getLanguage().enqueue(new Callback<LanguageResponse>() {
            @Override
            public void onResponse(Call<LanguageResponse> call, Response<LanguageResponse> response) {
                if (response.body() != null) {
                    if (!response.body().error) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        list = response.body().data;
                        LanguageResponse.LanguageModel model = new LanguageResponse.LanguageModel();
                        model.code = "00";
                        model.country = "-";
                        list.add(0, model);
                        setSpinnerItem(response.body().data);
                    } else {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(ImageRecognitionActivity.this, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LanguageResponse> call, Throwable t) {
                Log.e("getLanguage", t.getMessage());
            }
        });

        buttonTerjemahkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerBahasaTerjemahan.getSelectedItemPosition() != 0) {
                    progressDialog.setMessage("Tunggu Sebentar...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    translate(textViewTeksAsal.getText().toString(), from, list.get(spinnerBahasaTerjemahan.getSelectedItemPosition()).code);
                } else {
                    Toast.makeText(ImageRecognitionActivity.this, "Pilih bahasa tujuan dengan benar!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ImageRecognitionActivity.this)
                        .setMessage("Apakah anda yakin ingin mengahpus data ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                imageViewGambar.setImageResource(R.drawable.ic_round_image);
                                textViewBahasaAsal.setText("-");
                                textViewTeksAsal.setText("-");
                                spinnerBahasaTerjemahan.setSelection(0);
                                textViewHasilTerjemahan.setText("-");
                                spinnerBahasaTerjemahan.setEnabled(false);
                                textViewHasilTerjemahan.setEnabled(false);
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    InputImage inputImage = InputImage.fromFilePath(this, resultUri);
                    TextRecognizer recognizer = TextRecognition.getClient();
                    recognizer.process(inputImage)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text text) {
                                    detectLanguage(text.getText());
                                    textViewTeksAsal.setText(text.getText());
                                    //translate(text.getText());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ImageRecognitionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageViewGambar.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void setSpinnerItem(ArrayList<LanguageResponse.LanguageModel> list) {
        ArrayAdapter<LanguageResponse.LanguageModel> adapter = new ArrayAdapter<LanguageResponse.LanguageModel>(this, R.layout.item_spinner, list);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        spinnerBahasaTerjemahan.setAdapter(adapter);

        spinnerBahasaTerjemahan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textViewHasilTerjemahan.setText("-");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void detectLanguage(String s) {
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(s)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (languageCode.equals("und")) {
                                    Toast.makeText(ImageRecognitionActivity.this, "Bahasa pada gambar tidak terdeksi, mohon ulangi ambil gambar", Toast.LENGTH_SHORT).show();
                                    Log.i("detectLanguage", "Can't identify language.");
                                    spinnerBahasaTerjemahan.setEnabled(false);
                                    textViewHasilTerjemahan.setEnabled(false);
                                } else {
                                    Log.i("detectLanguage", "Language: " + languageCode);
                                    for (LanguageResponse.LanguageModel model : list) {
                                        if (model.code.contentEquals(languageCode)) {
                                            textViewBahasaAsal.setText(model.country);
                                            from = model.code;
                                        }
                                    }
                                    spinnerBahasaTerjemahan.setEnabled(true);
                                    textViewHasilTerjemahan.setEnabled(true);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("detectLanguage", e.getMessage());
                            }
                        });
    }

    public void translate(String text, String from, String to) {
        if (!to.equalsIgnoreCase("00") && from != null) {
            Log.e("language", from + " | " + to);
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(from)
                    .setTargetLanguage(to)
                    .build();
            translator = Translation.getClient(options);

            DownloadConditions conditions = new DownloadConditions.Builder()
                    .requireWifi()
                    .build();

            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void v) {
                                    // Model downloaded successfully. Okay to start translating.
                                    // (Set a flag, unhide the translation UI, etc.)
                                    translator.translate(text)
                                            .addOnSuccessListener(
                                                    new OnSuccessListener<String>() {
                                                        @Override
                                                        public void onSuccess(@NonNull String translatedText) {
                                                            // Translation successful.
                                                            Log.e("Translate", translatedText);
                                                            textViewHasilTerjemahan.setText(translatedText);
                                                            if (progressDialog.isShowing()) {
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    })
                                            .addOnFailureListener(
                                                    new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Error.
                                                            // ...
                                                            Log.e("Translate", e.getMessage());
                                                        }
                                                    });
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Model couldn’t be downloaded or other internal error.
                                    // ...
                                    Log.e("TranslateFailed", e.getMessage());
                                }
                            });
        } else {
            spinnerBahasaTerjemahan.setSelection(0);
            textViewHasilTerjemahan.setText("-");
            Toast.makeText(this, "Gagal mendeteksi bahasa dari gambar", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if (translator != null) {
//            translator.close();
//        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}