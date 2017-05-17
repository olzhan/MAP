package com.olzhan.map;

import android.Manifest;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.*;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private CameraUpdate CU;
    private Polyline line;
    private PolylineOptions PLO;
    Polyline PL=null;
    Circle Cr=null;
    static final String COOKIES_HEADER = "Set-Cookie";
    static CookieManager msCookieManager = new CookieManager();
    public static String LOG_TAG = "my_log";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        CookieHandler.setDefault(msCookieManager);
        PLO=new PolylineOptions()
                .color(Color.RED)
                .width(5);
         new AsyncRequest().execute("123", "/ajax", "foo=bar");


    }
    class AsyncRequest extends AsyncTask<String, LatLng, String> {

        @Override
        protected String doInBackground(String... arg) {
            String strJson="";
            HashMap<String, String> postDataParams=new HashMap<String, String>();
            postDataParams.put("j_username","olzhan");
            postDataParams.put("j_password","585696");
            performPostCall("http://sokol.berkut-tech.kz/mongo/j_spring_security_check",postDataParams);
        double offset=0;
            while (true) {
                try{
                    strJson=performGetCall("http://sokol.berkut-tech.kz/mongo/do/currentValue/findInformationByIMEI?_dc=1494914653455&imei=868204001602357", postDataParams);
                    double lat,lon;
                    JSONObject dataJsonObj = null;
                    JSONObject nomJsonObj = null;
                    String secondName = "";

                    try {
                        dataJsonObj = new JSONObject(strJson);
                        nomJsonObj = dataJsonObj.getJSONObject("nominatim");
                        lat=nomJsonObj.getDouble("lat")+offset; offset=offset+0.001;
                        lon=nomJsonObj.getDouble("lon");
                        LatLng mLatLng=new LatLng(lat,lon);
                        publishProgress(mLatLng);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    TimeUnit.SECONDS.sleep(5);}
                catch (InterruptedException e){ return "";}
            }

        }

        @Override
        protected void onProgressUpdate(LatLng... values) {
            super.onProgressUpdate(values);
            if (Cr==null){
            Cr= mMap.addCircle(new CircleOptions().center(values[0]) .radius(10) .fillColor(Color.RED));}
            Cr.setCenter(values[0]);
            CU=CameraUpdateFactory.newLatLngZoom(values[0],16);
            mMap.moveCamera(CU);
            PLO.add(values[0]);
            if (PL==null) {
                PL = mMap.addPolyline(PLO);
            }
            PL.setPoints(PLO.getPoints());
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);


        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng Almaty = new LatLng(43.23,76.86);
        int PERMISSION_REQUEST_CODE=0;
        double LatN=0,LatS=90,LonE=-180,LonW=180;

        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_SMS
                },
                PERMISSION_REQUEST_CODE);
        String path = Environment.getExternalStorageDirectory().toString() + "/Download/2017.gpx";//"/storage/84E3-0BFA/2017.gpx";//
        File gpxFile = new File(path);
        List<Location> gpxList = decodeGPX(gpxFile);
         LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i < gpxList.size(); i++){
            LatLng TPL= new LatLng(((Location)gpxList.get(i)).getLatitude(),((Location)gpxList.get(i)).getLongitude());
            PLO.add(TPL);
            builder.include(TPL);
            if (((Location)gpxList.get(i)).getLatitude()>LatN) {LatN=((Location)gpxList.get(i)).getLatitude();}
            if (((Location)gpxList.get(i)).getLatitude()<LatS) {LatS=((Location)gpxList.get(i)).getLatitude();}
            if (((Location)gpxList.get(i)).getLongitude()>LonE) {LonE=((Location)gpxList.get(i)).getLongitude();}
            if (((Location)gpxList.get(i)).getLongitude()<LonW) {LonW=((Location)gpxList.get(i)).getLongitude();}
        }
        LatLng SW= new LatLng(LatS,LonW);
        LatLng NE=new LatLng(LatN,LonE);
        LatLngBounds LLB=builder.build();//new LatLngBounds(SW,NE);

        //line=mMap.addPolyline(PLO);



    }
    public String  performPostCall(String requestURL,
                                   HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setUseCaches (false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
           int responseCode=conn.getResponseCode();
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

           if (responseCode == HttpURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
    //----------------
    public String  performGetCall(String requestURL,
                                   HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                response= sb.toString();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
    //----------------
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
    private List<Location> decodeGPX(File file){
        List<Location> list = new ArrayList<Location>();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            FileInputStream fileInputStream = new FileInputStream(file);
            Document document = documentBuilder.parse(fileInputStream);
            Element elementRoot = document.getDocumentElement();

            NodeList nodelist_trkpt = elementRoot.getElementsByTagName("trkpt");

            for(int i = 0; i < nodelist_trkpt.getLength(); i++){

                Node node = nodelist_trkpt.item(i);
                NamedNodeMap attributes = node.getAttributes();

                String newLatitude = attributes.getNamedItem("lat").getTextContent();
                Double newLatitude_double = Double.parseDouble(newLatitude);

                String newLongitude = attributes.getNamedItem("lon").getTextContent();
                Double newLongitude_double = Double.parseDouble(newLongitude);

                String newLocationName = newLatitude + ":" + newLongitude;
                Location newLocation = new Location(newLocationName);
                newLocation.setLatitude(newLatitude_double);
                newLocation.setLongitude(newLongitude_double);

                list.add(newLocation);

            }

            fileInputStream.close();

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return list;
    }
}
