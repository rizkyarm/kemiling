package com.android.kemilingcom;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class myAccount extends AppCompatActivity {

    private static final String TAG = "myAccount";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private EditText editEmail, editPhone, editBusinessName, edituser;
    private ImageView imgProfile;
    private TextView yourName;
    private Button btnSave;
    private SharedPreferences sharedPreferences;
    private String userName;
    public Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        // Initialize views
        editEmail = findViewById(R.id.editemail);
        edituser = findViewById(R.id.edituser);
        editPhone = findViewById(R.id.editnotelp);
        editBusinessName = findViewById(R.id.editNU);
        imgProfile = findViewById(R.id.img_profile);
        yourName = findViewById(R.id.your_name);
        btnSave = findViewById(R.id.btn_save);

        // Check for storage permission
        checkStoragePermission();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userName = sharedPreferences.getString("userName", "");

        // Check if userName is not null
        if (userName != null && !userName.isEmpty()) {
            // Set username to TextView
            yourName.setText(userName);

            // Fetch user data
            new GetProfileDataTask().execute(userName);
        } else {
            Toast.makeText(this, "Username is not available in SharedPreferences", Toast.LENGTH_SHORT).show();
        }

        // Set profile image click listener
        imgProfile.setOnClickListener(v -> openImagePicker());

        // Save user data
        btnSave.setOnClickListener(v -> updateUserData(userName, selectedImageUri));
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String fileToBase64(Uri uri, String format) {
        String base64String = "";
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] bytes = getBytes(inputStream);
            String base64Header = "data:image/" + format + ";base64,";
            base64String = base64Header + Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64String;
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }


    public void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void updateUserData(String userName, Uri selectedImageUri) {
        if (userName == null || userName.isEmpty()) {
            Toast.makeText(this, "Username is not available in SharedPreferences", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject userData = new JSONObject();
        try {
            // Menambahkan data lainnya seperti email, no_telp, dan nama_usaha
            userData.put("email", editEmail.getText().toString());
            userData.put("username", edituser.getText().toString());
            userData.put("no_telp", editPhone.getText().toString());
            userData.put("nama_usaha", editBusinessName.getText().toString());

            // Mengecek apakah gambar dipilih
            if (selectedImageUri != null) {
                // Tentukan format gambar (misalnya "jpeg", "png", "heic", "webp", "jpg")
                String format = "jpeg"; // Ubah sesuai format gambar yang dipilih

                // Menentukan format berdasarkan URI
                String mimeType = getContentResolver().getType(selectedImageUri);
                if (mimeType != null) {
                    switch (mimeType) {
                        case "image/jpeg":
                            format = "jpeg";
                            break;
                        case "image/png":
                            format = "png";
                            break;
                        case "image/heic":
                            format = "heic";
                            break;
                        case "image/webp":
                            format = "webp";
                            break;
                        case "image/jpg":
                            format = "jpg";
                            break;
                    }
                }

                // Mengubah file gambar menjadi base64 tanpa kompresi
                String base64ProfileImage = fileToBase64(selectedImageUri, format);
                userData.put("profile_image", base64ProfileImage);  // Menambahkan base64 gambar ke dalam JSON
                Log.d("ImageBase64", "Profile Image: " + base64ProfileImage);  // Cek base64 yang dikirim
            } else {
                Log.d("ImageBase64", "No profile image selected");
            }



            // Mengirimkan data JSON ke API menggunakan AsyncTask atau HTTP request
            new UpdateUserTask().execute(userName, userData.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON data", e);
        }
    }


    private void loadProfileImage(String imageUrl) {
        // Normalize the URL
        imageUrl = normalizeUrl(imageUrl);

        // Load the image using Picasso
        Picasso.get()
                .load(imageUrl)
                .into(imgProfile); // Memuat gambar ke ImageView
    }

    // Method to normalize the URL
    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty() || url.equals("null")) {
            return "https://store.kemiling.com/assets/icon/profile.png"; // Default image URL
        }

        // Hanya menghapus "../" pada URL, bukan seluruh struktur URL
        url = url.replaceAll("\\.\\./", ""); // Hapus "../" pada path

        return url;
    }

    // Class for updating user data
    private class UpdateUserTask extends AsyncTask<String, Void, String> {
        private String profileImageUrl;

        @Override
        protected String doInBackground(String... params) {
            String userName = params[0];
            String userData = params[1];
            String uploadUrl = "https://store.kemiling.com/side-page/User/api_profile.php?username=" + userName;

            try {
                URL url = new URL(uploadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(userData.getBytes());
                os.flush();
                os.close();

                // Baca respons
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d("Server Response", response.toString()); // Logging Server Response

                    // Parsing server response to get profile_image URL
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    profileImageUrl = jsonResponse.optString("profile_image");

                    return response.toString();
                } else {
                    Log.e("Upload Error", "Upload gagal. Kode: " + responseCode); // Logging Error Response
                    return "Upload gagal. Kode: " + responseCode;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error upload gambar", e);
                return "Error upload gambar: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(myAccount.this, result, Toast.LENGTH_SHORT).show();
                // Load the profile image on the main thread using Picasso
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadProfileImage(profileImageUrl); // Memuat gambar profil setelah update
                    }
                });
            } else {
                Toast.makeText(myAccount.this, "Error uploading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            try {
                // Load the selected image into the ImageView using Picasso
                Picasso.get()
                        .load(selectedImageUri)
                        .into(imgProfile); // Muat gambar ke ImageView
            } catch (Exception e) {
                Log.e("ImageError", "Error loading image: " + e.getMessage());
            }
        }
    }

    // Class for fetching profile data
    private class GetProfileDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String userName = params[0];
            String apiUrl = "https://store.kemiling.com/side-page/User/api_profile.php?username=" + userName;
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                } else {
                    Log.e(TAG, "GET request not worked. Response Code: " + responseCode);
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching user data", e);
                return null;
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject user = new JSONObject(result);
                    // Update UI with user data
                    editEmail.setText(user.getString("email"));
                    edituser.setText(user.getString("username"));
                    editPhone.setText(user.getString("no_telp"));
                    editBusinessName.setText(user.getString("nama_usaha"));

                    // Get profile image URL and normalize it
                    String profileImageUrl = "https://store.kemiling.com/side-page/Pendaftaran/" + user.getString("profile_image");
                    profileImageUrl = normalizeUrl(profileImageUrl);

                    // Log the URL to ensure it's correct
                    Log.d(TAG, "Profile image URL: " + profileImageUrl);

                    Glide.with(myAccount.this)
                            .load(profileImageUrl)
                            .error(R.drawable.account_icon)
                            .into(imgProfile);

                    Toast.makeText(myAccount.this, "User data loaded", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing user data", e);
                    Toast.makeText(myAccount.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(myAccount.this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
