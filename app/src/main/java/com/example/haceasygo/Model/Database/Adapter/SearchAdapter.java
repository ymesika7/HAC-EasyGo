package com.example.haceasygo.Model.Database.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.haceasygo.Model.Database.Sites;
import com.example.haceasygo.R;

import java.util.List;

class SearchViewHolder extends RecyclerView.ViewHolder{
    public TextView type, number;

    /** Constructor
     * @param  itemView layout view reference
     */
    public SearchViewHolder(View itemView){
     super(itemView);
     type = (TextView)itemView.findViewById(R.id.type);
     number = (TextView)itemView.findViewById(R.id.number);
    }
}

public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
    private Context context;
    private List<Sites> sites;
    View itemView;

    /** Constructor
     * @param  sites list of sites
     * @param context search fragment context
     */
    public SearchAdapter(Context context, List<Sites> sites){
        this.context = context;
        this.sites = sites;
    }

    /**
     * Set up view holder
     */
    @Override
    public SearchViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        itemView = inflater.inflate(R.layout.list_item, viewGroup, false);
        return new SearchViewHolder(itemView);
    }

    /**
     * Set up site description
     */
    @Override
    public void onBindViewHolder( SearchViewHolder holder, int i) {
        String description = sites.get(i).getNumber();
        if(! description.equals(context.getString(R.string.setup_number))) {
            if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_class)))
                holder.type.setText(context.getString(R.string.ui_class) + ", " + description);
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_room)))
                holder.type.setText(context.getString(R.string.ui_room) + ", " + description);
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_computer_lab)))
                holder.type.setText( context.getString(R.string.ui_computer_lab) + ", " + description);
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_wc)))
                holder.type.setText(context.getString(R.string.ui_wc));
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_wc_man)))
                holder.type.setText(context.getString(R.string.ui_wc_man));
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_wc_women)))
                holder.type.setText(context.getString(R.string.ui_wc_woman));
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_printer)))
                holder.type.setText(context.getString(R.string.ui_mail2print));

            holder.number.setText("קומה " + description.charAt(0) + ", בניין " + description.charAt(1));
            sites.get(i).setDescription(holder.type.getText() + ", " + holder.number.getText());
        }
        else{
            if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_class))){
                holder.type.setText(context.getString(R.string.ui_classes));
            }
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_room))){
                holder.type.setText(context.getString(R.string.ui_room));
            }
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_wc))) {
                holder.type.setText(context.getString(R.string.ui_wc));
            }
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_computer_lab))) {
                holder.type.setText(context.getString(R.string.ui_computer_lab));
            }
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_printer))) {
                holder.type.setText(context.getString(R.string.ui_printer));
            }
            else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_local))) {
                holder.type.setText(context.getString(R.string.ui_search_local));
            }
        }

        ImageView img = (ImageView) itemView.findViewById(R.id.list_item_image);
        if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_class)) || sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_room)))
            img.setImageResource(R.mipmap.ic_classroom);
        else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_wc))
                ||sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_wc_women))
                || sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_wc_man)) )
            img.setImageResource(R.drawable.ic_wc);
        else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_computer_lab)))
            img.setImageResource(R.drawable.ic_lab);
        else if(sites.get(i).getType().equalsIgnoreCase(context.getString(R.string.type_printer)))
            img.setImageResource(R.mipmap.ic_printer);
        else
            img.setImageResource(R.drawable.ic_my_location);
    }

    @Override
    public int getItemCount() {
        return sites.size();
    }

    public String getNumber(int i){
        return sites.get(i).number;
    }

    public String getType(int i) {return sites.get(i).type; }

    public String getDescription(int i) {return sites.get(i).description; }
}


