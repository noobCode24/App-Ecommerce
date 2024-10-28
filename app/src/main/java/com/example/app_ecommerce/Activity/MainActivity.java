package com.example.app_ecommerce.Activity;

import static com.example.app_ecommerce.utils.Utils.user_current;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecommerce.Adapter.ProductAdapter;
import com.example.app_ecommerce.Model.ProductModel;
import com.example.app_ecommerce.Model.User;
import com.example.app_ecommerce.Model.UserModel;
import com.example.app_ecommerce.R;
import com.example.app_ecommerce.Retrofit.ApiEcommerce;
import com.example.app_ecommerce.Retrofit.RetrofitClient;
import com.example.app_ecommerce.utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private ProductAdapter productAdapter;
    private List<ProductModel> productList;
    private RecyclerView rvPopularProducts;
    private CompositeDisposable compositeDisposable;
    private ApiEcommerce apiEcommerce;
    private ConstraintLayout categoryPC, categoryPhone, categoryHeadPhone, categoryGaming;
    private TextView seeAll, txt_username;
    private LinearLayout layoutCart, layoutWishlist, layoutProfile;
    private TextView tvNotificationCountShopping;
    private ImageView ivShopping, img_searchMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // khoi tao va cau hinh retrofit dung de ket noi toi server thong qua api
        apiEcommerce = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiEcommerce.class);
        // xu ly luu tru nguoi dung voi paper, tu dong dang nhap neu du lieu nguoi dung da duoc luu tru
        Paper.init(this);
        if(Paper.book().read("user") != null){
            User user = Paper.book().read("user");
            Utils.user_current = user;
        }
        getToken();
        Anhxa();
        if (isConnected(this)){
            getProducts();
            getEventClick();
            initControl();
        } else {
            Toast.makeText(getApplicationContext(), "Không có Internet, vui lòng kết nối!", Toast.LENGTH_LONG).show();
        }
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        if (!TextUtils.isEmpty(s)) {
                            compositeDisposable.add(apiEcommerce.updateToken(String.valueOf(Utils.user_current.getUser_id()), s)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            messageModel -> {

                                            },
                                            throwable -> {
                                                Log.d("log", throwable.getMessage());
                                            }
                                    ));
                        }
                    }
                });
    }

    private void initControl() {
        txt_username.setText(user_current.getUser_name());
        layoutCart.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CartActivity.class);
            startActivity(intent);
        });

        layoutWishlist.setOnClickListener(v ->{
            Intent intent = new Intent(getApplicationContext(), PurchaseHistoryActivity.class);
            startActivity(intent);
        });

        ivShopping.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        img_searchMain.setOnClickListener(v -> {
            openCategoryActivity("searchAll");
        });

        layoutProfile.setOnClickListener(v -> {
//            xoa key user
            Paper.book().delete("user");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            FirebaseAuth.getInstance().signOut();
            finish();
        });
    }

    private void updateCartCount() {
        int productCount = Utils.ShoppingCartList.size(); // Đếm số sản phẩm khác nhau trong giỏ hàng
        tvNotificationCountShopping.setText(String.valueOf(productCount));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartCount(); // Cập nhật số lượng giỏ hàng
    }

    // lay du lieu tu server
    private void getProducts() {
        // gui yeu cau toi server
        compositeDisposable.add(apiEcommerce.getProduct() //duoc su dung de quan ly va huy cac observable trong qua trinh yeu cau du lieu voi Rxjava
                .subscribeOn(Schedulers.io()) // thuc hien thao tac lay du lieu tren luong io
                .observeOn(AndroidSchedulers.mainThread()) // xu ly tren luong chinh sau khi lay du lieu
                .subscribe(
                        getProductModel -> {
                            if(getProductModel.isSuccess()) {
                                productList = getProductModel.getResult(); // lay du lieu tu server
                                productAdapter = new ProductAdapter(getApplicationContext(),productList); // tao adapter
                                rvPopularProducts.setAdapter(productAdapter); // hien thi danh sach san pham
                            }
                        },
                        throwable -> {
                            Toast.makeText(getApplicationContext(), "Không thể kết nối được với server" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                ));
    }

    private void Anhxa() {
        rvPopularProducts = findViewById(R.id.rvAllProducts);
        // Sử dụng LinearLayoutManager cho trượt ngang
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvPopularProducts.setLayoutManager(layoutManager);
        rvPopularProducts.setHasFixedSize(true);

        //khoi tao list
        productList = new ArrayList<>();
        compositeDisposable = new CompositeDisposable();

        //khoi tao category
        categoryPC = findViewById(R.id.categoryPC);
        categoryPhone = findViewById(R.id.categoryPhone);
        categoryHeadPhone = findViewById(R.id.categoryHeadPhone);
        categoryGaming = findViewById(R.id.categoryGaming);
        seeAll = findViewById(R.id.tvSeeAll);

        //Khoi tao Linearlayout
        layoutCart = findViewById(R.id.layoutCart);
        layoutWishlist = findViewById(R.id.layoutWishlist);
        layoutProfile = findViewById(R.id.layoutProfile);
        tvNotificationCountShopping = findViewById(R.id.tvNotificationCountShopping);

        //Khoi tao ImageView
        ivShopping = findViewById(R.id.ivShopping);
        img_searchMain = findViewById(R.id.img_searchMain);

        txt_username = findViewById(R.id.txt_username);
        //Danh sách toàn cục
        if (Utils.ShoppingCartList == null){
            Utils.ShoppingCartList = new ArrayList<>();
        }
    }

    private void getEventClick() {
        categoryPC. setOnClickListener(v -> {
            openCategoryActivity("PC");
        });

        // Xử lý click cho category Phone
        categoryPhone.setOnClickListener(v -> {
            openCategoryActivity("Phone");
        });

        // Xử lý click cho category HeadPhone
        categoryHeadPhone.setOnClickListener(v -> {
            openCategoryActivity("HeadPhone");
        });

        // Xử lý click cho category Gaming
        categoryGaming.setOnClickListener(v -> {
            openCategoryActivity("Gaming");
        });

        // Xử lý click cho "See All"
        seeAll.setOnClickListener(v -> {
            openCategoryActivity("All");
        });
    }

    // Mở categoryActivity với tham số category
    private void openCategoryActivity(String category){
        Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }
    // kiem tra thiet bị co kết nối với internet hay không để lay du lieu tu db
    private boolean isConnected (Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); // them quyen
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi != null && wifi.isConnected() ||(mobile != null && mobile.isConnected())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}