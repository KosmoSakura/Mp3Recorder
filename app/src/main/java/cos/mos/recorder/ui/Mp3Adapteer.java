package cos.mos.recorder.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cos.mos.recorder.R;

/**
 * @Description:
 * @Author: Kosmos
 * @Date: 2019.05.25 16:35
 * @Email: KosmoSakura@gmail.com
 */
public class Mp3Adapteer extends BaseAdapter {
    private List<String> playList;
    private Context context;

    public Mp3Adapteer(List<String> playList, Context context) {
        this.playList = playList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return playList.size();
    }

    @Override
    public String getItem(int position) {
        return playList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        Holder holder;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.lay_list, parent, false);
            holder = new Holder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (Holder) view.getTag();
        }
        holder.dir.setText(playList.get(position));
        return view;
    }

    private class Holder {
        final TextView dir;

        public Holder(View root) {
            dir = root.findViewById(R.id.item_dir);
        }
    }
}
