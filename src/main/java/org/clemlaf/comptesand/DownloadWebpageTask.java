package org.clemlaf.comptesand;

import android.os.AsyncTask;
import android.content.Context;
import java.io.*;
import java.net.*;
//import java.util.Base64;
import java.lang.ref.WeakReference;
import android.net.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.KeyStore;
import javax.net.ssl.*;
import java.util.ArrayList;
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
  private String path;
  private boolean askpass;
  private String authStringEnc;
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
    myDBOH = MyDatabaseOpenHelper.getInstance(cont);
    path=PreferenceManager.getDefaultSharedPreferences(cont).getString(cont.getString(R.string.my_pref_crt_path),"");
    askpass=PreferenceManager.getDefaultSharedPreferences(cont).getBoolean(cont.getString(R.string.my_pref_passauth),false);
  }
  @Override
  protected String doInBackground(String... urls) {
    Cursor cc=myDBOH.getAllEntrees();
    JSONArray entrees=new JSONArray();
    ArrayList<Long> idList=new ArrayList<Long>();
    while(cc.moveToNext()){
      JSONObject ent=new JSONObject();
      // String ent="null";
      try{
        long id=cc.getLong(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry._ID));
        int cps= cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_CPS));
        int cpd= cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_CPD));
        int cat= cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_CAT));
        int moy= cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_MOY));
        int syn= cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_SYN));
        if(syn==0){
          idList.add(id);
          ent.put("id","new");
          ent.put("date",cc.getString(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_DAT)));
          ent.put("cpS_id",cps==0 ? "" : ""+cps);
          ent.put("cpD_id",cpd==0 ? "" : ""+cpd);
          ent.put("category_id", cat==0 ? "" : ""+cat);
          ent.put("moyen_id", moy==0 ? "" : ""+moy);
          ent.put("com",cc.getString(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_COM)));
          ent.put("pr",String.format("%.2f",cc.getFloat(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_PRI))));
          ent.put("poS","false");
          ent.put("poD","false");
          /*ent="id=new&date="+
          cc.getString(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_DAT))+
          "&cp_s="+(cps==0?"":cps)+
          "&cp_d="+(cpd==0?"":cpd)+
          "&cat="+ (cat==0?"":cat)+
          "&com="+
          URLEncoder.encode(
          cc.getString(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_COM)), "UTF-8")+
          "&pr="+
          cc.getFloat(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_PRI))+
          "&pt=false&moy="+(moy==0?"":moy);*/
          entrees.put(ent);
        }
      } catch (Exception e){
        Log.e("CLEMLAF",ent.toString());
      }
    }
    cc.close();
    String getUrl="";
    if(urls.length > 2)
      authStringEnc=urls[2];
    try{
      JSONObject pack=new JSONObject();
      pack.put("entries", entrees);
      getUrl=downloadUrl(urls[0],pack);
      if (getUrl.substring(0,3).equals("200")){
        for(long aid : idList) {
          myDBOH.putSync(aid);
        }
        getUrl=getUrl.substring(3);
      }
    }
    catch (JSONException e) {
      Log.e("CLEMLAF","error building json");
      getUrl= "Unable to build up JSON request.";
    }
    catch (IOException e) {
      Log.e("CLEMLAF",urls[0]);
      getUrl= "Unable to retrieve web page. URL may be invalid.";
    }
    // params comes from the execute() call: params[0] is the url.
      authStringEnc=null;
    return getUrl;
  }
  // onPostExecute displays the results of the AsyncTask.
  @Override
  protected void onPostExecute(String result) {
    try {
      JSONObject jd=new JSONObject(result);
      myDBOH = MyDatabaseOpenHelper.getInstance(cont);
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
  private String downloadUrl(String myurl, JSONObject params) throws IOException {
    InputStream is = null;
    String contentAsString="";

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
      Log.e("CLEMLAF",params.toString());
      //Log.e("CLEMLAF",URLEncoder.encode(params.toString()));
      URL url = new URL(myurl);
      HttpURLConnection conn=null;
      // handling the case with ssl connection
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
      conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
      if(askpass){
        conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
        Log.e("CLEMLAF", authStringEnc);
      }
      conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
      conn.setFixedLengthStreamingMode(params.toString().getBytes("utf-8").length);
      //Send request
      DataOutputStream wr = new DataOutputStream (
      conn.getOutputStream ());
      wr.write (params.toString().getBytes("utf-8"));
      wr.flush ();
      wr.close ();
      // Starts the query
      //conn.connect();
      int response = conn.getResponseCode();
      contentAsString=""+response;
      Log.e("CLEMLAF",""+response);
      //Log.d(DEBUG_TAG, "The response is: " + response);
      is = conn.getInputStream();
        // Convert the InputStream into a string
      if(response==200)
        contentAsString += readIt(is);

      // Makes sure that the InputStream is closed after the app is
      // finished using it.
    } catch (Exception e){
      Log.e("CLEMLAF","erreur Connection");
      Log.e("CLEMLAF", e.toString());
      contentAsString="erreur";
    }
    finally {
      if (is != null) {
        is.close();
      }
      return contentAsString;
    }
  }
  // Reads an InputStream and converts it to a String.
  private String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
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
