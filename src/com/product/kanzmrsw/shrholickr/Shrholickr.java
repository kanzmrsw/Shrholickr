
package com.product.kanzmrsw.shrholickr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

public class Shrholickr extends Activity {
    String subject = "";
    String longUrl = "";
    String shortUrl = "";
    String login = "";
    String apiKey = "";
    boolean shortUrlFlag;
    boolean subjectFlag;
    private WebView webView;
    int limit;
    int service;
    String mode;
    String hashtag;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shortUrlFlag = false;
        subjectFlag = false;

        limit = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(
                "list_key", "40"));
        service = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(
                "service_key", "0"));

        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            setContentView(R.layout.main);
            Intent intent = getIntent();
            longUrl = intent.getStringExtra(Intent.EXTRA_TEXT);

            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/custom_hourphoto.ttf");
            TextView tv = (TextView) findViewById(R.id.CustomFontText);
            tv.setTypeface(tf);

            if (PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean("checkbox_key", true)) {
                hashtag = " #shrholickr";
            } else {
                hashtag = "";
            }

            if ((subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)) == null
            // after froyo
                    || (subject = intent.getStringExtra(Intent.EXTRA_TITLE)) == null) {
                webView = new WebView(this);
                webView.getSettings().setLoadsImagesAutomatically(false);
                webView.getSettings().setJavaScriptEnabled(false);
                webView.setVisibility(View.INVISIBLE);
                webView.setWebChromeClient(new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {
                        Toast.makeText(Shrholickr.this, "progress = " + progress,
                                Toast.LENGTH_SHORT);
                        if (progress >= 100) {
                            subject = webView.getTitle();
                            shortenSubject();
                        }
                    }
                });
                webView.loadUrl(longUrl);
            } else if (subject != null) {
                shortenSubject();
            } else {
                subject = "no subject";
            }

            // HTTP GET request
            Uri.Builder uriBuilder = new Uri.Builder();
            if (service < 2 || service > 7) { // bit.ly or j.mp
                login = "kanzmrsw"; // bit.ly or j.mp account
                apiKey = "R_a2b375b5b349a6bdcc71b008b3202321"; // bit.ly or j.mp
                                                               // API key

                if (service == 0 || service > 3) {
                    uriBuilder.path("http://api.bit.ly/v3/shorten");
                } else if (service == 1) { // j.mp
                    uriBuilder.path("http://api.j.mp/v3/shorten");
                }

                uriBuilder.appendQueryParameter("login", login);
                uriBuilder.appendQueryParameter("apiKey", apiKey);
                uriBuilder.appendQueryParameter("longUrl", Uri.encode(longUrl));
                uriBuilder.appendQueryParameter("format", "json");
                String uri = Uri.decode(uriBuilder.build().toString());

                // get shortUrl by bit.ly API
                try {
                    HttpUriRequest httpGet = new HttpGet(uri);
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        String entity = EntityUtils.toString(httpResponse.getEntity());
                        JSONObject jsonEntity = new JSONObject(entity);
                        if (jsonEntity != null) {
                            JSONObject jsonResult = jsonEntity.optJSONObject("data");
                            if (jsonResult != null) {
                                shortUrl = jsonResult.optString("url");
                            }
                        }
                    }
                } catch (IOException e) {
                } catch (JSONException e) {
                }
            } else if (service == 2) { // is.gd
                uriBuilder.path("http://is.gd/api.php");
                uriBuilder.appendQueryParameter("longurl", Uri.encode(longUrl));
                String uri = Uri.decode(uriBuilder.build().toString());

                // get shortUrl by is.gd API
                try {
                    HttpUriRequest httpGet = new HttpGet(uri);
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        HttpEntity entity = httpResponse.getEntity();
                        final InputStream in = entity.getContent();
                        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                        shortUrl = br.readLine();
                    }
                } catch (IOException e) {
                }
            } else if (service == 3) { // p.tl
                apiKey = "4b189ce07c649da4c3ac023b1d8efc472e6501b7"; // bit.ly
                                                                     // or j.mp
                                                                     // API key
                uriBuilder.path("http://p.tl/api/api_simple.php");
                uriBuilder.appendQueryParameter("key", apiKey);
                uriBuilder.appendQueryParameter("url", Uri.encode(longUrl));
                String uri = Uri.decode(uriBuilder.build().toString());

                // get shortUrl by p.tl API
                try {
                    HttpUriRequest httpGet = new HttpGet(uri);
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        String entity = EntityUtils.toString(httpResponse.getEntity());
                        JSONObject jsonEntity = new JSONObject(entity);
                        if (jsonEntity != null) {
                            shortUrl = jsonEntity.optString("short_url");
                        }
                    }
                } catch (IOException e) {
                } catch (JSONException e) {
                }
            } else if (service == 4) { // goo.gl
                apiKey = "AIzaSyD1tQL2cwXIoO4RkeWjlItsRVL6o0bviH0";
                uriBuilder.path("https://www.googleapis.com/urlshortener/v1/url?key=" + apiKey);
                String uri = Uri.decode(uriBuilder.build().toString());

                // get shortUrl by bit.ly API
                try {
                    String params = "{\"longUrl\":\"" + longUrl + "\"}";
                    HttpPost httpPost = new HttpPost(uri);
                    DefaultHttpClient client = new DefaultHttpClient();
                    StringEntity paramEntity = new StringEntity(params);
                    paramEntity.setChunked(false);
                    paramEntity.setContentType("application/json");
                    httpPost.setEntity(paramEntity);
                    HttpResponse httpResponse = client.execute(httpPost);

                    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        String entity = EntityUtils.toString(httpResponse.getEntity());
                        JSONObject jsonEntity = new JSONObject(entity);
                        if (jsonEntity != null) {
                            shortUrl = jsonEntity.optString("id");
                        }
                    }
                } catch (IOException e) {
                } catch (JSONException e) {
                }
            }
            shortUrlFlag = true;
            if (subjectFlag) {
                callActionSend();
            }
        } else {
            Intent prefIntent = new Intent(this, ShrholickrPreference.class);
            startActivity(prefIntent);
            finish();
        }
    }

    private void shortenSubject() {
        subjectFlag = true;
        if (shortUrlFlag) {
            if (subject.length() >= limit) {
                subject = subject.substring(0, limit - 3) + "...";
            }
            callActionSend();
        }
    }

    private void callActionSend() {
        Intent outIntent = new Intent(Intent.ACTION_SEND);
        outIntent.setType("text/plain");
        outIntent.putExtra(Intent.EXTRA_TEXT, ":" + subject + " " + shortUrl + hashtag);
        try {
            startActivity(outIntent);
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "client not found", Toast.LENGTH_LONG).show();
        }
    }

}
