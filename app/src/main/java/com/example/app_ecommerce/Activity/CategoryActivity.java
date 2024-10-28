package com.example.app_ecommerce.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecommerce.Adapter.CategoryAdapter;
import com.example.app_ecommerce.Adapter.ProductAdapter;
import com.example.app_ecommerce.Model.ProductModel;
import com.example.app_ecommerce.R;
import com.example.app_ecommerce.Retrofit.ApiEcommerce;
import com.example.app_ecommerce.Retrofit.RetrofitClient;
import com.example.app_ecommerce.utils.Utils;

import java.security.PrivateKey;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CategoryActivity extends AppCompatActivity {
    private RecyclerView rvAllProducts;
    private ApiEcommerce apiEcommerce;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<ProductModel> productList;
    private CategoryAdapter categoryAdapter;
    private ImageView btnBack;
    private TextView tvNotificationCountShopping, txtProductByCategoryEmpty;
    private ImageView ivShopping;
    private EditText etSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiEcommerce = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiEcommerce.class);
        Anhxa();
        getData();
        ActionBack();
        updateCartCount();
        initControl();
        getSearch();
    }

    private void getSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getDataSearch();
            }
        });
    }

    private void getDataSearch(){
        String str_search = etSearch.getText().toString().trim();
        String category = getIntent().getStringExtra("category");
        // Nếu danh mục là "All" hoặc "searchAll" thì tìm kiếm toàn bộ sản phẩm
        if (category.equals("All") || category.equals("searchAll")) {
            compositeDisposable.add(apiEcommerce.search(str_search)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            getProductModel -> {
                                if (getProductModel.isSuccess() && !getProductModel.getResult().isEmpty()) {
                                    productList = getProductModel.getResult();
                                    categoryAdapter = new CategoryAdapter(this, productList);
                                    rvAllProducts.setAdapter(categoryAdapter);
                                    hindAndShowRV(true);
                                } else {
                                    hindAndShowRV(false);
                                }
                            },
                            throwable -> {
                                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    ));
        } else {
            // Nếu danh mục cụ thể, tìm kiếm trong danh mục đó
            int categoryId = getCategoryId(category);
            compositeDisposable.add(apiEcommerce.searchByCategory(categoryId, str_search)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            getProductModel -> {
                                if (getProductModel.isSuccess() && !getProductModel.getResult().isEmpty()) {
                                    productList = getProductModel.getResult();
                                    categoryAdapter = new CategoryAdapter(this, productList);
                                    rvAllProducts.setAdapter(categoryAdapter);
                                    hindAndShowRV(true);
                                } else {
                                    hindAndShowRV(false);
//                                    Toast.makeText(this, "Không có sản phẩm nào trong danh mục " + category, Toast.LENGTH_LONG).show();
                                }
                            },
                            throwable -> {
                                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    ));
        }
    }

    private void hindAndShowRV(boolean b) {
        if (b) {
            rvAllProducts.setVisibility(View.VISIBLE);
            txtProductByCategoryEmpty.setVisibility(View.GONE);
        } else {
            rvAllProducts.setVisibility(View.GONE);
            txtProductByCategoryEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void initControl() {
        ivShopping.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CartActivity.class);
            startActivity(intent);
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


    private void ActionBack() {
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getData() {
        String category = getIntent().getStringExtra("category");

        if (category.equals("All") || category.equals("searchAll")){
            // Lấy tất cả sản phẩm
            compositeDisposable.add(apiEcommerce.getProduct()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            getProductModel -> {
                                if(getProductModel.isSuccess()) {
                                    // Cập nhật danh sách sản phẩm ở đây
                                    productList = getProductModel.getResult();
                                    categoryAdapter = new CategoryAdapter(this,productList);
                                    rvAllProducts.setAdapter(categoryAdapter);
                                    // Cập nhật adapter ở đây
                                    hindAndShowRV(true);
                                }else {
                                    hindAndShowRV(false);
                                }
                            },
                            throwable -> {
                                Toast.makeText(getApplicationContext(), "Không thể kết nối được với server" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    ));
        } else {
            int loai = getCategoryId(category);
            compositeDisposable.add(apiEcommerce.getProductByCategory(loai)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            getProductModel -> {
                                if(getProductModel.isSuccess()) {
                                    productList = getProductModel.getResult();
                                    categoryAdapter = new CategoryAdapter(this,productList);
                                    rvAllProducts.setAdapter(categoryAdapter);
                                    hindAndShowRV(true);
                                }else{
                                    hindAndShowRV(true);
                                }
                            },
                            throwable -> {
                                Toast.makeText(getApplicationContext(), "Không thể kết nối được với server" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    ));
        }
    }

    private int getCategoryId(String category) {
        switch (category) {
            case "PC":
                return 1;
            case "Phone":
                return 2;
            case "HeadPhone":
                return 3;
            case "Gaming":
                return 4;
            default:
                return 0; // Trả về 0 nếu lấy tất cả sản phẩm
        }
    }

    private void Anhxa() {
        rvAllProducts = findViewById(R.id.rvAllProducts);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvAllProducts.setLayoutManager(layoutManager);
        rvAllProducts.setHasFixedSize(true);
        ivShopping = findViewById(R.id.ivShopping);
        tvNotificationCountShopping = findViewById(R.id.tvNotificationCountShopping);
        etSearch = findViewById(R.id.etSearch);
        txtProductByCategoryEmpty = findViewById(R.id.txtHistoryEmpty);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}