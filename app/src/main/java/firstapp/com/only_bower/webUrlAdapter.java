package firstapp.com.only_bower;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class webUrlAdapter extends ArrayAdapter<webUrlItem> {
    private int resourceId;

    public webUrlAdapter(@NonNull Context context, int resource, @NonNull List<webUrlItem> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        webUrlItem now = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.url = (TextView) view.findViewById(R.id.url);
            viewHolder.title = (TextView) view.findViewById(R.id.title);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.url.setText(now.url);
        viewHolder.title.setText(now.title);
        return view;
    }

    class ViewHolder{
        TextView url;
        TextView title;
    }
}
