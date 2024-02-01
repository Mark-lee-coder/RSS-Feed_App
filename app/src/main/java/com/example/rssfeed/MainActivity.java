package com.example.rssfeed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView lvRSS;
    ArrayList<String> titles;
    ArrayList<String> links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvRSS = findViewById(R.id.lvRSS);
        titles = new ArrayList<>();
        links = new ArrayList<>();

        lvRSS.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(links.get(position));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        new ProcessInBackground().execute();
    }

    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        }

        catch (IOException ioException) {
            return null;
        }
    }

    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception> {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading RSS feed, please wait...");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... integers) {
            try {
                URL url = new URL("http://feeds.news24.com/articles/fin24/tech/rss");

                XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                xmlPullParserFactory.setNamespaceAware(false);

                XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
                xmlPullParser.setInput(getInputStream(url), "UTF_8");

                boolean insideItem = false;
                int eventType = xmlPullParser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xmlPullParser.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        }

                        else if (xmlPullParser.getName().equalsIgnoreCase("title")) {
                            if (insideItem) {
                                titles.add(xmlPullParser.nextText());
                            }
                        }

                        else if (xmlPullParser.getName().equalsIgnoreCase("link")) {
                            if (insideItem) {
                                links.add(xmlPullParser.nextText());
                            }
                        }
                    }

                    else if (eventType == XmlPullParser.END_TAG && xmlPullParser.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                    }

                    eventType = xmlPullParser.next();
                }
            }

            catch (XmlPullParserException | IOException malformedURLException) {
                exception = malformedURLException;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception exception1) {
            super.onPostExecute(exception1);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, titles);
            lvRSS.setAdapter(arrayAdapter);
            progressDialog.dismiss();
        }
    }
}