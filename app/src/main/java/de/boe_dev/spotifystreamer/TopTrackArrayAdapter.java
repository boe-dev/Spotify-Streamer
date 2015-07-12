package de.boe_dev.spotifystreamer;

import android.content.Context;
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
 * Created by benny on 18.06.15.
 */
public class TopTrackArrayAdapter extends ArrayAdapter<TopTrackModel> {

    Context context;
    List<TopTrackModel> trackList;

    public TopTrackArrayAdapter(Context context, List<TopTrackModel> trackList) {
        super(context, R.layout.list_item_track, trackList);
        this.context = context;
        this.trackList = trackList;
    }

    static class ViewHolder {
        protected ImageView cover;
        protected TextView trackName, albumName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;

        if (convertView == null) {

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item_track, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.cover = (ImageView) view.findViewById(R.id.imageview_album_cover);
            viewHolder.trackName = (TextView) view.findViewById(R.id.textview_track_name);
            viewHolder.albumName = (TextView) view.findViewById(R.id.textview_album_name);
            view.setTag(viewHolder);
            viewHolder.trackName.setTag(trackList.get(position));

        } else {
            view = convertView;
            ((ViewHolder) view.getTag()).trackName.setTag(trackList.get(position));
        }
        final ViewHolder holder = (ViewHolder) view.getTag();
        if (!trackList.get(position).getImageUrl().equals("")) {
            Picasso.with(context).load(trackList.get(position).getImageUrl()).transform(new CircleTransform()).into(holder.cover);
        } else {
            Picasso.with(context).load(R.drawable.image_not_found).transform(new CircleTransform()).into(holder.cover);
        }
        holder.trackName.setText(trackList.get(position).getName());
        holder.albumName.setText(trackList.get(position).getAlbum());
        return view;
    }
}
