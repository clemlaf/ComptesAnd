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
import android.widget.EditText;
import android.net.*;
import android.util.Base64;
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
import android.util.Log;
import android.util.SparseArray;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import static java.lang.Math.max;
import java.lang.ClassLoader;

public class MainActivity extends FragmentActivity implements DownloadWebpageTask.TaskListener
{
  static final int NUM_ITEMS=2;
  public static String EXTRAID="org.clemlaf.comptesand.SelectedId";
  public static String EXTRA_PAGE="org.clemlaf.comptesand.ActivePage";
  private ViewPager vp;
  private MyAdapter ad;
  private SharedPreferences sharedPref;
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    sharedPref= PreferenceManager.getDefaultSharedPreferences(this);
    populateList();
    if(savedInstanceState!=null){
      vp.setCurrentItem(savedInstanceState.getInt(EXTRA_PAGE,0));
    }
  }
  @Override
  public void onResume(){
    super.onResume();
    //populateList();
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
  @Override
  public void onSaveInstanceState(Bundle outState){
    //super.onSaveInstanceState(outState);
    outState.putInt(EXTRA_PAGE,vp.getCurrentItem());
    Log.e("CLEMLAF","Page "+vp.getCurrentItem());
  }
  private void populateList(){
    this.vp=(ViewPager) findViewById(R.id.my_viewpager);
    this.ad=new MyAdapter(getSupportFragmentManager());
    vp.setAdapter(ad);
  }
  @Override
  public void onFinishSync(){
    //this.ad.notifyDataSetChanged();
    this.ad=new MyAdapter(getSupportFragmentManager());
    vp.setAdapter(ad);
  }
  private void refreshFrag(){
    this.ad.notifyDataSetChanged();
    //this.ad=new MyAdapter(getSupportFragmentManager());
    //vp.setAdapter(ad);
    /*MyDatabaseOpenHelper myDBOH=new MyDatabaseOpenHelper(this);
    for(int i=0;i<NUM_ITEMS;i++){
      MyListFragment frag=(MyListFragment) this.ad.getItem(i);
      /*  Cursor cur;
      int[] info= frag.getInfo();
      if(info[0]==1)
      cur=myDBOH.getSyncedEntrees(info[1],info[2]);
      else
      cur=myDBOH.getUnSyncedEntrees();
      frag.my_set_cursor(cur);
    }*/
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
      boolean askpass=this.sharedPref.getBoolean(getString(R.string.my_pref_passauth),false);
      if(askpass){
        Log.e("CLEMLAF","asking pwd");
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.pwdprompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.my_username_txtent);
        final EditText passInput = (EditText) promptsView.findViewById(R.id.my_password_txtent);
        // set dialog message
        final MainActivity cont=this;
        alertDialogBuilder.setCancelable(true)
        .setPositiveButton(R.string.my_go_button_text,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog,int id) {
            Log.e("CLEMLAF","auth clicked ok");
            String name=userInput.getText().toString();
            String password=passInput.getText().toString();
            String authString = name + ":" + password;
            byte[] authEncBytes = Base64.encode(authString.getBytes(),Base64.DEFAULT);
            String authStringEnc = new String(authEncBytes);
            Log.e("CLEMLAF",authStringEnc);
            String hostn="";
            if(sharedPref.getBoolean(getString(R.string.my_pref_nothome),false)){
              hostn=sharedPref.getString(getString(R.string.my_pref_out_hostname),"");
            }else{
              hostn=sharedPref.getString(getString(R.string.my_pref_home_hostname),"");
            }
            new DownloadWebpageTask(cont,cont).execute(new String[]{
              hostn+getString(R.string.my_update_address),
              hostn+getString(R.string.my_param_address),
              authStringEnc
            });
          }
        });
        AlertDialog ald=alertDialogBuilder.create();
        ald.show();
      }
      else{
        new DownloadWebpageTask(this,this).execute(new String[]{
          hostn+getString(R.string.my_update_address),
          hostn+getString(R.string.my_param_address)
        });
      }
    } else if (hostn.length()==0) {
      Toast.makeText(this,getString(R.string.my_errnohn_toast_text),Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(this,getString(R.string.my_errnoc_toast_text),Toast.LENGTH_SHORT).show();
    }

  }

  private class MyAdapter extends PagerAdapter {
    private final FragmentManager mFragmentManager;
    private SparseArray<Fragment> mFragments;
    private FragmentTransaction mCurTransaction;
    public MyAdapter(FragmentManager fm) {
      //super(fm);
      mFragmentManager=fm;
      mFragments=new SparseArray<Fragment>();
      mFragments.put(0,MyListFragment.newInstance(false,false));
      mFragments.put(1,MyListFragment.newInstance(true,true));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
      Fragment fragment = getItem(position);
      if (mCurTransaction == null) {
        mCurTransaction = mFragmentManager.beginTransaction();
      }
      mCurTransaction.add(container.getId(),fragment,"fragment:"+position);
      return fragment;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      if (mCurTransaction == null) {
        mCurTransaction = mFragmentManager.beginTransaction();
      }
      mCurTransaction.detach(mFragments.get(position));
      mFragments.remove(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object fragment) {
      return ((Fragment) fragment).getView() == view;
    }
    @Override
    public void finishUpdate(ViewGroup container) {
      if (mCurTransaction != null) {
        mCurTransaction.commitAllowingStateLoss();
        mCurTransaction = null;
        mFragmentManager.executePendingTransactions();
      }
    }

    @Override
    public int getCount() {
      return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      if(position==0){
        return getString(R.string.my_unsync_tab_title);
      }else{
        return getString(R.string.my_sync_tab_title);
      }
    }

    public Fragment getItem(int position) {
      return mFragments.get(position) ;
    }
  }
}
