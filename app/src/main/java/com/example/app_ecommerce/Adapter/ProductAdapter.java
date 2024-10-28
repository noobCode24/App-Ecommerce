package com.example.app_ecommerce.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecommerce.Activity.DetailActivity;
import com.example.app_ecommerce.Interface.ItemClickListener;
import com.example.app_ecommerce.Model.ProductModel;
import com.example.app_ecommerce.R;
import com.example.app_ecommerce.utils.Utils;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> {
    private List<ProductModel> productList;
    private Context context; // ngu canh de hien thi giao dien

    public ProductAdapter(Context context, List<ProductModel> productList) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // tạo view holder cho tung thang item
        // LayoutInflater gan layout viewholder_pop_list vao tung item cua recyclerView
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_pop_list, parent, false);

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.MyViewHolder holder, int position) {
        ProductModel productModel = productList.get(position);
        holder.txt_title.setText(productModel.getProduct_name());
        DecimalFormat decimalFormatWithDecimal = new DecimalFormat("$#,##0.00"); // Định dạng với 2 chữ số thập phân
        DecimalFormat decimalFormatWithoutDecimal = new DecimalFormat("$#,##0"); // Định dạng không có chữ số thập phân
        // Kiểm tra nếu giá là số nguyên hay không
        if (productModel.getPrice() % 1 == 0) {
            // Hiển thị giá không có phần thập phân
            holder.txt_price.setText(decimalFormatWithoutDecimal.format(productModel.getPrice()));
        } else {
            // Hiển thị giá có phần thập phân
            holder.txt_price.setText(decimalFormatWithDecimal.format(productModel.getPrice()));
        }

        // Định dạng và hiển thị số lượng bán
        holder.txt_soldquantity.setText("Đã bán: " + formatSoldQuantity(productModel.getSold_quantity()));

        // Kiểm tra ảnh từ drawable
        int imageResourceId = context.getResources().getIdentifier(productModel.getImage(), "drawable", context.getPackageName());

        if (imageResourceId != 0) {
            // Nếu ảnh tồn tại trong drawable, hiển thị nó
            holder.item_image.setImageResource(imageResourceId);
        } else {
            if(productModel.getImage().contains("http")){
                Picasso.get().load(productModel.getImage()).into(holder.item_image);
            } else {
                String img = Utils.BASE_URL+"images/" + productModel.getImage();
                Picasso.get().load(img).into(holder.item_image);
            }
        }

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int pos, boolean isLongclick) {
                if(!isLongclick) {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("Detail", productModel);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
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


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_title, txt_price, txt_rate, txt_soldquantity;
        ImageView item_image;

        private ItemClickListener itemClickListener;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_title = itemView.findViewById(R.id.item_title);
            txt_price = itemView.findViewById(R.id.txt_price);
            txt_soldquantity = itemView.findViewById(R.id.SoldquantityTxt);
            item_image = itemView.findViewById(R.id.item_image);
            itemView.setOnClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false);
        }
    }
}
