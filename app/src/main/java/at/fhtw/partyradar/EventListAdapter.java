package at.fhtw.partyradar;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import at.fhtw.partyradar.helper.Utility;

public class EventListAdapter extends CursorAdapter {

    public EventListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_event_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder)view.getTag();

        // Read title for large text view from cursor
        String largeTextViewContent = cursor.getString(EventListFragment.COL_TITLE);
        // Read location name for small text view from cursor
        String locationName = cursor.getString(EventListFragment.COL_LOCATION_NAME);
        // Read address for small text view from cursor
        String address = cursor.getString(EventListFragment.COL_ADDRESS);
        // Read address additions for small text view from cursor
        String addressAdditions = cursor.getString(EventListFragment.COL_ADDRESS_ADDITIONS);
        // Read city for small text view from cursor
        String city = cursor.getString(EventListFragment.COL_CITY);
        // Read distance for small text view from cursor
        Double distance = Utility.rad2meter(cursor.getDouble(EventListFragment.COL_DISTANCE));
        // Build string for small text view
        String smallTextViewContent = locationName + " - " + address + " " + addressAdditions + " " + city + "  - Distance: " + Utility.round(distance / 1000, 2) + " km";

        viewHolder.largeTextView.setText(largeTextViewContent);
        viewHolder.smallTextView.setText(smallTextViewContent);
    }

    /**
     * Cache of the children views for a event list item.
     */
    public static class ViewHolder {
        public final TextView largeTextView;
        public final TextView smallTextView;

        public ViewHolder(View view) {
            largeTextView = (TextView) view.findViewById(R.id.list_item_event_list_large);
            smallTextView = (TextView) view.findViewById(R.id.list_item_event_list_small);
        }
    }
}
