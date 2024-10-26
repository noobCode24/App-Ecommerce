package com.example.app_ecommerce.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecommerce.Adapter.CartAdapter;
import com.example.app_ecommerce.Model.CreateOrder;
import com.example.app_ecommerce.Model.MessageModel;
import com.example.app_ecommerce.Model.ShoppingCart;
import com.example.app_ecommerce.R;
import com.example.app_ecommerce.Retrofit.ApiEcommerce;
import com.example.app_ecommerce.Retrofit.RetrofitClient;
import com.example.app_ecommerce.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.DecimalFormat;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class CartActivity extends AppCompatActivity {
    private ImageView btnBack, btntoAddress, btntopay;
    private RecyclerView recyclerViewCart;
    private TextView txt_subtotal, txt_delivery, txt_tax, txt_total, txtAddress, txtCash, txtCartEmpty, textView4, textView18;
    private Button btnOrder;
    private CartAdapter cartAdapter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ApiEcommerce apiEcommerce;
    private ConstraintLayout layoutline1, layoutline2;
    double total;
    private MessageModel messageModel;
    int id_invoice;
    private String address, payment;
    private int totalQuantity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ZaloPaySDK.init(2553, Environment.SANDBOX);

        initView();
        initControl();
        calculateCart();
        enableSwipeToDelete();

    }

    private void calculateCart() {
        double subTotal = 0;

        // Tính tổng giá trị của các sản phẩm trong giỏ hàng
        for (int i = 0; i < Utils.ShoppingCartList.size(); i++) {
            subTotal += Utils.ShoppingCartList.get(i).getPrice();
            totalQuantity += Utils.ShoppingCartList.get(i).getQuantity();
        }

        // Phí giao hàng cố định
        double delivery = 10.0;

        // Thuế 2% dựa trên tổng giá trị
        double percentTax = 0.02;
        double tax = Math.round(subTotal * percentTax * 100.0) / 100.0;

        // Tổng tiền (tổng sản phẩm + thuế + phí giao hàng)
        total = Math.round((subTotal + tax + delivery) * 100.0) / 100.0;

        // Định dạng các giá trị
        DecimalFormat decimalFormatWithDecimal = new DecimalFormat("$#,##0.00");
        DecimalFormat decimalFormatWithoutDecimal = new DecimalFormat("$#,##0");

        // Kiểm tra nếu giá trị là số nguyên thì hiển thị không có phần thập phân
        txt_subtotal.setText(subTotal % 1 == 0 ? decimalFormatWithoutDecimal.format(subTotal) : decimalFormatWithDecimal.format(subTotal));
        txt_delivery.setText(delivery % 1 == 0 ? decimalFormatWithoutDecimal.format(delivery) : decimalFormatWithDecimal.format(delivery));
        txt_tax.setText(tax % 1 == 0 ? decimalFormatWithoutDecimal.format(tax) : decimalFormatWithDecimal.format(tax));
        txt_total.setText(total % 1 == 0 ? decimalFormatWithoutDecimal.format(total) : decimalFormatWithDecimal.format(total));
    }

    private void initControl() {
        // Xử lý sự kiện nhấn nút quay lại
        btnBack.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("updated_cart_count", Utils.ShoppingCartList.size());
            setResult(RESULT_OK, returnIntent);
            finish();
        });

        // Cài đặt RecyclerView
        recyclerViewCart.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewCart.setLayoutManager(layoutManager);
        updateCartView();
        checkBtnOrder();

        // Chuyển tới AddressActivity khi nhấn nút btntoAddress
        btntoAddress.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, AddressActivity.class);
            startActivityForResult(intent, 1);  // Sử dụng startActivityForResult
        });
        btntopay.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, PaymentMethod.class);
            startActivityForResult(intent, 2);
        });
    }

    private void checkBtnOrder() {
        btnOrder.setOnClickListener(v -> {
            address = txtAddress.getText().toString().trim();
            payment = txtCash.getText().toString().trim();
            if (address.isEmpty() || address.equals("Address")) {
                // Hiển thị thông báo nếu chưa nhập địa chỉ
                Toast.makeText(CartActivity.this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            } else {
                if(payment.isEmpty() || payment.equals("Cash")) {
                    Toast.makeText(CartActivity.this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                } else if (payment.equals("Cash on Delivery")){
                    proceedToOrder();
                } else if (payment.equals("Momo")){
                    //thuc hien o day
                } else if (payment.equals("ZaloPay")){
                    //thuc hien o day
                    proceedToOrderWithZaloPay();
                }
            }
        });
    }

    private void proceedToOrderWithZaloPay() {
        String str_email = Utils.user_current.getEmail();
        String str_mobile = Utils.user_current.getMobile();
        int id = Utils.user_current.getUser_id();
        compositeDisposable.add(apiEcommerce.createOrder(str_email, str_mobile, address, totalQuantity, total, id, new Gson().toJson(Utils.ShoppingCartList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        messageModel -> {
                            Toast.makeText(getApplicationContext(),"Thanh toan don hang thanh cong", Toast.LENGTH_SHORT).show();
                            Utils.ShoppingCartList.clear(); // Xóa tất cả sản phẩm trong giỏ hàng
                            cartAdapter.notifyDataSetChanged(); // Cập nhật lại RecyclerView
                            id_invoice = messageModel.getInvoice_id();
                            requestZalo();
                        },
                        throwable -> {
                            Log.e("API Error", "Error: " + throwable.getMessage());
                            Toast.makeText(getApplicationContext(),"Thanh toan don hang ko thanh cong", Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void requestZalo() {
        CreateOrder orderApi = new CreateOrder();
        try {
            JSONObject data = orderApi.createOrder("100000");
            String code = data.getString("return_code");
            if (code.equals("1")) {
                String token = data.getString("zp_trans_token");

                ZaloPaySDK.getInstance().payOrder(CartActivity.this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String s, String s1, String s2) {
                        compositeDisposable.add(apiEcommerce.updateZalo(id_invoice, token)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        messageModel -> {
                                            if(messageModel.isSuccess()){
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        },
                                        throwable -> {
                                            Toast.makeText(getApplicationContext(),throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                ));

                    }

                    @Override
                    public void onPaymentCanceled(String s, String s1) {

                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {

                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void proceedToOrder() {
        String str_email = Utils.user_current.getEmail();
        String str_mobile = Utils.user_current.getMobile();
        int id = Utils.user_current.getUser_id();
        Log.d("test", new Gson().toJson(Utils.ShoppingCartList));
        compositeDisposable.add(apiEcommerce.createOrder(str_email, str_mobile, address, totalQuantity, total, id, new Gson().toJson(Utils.ShoppingCartList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        messageModel -> {
                            Toast.makeText(getApplicationContext(),"Thanh toan don hang thanh cong", Toast.LENGTH_SHORT).show();
                            Utils.ShoppingCartList.clear(); // Xóa tất cả sản phẩm trong giỏ hàng
                            cartAdapter.notifyDataSetChanged(); // Cập nhật lại RecyclerView
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        },
                        throwable -> {
                            // dang bị lỗi định dạng json nhưng vẫn thêm vô được db
                            Toast.makeText(getApplicationContext(),"Thanh toan don hang thanh cong", Toast.LENGTH_SHORT).show();
                            Utils.ShoppingCartList.clear(); // Xóa tất cả sản phẩm trong giỏ hàng
                            cartAdapter.notifyDataSetChanged(); // Cập nhật lại RecyclerView
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                ));
    }

    private void updateCartView() {
        if (Utils.ShoppingCartList.isEmpty()) {
            // Ẩn tất cả các phần tử trong ScrollView trừ tiêu đề giỏ hàng
            recyclerViewCart.setVisibility(View.GONE);
            txtCartEmpty.setVisibility(View.VISIBLE);
        } else {
            // Hiển thị danh sách giỏ hàng
            recyclerViewCart.setVisibility(View.VISIBLE); // Hiển thị RecyclerView
            cartAdapter = new CartAdapter(getApplicationContext(), Utils.ShoppingCartList, this::calculateCart); // Truyền listener
            recyclerViewCart.setAdapter(cartAdapter);
            textView4.setVisibility(View.VISIBLE);
            layoutline1.setVisibility(View.VISIBLE);
            textView18.setVisibility(View.VISIBLE);
            layoutline2.setVisibility(View.VISIBLE);
            btnOrder.setVisibility(View.VISIBLE);
            txtCartEmpty.setVisibility(View.GONE);
        }
    }

    private void initView() {
        apiEcommerce = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiEcommerce.class);
        btnBack = findViewById(R.id.btnBack);
        txt_subtotal = findViewById(R.id.txt_subtotal);
        txt_delivery = findViewById(R.id.txt_delivery);
        txt_tax = findViewById(R.id.txt_tax);
        txt_total = findViewById(R.id.txt_total);
        btnOrder = findViewById(R.id.btnOrder);
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        btntoAddress = findViewById(R.id.btntoAddress);
        btntopay = findViewById(R.id.btntopay);
        txtAddress = findViewById(R.id.txtAddress);
        txtCash = findViewById(R.id.txtCash);
        txtCartEmpty = findViewById(R.id.txtCartEmpty);
        textView4 = findViewById(R.id.textView4);
        layoutline1 = findViewById(R.id.layoutline1);
        textView18 = findViewById(R.id.textView18);
        layoutline2 = findViewById(R.id.layoutline2);
    }

    private void enableSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // Không cần di chuyển item trong danh sách
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // Hiển thị nút xoá và chờ người dùng click
                new Handler().postDelayed(() -> {
                    // Xử lý sự kiện click nút xoá
                    ShoppingCart removedItem = Utils.ShoppingCartList.get(position);

                    // Xóa sản phẩm khỏi danh sách giỏ hàng
                    Utils.ShoppingCartList.remove(position);
                    cartAdapter.notifyItemRemoved(position);

                    // Cập nhật lại tổng giá trị giỏ hàng
                    calculateCart();
                }, 100); // Đợi một thời gian ngắn trước khi xử lý click
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;

                    // Giới hạn khoảng cách vuốt (dX) không vượt quá 50dp
                    float deleteButtonWidth = 350 * recyclerView.getContext().getResources().getDisplayMetrics().density; // 50dp in pixels
                    if (dX < -deleteButtonWidth) {
                        dX = -deleteButtonWidth;
                    }

                    // Vẽ nền đỏ cho phần hiển thị nút xoá
                    Paint p = new Paint();
                    p.setColor(Color.RED);
                    RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    c.drawRect(background, p);

                    // Vẽ icon xóa
                    Bitmap icon = BitmapFactory.decodeResource(recyclerView.getContext().getResources(), R.drawable.ic_delete);
                    float iconMargin = (itemView.getHeight() - icon.getHeight()) / 2; // Căn giữa icon
                    float iconTop = itemView.getTop() + (itemView.getHeight() - icon.getHeight()) / 2;
                    float iconLeft = itemView.getRight() - iconMargin - icon.getWidth();
                    c.drawBitmap(icon, iconLeft, iconTop, p);

                    // Vuốt item theo cử chỉ vuốt của người dùng
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        };

        // Gắn ItemTouchHelper vào RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewCart);
    }

    // Nhận kết quả trả về từ AddressActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Nhận địa chỉ từ AddressActivity
            String address = data.getStringExtra("address");

            txtAddress.setText(address);
            txtAddress.setTextAppearance(R.style.textStyleBold);  // Thay đổi kiểu chữ
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            String paymentMethod = data.getStringExtra("paymentMethod");
            txtCash.setText(paymentMethod);
            txtCash.setTextAppearance(R.style.textStyleBold);
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}