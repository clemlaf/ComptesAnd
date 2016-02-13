package org.clemlaf.comptesand;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.Button;
import android.os.AsyncTask;
import java.io.*;
import java.net.*;
import android.net.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.KeyStore;
import javax.net.ssl.*;
import org.json.*;
import android.util.Log;
import static java.lang.Math.max;

public class MainActivity extends Activity
{
    public static String EXTRAID="org.clemlaf.comptesand.SelectedId";
    private ListView lst;
    private boolean synced=false;
    private int syncpage=0;
    private int synclimit=30;
    private boolean endsync=false;
    private SharedPreferences sharedPref;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sharedPref=this.getSharedPreferences(getString(R.string.my_pref_file_key),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPref.edit();
        editor.putString(getString(R.string.my_pref_hostname), "https://macbook/comptes");
        editor.commit();
        populateList();
        showHideButtons();
    }
    @Override
    public void onResume(){
        super.onResume();
        populateList();
        showHideButtons();
    }
    private void showHideButtons(){
        if(synced){
            if(this.syncpage>0)
                ((Button) findViewById(R.id.my_showprev_button)).setVisibility(Button.VISIBLE);
            else
                ((Button) findViewById(R.id.my_showprev_button)).setVisibility(Button.GONE);
            if(this.endsync)
                ((Button) findViewById(R.id.my_shownext_button)).setVisibility(Button.GONE);
            else
                ((Button) findViewById(R.id.my_shownext_button)).setVisibility(Button.VISIBLE);
            ((Button) findViewById(R.id.my_showsync_button)).setText(getString(R.string.my_showunsync_button_text));
            ((TextView) findViewById(R.id.my_list_title)).setText(getString(R.string.my_sync_list_title));

        }else{
            ((Button) findViewById(R.id.my_showprev_button)).setVisibility(Button.GONE);
            ((Button) findViewById(R.id.my_shownext_button)).setVisibility(Button.GONE);
            ((Button) findViewById(R.id.my_showsync_button)).setText(getString(R.string.my_showsync_button_text));
            ((TextView) findViewById(R.id.my_list_title)).setText(getString(R.string.my_unsync_list_title));
        }
    }
    private void populateList(){
        this.lst=(ListView)findViewById(R.id.my_list_view);
        MyDatabaseOpenHelper myDBOH= new MyDatabaseOpenHelper(this);
        SimpleCursorAdapter listAdapt= new SimpleCursorAdapter(this,
                R.layout.listitem,
                null,
                new String[] { 
                    MyDatabaseOpenHelper.EntreesEntry.C_DAT,
                    "s_" + MyDatabaseOpenHelper.ComptesEntry.C_NAME,
                    "d_" + MyDatabaseOpenHelper.ComptesEntry.C_NAME,
                    MyDatabaseOpenHelper.CategoryEntry.C_NAME,
                    MyDatabaseOpenHelper.MoyensEntry.C_NAME,
                    //MyDatabaseOpenHelper.EntreesEntry.C_CPS,
                    MyDatabaseOpenHelper.EntreesEntry.C_COM,
                    MyDatabaseOpenHelper.EntreesEntry.C_PRI
                },
                new int[] { 
                    R.id.my_list_date,
                    R.id.my_list_cps,
                    R.id.my_list_cpd,
                    R.id.my_list_cat,
                    R.id.my_list_moy,
                    R.id.my_list_com,
                    R.id.my_list_prix,
                }	);
	if(this.synced){
            listAdapt.changeCursor(myDBOH.getSyncedEntrees(this.syncpage,this.synclimit));
            if(listAdapt.getCount()>=this.synclimit){
                this.endsync=false;
            }else{
                this.endsync=true;
            }
	}else{
            listAdapt.changeCursor(myDBOH.getUnSyncedEntrees());
        }
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
    public void switchSync(View v){
        this.synced=!this.synced;
        populateList();
        showHideButtons();
    }
    public void syncContent(View v){
        ConnectivityManager connMgr = (ConnectivityManager) 
            getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        String hostn=this.sharedPref.getString(getString(R.string.my_pref_hostname),"");
        if (networkInfo != null && networkInfo.isConnected() && hostn.length()>0) {
            new DownloadWebpageTask(this).execute(new String[]{
                hostn+getString(R.string.my_update_address),
                hostn+getString(R.string.my_param_address)
            });
        } else {
            Toast.makeText(this,getString(R.string.my_errnoc_toast_text),Toast.LENGTH_SHORT).show();
        }

    }
    public void prevPage(View v){
        this.syncpage=max(this.syncpage-1,0);
        populateList();
        showHideButtons();
    }
    public void nextPage(View v){
        if(!this.endsync)
            this.syncpage=this.syncpage+1;
        populateList();
        showHideButtons();
    }
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        private MainActivity myContext;
        public DownloadWebpageTask(MainActivity cont){
            myContext=cont;
        }
        @Override
        protected String doInBackground(String... urls) {
            MyDatabaseOpenHelper myDBOH = new MyDatabaseOpenHelper(this.myContext);
            Cursor cc=myDBOH.getAllEntrees();
            while(cc.moveToNext()){
                String ent="null";
                try{
                    long id=cc.getLong(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry._ID));
                    int cps= cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_CPS));
                    int cpd= cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_CPD));
                    int cat= cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_CAT));
                    int moy= cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_MOY));
                    int syn= cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_SYN));
                    if(syn==0){
                        ent="id=new&date="+
                            cc.getString(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_DAT))+
                            "&cp_s="+(cps==0?"":cps)+
                            "&cp_d="+(cpd==0?"":cpd)+
                            "&cat="+(cat==0?"":cat)+
                            "&com="+
                            URLEncoder.encode(
                                    cc.getString(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_COM)), "UTF-8")+
                            "&pr="+
                            cc.getFloat(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_PRI))+
                            "&pt=false&moy="+(moy==0?"":moy);
                        String resp=downloadUrl(urls[0],ent);
                        if (resp.equals("200")){
                            myDBOH.putSync(id);
                        }
                    }
                } catch (Exception e){
                    Log.e("CLEMLAF",urls[0]);
                    Log.e("CLEMLAF",ent);
                }
            }
            cc.close();
            // params comes from the execute() call: params[0] is the url.
            try {

                Log.e("CLEMLAF",urls[0]);
                return downloadUrl(urls[1],"");
            } catch (IOException e) {
                Log.e("CLEMLAF",urls[1]);
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
        this.myContext.populateList();
            }
            catch (JSONException e){
                Log.e("CLEMLAF",result);
        Toast.makeText(this.myContext,getString(R.string.my_errj_toast_text),Toast.LENGTH_SHORT).show();
            }
        }
        private String downloadUrl(String myurl, String params) throws IOException {
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
                conn.setDoOutput(true);
                //Send request
                DataOutputStream wr = new DataOutputStream (
                        conn.getOutputStream ());
                wr.writeBytes (params);
                wr.flush ();
                wr.close ();
                // Starts the query
                //conn.connect();
                int response = conn.getResponseCode();
                Log.e("CLEMLAF",""+response);
                //Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();
                if(params.length()==0){

                // Convert the InputStream into a string
                String contentAsString = readIt(is);
                return contentAsString;
                }else{
                    return ""+response;
                }

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
