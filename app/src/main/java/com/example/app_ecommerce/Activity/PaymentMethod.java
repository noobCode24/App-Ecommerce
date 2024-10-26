package com.example.app_ecommerce.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.app_ecommerce.R;
import com.example.app_ecommerce.utils.Utils;

public class PaymentMethod extends AppCompatActivity {
    private RadioButton radioMomo, radioZaloPay, radioCash;
    private AppCompatButton btnConfirmPayment;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_method);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initView();
        initControl();
        // Bật nút Xác nhận khi một phương thức thanh toán được chọn
        View.OnClickListener onRadioButtonClicked = v -> enableConfirmButton();
        radioMomo.setOnClickListener(onRadioButtonClicked);
        radioZaloPay.setOnClickListener(onRadioButtonClicked);
        radioCash.setOnClickListener(onRadioButtonClicked);

        // Khi bấm Xác nhận, gửi kết quả về CartActivity
        btnConfirmPayment.setOnClickListener(v -> {
            String selectedPaymentMethod = getSelectedPaymentMethod();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("paymentMethod", selectedPaymentMethod);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void initControl() {
        btnBack.setOnClickListener(v -> finish());
    }

    private String getSelectedPaymentMethod() {
        if (radioMomo.isChecked()) return "Momo";
        if (radioZaloPay.isChecked()) return "ZaloPay";
        return "Cash on Delivery";
    }

    private void enableConfirmButton() {
        btnConfirmPayment.setEnabled(true);
        btnConfirmPayment.setAlpha(1.0f);
    }

    private void initView() {
        radioMomo = findViewById(R.id.radioMomo);
        radioZaloPay = findViewById(R.id.radioZaloPay);
        radioCash = findViewById(R.id.radioCash);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        btnBack = findViewById(R.id.btnBack);
    }
}