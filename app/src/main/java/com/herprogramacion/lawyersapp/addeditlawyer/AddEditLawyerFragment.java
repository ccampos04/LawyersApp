package com.herprogramacion.lawyersapp.addeditlawyer;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.icu.text.SymbolTable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.herprogramacion.lawyersapp.R;
import com.herprogramacion.lawyersapp.data.Lawyer;
import com.herprogramacion.lawyersapp.data.LawyersDbHelper;

import java.io.IOException;

/**
 * Vista para creación/edición de un abogado
 */
public class AddEditLawyerFragment extends Fragment {
    private static final String ARG_LAWYER_ID = "arg_lawyer_id";

    private String mLawyerId;
    private LawyersDbHelper mLawyersDbHelper;
    private FloatingActionButton mSaveButton;
    private TextInputEditText mNameField;
    private TextInputEditText mPhoneNumberField;
    private TextInputEditText mSpecialtyField;
    private TextInputEditText mBioField;
    private TextInputLayout mNameLabel;
    private TextInputLayout mPhoneNumberLabel;
    private TextInputLayout mSpecialtyLabel;
    private TextInputLayout mBioLabel;
    private ImageButton mImageButton;
    static final int REQUEST_IMAGE_GALLERY = 2;
    private ImageView mImageView;
    private String mAvatarUri = "";
    private TextView labelImagen;
    private Uri avatarUri;

    public AddEditLawyerFragment() {
        // Required empty public constructor
    }

    public static AddEditLawyerFragment newInstance(String lawyerId) {
        AddEditLawyerFragment fragment = new AddEditLawyerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LAWYER_ID, lawyerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLawyerId = getArguments().getString(ARG_LAWYER_ID);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == this.getActivity().RESULT_CANCELED) {
            return;
        }

        if (data != null) {
            avatarUri = data.getData();
            mAvatarUri = avatarUri.toString();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), avatarUri);
                mImageView.setImageBitmap(bitmap);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_edit_lawyer, container, false);

        // Referencias UI
        mSaveButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        mNameField = (TextInputEditText) root.findViewById(R.id.et_name);
        mPhoneNumberField = (TextInputEditText) root.findViewById(R.id.et_phone_number);
        mSpecialtyField = (TextInputEditText) root.findViewById(R.id.et_specialty);
        mBioField = (TextInputEditText) root.findViewById(R.id.et_bio);
        mNameLabel = (TextInputLayout) root.findViewById(R.id.til_name);
        mPhoneNumberLabel = (TextInputLayout) root.findViewById(R.id.til_phone_number);
        mSpecialtyLabel = (TextInputLayout) root.findViewById(R.id.til_specialty);
        mBioLabel = (TextInputLayout) root.findViewById(R.id.til_bio);
        mImageButton = (ImageButton) root.findViewById(R.id.imageButton);
        mImageView = (ImageView) root.findViewById(R.id.imageView);

        // Eventos
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEditLawyer();
            }
        });

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGaleria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intentGaleria.setType("image/");
                startActivityForResult(intentGaleria, 0);
            }
        });


        mLawyersDbHelper = new LawyersDbHelper(getActivity());

        // Carga de datos
        if (mLawyerId != null) {
            loadLawyer();
        }

        return root;
    }

    private void loadLawyer() {
        new GetLawyerByIdTask().execute();
    }

    private void addEditLawyer() {
        boolean error = false;

        String name = mNameField.getText().toString();
        String phoneNumber = mPhoneNumberField.getText().toString();
        String specialty = mSpecialtyField.getText().toString();
        String bio = mBioField.getText().toString();
        String avatarUri = mAvatarUri;

        if (TextUtils.isEmpty(name)) {
            mNameLabel.setError(getString(R.string.field_error));
            error = true;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberLabel.setError(getString(R.string.field_error));
            error = true;
        }

        if (TextUtils.isEmpty(specialty)) {
            mSpecialtyLabel.setError(getString(R.string.field_error));
            error = true;
        }


        if (TextUtils.isEmpty(bio)) {
            mBioLabel.setError(getString(R.string.field_error));
            error = true;
        }

        if (avatarUri == ""){
            Log.i("AddEditLawyerFragment", avatarUri+"este es el avatar uri!");
            System.out.print(avatarUri);
            labelImagen.setText("Ingresa una imagen");
            error = true;
        }

        if (error) {
            return;
        }

        Lawyer lawyer = new Lawyer(name, specialty, phoneNumber, bio, mAvatarUri);

        new AddEditLawyerTask().execute(lawyer);

    }

    private void showLawyersScreen(Boolean requery) {
        if (!requery) {
            showAddEditError();
            getActivity().setResult(Activity.RESULT_CANCELED);
        } else {
            getActivity().setResult(Activity.RESULT_OK);
        }

        getActivity().finish();
    }

    private void showAddEditError() {
        Toast.makeText(getActivity(),
                "Error al agregar nueva información", Toast.LENGTH_SHORT).show();
    }

    private void showLawyer(Lawyer lawyer) {
        mNameField.setText(lawyer.getName());
        mPhoneNumberField.setText(lawyer.getPhoneNumber());
        mSpecialtyField.setText(lawyer.getSpecialty());
        mBioField.setText(lawyer.getBio());
    }

    private void showLoadError() {
        Toast.makeText(getActivity(),
                "Error al editar abogado", Toast.LENGTH_SHORT).show();
    }

    private class GetLawyerByIdTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... voids) {
            return mLawyersDbHelper.getLawyerById(mLawyerId);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null && cursor.moveToLast()) {
                showLawyer(new Lawyer(cursor));
            } else {
                showLoadError();
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        }

    }

    private class AddEditLawyerTask extends AsyncTask<Lawyer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Lawyer... lawyers) {
            if (mLawyerId != null) {
                return mLawyersDbHelper.updateLawyer(lawyers[0], mLawyerId) > 0;

            } else {
                return mLawyersDbHelper.saveLawyer(lawyers[0]) > 0;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {
            showLawyersScreen(result);
        }

    }

}
