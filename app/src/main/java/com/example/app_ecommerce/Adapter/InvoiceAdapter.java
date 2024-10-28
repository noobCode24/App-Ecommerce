package com.example.app_ecommerce.Adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecommerce.Interface.ItemClickDeleteListener;
import com.example.app_ecommerce.Model.Invoice;
import com.example.app_ecommerce.R;

import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.MyViewHolder> {
    private List<Invoice> listInvoice;
    private Context context;
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private ItemClickDeleteListener itemClickDeleteListener;

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
        holder.idInvoice.setText("Mã đơn hàng:# " + invoice.getId());

        String addressText = "Địa chỉ: " + invoice.getAddress();
        SpannableString spannableAddress = new SpannableString(addressText);
        spannableAddress.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.black)),
                8, addressText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.address_invoice.setText(spannableAddress);

        holder.idStatusInvoice.setText(statusInvoice(invoice.getStatus()));
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

    private String statusInvoice(int status){
        String result = "";
        switch (status){
            case 0:
                result = "Chờ xác nhận";
                break;
            case 1:
                result = "Đã xác nhận";
                break;
            case 2:
                result = "Đang giao hàng";
                break;
            case 3:
                result = "Đã giao hàng";
                break;
            case 4:
                result = "Đã hủy";
                break;
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return listInvoice.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView idInvoice, address_invoice, idStatusInvoice;
        RecyclerView recyclerview_Detail;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            idInvoice = itemView.findViewById(R.id.idInvoice);
            recyclerview_Detail = itemView.findViewById(R.id.recyclerview_Detail);
            address_invoice = itemView.findViewById(R.id.address_invoice);
            idStatusInvoice = itemView.findViewById(R.id.idStatus);
        }
    }

}
