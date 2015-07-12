package de.boe_dev.spotifystreamer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.boe_dev.spotifystreamer.functions.CircleTransform;

/**
 * Created by benny on 14.06.15.
 */
public class ArtistArrayAdapter extends ArrayAdapter<ArtistModel> {

    private Context context;
    private List<ArtistModel> artistList;

    public ArtistArrayAdapter(Context context, List<ArtistModel> artist) {
        super(context, R.layout.list_item_artist, artist);
        this.context = context;
        this.artistList = artist;
    }

    static class ViewHolder {
        protected ImageView image;
        protected TextView name;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {

            Log.d("ArtistArrayAdapter", artistList.get(position).getName());

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item_artist, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) view.findViewById(R.id.imageview_artist_image);
            viewHolder.name = (TextView) view.findViewById(R.id.textview_artist_name);
            view.setTag(viewHolder);
            viewHolder.name.setTag(artistList.get(position));
        } else {
            view = convertView;
            ((ViewHolder) view.getTag()).name.setTag(artistList.get(position));
        }
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.name.setText(artistList.get(position).getName());
        if (!artistList.get(position).getImageUrl().equals("")) {
            Picasso.with(context).load(artistList.get(position).getImageUrl()).transform(new CircleTransform()).into(holder.image);
        } else {
            Picasso.with(context).load(R.drawable.image_not_found).transform(new CircleTransform()).into(holder.image);
        }
        return view;
    }
}


