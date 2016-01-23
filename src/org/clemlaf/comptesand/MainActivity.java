package org.clemlaf.comptesand;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
        MyDatabaseOpenHelper myDBOH = new MyDatabaseOpenHelper(this);
        myDBOH.emptyParams();
        myDBOH.fillTest();
	Cursor cur=myDBOH.getAllComptes();
	cur.moveToFirst();
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Send Data");
        alertDialog.setMessage(cur.getString(2));
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("https://macbook/comptes/update");
                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("id", "new"));
                    nameValuePairs.add(new BasicNameValuePair("cp_s", cps));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
                // here you can add functions
            }
        });
        alertDialog.show();
    }
}
