package com.polsl.roadtracker.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.activity.MapActivity;
import com.polsl.roadtracker.database.entity.RouteData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Rafał Swoboda on 2017-03-31.
 */

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.DataViewHolder> {
    String[] mDataset = {"Data", "In", "Adapter"};
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

        holder.view.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapActivity.class);
            intent.putExtra("ROUTE_ID", tracks.get(position).getId());
            intent.putExtra("ROUTE_DESCRIPTION", tracks.get(position).getDescription());
            context.startActivity(intent);
            toast = Toast.makeText(context, "You clicked an item " + tracks.get(position).getId(), Toast.LENGTH_SHORT);
            toast.show();
        });

        holder.optionsItemView.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(context, holder.optionsItemView);
            popup.inflate(R.menu.list_popup_menu);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.ready_to_send:
                            //TODO: add some information about readiness to send
                            toast = Toast.makeText(context, "Ready to send", Toast.LENGTH_SHORT);
                            toast.show();
                            break;
                        case R.id.delete_route:
                            tracks.get(position).delete();
                            tracks.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, tracks.size());
                            toast = Toast.makeText(context, "Delete successful", Toast.LENGTH_SHORT);
                            toast.show();
                            break;
                        case R.id.change_name:
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Change route's name");

                            // Set up the input
                            final EditText input = new EditText(context);
                            input.requestFocus();
                            // Specify the type of input expected
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            builder.setView(input);

                            // Set up the buttons
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String m_Text = input.getText().toString();
                                    tracks.get(position).setDescription(m_Text);
                                    notifyItemRangeChanged(position, tracks.size());

                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();
                            break;
                    }
                    return false;
                }
            });
            popup.show();
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