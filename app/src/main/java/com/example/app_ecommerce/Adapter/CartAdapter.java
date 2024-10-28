package com.example.app_ecommerce.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecommerce.Model.ShoppingCart;
import com.example.app_ecommerce.R;
import com.example.app_ecommerce.Retrofit.OnCartQuantityChangeListener;
import com.example.app_ecommerce.utils.Utils;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder> {
    private Context context;
    private List<ShoppingCart> CartList;
    private OnCartQuantityChangeListener quantityChangeListener;

    public CartAdapter(Context context, List<ShoppingCart> cartList, OnCartQuantityChangeListener quantityChangeListener) {
        CartList = cartList;
        this.context = context;
        this.quantityChangeListener = quantityChangeListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cart, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ShoppingCart shoppingCart = CartList.get(position);

        holder.txt_titleCart.setText(shoppingCart.getProduct_name());
        holder.numberItemTxt.setText(String.valueOf(shoppingCart.getQuantity()));
        DecimalFormat decimalFormatWithDecimal = new DecimalFormat("$#,##0.00");
        DecimalFormat decimalFormatWithoutDecimal = new DecimalFormat("$#,##0");
        if (shoppingCart.getOriginalPrice() % 1 == 0) {
            holder.txt_priceCart.setText(decimalFormatWithoutDecimal.format(shoppingCart.getOriginalPrice()));
            holder.txt_totalEachItem.setText(decimalFormatWithoutDecimal.format(shoppingCart.getPrice()));
        } else {
            holder.txt_priceCart.setText(decimalFormatWithDecimal.format(shoppingCart.getOriginalPrice()));
            holder.txt_totalEachItem.setText(decimalFormatWithDecimal.format(shoppingCart.getPrice()));
        }

        int imageResourceId = context.getResources().getIdentifier(shoppingCart.getImage(), "drawable", context.getPackageName());
        if (imageResourceId != 0) {
            holder.img_picCart.setImageResource(imageResourceId);
        } else {
            if(shoppingCart.getImage().contains("http")){
                Picasso.get().load(shoppingCart.getImage()).into(holder.img_picCart);
            } else {
                String img = Utils.BASE_URL+"images/" + shoppingCart.getImage();
                Picasso.get().load(img).into(holder.img_picCart);
            }
        }

        // Xử lý sự kiện nút giảm số lượng
        holder.minusCartBtn.setOnClickListener(v -> {
            int currentQuantity = shoppingCart.getQuantity();
            if (currentQuantity > 1) {
                shoppingCart.setQuantity(currentQuantity - 1);
                holder.numberItemTxt.setText(String.valueOf(shoppingCart.getQuantity()));

                // Cập nhật giá sản phẩm
                double totalEachItem = shoppingCart.getQuantity() * shoppingCart.getOriginalPrice();
                shoppingCart.setPrice(totalEachItem);
                holder.txt_totalEachItem.setText(totalEachItem % 1 == 0
                        ? decimalFormatWithoutDecimal.format(totalEachItem)
                        : decimalFormatWithDecimal.format(totalEachItem));

                // Gọi interface để thông báo sự thay đổi số lượng
                quantityChangeListener.onQuantityChanged();

                // Cập nhật lại chỉ mục hiện tại
                notifyItemChanged(holder.getAdapterPosition());
            } else {
                CartList.remove(position);
                notifyItemRemoved(position);
            }
        });

        // Xử lý sự kiện nút tăng số lượng
        holder.plusCartBtn.setOnClickListener(v -> {
            int currentQuantity = shoppingCart.getQuantity();
            int stockQuantity = shoppingCart.getStock_quantity();

            if (currentQuantity < stockQuantity){
                shoppingCart.setQuantity(currentQuantity + 1);
                holder.numberItemTxt.setText(String.valueOf(shoppingCart.getQuantity()));

                // Cập nhật giá sản phẩm
                double totalEachItem = shoppingCart.getQuantity() * shoppingCart.getOriginalPrice();
                shoppingCart.setPrice(totalEachItem);
                holder.txt_totalEachItem.setText(totalEachItem % 1 == 0
                        ? decimalFormatWithoutDecimal.format(totalEachItem)
                        : decimalFormatWithDecimal.format(totalEachItem));

                // Gọi interface để thông báo sự thay đổi số lượng
                quantityChangeListener.onQuantityChanged();

                // Cập nhật lại chỉ mục hiện tại
                notifyItemChanged(holder.getAdapterPosition());
            } else {
                Toast.makeText(context, "Hàng tồn kho không đủ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return CartList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView img_picCart;
        TextView txt_titleCart, txt_priceCart,txt_totalEachItem, numberItemTxt, minusCartBtn, plusCartBtn;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img_picCart = itemView.findViewById(R.id.img_picCart);
            txt_titleCart = itemView.findViewById(R.id.txt_titleCart);
            txt_priceCart = itemView.findViewById(R.id.txt_priceCart);
            txt_totalEachItem = itemView.findViewById(R.id.txt_totalEachItem);
            numberItemTxt = itemView.findViewById(R.id.numberItemTxt);
            minusCartBtn = itemView.findViewById(R.id.minusCartBtn);
            plusCartBtn = itemView.findViewById(R.id.plusCartBtn);
        }
    }
}
