package com.polsl.roadtracker.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.database.entity.RouteData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import static android.R.id.message;

/**
 * Created by Rafał Swoboda on 2017-03-31.
 */

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.DataViewHolder> {
    private List<RouteData> tracks;
    private Context context;
    private Toast toast;

    public RouteListAdapter(List<RouteData> tracks, Context context) {
        this.tracks = tracks;
        this.context = context;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemTrack = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.route_list_item, viewGroup, false);
        DataViewHolder viewHolder = new DataViewHolder(itemTrack);
        viewHolder.view = itemTrack;
        viewHolder.dateItemView = (TextView) itemTrack.findViewById(R.id.date_text);
        viewHolder.descriptionItemView = (TextView) itemTrack.findViewById(R.id.description_text);
        viewHolder.durationItemView = (TextView) itemTrack.findViewById(R.id.duration_text);
        viewHolder.optionsItemView = (TextView) itemTrack.findViewById(R.id.list_options);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        RouteData info = tracks.get(position);
        holder.dateItemView.setText(dateFormat.format(info.getStartDate()));
        holder.descriptionItemView.setText(info.getDescription());
        holder.durationItemView.setText("Duration: " + info.calculateDuration());
        holder.position = position;
        holder.optionsItemView.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {

            PopupMenu popup = new PopupMenu(context, holder.optionsItemView);
            popup.inflate(R.menu.list_popup_menu);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.show_on_map:
                            toast = Toast.makeText(context,"Show on map",Toast.LENGTH_SHORT);
                            toast.show();
                            break;
                        case R.id.delete_route:
                            tracks.get(position).delete();
                            tracks.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, tracks.size());
                            toast = Toast.makeText(context,"Delete successful",Toast.LENGTH_SHORT);
                            toast.show();
                            break;
                    }
                    return false;
                }
            });
            popup.show();
        }
    });
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        TextView dateItemView;
        TextView descriptionItemView;
        TextView durationItemView;
        TextView optionsItemView;
        View view;
        int position;

        public DataViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }
}