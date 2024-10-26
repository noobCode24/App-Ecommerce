package com.example.app_ecommerce.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecommerce.Model.Invoice;
import com.example.app_ecommerce.R;

import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.MyViewHolder> {
    private List<Invoice> listInvoice;
    private Context context;
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public InvoiceAdapter(Context context, List<Invoice> listInvoice) {
        this.context = context;
        this.listInvoice = listInvoice;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_purchase_history_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Invoice invoice = listInvoice.get(position);
        holder.idInvoice.setText("Đơn hàng: " + invoice.getId());
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                holder.recyclerview_Detail.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        layoutManager.setInitialPrefetchItemCount(invoice.getItem().size());
//        can phai co adapter cho chi tiet
        InvoiceDetailAdapter invoiceDetailAdapter = new InvoiceDetailAdapter(context, invoice.getItem());
        holder.recyclerview_Detail.setLayoutManager(layoutManager);
        holder.recyclerview_Detail.setAdapter(invoiceDetailAdapter);
        holder.recyclerview_Detail.setRecycledViewPool(viewPool);
    }

    @Override
    public int getItemCount() {
        return listInvoice.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView idInvoice;
        RecyclerView recyclerview_Detail;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            idInvoice = itemView.findViewById(R.id.idInvoice);
            recyclerview_Detail = itemView.findViewById(R.id.recyclerview_Detail);
        }
    }

}
