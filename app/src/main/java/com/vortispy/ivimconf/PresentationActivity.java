package com.vortispy.ivimconf;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class PresentationActivity extends Activity {
    static private String GITHUB_BASE_URL = "http://github.com/";
    static private String TWITTER_BASE_URL = "https://mobile.twitter.com/";

    private JSONObject presentation;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(this, "REQUIRE JSON OBJECT", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            presentation = new JSONObject(getIntent().getStringExtra("detail"));

        } catch (JSONException e) {
            Toast.makeText(this, "REQUIRE JSON OBJECT", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject speaker;
        try {
            JSONArray speakers = presentation.getJSONArray("speakers");
            speaker = speakers.getJSONObject(0);
        } catch (JSONException e) {
            Toast.makeText(this, "REQUIRE SPEAKERS", Toast.LENGTH_LONG).show();
            return;
        }
        // user icon
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        try {
            String url = speaker.getString("avatar");
            Bitmap bmp = BitmapFactory.decodeFile(Uri.parse(url).getPath());
            imageView.setImageBitmap(bmp);
        } catch (Exception e) {
            Toast.makeText(this, "REQUIRE AVATAR", Toast.LENGTH_LONG).show();
            return;
        }

        // user name
        TextView textView = (TextView)findViewById(R.id.user_name);
        try {
            textView.setText(speaker.getString("name"));
        } catch (JSONException e) {
            Toast.makeText(this, "REQUIRE NAME", Toast.LENGTH_LONG).show();
            return;
        }

        // github account
        textView = (TextView)findViewById(R.id.user_name);
        try {
            textView.setText(speaker.getString("github"));

            // click github
            final TextView finalTextView = textView;
            findViewById(R.id.github).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = GITHUB_BASE_URL + finalTextView.getText();
                    Intent intent = new Intent(getApplicationContext(),WebViewActivity.class);

                    intent.putExtra("test_result", url);
                    startActivity(intent);
                }
            });
        } catch (JSONException e) {
            textView.setText("");
        }

        // twitter account
        textView = (TextView)findViewById(R.id.user_name);
        try {
            textView.setText(speaker.getString("twitter"));

            // click twitter
            final TextView finalTextView = textView;
            findViewById(R.id.twitter).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = TWITTER_BASE_URL + finalTextView.getText();
                    Intent intent = new Intent(getApplicationContext(),WebViewActivity.class);

                    intent.putExtra("test_result", url);
                    startActivity(intent);

                }
            });
        } catch (JSONException e) {
            textView.setText("");
        }


        // set title
        // TODO: internationalization
        textView = (TextView)findViewById(R.id.title);
        try {
            textView.setText(presentation.getString("title"));
        } catch (JSONException e) {
            Toast.makeText(this, "REQUIRE TITLE", Toast.LENGTH_LONG).show();
            textView.setText("");
            return;
        }

        // set detail
        // TODO: internationalization
        textView = (TextView)findViewById(R.id.detail);
        try {
            textView.setText(presentation.getString("detail"));
        } catch (JSONException e) {
            Toast.makeText(this, "REQUIRE DETAIL", Toast.LENGTH_LONG).show();
            textView.setText("");
            return;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
