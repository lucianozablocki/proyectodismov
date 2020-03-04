package com.proyfinaldismov;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    EditText email,password;
    Button button_login;
    TextView signup;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton signin_google;
    LoginButton signin_facebook;
    CallbackManager callbackManager;
    FirebaseDatabase database;
    DatabaseReference userRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.pass);
        button_login = findViewById(R.id.button_login);
        signup = findViewById(R.id.signup);
        mAuth = FirebaseAuth.getInstance();
        signin_google = findViewById(R.id.login_with_google);
        signin_facebook = findViewById(R.id.login_with_facebook);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signin_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        callbackManager = CallbackManager.Factory.create();
        signin_facebook.setReadPermissions("email");
        signin_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
//                        Log.d("LUCSI","INTO ONCLICK METHOD");
                        handleFacebookToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("LUCSI",error.toString());
                    }
                });

            }
        });


        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_email = email.getText().toString();
                String str_pass = password.getText().toString();

                if (str_email.isEmpty()) {
                    email.setError("Introducí un email!");
                    email.requestFocus();
                } else if (str_pass.isEmpty()) {
                    password.setError("Introducí una contraseña!");
                    password.requestFocus();
                } else if (str_email.isEmpty() && str_pass.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Tenés que proporcionar email y contraseña", Toast.LENGTH_SHORT).show();
                } else if (!(str_email.isEmpty() && str_pass.isEmpty())) {
                    mAuth.signInWithEmailAndPassword(str_email, str_pass).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!(task.isSuccessful())) {
                                Toast.makeText(getApplicationContext(), "Error al ingresar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
//                                Toast.makeText(getApplicationContext(), "Login exitoso", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(LoginActivity.this, MainPage.class); //create mainpage activity and declare it in manifest file
                                startActivity(i);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "General error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Toast.makeText(getApplicationContext(),"Login exitoso",Toast.LENGTH_LONG).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            //here we can extract email from user
                            database = FirebaseDatabase.getInstance();
                            userRef = database.getReference("usuarios");
                            userRef.child(mAuth.getUid()).child("email").setValue(user.getEmail());
                            Intent i = new Intent(getApplicationContext(),MainPage.class);
                            startActivity(i);

                        } else {
                            Toast.makeText(getApplicationContext(),"Error al ingresar: " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            // If sign in fails, display a message to the user.
                        }
                    }
                });

    }

    private void handleFacebookToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
//                    Toast.makeText(getApplicationContext(), "Login exitoso", Toast.LENGTH_LONG).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    //HERE we can extract email
                    database = FirebaseDatabase.getInstance();
                    userRef = database.getReference("usuarios");
                    userRef.child(mAuth.getUid()).child("email").setValue(user.getEmail());
                    Intent i = new Intent(getApplicationContext(), MainPage.class);
                    startActivity(i);

                } else {
                    Toast.makeText(getApplicationContext(), "Error al ingresar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    // If sign in fails, display a message to the user.
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
            }
        }
    };

}

