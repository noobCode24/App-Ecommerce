package com.example.app_ecommerce.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecommerce.Model.Item;
import com.example.app_ecommerce.R;
import com.example.app_ecommerce.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class InvoiceDetailAdapter extends RecyclerView.Adapter<InvoiceDetailAdapter.MyViewHolder> {
    private List<Item> listItem;
    private Context context;

    public InvoiceDetailAdapter(Context context, List<Item> listItem) {
        this.context = context;
        this.listItem = listItem;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_detail, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item item = listItem.get(position);
        holder.txt_titleCart.setText(item.getProduct_name() + "");
        holder.txt_quantityCart.setText("Số lượng: " + item.getQuantity());
        int imageResourceId = context.getResources().getIdentifier(item.getImage(), "drawable", context.getPackageName());
        if (imageResourceId != 0) {
            // Nếu ảnh tồn tại trong drawable, hiển thị nó
            holder.img_picCart.setImageResource(imageResourceId);
        } else {
            if(item.getImage().contains("http")){
                Picasso.get().load(item.getImage()).into(holder.img_picCart);
            } else {
                String img = Utils.BASE_URL+"images/" + item.getImage();
                Picasso.get().load(img).into(holder.img_picCart);
            }
        }

    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img_picCart;
        TextView txt_titleCart, txt_quantityCart;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img_picCart = itemView.findViewById(R.id.img_picCart);
            txt_titleCart = itemView.findViewById(R.id.txt_titleCart);
            txt_quantityCart = itemView.findViewById(R.id.txt_quantityCart);
        }

    }
}
