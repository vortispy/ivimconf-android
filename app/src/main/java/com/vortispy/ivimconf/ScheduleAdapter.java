package com.vortispy.ivimconf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by vortispy on 2014/11/06.
 */
public class ScheduleAdapter extends ArrayAdapter<JSONObject> {
    private LayoutInflater inflater;

    private LruCache<String, Bitmap> mMemoryCache;

    public ScheduleAdapter(Context context, int resource, List<JSONObject> items) {
        super(context, resource, items);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject item = getItem(position);
        if (convertView == null){
            convertView = inflater.inflate(R.layout.schedule_item, null);
        }

        TextView startTime = (TextView) convertView.findViewById(R.id.startTimeView);
        TextView titleView = (TextView) convertView.findViewById(R.id.titleView);
        TextView speakerName = (TextView) convertView.findViewById(R.id.speakerNameView);
        ImageView avatar = (ImageView) convertView.findViewById(R.id.spekerAvatar);


        try{
            long startUtc = item.getInt("starttime");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+09:00"));
            String time = simpleDateFormat.format(new Date(startUtc*1000));
            startTime.setText(time);

            String titleText = item.getString("title");
            titleView.setText(titleText);


            if(item.has("speakers")){
                JSONObject speaker = item.getJSONArray("speakers").getJSONObject(0);
                String spName = speaker.getString("name");
                speakerName.setText(spName);

                String urlAbatar = speaker.getString("avatar");
                Bitmap bitmap = getBitmapFromMemCache(urlAbatar);
                avatar.setVisibility(View.VISIBLE);
                if(bitmap != null){
                    avatar.setImageBitmap(bitmap);
                }
                else {
                    SpAvatar spAvatar = new SpAvatar(avatar, urlAbatar);
                    new GetAvatar().execute(spAvatar);
                }
            }
            else {
                avatar.setVisibility(View.INVISIBLE);
            }

            String typeOfSchedule = item.getString("type");

            if(typeOfSchedule == "presentation"){

            } else {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private class SpAvatar{
        ImageView imageView;
        String url;
        String errorMessage = null;
        Bitmap bitmap;

        public SpAvatar(ImageView imageView, String url){
            this.imageView = imageView;
            this.url = url;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
    private class GetAvatar extends AsyncTask<SpAvatar, Void, SpAvatar> {
        @Override
        protected SpAvatar doInBackground(SpAvatar... spAvatars) {
            SpAvatar ret = spAvatars[0];

            ret.bitmap = getBitmapFromMemCache(ret.url);
            if(ret.bitmap != null){
                return ret;
            }

            try{
                URL avatarUrl = new URL(ret.url);
                InputStream inputStream = avatarUrl.openStream();

                ret.bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                ret.setErrorMessage(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                ret.setErrorMessage(e.getMessage());
            }

            return ret;
        }

        @Override
        protected void onPostExecute(SpAvatar spAvatar) {
            if(spAvatar.getErrorMessage() != null){
                Log.d("avatar", spAvatar.getErrorMessage());
            } else {
                Bitmap bitmap = spAvatar.bitmap;
                addBitmapToMemoryCache(spAvatar.url, bitmap);
                spAvatar.imageView.setImageBitmap(bitmap);
            }

        }
    }
}
