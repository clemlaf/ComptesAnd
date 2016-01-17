package org.clemlaf.comptesand;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

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
        Intent intent=new Intent(this, AddActivity.class);
        startActivity(intent);
    }
    public void tolistContent(View v){
    }
    public void syncContent(View v){
    }
}
