package ru.mobile.beerhoven.presentation.ui.admin.post;

import static android.provider.MediaStore.Images.Media.getBitmap;
import static java.util.Objects.requireNonNull;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import es.dmoral.toasty.Toasty;
import ru.mobile.beerhoven.R;
import ru.mobile.beerhoven.databinding.FragmentAddNewsBinding;
import ru.mobile.beerhoven.domain.model.News;
import ru.mobile.beerhoven.presentation.activity.MainActivity;
import ru.mobile.beerhoven.utils.Constants;
import soup.neumorphism.NeumorphButton;

public class AddNewsFragment extends Fragment {
   private Activity mActivity;
   private AddNewsViewModel mViewModel;
   private AlertDialog mAlertDialog;
   private EditText mTitle, mDescription;
   private ImageView mPostImage;
   private NeumorphButton mAddPostButton;
   private News mDataModel;
   private Uri mUriImage;
   private String[] cameraPermissions;
   private String[] storagePermissions;
   private String title, description, time;

   private static final String TAG = "AddNewsFragment";

   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      FragmentAddNewsBinding mFragmentBind = FragmentAddNewsBinding.inflate(inflater, container, false);
      mTitle = mFragmentBind.newsTitle;
      mDescription = mFragmentBind.newsDesc;
      mPostImage = mFragmentBind.newsImage;
      mAddPostButton = mFragmentBind.btnNewsAdd;
      cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
      storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
      return mFragmentBind.getRoot();
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      mViewModel = new AddNewsViewModel(getActivity());
      Date currentDate = new Date();

      mPostImage.setOnClickListener(v -> showImagePickDialog());

      mAddPostButton.setOnClickListener(v -> {
         // Add model state
         description = mDescription.getText().toString().trim();
         title = mTitle.getText().toString().trim();
         time = String.valueOf(currentDate);
         mDataModel = new News(description, time, title, mUriImage != null ? mUriImage.toString() : null);

         ((MainActivity) requireActivity()).onIncreaseNewsCounter();

         // Add news
         mViewModel.onAddPostResponse(mDataModel).observe(getViewLifecycleOwner(), res -> {
            if (res) {
               Toasty.success(mActivity, R.string.news_add_success, Toast.LENGTH_SHORT, true).show();
            } else {
               Toasty.error(mActivity, R.string.news_add_failed, Toast.LENGTH_SHORT, true).show();
            }
         });
      });
   }

   private void showImagePickDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
      View view = LayoutInflater.from(mActivity).inflate(R.layout.custom_alert_dialog, null);
      builder.setView(view);
      mAlertDialog = builder.create();

      view.findViewById(R.id.btn_add_camera).setOnClickListener(v -> {
         if (!checkCameraPermission()) {
            requestCameraPermission();
            pickFromCamera();
         } else {
            pickFromCamera();
         }
      });

      view.findViewById(R.id.btn_add_gallery).setOnClickListener(v -> {
         if (!checkStoragePermission()) {
            requestStoragePermission();
            pickFromGallery();
         } else {
            pickFromGallery();
         }
      });

      view.findViewById(R.id.btn_cancel_container).setOnClickListener(v -> mAlertDialog.cancel());

      if (mAlertDialog.getWindow() != null) {
         mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
      }

      mAlertDialog.show();
   }

   private void pickFromCamera() {
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      File photo = new File(requireNonNull(mActivity).getExternalFilesDir(null), "test.jpg");
      mUriImage = FileProvider.getUriForFile(mActivity, mActivity.getPackageName() + ".provider", photo);
      intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriImage);
      startActivityForResult(intent, Constants.IMAGE_PICK_CAMERA_CODE);
      mAlertDialog.dismiss();
   }

   private void pickFromGallery() {
      Intent intent = new Intent(Intent.ACTION_PICK);
      intent.setType("image/*");
      startActivityForResult(intent, Constants.IMAGE_PICK_GALLERY_CODE);
      mAlertDialog.dismiss();
   }

   private boolean checkStoragePermission() {
      return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
   }

   private void requestStoragePermission() {
      ActivityCompat.requestPermissions(mActivity, storagePermissions, Constants.STORAGE_REQUEST_CODE);
   }

   private boolean checkCameraPermission() {
      return ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED) &&
          ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
   }

   private void requestCameraPermission() {
      ActivityCompat.requestPermissions(mActivity, cameraPermissions, Constants.CAMERA_REQUEST_CODE);
   }

   @Override
   public void onStart() {
      super.onStart();
      mActivity = getActivity();
      if (isAdded() && mActivity != null) {
         Log.i(TAG, "activity created");
      }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      switch (requestCode) {
         case Constants.IMAGE_PICK_GALLERY_CODE:
            if (data != null) {
               mUriImage = data.getData();
               try {
                  Bitmap bitmap = getBitmap(requireNonNull(getActivity()).getContentResolver(), mUriImage);
                  mPostImage.setImageBitmap(bitmap);
                  Toasty.success(requireActivity(), R.string.add_image_inside, Toast.LENGTH_SHORT, true).show();
               } catch (IOException e) {
                  e.printStackTrace();
               }
               break;
            }
         case Constants.IMAGE_PICK_CAMERA_CODE:
            Bitmap bitmap = BitmapFactory.decodeFile(requireNonNull(getActivity()).getExternalFilesDir(null) + "/test.jpg");
            mPostImage.setImageBitmap(bitmap);
            Toasty.success(requireActivity(), R.string.add_image_inside, Toast.LENGTH_SHORT, true).show();
            break;
      }
   }
}
