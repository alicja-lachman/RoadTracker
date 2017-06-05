package com.polsl.roadtracker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.activity.SendingActivity;
import com.polsl.roadtracker.database.entity.RouteData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by alachman on 20.05.2017.
 */

public class SendingListAdapter extends RecyclerView.Adapter<SendingListAdapter.DataViewHolder> {
    private List<RouteData> tracks;
    private Context context;
    private Toast toast;

    public SendingListAdapter(List<RouteData> tracks, Context context) {
        this.tracks = tracks;
        this.context = context;
    }


    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemTrack = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sending_list_item, viewGroup, false);
        DataViewHolder viewHolder = new DataViewHolder(itemTrack);
        viewHolder.view = itemTrack;
        viewHolder.dateItemView = (TextView) itemTrack.findViewById(R.id.date_text);
        viewHolder.descriptionItemView = (TextView) itemTrack.findViewById(R.id.description_text);
        viewHolder.sendButton = (Button) itemTrack.findViewById(R.id.button_send_item);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM yyyy HH:mm:ss");
        RouteData info = tracks.get(position);
        holder.dateItemView.setText(dateFormat.format(info.getStartDate()));
        holder.descriptionItemView.setText(info.getDescription());
        holder.position = position;
        if (info.isSetToSend())
            holder.sendButton.setEnabled(true);
        else {
            holder.sendButton.setEnabled(false);
        }
      //  holder.sendButton.setOnClickListener(v -> ((SendingActivity) context).sendRoute(info));
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        TextView dateItemView;
        TextView descriptionItemView;
        Button sendButton;
        View view;
        int position;

        public DataViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }
}