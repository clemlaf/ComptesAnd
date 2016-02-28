package org.clemlaf.comptesand;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.content.Intent;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.app.NotificationManager;
import static java.lang.Math.max;

public class MainActivity extends FragmentActivity
{
    static final int NUM_ITEMS=2;
    public static String EXTRAID="org.clemlaf.comptesand.SelectedId";
    private ViewPager vp;
    private MyAdapter ad;
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
        sharedPref= PreferenceManager.getDefaultSharedPreferences(this);
        populateList();
        showHideButtons();
    }
    @Override
    public void onResume(){
        super.onResume();
        populateList();
        showHideButtons();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.my_settings:
                Intent intent=new Intent(this, PrefActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void showHideButtons(){
        if(synced){
/*            if(this.syncpage>0)
                ((Button) findViewById(R.id.my_showprev_button)).setVisibility(Button.VISIBLE);
            else
                ((Button) findViewById(R.id.my_showprev_button)).setVisibility(Button.GONE);
            if(this.endsync)
                ((Button) findViewById(R.id.my_shownext_button)).setVisibility(Button.GONE);
            else
                ((Button) findViewById(R.id.my_shownext_button)).setVisibility(Button.VISIBLE);*/
            ((Button) findViewById(R.id.my_showsync_button)).setText(getString(R.string.my_showunsync_button_text));
            ((TextView) findViewById(R.id.my_list_title)).setText(getString(R.string.my_sync_list_title));

        }else{
/*            ((Button) findViewById(R.id.my_showprev_button)).setVisibility(Button.GONE);
            ((Button) findViewById(R.id.my_shownext_button)).setVisibility(Button.GONE);*/
            ((Button) findViewById(R.id.my_showsync_button)).setText(getString(R.string.my_showsync_button_text));
            ((TextView) findViewById(R.id.my_list_title)).setText(getString(R.string.my_unsync_list_title));
        }
    }
    private void populateList(){
        this.vp=(ViewPager) findViewById(R.id.my_viewpager);
        ad=new MyAdapter(getSupportFragmentManager());
        vp.setAdapter(ad);
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
        String hostn="";
        if(this.sharedPref.getBoolean(getString(R.string.my_pref_nothome),false)){
            hostn=this.sharedPref.getString(getString(R.string.my_pref_out_hostname),"");
        }else{
            hostn=this.sharedPref.getString(getString(R.string.my_pref_home_hostname),"");
        }
        if (networkInfo != null && networkInfo.isConnected() && hostn.length()>0) {
            new DownloadWebpageTask(this).execute(new String[]{
                hostn+getString(R.string.my_update_address),
                hostn+getString(R.string.my_param_address)
            });
        } else if (hostn.length()==0) {
            Toast.makeText(this,getString(R.string.my_errnohn_toast_text),Toast.LENGTH_SHORT).show();
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
        private int nid=1;
        public DownloadWebpageTask(MainActivity cont){
            myContext=cont;
        }
        @Override
        protected String doInBackground(String... urls) {
            NotificationCompat.Builder myNotBuilder= new NotificationCompat.Builder(this.myContext)
                .setContentTitle(getString(R.string.my_not_title))
                .setContentText(getString(R.string.my_not_text))
                .setSmallIcon(android.R.drawable.ic_popup_sync);
            NotificationManager myNot=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            myNot.notify(this.nid,myNotBuilder.build());
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
            NotificationManager myNot=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            myNot.cancel(this.nid);
        }
        private String downloadUrl(String myurl, String params) throws IOException {
            InputStream is = null;

            try {

            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            String path=this.myContext.sharedPref.getString(getString(R.string.my_pref_crt_path),"");
            SSLContext context=null;
            if (path.length()>0){
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                File caFile=new File(path);
                InputStream caInput = new BufferedInputStream(new FileInputStream(caFile));
                //this.myContext.getResources().openRawResource(R.raw.macbook);
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
                context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);
            }
            Log.e("CLEMLAF",myurl);
                URL url = new URL(myurl);
                int response;
                HttpURLConnection conn=null;
                if(myurl.startsWith("https://")){
                    HttpsURLConnection conns = (HttpsURLConnection) url.openConnection();
                    if(context!=null)
                        conns.setSSLSocketFactory(context.getSocketFactory());
                        conn= (HttpURLConnection) conns;
                    }else{
                    conn = (HttpURLConnection) url.openConnection();
                }
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
                    response = conn.getResponseCode();
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
                Log.e("CLEMLAF","erreur Connection");
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
     public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0){
                return new UnSyncedFragment();
            }else{
                return new SyncedFragment();
            }
        }
    }
    // Instances of this class are fragments representing a single
    // object in our collection.
    public static class SyncedFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
                MainActivity ma=(MainActivity) getActivity();
            View rootView = inflater.inflate(
                    R.layout.synced, container, false);
            ListView lst=(ListView) rootView.findViewById(R.id.my_slist_view);
        MyDatabaseOpenHelper myDBOH= new MyDatabaseOpenHelper(ma);
        SimpleCursorAdapter listAdapt= new SimpleCursorAdapter(ma,
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
            listAdapt.changeCursor(myDBOH.getSyncedEntrees(ma.syncpage,ma.synclimit));
            if(listAdapt.getCount()>=ma.synclimit){
                ma.endsync=false;
            }else{
                ma.endsync=true;
            }
        lst.setAdapter(listAdapt);
            return rootView;
        }
    }
    public static class UnSyncedFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.unsynced, container, false);
            ListView lst=(ListView) rootView.findViewById(R.id.my_ulist_view);
        MyDatabaseOpenHelper myDBOH= new MyDatabaseOpenHelper(getActivity());
        SimpleCursorAdapter listAdapt= new SimpleCursorAdapter(getActivity(),
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
            listAdapt.changeCursor(myDBOH.getUnSyncedEntrees());
        lst.setAdapter(listAdapt);
            return rootView;
        }
    }

}
