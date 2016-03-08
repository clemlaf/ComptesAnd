package org.clemlaf.comptesand;

import android.os.AsyncTask;
import android.content.Context;
import java.io.*;
import java.net.*;
import java.lang.ref.WeakReference;
import android.net.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.KeyStore;
import javax.net.ssl.*;
import org.json.*;
import android.util.Log;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.Toast;

public class DownloadWebpageTask extends AsyncTask<String, Void, String> {
  private int nid=1;
  private MyDatabaseOpenHelper myDBOH;
  private Context cont;
  String path;
  public interface TaskListener{
    void onFinishSync();
  }
  private final WeakReference<TaskListener> mTLref;
  public void notifyFinishSync(){
    TaskListener listener=mTLref.get();
    if(listener !=null){
      listener.onFinishSync();
    }
  }
  public DownloadWebpageTask(MainActivity acti, TaskListener listener){
    this.cont=acti;
    this.mTLref= new WeakReference<TaskListener>(listener);
  }
  @Override
  protected void onPreExecute(){
    NotificationCompat.Builder myNotBuilder= new NotificationCompat.Builder(cont)
    .setContentTitle(cont.getString(R.string.my_not_title))
    .setContentText(cont.getString(R.string.my_not_text))
    .setSmallIcon(android.R.drawable.ic_popup_sync);
    NotificationManagerCompat myNot=NotificationManagerCompat.from(cont);
    myNot.notify(this.nid,myNotBuilder.build());
    myDBOH = new MyDatabaseOpenHelper(cont);
    path=PreferenceManager.getDefaultSharedPreferences(cont).getString(cont.getString(R.string.my_pref_crt_path),"");
  }
  @Override
  protected String doInBackground(String... urls) {
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
    String getUrl="";
    try {

      Log.e("CLEMLAF",urls[0]);
      getUrl=downloadUrl(urls[1],"");
    } catch (IOException e) {
      Log.e("CLEMLAF",urls[1]);
      return "Unable to retrieve web page. URL may be invalid.";
    }
    return getUrl;
  }
  // onPostExecute displays the results of the AsyncTask.
  @Override
  protected void onPostExecute(String result) {
    try {
      JSONObject jd=new JSONObject(result);
      myDBOH = new MyDatabaseOpenHelper(cont);
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
      Toast.makeText(cont,cont.getString(R.string.my_synced_toast_text),Toast.LENGTH_SHORT).show();
      notifyFinishSync();
    }
    catch (JSONException e){
      Log.e("CLEMLAF",result);
      Toast.makeText(cont,cont.getString(R.string.my_errj_toast_text),Toast.LENGTH_SHORT).show();
    }finally{
      NotificationManagerCompat myNot=NotificationManagerCompat.from(cont);
      myNot.cancel(this.nid);
    }
  }
  private String downloadUrl(String myurl, String params) throws IOException {
    InputStream is = null;

    try {

      // Load CAs from an InputStream
      // (could be from a resource or ByteArrayInputStream or ...)
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
