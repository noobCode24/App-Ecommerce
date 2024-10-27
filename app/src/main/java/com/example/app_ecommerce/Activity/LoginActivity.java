package com.example.app_ecommerce.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.app_ecommerce.Model.UserModel;
import com.example.app_ecommerce.R;
import com.example.app_ecommerce.Retrofit.ApiEcommerce;
import com.example.app_ecommerce.Retrofit.RetrofitClient;
import com.example.app_ecommerce.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.paperdb.Paper;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    private TextView txt_openRegister, txt_forgotpass;
    private EditText txt_email, txt_pass;
    private Button btn_Login;
    private ApiEcommerce apiEcommerce;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initView();
        initControl();
    }

    private void initControl() {
        txt_openRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        txt_forgotpass.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPassActivity.class);
            startActivity(intent);
        });

        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_email = txt_email.getText().toString().trim();
                String str_pass = txt_pass.getText().toString().trim();

                if (TextUtils.isEmpty(str_email)) {
                    Toast.makeText(getApplicationContext(), "Bạn chưa nhập Email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(str_pass)) {
                    Toast.makeText(getApplicationContext(), "Bạn chưa nhập Pass", Toast.LENGTH_SHORT).show();
                } else {
                    //save
                    Paper.book().write("email", str_email);
                    Paper.book().write("pass", str_pass);
                    if (user != null) {
                        // user da co dang nhap fire base chưa sigout
                        Login(str_email, str_pass);
                    } else {
                        //user da dang xuat ra
                        // co 2 noi quan ly mk la firebase, 2 la hosting
                        firebaseAuth.signInWithEmailAndPassword(str_email, str_pass)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            Login(str_email, str_pass);
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    private void initView() {
        Paper.init(this);
        apiEcommerce = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiEcommerce.class);
        txt_openRegister = findViewById(R.id.txt_openRegister);
        txt_email = findViewById(R.id.txt_email);
        txt_pass = findViewById(R.id.txt_pass);
        btn_Login = findViewById(R.id.btn_Login);
        txt_forgotpass = findViewById(R.id.txt_forgotpass);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //read data
        if(Paper.book().read("email") != null && Paper.book().read("pass") != null) {
            txt_email.setText(Paper.book().read("email"));
            txt_pass.setText(Paper.book().read("pass"));
            if (Paper.book().read("isLogin") != null) {
                boolean flag = Paper.book().read("isLogin");
                if (flag) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            Login(Paper.book().read("email"), Paper.book().read("pass"));
                        }
                    }, 1000);
                }
            }
        }
    }

    private void Login(String email, String pass) {
        compositeDisposable.add(apiEcommerce.login(email, pass)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userModel -> {
                            if (userModel.isSuccess()){
                                isLogin = true;
                                Paper.book().write("isLogin", isLogin);
                                Utils.user_current = userModel.getResult().get(0);
//                                luu lai thong tin nguoi dung
                                Paper.book().write("user", userModel.getResult().get(0));
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Login failed: " + userModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                )
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.user_current != null) {
            if (Utils.user_current.getEmail() != null && Utils.user_current.getPass() != null) {
                txt_email.setText(Utils.user_current.getEmail());
                txt_pass.setText(Utils.user_current.getPass());
            }
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
