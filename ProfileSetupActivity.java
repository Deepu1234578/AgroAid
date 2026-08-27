package com.example.agroaid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.*;

public class ProfileSetupActivity extends AppCompatActivity {

    EditText firstName, lastName, email, phone, farmSize;
    Spinner spinnerState, spinnerCrop, spinnerLanguage;
    RadioGroup rgFarmingType;
    Button saveBtn;
    ImageView ivAvatar;
    TextView tvPickPhoto;

    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseStorage storage;

    Uri selectedImageUri = null;

    // Indian states list
    private static final String[] STATES = {
            "-- Select State --",
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar",
            "Chhattisgarh", "Goa", "Gujarat", "Haryana", "Himachal Pradesh",
            "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra",
            "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha",
            "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana",
            "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
            "Delhi", "Jammu & Kashmir", "Ladakh", "Puducherry"
    };

    // Crop list
    private static final String[] CROPS = {
            "-- Select Primary Crop --",
            "Rice", "Wheat", "Maize", "Ragi (Finger Millet)", "Jowar (Sorghum)",
            "Bajra (Pearl Millet)", "Barley", "Soybean", "Groundnut",
            "Mustard", "Sunflower", "Cotton", "Sugarcane", "Jute",
            "Tomato", "Potato", "Onion", "Chilli", "Brinjal",
            "Banana", "Mango", "Coconut", "Turmeric", "Ginger",
            "Tea", "Coffee", "Rubber", "Other"
    };

    // Languages
    private static final String[] LANGUAGES = {
            "English", "हिन्दी (Hindi)", "தமிழ் (Tamil)", "తెలుగు (Telugu)",
            "ಕನ್ನಡ (Kannada)", "മലയാളം (Malayalam)", "বাংলা (Bengali)",
            "मराठी (Marathi)", "ਪੰਜਾਬੀ (Punjabi)", "ગુજરાતી (Gujarati)",
            "ଓଡ଼ିଆ (Odia)", "অসমীয়া (Assamese)"
    };

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivAvatar.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bind views
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        farmSize = findViewById(R.id.farmSize);
        spinnerState = findViewById(R.id.spinnerState);
        spinnerCrop = findViewById(R.id.spinnerCrop);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        rgFarmingType = findViewById(R.id.rgFarmingType);
        saveBtn = findViewById(R.id.saveBtn);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvPickPhoto = findViewById(R.id.tvPickPhoto);

        // Pre-fill email from Firebase Auth (Google sign-in)
        if (auth.getCurrentUser() != null) {
            String authEmail = auth.getCurrentUser().getEmail();
            if (authEmail != null) email.setText(authEmail);

            String displayName = auth.getCurrentUser().getDisplayName();
            if (displayName != null && displayName.contains(" ")) {
                String[] parts = displayName.split(" ", 2);
                firstName.setText(parts[0]);
                lastName.setText(parts[1]);
            } else if (displayName != null) {
                firstName.setText(displayName);
            }
        }

        // Setup spinners
        setupSpinner(spinnerState, STATES);
        setupSpinner(spinnerCrop, CROPS);
        setupSpinner(spinnerLanguage, LANGUAGES);

        // Avatar picker
        ivAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        tvPickPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Save
        saveBtn.setOnClickListener(v -> validateAndSave());
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void validateAndSave() {
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPhone = phone.getText().toString().trim();
        String size = farmSize.getText().toString().trim();

        if (fName.isEmpty()) {
            firstName.setError("First name is required");
            firstName.requestFocus();
            return;
        }
        if (userEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Valid email is required");
            email.requestFocus();
            return;
        }
        if (userPhone.isEmpty()) {
            phone.setError("Phone number is required");
            phone.requestFocus();
            return;
        }
        if (spinnerState.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select your state", Toast.LENGTH_SHORT).show();
            return;
        }

        saveBtn.setEnabled(false);
        saveBtn.setText("Saving...");

        // Determine farming type
        int selectedFarmingId = rgFarmingType.getCheckedRadioButtonId();
        String farmingType = "Organic";
        if (selectedFarmingId == R.id.rbConventional) farmingType = "Conventional";
        else if (selectedFarmingId == R.id.rbMixed) farmingType = "Mixed";

        String selectedState = spinnerState.getSelectedItem().toString();
        String selectedCrop = spinnerCrop.getSelectedItemPosition() > 0
                ? spinnerCrop.getSelectedItem().toString() : "";
        String selectedLang = spinnerLanguage.getSelectedItem().toString();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", fName);
        userMap.put("lastName", lName);
        userMap.put("email", userEmail);
        userMap.put("phone", userPhone);
        userMap.put("state", selectedState);
        userMap.put("farmSizeAcres", size.isEmpty() ? "" : size);
        userMap.put("primaryCrop", selectedCrop);
        userMap.put("farmingType", farmingType);
        userMap.put("preferredLanguage", selectedLang);
        userMap.put("profileComplete", true);

        String userId = auth.getCurrentUser().getUid();

        if (selectedImageUri != null) {
            // Upload photo first, then save profile
            StorageReference ref = storage.getReference()
                    .child("profile_photos/" + userId + ".jpg");
            ref.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                userMap.put("photoUrl", downloadUri.toString());
                                saveToFirestore(userId, userMap);
                            }))
                    .addOnFailureListener(e -> {
                        // Save without photo if upload fails
                        saveToFirestore(userId, userMap);
                    });
        } else {
            saveToFirestore(userId, userMap);
        }
    }

    private void saveToFirestore(String userId, HashMap<String, Object> userMap) {
        db.collection("users")
                .document(userId)
                .set(userMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile saved! Welcome to AgroAid 🌿",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    saveBtn.setEnabled(true);
                    saveBtn.setText("Save & Continue →");
                    Toast.makeText(this, "Error saving profile. Try again.", Toast.LENGTH_SHORT).show();
                });
    }
}