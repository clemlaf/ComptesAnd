package org.clemlaf.comptesand;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import static java.lang.Math.max;

public class MyListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
  Activity myActivity;
  MyDatabaseOpenHelper myDBOH;
  ListView lst;
  View rootView;
  public static String EXTRA_SYNC="org.clemlaf.comptesand.SYNC";
  public static String EXTRA_PAGED="org.clemlaf.comptesand.PAGE";
  private int syncpage=0;
  private int synclimit=30;
  private boolean paged=false;
  private boolean synced=false;
  private boolean endsync=false;

  SimpleCursorAdapter listAdapt;

  public static final MyListFragment newInstance(boolean paged,boolean synced){
    MyListFragment f=new MyListFragment();
    final Bundle bdl = new Bundle(2);
    bdl.putBoolean(EXTRA_PAGED, paged);
    bdl.putBoolean(EXTRA_SYNC, synced);
    f.setArguments(bdl);
    return f;
  }
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    paged=getArguments().getBoolean(EXTRA_PAGED);
    synced=getArguments().getBoolean(EXTRA_SYNC);
    synclimit=Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(myActivity).getString(myActivity.getString(R.string.my_pref_nbpage),"30"));
    myDBOH=MyDatabaseOpenHelper.getInstance(myActivity);
    listAdapt= new SimpleCursorAdapter(myActivity,
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
    //my_set_cursor();
  }
  @Override
  public void onAttach(Activity acti){
    super.onAttach(acti);
    myActivity=acti;
  }

  @Override
  public void onResume(){
    super.onResume();
    my_set_cursor();
  }
  @Override
  public View onCreateView(LayoutInflater inflater,
  ViewGroup container, Bundle savedInstanceState) {
    // The last two arguments ensure LayoutParams are inflated
    // properly.
    rootView = inflater.inflate(
    R.layout.synced, container, false);
    ((Button) rootView.findViewById(R.id.my_showprev_button)).setVisibility(Button.GONE);
    ((Button) rootView.findViewById(R.id.my_showprev_button)).setOnClickListener(this);
    ((Button) rootView.findViewById(R.id.my_shownext_button)).setVisibility(Button.GONE);
    ((Button) rootView.findViewById(R.id.my_shownext_button)).setOnClickListener(this);
    lst=(ListView) rootView.findViewById(R.id.my_list_view);
    lst.setAdapter(listAdapt);
    lst.setOnItemClickListener(this);
    return rootView;
  }
  @Override
  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3){
    Cursor cc= (Cursor) lst.getItemAtPosition(position);
    Intent newint= new Intent(lst.getContext(),AddActivity.class);
    newint.putExtra(MainActivity.EXTRAID,cc.getLong(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry._ID)));
    startActivity(newint);
  }
  private void showHideButtons(View v){
    if(paged){
      if(this.syncpage>0)
      ((Button) v.findViewById(R.id.my_showprev_button)).setVisibility(Button.VISIBLE);
      else
      ((Button) v.findViewById(R.id.my_showprev_button)).setVisibility(Button.GONE);
      if(this.endsync)
      ((Button) v.findViewById(R.id.my_shownext_button)).setVisibility(Button.GONE);
      else
      ((Button) v.findViewById(R.id.my_shownext_button)).setVisibility(Button.VISIBLE);
    }else{
      ((Button) v.findViewById(R.id.my_showprev_button)).setVisibility(Button.GONE);
      ((Button) v.findViewById(R.id.my_shownext_button)).setVisibility(Button.GONE);
    }
  }
  @Override
  public void onClick(View v){
    switch(v.getId()){
      case R.id.my_showprev_button:
      this.syncpage=max(this.syncpage-1,0);
      my_set_cursor();
      break;
      case R.id.my_shownext_button:
      if(!this.endsync)
      this.syncpage=this.syncpage+1;
      my_set_cursor();
      break;
    }
  }
  public void my_set_cursor(){
    Cursor cur;
    if(this.synced){
      cur= myDBOH.getSyncedEntrees(this.syncpage,this.synclimit);
    }else{
      cur= myDBOH.getUnSyncedEntrees();
    }
    my_set_cursor(cur);
  }
  public void my_set_cursor(Cursor cur){
      listAdapt.changeCursor(cur);
    if(this.paged){
      if(listAdapt.getCount()>=this.synclimit){
        this.endsync=false;
      }else{
        this.endsync=true;
      }
    }
    showHideButtons(rootView );
    if(lst!=null)
      lst.setAdapter(listAdapt);
  }
  public int[] getInfo(){
    int[] out=new int[4];
    out[0]= this.synced ? 1 : 0;
    out[1]=(int) this.syncpage;
    out[2]=(int) this.synclimit;
    return out;
  }
}
