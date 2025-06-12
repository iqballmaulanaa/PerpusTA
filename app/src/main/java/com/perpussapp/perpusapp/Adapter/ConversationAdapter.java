package com.perpussapp.perpusapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.joooonho.SelectableRoundedImageView;
import com.perpussapp.perpusapp.Activity.ImageActivity;
import com.perpussapp.perpusapp.Model.CoversationModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder>implements Filterable {
    public static final int TYPE_1 = 1;
    public static final int TYPE_2 = 2;
    DatabaseReference databaseReference;
    private Context context;
    private List<CoversationModel> modelChats;
    private List<CoversationModel> dataListfull = new ArrayList<>();
    private String TAG="AdapterChatTAG";
    private Constant constant;
    private CallBack mCallBack;

    public interface CallBack {
        void onClick(int position, boolean isDone);
    }

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    modelChats = dataListfull;
                } else {
                    List<CoversationModel> filteredList = new ArrayList<>();
                    for (CoversationModel row : dataListfull) {
                        if (row.getMessage().toLowerCase().contains(charString)
                                || row.getMessage().startsWith(charString)
                                || row.getMessage().toUpperCase().contains(charString)
                                ||row.getMessage().contains(charString)) {
                            filteredList.add(row);
                        }
                    }

                    modelChats = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = modelChats;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                modelChats = (ArrayList<CoversationModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public ConversationAdapter(Context context, ArrayList<CoversationModel> modelChats) {
        this.context = context;
        this.modelChats = modelChats;
        this.dataListfull = modelChats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        constant = new Constant(context);
        View view;
        if (constant.getLevel(context)==1){
            switch (viewType) {
                case TYPE_1:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_item, null);
                    return new ItemPertamaViewHolder(view);
                case TYPE_2:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receive_item, null);
                    return new ItemKeduaViewHolder(view);
                default:
                    return null;
            }
        }else {
            switch (viewType) {
                case TYPE_1:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receive_item, null);
                    return new ItemPertamaViewHolder(view);
                case TYPE_2:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_item, null);
                    return new ItemKeduaViewHolder(view);
                default:
                    return null;
            }
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int viewType = modelChats.get(position).getIsAdmin();
        switch (viewType) {
            case TYPE_1:
                ItemPertamaViewHolder itemPertamaViewHolder = (ItemPertamaViewHolder) holder;
                itemPertamaViewHolder.textViewTime.setText(TimeAgo(modelChats.get(position).getTime()));
                if (modelChats.get(position).getMessageType()==1){
                    itemPertamaViewHolder.textViewMessage.setVisibility(View.VISIBLE);
                    itemPertamaViewHolder.selectableRoundedImageView.setVisibility(View.GONE);
                    itemPertamaViewHolder.textViewMessage.setText(Html.fromHtml(
                            " <span>"+modelChats.get(position).getMessage()+"</span>" ));
                }else {
                    itemPertamaViewHolder.textViewMessage.setVisibility(View.GONE);
                    itemPertamaViewHolder.selectableRoundedImageView.setVisibility(View.VISIBLE);
                    Glide.with(context).load(modelChats.get(position).getMessage())
                            .into(itemPertamaViewHolder.selectableRoundedImageView);
                    itemPertamaViewHolder.selectableRoundedImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ImageActivity.class);
                            intent.putExtra("link", modelChats.get(position).getMessage());
                            context.startActivity(intent);
                        }
                    });
                }
                break;
            case TYPE_2:
                ItemKeduaViewHolder itemKeduaViewHolder = (ItemKeduaViewHolder) holder;

                itemKeduaViewHolder.textViewTime.setText(TimeAgo(modelChats.get(position).getTime()));
                if (modelChats.get(position).getMessageType()==1){
                    itemKeduaViewHolder.textViewMessage.setVisibility(View.VISIBLE);
                    itemKeduaViewHolder.selectableRoundedImageView.setVisibility(View.GONE);
                    itemKeduaViewHolder.textViewMessage.setText(Html.fromHtml(
                            "<span>"+modelChats.get(position).getMessage()+"</span>" ));
                }else {
                    itemKeduaViewHolder.textViewMessage.setVisibility(View.GONE);
                    itemKeduaViewHolder.selectableRoundedImageView.setVisibility(View.VISIBLE);
                    Glide.with(context).load(modelChats.get(position).getMessage())
                            .into(itemKeduaViewHolder.selectableRoundedImageView);
                    itemKeduaViewHolder.selectableRoundedImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ImageActivity.class);
                            intent.putExtra("link", modelChats.get(position).getMessage());
                            context.startActivity(intent);
                        }
                    });
                }

                break;
        }
    }



    private class ItemPertamaViewHolder extends ViewHolder {
        private TextView textViewMessage,textViewTime;
        private SelectableRoundedImageView selectableRoundedImageView;
        public ItemPertamaViewHolder(View view) {
            super(view);
            selectableRoundedImageView = view.findViewById(R.id.selectableRoundedImageView);
            textViewTime = view.findViewById(R.id.textViewTime);
            textViewMessage = view.findViewById(R.id.textViewMessage);
        }
    }

    private class ItemKeduaViewHolder extends ViewHolder {
        private TextView textViewMessage,textViewTime;
        private SelectableRoundedImageView selectableRoundedImageView;
        public ItemKeduaViewHolder(View view) {
            super(view);
            selectableRoundedImageView = view.findViewById(R.id.selectableRoundedImageView);
            textViewMessage = view.findViewById(R.id.textViewMessage);
            textViewTime = view.findViewById(R.id.textViewTime);

        }

    }


    @Override
    public int getItemViewType(int position) {
        return modelChats.get(position).getIsAdmin();
    }

    @Override
    public int getItemCount() {
        return modelChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    public static String TimeAgo(long time){
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }



}