package com.globe.hand.Login.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.globe.hand.R;
import com.globe.hand.common.BaseActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ssangwoo on 2017-12-21.
 */

public class HandJoinFragment extends Fragment
            implements View.OnClickListener {

    private final int PERMISSION_REQUEST_CODE = 100;
    private final int PICK_FROM_ALBUM = 90;

    private static final String USER_EMAIL = "user_email";
    private static final String USER_NICKNAME = "user_nickname";

    String imgPath;
    String absolutePath;

    static String  imgUpLoadPath = "";

    private OnCallbackJoinListener listener;

    private EditText editEmail;
    private EditText editPass;
    private EditText editNickname;
    private ToggleButton toggleGender;

    private String userEmail;
    private String userNickname;

    private CircleImageView ivProfile;

    public HandJoinFragment() { }

    public static HandJoinFragment newInstance(String userEmail, String userNickname) {
        HandJoinFragment fragment = new HandJoinFragment();

        Bundle args = new Bundle();
        args.putString(USER_EMAIL, userEmail);
        args.putString(USER_NICKNAME, userNickname);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_hand_join, container, false);
        ivProfile = view.findViewById(R.id.join_profile_image);
        editEmail = view.findViewById(R.id.join_edit_email);
        editPass = view.findViewById(R.id.join_edit_pass);
        editNickname = view.findViewById(R.id.join_edit_nickname);
        toggleGender = view.findViewById(R.id.join_toggle_gender);

        ivProfile.setOnClickListener(this);

        editEmail.setText(userEmail);
        editNickname.setText(userNickname);

        Button btnJoin = view.findViewById(R.id.join_btn_join);
        Button btnCancel = view.findViewById(R.id.join_btn_cancel);

        btnJoin.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            userEmail = getArguments().getString(USER_EMAIL);
            userNickname = getArguments().getString(USER_NICKNAME);
            checkPermission();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.join_profile_image:
                //TODO : 이미지가져와서 뿌려줘버리기
                doTakeAlbumAction();
                break;

            case R.id.join_btn_join:
                if(isVailedForm()) {
                    String selectedGender = toggleGender.isSelected() ?
                            toggleGender.getTextOn().toString():
                            toggleGender.getTextOff().toString();
                    listener.processJoin(
                            editEmail.getText().toString(), editPass.getText().toString(),
                            editNickname.getText().toString(),
                            selectedGender, imgUpLoadPath);
                }
                break;
            case R.id.join_btn_cancel:
                listener.backToLogin();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnCallbackJoinListener) {
            listener = (OnCallbackJoinListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public boolean isVailedForm() {
        // 임시
        String email = editEmail.getText().toString();
        String pass = editPass.getText().toString();

        if(email.trim().isEmpty()) {
            Toast.makeText(getContext(), "이메일을 적어주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(pass.trim().isEmpty()) {
            Toast.makeText(getContext(), "비밀번호를 적어주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

//        if(gender.isEmpty() || gender.equals("선택")) {
//            Toast.makeText(getContext(), "성별을 선택해주세요", Toast.LENGTH_SHORT).show();
//            return false;
//        }

        return true;
    }

    public interface OnCallbackJoinListener {
        void processJoin(String userEmail, String userPassword, String userNickname, String gender, String profile_path);
        void backToLogin();
    }

    private void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    public String getImageNameToUri(Uri data) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);

        return imgName;
    }

    public static void saveBitmaptoJpeg(Bitmap bitmap, String folder, String name) {
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/" + folder + "/";
        String file_name = name + ".jpg";
        String string_path = ex_storage + foler_name;
        imgUpLoadPath = string_path + file_name;


        File file_path;
        try {
            file_path = new File(string_path);
            if (!file_path.isDirectory()) {
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path + file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        } catch (FileNotFoundException exception) {
            Log.e("FileNotFoundException", exception.getMessage());
        } catch (IOException exception) {
            Log.e("IOException", exception.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_FROM_ALBUM:
                try {
                    //이미지 경로, 이름
                    if (data == null) break;
                    String imaName = getImageNameToUri(data.getData());
                    Uri selectImageUri = data.getData();

                    Cursor c = getContext().getContentResolver().query(Uri.parse(selectImageUri.toString()), null, null, null, null);
                    c.moveToNext();
                    absolutePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));

                    Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());

                    //이미지 리사이징
                    int height = image_bitmap.getHeight();
                    int width = image_bitmap.getWidth();

                    Log.e("체크", absolutePath);
                    Bitmap src = BitmapFactory.decodeFile(absolutePath);
                    Bitmap resized = Bitmap.createScaledBitmap(src, width / 2, height / 2, true);

                    saveBitmaptoJpeg(resized, "resizeTmp", imaName);

                    ivProfile.setImageBitmap(resized);


                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

            } else {
                // 사용자 언제나 허락시
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // 퍼미션 모두 허용일 시
                } else {

                }
                break;
        }
    }
}
