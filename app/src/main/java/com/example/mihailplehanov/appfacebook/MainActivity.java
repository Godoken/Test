package com.example.mihailplehanov.appfacebook;



import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;

    CallbackManager callbackManager;

    LoginButton loginButton;

    TextView textView;

    int user_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                GenericTypeIndicator<Integer> genericTypeIndicator = new GenericTypeIndicator<Integer>(){};
                user_id = dataSnapshot.child("Пользователи").child("Количество пользователей").getValue(genericTypeIndicator);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        callbackManager = CallbackManager.Factory.create();


        textView = (TextView) findViewById(R.id.textView);


        loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.setReadPermissions("email", "public_profile");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                final GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback(){
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response){

                        try {
                            JSONObject jsonObject = response.getJSONObject();
                            String name = jsonObject.getString("name");
                            textView.setText(name);

                            myRef.child("Пользователи").child("Список пользователей").child("Пользователь №" + String.valueOf(user_id + 1)).child("Имя").setValue(name);

                            myRef.child("Пользователи").child("Количество пользователей").setValue(user_id + 1);

                            Toast toast = Toast.makeText(getApplicationContext(), "Пользователь зарегистрирован", Toast.LENGTH_SHORT);
                            toast.show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        /////

                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name");

                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        callbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }
}
