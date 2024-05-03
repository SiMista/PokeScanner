package com.example.pokescanner.ui.scanner;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.pokescanner.R;
import com.example.pokescanner.database.ImagesDataSource;
import com.example.pokescanner.databinding.FragmentScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScannerFragment extends Fragment {
    private FragmentScannerBinding binding;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ImageCapture imageCapture;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScannerBinding.inflate(inflater, container, false);
        Button captureButton = binding.buttonCapture;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }

        captureButton.setOnClickListener(v -> takePhoto());
        return binding.getRoot();
    }


    private static final int REQUEST_CAMERA_PERMISSION = 101;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    // La permission a été refusée
                    new AlertDialog.Builder(getContext())
                            .setTitle("Permission refusée")
                            .setMessage("La permission d'accéder à la caméra a été refusée. L'application ne peut pas fonctionner sans cette permission.")
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startCamera() {
        Log.e("Camera", "Caméra lancée");
        final PreviewView viewFinder = binding.viewFinder;
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll(); // Unbind use cases before rebinding
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Log.e("CameraX", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }


    private void takePhoto() {
        Log.d("Photo", "Photo has been taken");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String dateTime = sdf.format(new Date());
        File photoFile = new File(getContext().getExternalFilesDir(null), "PIC_" + dateTime + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(getContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Photo capture succeeded
                        String savedUri = outputFileResults.getSavedUri() != null ? outputFileResults.getSavedUri().toString() : photoFile.getAbsolutePath();
                        Log.d("CameraXApp", "Photo capture succeeded: " + savedUri);

                        // Save image path in SQLite database
                        saveImagePath(savedUri);
                        triggerFlashAnimation(); // Déclencher l'effet de flash
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraXApp", "Photo capture failed: " + exception.getMessage());
                    }
                });
    }

    private void saveImagePath(String imagePath) {
        ImagesDataSource dataSource = new ImagesDataSource(getContext());
        dataSource.open();
        dataSource.insertImage(imagePath);
        dataSource.close();
    }

    private void triggerFlashAnimation() {
        View flashView = getView().findViewById(R.id.flashView);
        flashView.setVisibility(View.VISIBLE);
        flashView.animate()
                .alpha(0f)
                .setDuration(300) // Durée en millisecondes
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        flashView.setVisibility(View.GONE);
                        flashView.setAlpha(1f); // Reset l'opacité pour la prochaine utilisation
                    }
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up references to binding
    }
}
