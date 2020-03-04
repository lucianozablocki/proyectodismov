package com.proyfinaldismov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.icu.text.UnicodeSetSpanner;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MainActivity extends AppCompatActivity{
    Button button_signup;
    TextView login;
    public EditText email,password;
    FirebaseAuth mAuth;
    public Properties prop;
    public static Integer max_dislikes = 5;
    FirebaseDatabase database;
    DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        try{
//            InputStream input = new FileInputStream("/home/lucsi/AndroidStudioProjects/ProyFinalDISMOV/config.properties");
//            prop = new Properties();
//            prop.load(input);
//            max_dislikes = Integer.valueOf(prop.getProperty("max_dislikes"));
//        }
//        catch (FileNotFoundException f){
//            Log.d("LUCSI",f.toString());
//        }
//        catch (IOException i){
//            Log.d("LUCSI",i.toString());
//        }
        try{
            createNotificationChannel();
        }catch (Exception e){
            Log.d("LUCSI",e.toString());
        }

        mAuth = FirebaseAuth.getInstance();
        button_signup = findViewById(R.id.button_signup);
        login = findViewById(R.id.login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.pass);

        button_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_email = email.getText().toString();
                String str_pass = password.getText().toString();

                if(str_email.isEmpty()){
                    email.setError("Introducí un email!");
                    email.requestFocus();
                }
                else if(str_pass.isEmpty()){
                    password.setError("Introducí una contraseña!");
                    password.requestFocus();
                }
                else if (str_email.isEmpty() && str_pass.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Tenés que proporcionar email y contraseña", Toast.LENGTH_SHORT).show();
                }
                else if(!(str_email.isEmpty() && str_pass.isEmpty())){
                    mAuth.createUserWithEmailAndPassword(str_email,str_pass).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!(task.isSuccessful())){
                                Toast.makeText(getApplicationContext(),"Error al registrar: " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                            else{
                                try{
//                                Toast.makeText(getApplicationContext(),"Registro exitoso",Toast.LENGTH_LONG).show();
                                    //HERE we can save email of user in "usuarios"
                                    database = FirebaseDatabase.getInstance();
                                    userRef = database.getReference("usuariostest");
                                    userRef.child(mAuth.getUid()).child("email").setValue(mAuth.getCurrentUser().getEmail());
                                }catch (Exception e){
                                    Log.d("LUCSI",e.toString());
                                }
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(),"General error",Toast.LENGTH_SHORT).show();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(i);

            }
        });


    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "test";
            String description = "test";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("0", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
