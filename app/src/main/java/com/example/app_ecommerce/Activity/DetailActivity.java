package com.example.app_ecommerce.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.app_ecommerce.Model.ProductModel;
import com.example.app_ecommerce.Model.ShoppingCart;
import com.example.app_ecommerce.R;
import com.example.app_ecommerce.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

public class DetailActivity extends AppCompatActivity {
    private TextView txt_title, txt_price, txt_desc, txt_rate, txt_soldquantity, txt_priceDialog, txt_stockquantity, minusCartBtn, plusCartBtn, numberItemTxt, tvNotificationCountShopping;
    private ImageView img_pic, btnAddCart, btnBack, btn_exit, img_dialog, ivShoppingDetail;
    private Button btnBuyNow, btn_add_to_cart;

    ProductModel productModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initView();
        ActionBack();
        initData();
        initControl();
        openCartActivity();
    }

    private void openCartActivity() {
        ivShoppingDetail.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, CartActivity.class);
            startActivityForResult(intent, 22);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 22 && resultCode == RESULT_OK && data != null) {
            // Lấy số lượng sản phẩm trong giỏ hàng đã cập nhật
            int updatedCartCount = data.getIntExtra("updated_cart_count", 0);

            // Cập nhật lại số lượng sản phẩm hiển thị trong tvNotificationCountShopping
            tvNotificationCountShopping.setText(String.valueOf(updatedCartCount));
        }
    }


    private void initControl() {
        btnAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(DetailActivity.this);
                View view = LayoutInflater.from(DetailActivity.this).inflate(R.layout.bottom_sheet_add_to_cart, null);
                bottomSheetDialog.setContentView(view);

                txt_priceDialog = view.findViewById(R.id.txt_priceDialog);
                txt_stockquantity = view.findViewById(R.id.txt_stockquantity);
                minusCartBtn = view.findViewById(R.id.minusCartBtn);
                plusCartBtn = view.findViewById(R.id.plusCartBtn);
                numberItemTxt = view.findViewById(R.id.numberItemTxt);
                img_dialog = view.findViewById(R.id.img_dialog);
                btn_exit = view.findViewById(R.id.btn_exit);
                btn_add_to_cart = view.findViewById(R.id.btn_add_to_cart);

                txt_stockquantity.setText("kho: " + productModel.getStock_quantity());

                DecimalFormat decimalFormatWithDecimalDialog = new DecimalFormat("$#,##0.00");
                DecimalFormat decimalFormatWithoutDecimalDialog = new DecimalFormat("$#,##0");
                // Kiểm tra nếu giá là số nguyên hay không
                if (productModel.getPrice() % 1 == 0) {
                    txt_priceDialog.setText(decimalFormatWithoutDecimalDialog.format(productModel.getPrice()));
                } else {
                    txt_priceDialog.setText(decimalFormatWithDecimalDialog.format(productModel.getPrice()));
                }

                int imageResourceId = getResources().getIdentifier(productModel.getImage(), "drawable", getPackageName());
                if (imageResourceId != 0) {
                    img_dialog.setImageResource(imageResourceId);
                } else {
                    if(productModel.getImage().contains("http")){
                        Picasso.get().load(productModel.getImage()).into(img_dialog);
                    } else {
                        String img = Utils.BASE_URL+"images/" + productModel.getImage();
                        Picasso.get().load(img).into(img_dialog);
                    }
                }

                // Thêm sự kiện cho nút thoát
                btn_exit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                // Sự kiện thay đổi số lượng
                view.findViewById(R.id.minusCartBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentQuantity = Integer.parseInt(numberItemTxt.getText().toString());
                        if (currentQuantity > 1) {
                            numberItemTxt.setText(String.valueOf(currentQuantity - 1));
                        }
                    }
                });

                view.findViewById(R.id.plusCartBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentQuantity = Integer.parseInt(numberItemTxt.getText().toString());
                        numberItemTxt.setText(String.valueOf(currentQuantity + 1));
                    }
                });

                // Thêm sự kiện thêm vào giỏ hàng
                btn_add_to_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addToCart();
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.show();
            }
        });

    }

    private void addToCart() {
        int quantity = Integer.parseInt(numberItemTxt.getText().toString());
        boolean isProductInCart = false;

        // Kiểm tra sản phẩm đã tồn tại trong giỏ hàng chưa
        for (ShoppingCart cartItem : Utils.ShoppingCartList) {
            if (cartItem.getProduct_id() == productModel.getProduct_id()) {
                // Nếu sản phẩm đã tồn tại trong giỏ hàng, chỉ tăng số lượng sản phẩm đó
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                long newPrice = (long) (productModel.getPrice()) * cartItem.getQuantity();
                cartItem.setPrice(newPrice);
                isProductInCart = true;
                break;
            }
        }

        // Nếu sản phẩm chưa có trong giỏ hàng, thêm sản phẩm mới
        if (!isProductInCart) {
            long price = (long) (productModel.getPrice()) * quantity;
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setOriginalPrice(productModel.getPrice());
            shoppingCart.setPrice(price);
            shoppingCart.setQuantity(quantity);
            shoppingCart.setProduct_id(productModel.getProduct_id());
            shoppingCart.setProduct_name(productModel.getProduct_name());

            shoppingCart.setImage(productModel.getImage());
            Utils.ShoppingCartList.add(shoppingCart);

            // Chỉ khi có sản phẩm mới được thêm, số lượng sản phẩm khác nhau tăng lên
            int productCount = Utils.ShoppingCartList.size(); // Đếm số sản phẩm khác nhau
            tvNotificationCountShopping.setText(String.valueOf(productCount));
        }
    }

    private void initData() {
        productModel = (ProductModel) getIntent().getSerializableExtra("Detail");
        txt_title.setText(productModel.getProduct_name());
        txt_desc.setText(productModel.getProduct_desc());

        DecimalFormat decimalFormatWithDecimal = new DecimalFormat("$#,##0.00"); // Định dạng với 2 chữ số thập phân
        DecimalFormat decimalFormatWithoutDecimal = new DecimalFormat("$#,##0"); // Định dạng không có chữ số thập phân
        // Kiểm tra nếu giá là số nguyên hay không
        if (productModel.getPrice() % 1 == 0) {
            // Hiển thị giá không có phần thập phân
            txt_price.setText(decimalFormatWithoutDecimal.format(productModel.getPrice()));
        } else {
            txt_price.setText(decimalFormatWithDecimal.format(productModel.getPrice()));
        }

        // Định dạng và hiển thị số lượng bán
        txt_soldquantity.setText(formatSoldQuantity(productModel.getSold_quantity()));

        // Định dạng và hiển thị rate
        DecimalFormat decimalFormatRating = new DecimalFormat("#,##0.0");
        txt_rate.setText(decimalFormatRating.format(productModel.getRating()));

        int imageResourceId = getResources().getIdentifier(productModel.getImage(), "drawable", getPackageName());
        if (imageResourceId != 0) {
            // Nếu có ảnh trong drawable, hiển thị nó
            img_pic.setImageResource(imageResourceId);
        } else {
            if(productModel.getImage().contains("http")){
                Picasso.get().load(productModel.getImage()).into(img_pic);
            } else {
                String img = Utils.BASE_URL+"images/" + productModel.getImage();
                Picasso.get().load(img).into(img_pic);
            }
        }
    }

    private String formatSoldQuantity(int quantity) {
        if (quantity >= 1000000) {
            // Nếu số lượng lớn hơn 1 triệu, hiển thị với đơn vị "m" (ví dụ: 1,2m)
            return String.format("%.1fm", quantity / 1000000.0);
        } else if (quantity >= 1000) {
            // Nếu số lượng lớn hơn 1 ngàn, hiển thị với đơn vị "k" (ví dụ: 1,1k)
            return String.format("%.1fk", quantity / 1000.0);
        } else {
            // Nếu nhỏ hơn 1000, hiển thị như bình thường
            return String.valueOf(quantity);
        }
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

    private void initView() {
        txt_title = findViewById(R.id.txt_title);
        txt_price = findViewById(R.id.txt_price);
        txt_desc = findViewById(R.id.txt_desc);
        img_pic = findViewById(R.id.img_pic);
        btnAddCart = findViewById(R.id.btnAddCart);
        txt_rate = findViewById(R.id.txt_rate);
        txt_soldquantity = findViewById(R.id.txt_soldquantity);
        ivShoppingDetail = findViewById(R.id.ivShoppingDetail);
        tvNotificationCountShopping = findViewById(R.id.tvNotificationCountShopping);
        if(Utils.ShoppingCartList != null) {
            tvNotificationCountShopping.setText((String.valueOf(Utils.ShoppingCartList.size())));
        }
    }
}