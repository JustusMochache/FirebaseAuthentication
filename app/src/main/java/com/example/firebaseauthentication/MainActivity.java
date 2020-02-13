package com.example.firebaseauthentication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final int GOOGLE_SIGN = 123;

    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_logout)
    Button btnLogOut;
    @BindView(R.id.firebase_logo)
    ImageView imgLogo;
    @BindView(R.id.progress_bar_circular)
    ProgressBar progressBar;
    @BindView(R.id.title_app)
    TextView textViewTitle;

    GoogleSignInClient mGoogleSignIn;
    FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignIn = GoogleSignIn.getClient(this, signInOptions);

        btnLogin.setOnClickListener(v -> signInGoogle());
        btnLogOut.setOnClickListener(v -> LogOut());

        if(mAuth.getCurrentUser() !=null){
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }

    }

    void signInGoogle(){
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = mGoogleSignIn.getSignInIntent();
        startActivityForResult(intent, GOOGLE_SIGN );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== GOOGLE_SIGN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account!=null){
                    firebaseAuthWithGoogle(account);
                }

            } catch (ApiException e){
                e.printStackTrace();

            }        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("TAG", "FirebaseAuthWithGoogle: "+ account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        progressBar.setVisibility(View.GONE);
                        Log.d("TAG", "Sign In Successful");

                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);

                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Log.w("TAG", "Login Unsuccessful", task.getException());

                        Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user!=null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String photo = String.valueOf(user.getPhotoUrl());

            textViewTitle.append("\nInfo \n");
            textViewTitle.append(name + "\n");
            textViewTitle.append(email);

            Picasso.get().load(photo).into(imgLogo);
            btnLogin.setVisibility(View.INVISIBLE);
            btnLogOut.setVisibility(View.VISIBLE);
        } else{
            textViewTitle.setText(getString(R.string.firebase_login));
            Picasso.get().load(R.drawable.fb).into(imgLogo);
            btnLogin.setVisibility(View.VISIBLE);
            btnLogOut.setVisibility(View.INVISIBLE);
        }
    }

    void LogOut(){
        FirebaseAuth.getInstance().signOut();
        mGoogleSignIn.signOut().addOnCompleteListener(this, task -> updateUI(null));
    }
}
