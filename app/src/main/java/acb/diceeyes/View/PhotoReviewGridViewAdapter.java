package acb.diceeyes.View;
import android.content.Context;
//import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.ArrayList;

import acb.diceeyes.R;

/**
 * Created by anita on 19.11.2016.
 */

public class PhotoReviewGridViewAdapter extends ArrayAdapter {

    private static final String TAG = PhotoReviewGridViewAdapter.class.getSimpleName();

    private Context context;
    private int layoutResourceId;
    private ArrayList<PhotoItem> data = new ArrayList<>();

    public PhotoReviewGridViewAdapter(Context context, int layoutResourceId, ArrayList<PhotoItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row;
        ViewHolder holder;

        LayoutInflater inflater = LayoutInflater.from(context);
        row = inflater.inflate(layoutResourceId, parent, false);
        holder = new ViewHolder();
        holder.picture = (ImageView) row.findViewById(R.id.picture);

        final CheckBox deleteCheckbox = (CheckBox)row.findViewById(R.id.deleteCheckbox);
        holder.deleteCheckbox = deleteCheckbox;

        row.setTag(holder);

        final PhotoItem item = data.get(position);
        Log.v(TAG, "photo item " + item);
        holder.picture.setImageBitmap(item.getPicture());
        item.setImageView(holder.picture);
        item.setCheckbox(holder.deleteCheckbox);

        deleteCheckbox.setTag(position);

        boolean checked = item.isTaggedToDelete();
        if (checked){
            deleteCheckbox.setChecked(true);
        }
        else if (!checked)
        {
            deleteCheckbox.setChecked(false);
        }


        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v(TAG, "Picture tagged.");
                if (deleteCheckbox.isChecked())
                {
                    deleteCheckbox.setChecked(false);
                    item.setTaggedToDelete(false);
                }
                else
                {
                    deleteCheckbox.setChecked(true);
                    item.setTaggedToDelete(true);
                }
            }

        });
        return row;
    }

    public int getDataSize(){
        return data.size();
    }

    public ArrayList<PhotoItem> getData(){
        return data;
    }

    static class ViewHolder {
        ImageView picture;
        CheckBox deleteCheckbox;
    }

    public void addData (PhotoItem pictureItem){
        data.add(pictureItem);
    }
}