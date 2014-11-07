package com.vortispy.ivimconf;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    JSONObject infoJson;
    String strJson;
    String urlString = "http://vimconf.vim-jp.org/info.json";
    String localJsonFile = "info.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        reloadInfoJson();
        loadInfoJson();
    }

    private void reloadSchedule(){
        ScheduleFragment scheduleFragment = (ScheduleFragment) getFragmentManager()
                .findFragmentByTag("android:switcher:"+R.id.pager+":0");
        if(scheduleFragment != null)  // could be null if not instantiated yet
        {
            if(scheduleFragment.getView() != null)
            {
                // no need to call if fragment's onDestroyView()
                //has since been called.
                scheduleFragment.reloadSchedule(infoJson);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.refresh:
                reloadInfoJson();
                strJson = "";
                reloadSchedule();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void loadInfoJson(){

        if ((strJson = readJson(localJsonFile)) != null){
//            Log.d("json", String.valueOf(strJson.length()));
            try {
                infoJson = new JSONObject(strJson);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("json", "File read");
        } else {
            new GetInfoJSON().execute();
        }
    }

    public void reloadInfoJson(){
        new GetInfoJSON().execute();
        Log.d("json", "reload json");
    }

    public String readJson(String fileName){
        InputStream inputStream;
        String ret;
        try{
            inputStream = openFileInput(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }
            bufferedReader.close();
            ret = stringBuffer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Log.d("read", ret);
        return ret;
    }

    public Boolean writeJson(String data, String fileName){
        OutputStream outputStream;
        try{
            outputStream = openFileOutput(fileName, MODE_PRIVATE);
            outputStream.write(data.getBytes("UTF-8"));
//            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
//            writer.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:
                    return ScheduleFragment.newInstance(infoJson);
                case 1:
                    return PlaceholderFragment.newInstance(position + 1);
            }
            return ScheduleFragment.newInstance(infoJson);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_schedule).toUpperCase(l);
                case 1:
                    return getString(R.string.title_location).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_map, container, false);

            WebView webView = (WebView) rootView.findViewById(R.id.webView);
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(Boolean.TRUE);
            webView.loadUrl("file:///android_asset/map.html");

//            webView.loadUrl("http://www.google.com/");
            return rootView;
        }
    }

    private class GetInfoJSON extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... voids) {
            HttpClient httpClient = new DefaultHttpClient();
            StringBuilder uri = new StringBuilder(urlString);
            HttpGet request = new HttpGet(uri.toString());
            HttpResponse response;

            try{
                response = httpClient.execute(request);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }

            int status = response.getStatusLine().getStatusCode();

            if(HttpStatus.SC_OK == status){
                try{
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    response.getEntity().writeTo(outputStream);
                    strJson = outputStream.toString();
                    infoJson = new JSONObject(strJson);
                    writeJson(strJson, localJsonFile);
                    Log.d("write", strJson);
                    Log.d("length", String.valueOf(strJson.length()));
                } catch (IOException e) {
                    e.printStackTrace();
                    return e.getMessage();
                } catch (JSONException e){
                    e.printStackTrace();
                    return e.getMessage();
                }
            } else {
                return String.valueOf(status);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null){
                Log.d("getJSON", s);
            }
        }
    }
}
