package org.clemlaf.comptesand;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;
import android.os.AsyncTask;
import java.io.*;
import java.net.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.KeyStore;
import javax.net.ssl.*;
import org.json.*;
import android.util.Log;

public class MainActivity extends Activity
{
    public static String EXTRAID="org.clemlaf.comptesand.SelectedId";
    private ListView lst;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        populateList();
    }
    @Override
    public void onResume(){
        super.onResume();
        populateList();
    }
    private void populateList(){
        this.lst=(ListView)findViewById(R.id.my_list_view);
        MyDatabaseOpenHelper myDBOH= new MyDatabaseOpenHelper(this);
        SimpleCursorAdapter listAdapt= new SimpleCursorAdapter(this,
                R.layout.listitem,
                myDBOH.getAllEntrees(),
                new String[] { 
                    MyDatabaseOpenHelper.EntreesEntry.C_DAT,
                    "s_" + MyDatabaseOpenHelper.ComptesEntry.C_NAME,
                    //MyDatabaseOpenHelper.EntreesEntry.C_CPS,
                    MyDatabaseOpenHelper.EntreesEntry.C_COM,
                    MyDatabaseOpenHelper.EntreesEntry.C_PRI
                },
                new int[] { 
                    R.id.my_list_date,
                    R.id.my_list_cps,
                    R.id.my_list_com,
                    R.id.my_list_prix,
                }	);
        lst.setAdapter(listAdapt);
        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Cursor cc= (Cursor) lst.getItemAtPosition(position);
                Intent newint= new Intent(lst.getContext(),AddActivity.class);
                newint.putExtra(EXTRAID,cc.getLong(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry._ID)));
                startActivity(newint);
                /* write you handling code like...
                 *     String st = "sdcard/";
                 *         File f = new File(st+o.toString());
                 *             // do whatever u want to do with 'f' File object
                 *                 */  
            }
        });
    }
    public void addEntry(View v){
        /*AlertDialog alertDialog = new AlertDialog.Builder(this).create();
          alertDialog.setTitle("Send Data");
          alertDialog.setMessage("Are you sure?");
          alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
        // here you can add functions
          }
          });
          alertDialog.show();*/
        Intent intent=new Intent(this, AddActivity.class);
        startActivity(intent);
    }
    public void tolistContent(View v){
    }
    public void syncContent(View v){
        new DownloadWebpageTask(this).execute(new String[]{"https://macbook/comptes/param/list"});
    }
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        private Context myContext;
        public DownloadWebpageTask(Context cont){
            myContext=cont;
        }
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                Log.e("CLEMLAF",urls[0]);
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                Log.e("CLEMLAF",urls[0]);
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
            JSONObject jd=new JSONObject(result);
            MyDatabaseOpenHelper myDBOH = new MyDatabaseOpenHelper(this.myContext);
            myDBOH.emptyParams();
            String params[]={"comptes","categories","moyens"};
            String tables[]={
                MyDatabaseOpenHelper.ComptesEntry.TABLE_NAME,
                MyDatabaseOpenHelper.CategoryEntry.TABLE_NAME,
                MyDatabaseOpenHelper.MoyensEntry.TABLE_NAME
            };
            for(int i=0;i<params.length;i++){
                JSONArray jar=jd.getJSONArray(params[i]);
                for(int j=0;j<jar.length();j++){
                    JSONObject par=jar.getJSONObject(j);
                    myDBOH.addParam(tables[i],par.getInt("id"),par.getString("name"));
                }
            }
        Toast.makeText(this.myContext,getString(R.string.my_synced_toast_text),Toast.LENGTH_SHORT).show();
            }
            catch (JSONException e){
                Log.e("CLEMLAF",result);
        Toast.makeText(this.myContext,getString(R.string.my_errj_toast_text),Toast.LENGTH_SHORT).show();
            }
        }
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;

            try {

            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = this.myContext.getResources().openRawResource(R.raw.macbook);
                //new BufferedInputStream(MainActivity.context.getAssets().open("littlesvr.crt"));
            Certificate ca = cf.generateCertificate(caInput);
            Log.e("CLEMLAF","ca=" + ((X509Certificate) ca).getSubjectDN());
            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            
            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            
            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            Log.e("CLEMLAF",myurl);
                URL url = new URL(myurl);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setSSLSocketFactory(context.getSocketFactory());
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.e("CLEMLAF",""+response);
                //Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } catch (Exception e){
                Log.e("CLEMLAF","erreur SSL");
                return "erreur";
            }
            finally {
                if (is != null) {
                    is.close();
                } 
            }
        }
        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            return sb.toString();
        }
    }
}
