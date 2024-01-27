package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CogFragment extends Fragment {
    private InventoryViewModel viewModel;
    private LinearLayout layoutContainer;
    private Map<String, EditText> editTextMap;
    private Map<String, List<String>> categoryItems;
    private static final int READ_REQUEST_CODE = 42;
    private TextView txtUploadedFiles;
    private List<String> uploadedFileNames = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cog, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        layoutContainer = view.findViewById(R.id.layoutContainer);
        editTextMap = new HashMap<>();
        viewModel.populateDefaultCogValuesIfNeeded();

        Button btnUploadCsv = view.findViewById(R.id.btnUploadCsv);
        btnUploadCsv.setOnClickListener(v -> openFilePicker());

        txtUploadedFiles = view.findViewById(R.id.UploadedFiles); // Assume you have this TextView in your layout
        viewModel.getCogValuesLiveData().observe(getViewLifecycleOwner(), cogValues -> {
            txtUploadedFiles.setText(String.join(", ", viewModel.getUploadedCsvFiles()));
        });
        return view;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                String fileName = getFileName(fileUri);
                if (fileName != null && fileName.endsWith(".csv")) {
                    viewModel.processCsvFile(fileUri, getContext());
                    uploadedFileNames.add(fileName);
                    updateUploadedFilesTextView();
                    Toast.makeText(getContext(), "CSV file uploaded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Please select a valid CSV file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void updateUploadedFilesTextView() {
        StringBuilder sb = new StringBuilder("Uploaded Files:\n");
        for (String fileName : uploadedFileNames) {
            sb.append(fileName).append("\n");
        }
        txtUploadedFiles.setText(sb.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisplayedCsvFiles();;
    }

    private void updateDisplayedCsvFiles() {
        Set<String> processedFiles = viewModel.getProcessedCsvFileNames();
        // Assuming you have a TextView to display the file names
        TextView textView = getView().findViewById(R.id.UploadedFiles);
        String fileNames = String.join(", ", processedFiles);
        textView.setText("Processed files: " + fileNames);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save COG values to ViewModel
    }

}
