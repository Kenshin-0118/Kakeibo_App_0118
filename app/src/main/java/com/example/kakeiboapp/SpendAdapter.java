package com.example.kakeiboapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SpendAdapter extends RecyclerView.Adapter<SpendAdapter.ViewHolder> {
    private final RecycleviewInterface recycleviewInterface;
    Context context;
    ArrayList<Spends> SpendList;

    public SpendAdapter (Context context, ArrayList<Spends> SpendList, RecycleviewInterface recycleviewInterface){
        this.context = context;
        this.SpendList = SpendList;
        this.recycleviewInterface = recycleviewInterface;

    }


    @NonNull
    @Override
    public SpendAdapter .ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.single_item, parent, false);
        return new ViewHolder(view,recycleviewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull SpendAdapter.ViewHolder holder, int position) {

        Spends spend = SpendList.get(position);
        DecimalFormat decimalFormat = new DecimalFormat("₱#,##0.00");
        holder.category.setText(spend.getCategory());
        holder.amount.setText("Amount: "+decimalFormat.format(Double.parseDouble(spend.getAmount())));
        holder.datetime.setText(spend.getCreatedAsString());

        switch (spend.getCategory()) {
            case "Food and Dining":
                holder.circleImageView.setImageResource(R.drawable.category_food);
                break;
            case "Housing and Utilities":
                holder.circleImageView.setImageResource(R.drawable.category_house);
                break;
            case "Transportation and Travel":
                holder.circleImageView.setImageResource(R.drawable.category_traveler);
                break;
            case "Personal Care and Health":
                holder.circleImageView.setImageResource(R.drawable.category_heart);
                break;
            case "Entertainment and Recreation":
                holder.circleImageView.setImageResource(R.drawable.category_entertain);
                break;
            case "Clothing and Accessories":
                holder.circleImageView.setImageResource(R.drawable.category_hoodie);
                break;
            case "Education and Learning":
                holder.circleImageView.setImageResource(R.drawable.category_book);
                break;
            case "Others":
                holder.circleImageView.setImageResource(R.drawable.category_others);
                break;
            default:
                holder. circleImageView.setImageResource(R.drawable.category_all);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return SpendList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView category;
        TextView amount;
        TextView datetime;
        CircleImageView circleImageView;

        public ViewHolder(@NonNull View itemView, RecycleviewInterface recycleviewInterface) {
            super(itemView);
            category = itemView.findViewById(R.id.category_food);
            amount = itemView.findViewById(R.id.food_amount);
            datetime = itemView.findViewById(R.id.datetime);
            circleImageView = itemView.findViewById(R.id.profile_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(recycleviewInterface != null){
                        int post = getAdapterPosition();
                        if (post != RecyclerView.NO_POSITION){
                            recycleviewInterface.OnItemClick(post);
                        }

                    }
                }
            });
        }

    }

}


