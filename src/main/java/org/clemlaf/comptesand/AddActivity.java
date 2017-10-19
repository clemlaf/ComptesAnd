package org.clemlaf.comptesand;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
//import android.view.View.OnClickListener;
import android.widget.Toast;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
import android.content.Intent;
import android.widget.EditText;
//import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.database.Cursor;
import android.util.Log;

//public class AddActivity extends AppCompatActivity
public class AddActivity extends Activity
{
    private long curId=-1;
    private int isSynced=0;
    private EditText dateETxt;
    private EditText commETxt;
    private EditText prixETxt;
    private Spinner cpsSpin;
    private Spinner cpdSpin;
    private Spinner catSpin;
    private Spinner moySpin;
    private DatePickerDialog myDatePickerDialog;
    private SimpleDateFormat dateFormatter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);
        dateFormatter= new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        Intent myIntent=getIntent();
        if (myIntent.hasExtra(MainActivity.EXTRAID)){
            curId=myIntent.getLongExtra(MainActivity.EXTRAID,-1);
        }
        findViewByIds();
	setDateTimeField();
	showHiddenButtons();
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new PlaceholderFragment()).commit();
        }*/
    }
    private void showHiddenButtons(){
        Button savbut=(Button) findViewById(R.id.my_send_button);
        Button delbut=(Button) findViewById(R.id.my_delete_button);
        Button newbut=(Button) findViewById(R.id.my_dupe_button);
        Button todbut=(Button) findViewById(R.id.my_today_button);
        if(curId>=0){
            newbut.setVisibility(Button.VISIBLE);
            delbut.setVisibility(Button.VISIBLE);
        }else{
            newbut.setVisibility(Button.GONE);
            delbut.setVisibility(Button.GONE);
        }
        if(isSynced>0){
            dateETxt.setEnabled(false);
            commETxt.setEnabled(false);
            prixETxt.setEnabled(false);
            cpsSpin.setEnabled (false);
            cpdSpin.setEnabled (false);
            catSpin.setEnabled (false);
            moySpin.setEnabled (false);
            savbut.setVisibility(Button.GONE);
            todbut.setVisibility(Button.GONE);
        }else{
            dateETxt.setEnabled(true);
            commETxt.setEnabled(true);
            prixETxt.setEnabled(true);
            cpsSpin.setEnabled (true);
            cpdSpin.setEnabled (true);
            catSpin.setEnabled (true);
            moySpin.setEnabled (true);
            savbut.setVisibility(Button.VISIBLE);
            todbut.setVisibility(Button.VISIBLE);
            Calendar now = Calendar.getInstance();
            long diff= now.getTimeInMillis();
            String dateString=dateETxt.getText().toString();
            if(dateString!=null && dateString.length() > 0){
              try{
                diff-=dateFormatter.parse(dateString).getTime();
                Log.e("CLEMLAF","diff "+diff);
                if (diff<0 || diff > 3600*1000*24)
                todbut.setEnabled(true);
                else
                todbut.setEnabled(false);
              }catch (Exception e){
                Log.e("CLEMLAF","erreur Date");
              }
            }
          }
        }
        private void findViewByIds(){
          dateETxt= (EditText) findViewById(R.id.my_date_txtent);
          dateETxt.setInputType(InputType.TYPE_NULL);
          commETxt=(EditText) findViewById(R.id.my_comm_txtent);
          prixETxt=(EditText) findViewById(R.id.my_prix_txtent);
          cpsSpin=(Spinner) findViewById(R.id.my_cps_spin);
          cpdSpin=(Spinner) findViewById(R.id.my_cpd_spin);
          catSpin=(Spinner) findViewById(R.id.my_cat_spin);
          moySpin=(Spinner) findViewById(R.id.my_moy_spin);
          MyDatabaseOpenHelper myDBOH= MyDatabaseOpenHelper.getInstance(this);
          SimpleCursorAdapter cpsAdapt= new SimpleCursorAdapter(this,
            android.R.layout.simple_spinner_item,
            myDBOH.getAllComptes(),
            new String[] { MyDatabaseOpenHelper.ComptesEntry.C_NAME },
            new int[] { android.R.id.text1}	);
          cpsAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          cpsSpin.setAdapter(cpsAdapt);
          SimpleCursorAdapter cpdAdapt= new SimpleCursorAdapter(this,
            android.R.layout.simple_spinner_item,
            myDBOH.getAllComptes(),
            new String[] { MyDatabaseOpenHelper.ComptesEntry.C_NAME },
            new int[] { android.R.id.text1}	);
          cpdAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          cpdSpin.setAdapter(cpdAdapt);
          SimpleCursorAdapter catAdapt= new SimpleCursorAdapter(this,
            android.R.layout.simple_spinner_item,
            myDBOH.getAllCategories(),
            new String[] { MyDatabaseOpenHelper.CategoryEntry.C_NAME },
            new int[] { android.R.id.text1}	);
          catAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          catSpin.setAdapter(catAdapt);
          SimpleCursorAdapter moyAdapt= new SimpleCursorAdapter(this,
            android.R.layout.simple_spinner_item,
            myDBOH.getAllMoyens(),
            new String[] { MyDatabaseOpenHelper.MoyensEntry.C_NAME },
            new int[] { android.R.id.text1}	);
          moyAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          moySpin.setAdapter(moyAdapt);
          /*SimpleCursorAdapter commAdapt= new SimpleCursorAdapter(this,
          android.R.layout.simple_dropdown_item_1line,
          myDBOH.getCompletions(),
          new String[] { MyDatabaseOpenHelper.EntreesEntry.C_COM },
          new int[] { android.R.id.text1}	);
          commETxt.setAdapter(commAdapt);
          commAdapt.setCursorToStringConverter(new CursorToStringConverter() {
          public String convertToString(Cursor cursor) {
          // Get the label for this row out of the "state" column
          final int columnIndex = cursor.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_COM);
          final String str = cursor.getString(columnIndex);
          return str;
        }
      });*/
      if (curId>0){
        Cursor cc=myDBOH.getEntree(curId);
        cc.moveToFirst();
        dateETxt.setText(cc.getString(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_DAT)));
        commETxt.setText(cc.getString(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_COM)));
        prixETxt.setText(cc.getString(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_PRI)));
        Spinner st[]={cpsSpin, cpdSpin, catSpin, moySpin};
        SimpleCursorAdapter at[]={cpsAdapt, cpdAdapt, catAdapt, moyAdapt};
        int valt[]={
          cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_CPS)),
          cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_CPD)),
          cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_CAT)),
          cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_MOY))};
          String ct[]={MyDatabaseOpenHelper.ComptesEntry.C_ID,
            MyDatabaseOpenHelper.ComptesEntry.C_ID,
            MyDatabaseOpenHelper.CategoryEntry.C_ID,
            MyDatabaseOpenHelper.MoyensEntry.C_ID,
          };
          for (int i=0; i< st.length; i++){
            for (int j=0; j<at[i].getCount(); j++){
              Cursor ca=(Cursor) at[i].getItem(j);
              if(ca.getInt(ca.getColumnIndex(ct[i])) == valt[i]){
                st[i].setSelection(j);
                break;
              }
            }
          }
          isSynced=cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.EntreesEntry.C_SYN));
        }

      }

      private void setDateTimeField() {
        //dateETxt.setOnClickListener(this);
        Calendar newCalendar=Calendar.getInstance();
        String dateString=dateETxt.getText().toString();
        if(dateString==null){
          dateETxt.setText(dateFormatter.format(newCalendar.getTime()));
          dateString=dateETxt.getText().toString();
        }
        if(dateString!=null)
        try{
          newCalendar.setTime(dateFormatter.parse(dateString));
        }catch (Exception e){
          Log.e("CLEMLAF","erreur Date");
        }
        myDatePickerDialog= new DatePickerDialog(this, new OnDateSetListener() {
          public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            dateETxt.setText(dateFormatter.format(newDate.getTime()));
            showHiddenButtons();
          }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
      }

      public void sendForm(View v){
        MyDatabaseOpenHelper myDBOH= MyDatabaseOpenHelper.getInstance(this);
        int cps;
        int cpd;
        int cat;
        int moy;
        long res =0;
        Cursor cc;
        cc=(Cursor) cpsSpin.getSelectedItem();
        cps=(cc==null) ? 0 : cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.ComptesEntry.C_ID));
        cc=(Cursor) cpdSpin.getSelectedItem();
        cpd=(cc==null) ? 0 : cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.ComptesEntry.C_ID));
        cc=(Cursor) catSpin.getSelectedItem();
        cat=(cc==null) ? 0 : cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.CategoryEntry.C_ID));
        cc=(Cursor) moySpin.getSelectedItem();
        moy=(cc==null) ? 0 : cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.MoyensEntry.C_ID));
        if (cps>0 && (prixETxt.getText()).length()>0)
          res = myDBOH.addEntree(curId, dateETxt.getText().toString(),
            cps, cpd, cat, moy,
            commETxt.getText().toString(),
            Float.parseFloat(prixETxt.getText().toString()));
        if(curId<0)
          curId=res;
        showHiddenButtons();
        Toast.makeText(this,getString(R.string.my_saved_toast_text),Toast.LENGTH_SHORT).show();
      }
      public void deleteForm(View v){
        if(curId>=0){
          MyDatabaseOpenHelper myDBOH = MyDatabaseOpenHelper.getInstance(this);
          myDBOH.deleteEntree(curId);
          Toast.makeText(this,getString(R.string.my_deleted_toast_text),Toast.LENGTH_SHORT).show();
          finish();
        }
      }
      public void newForm(View v){
        curId=-1;
        isSynced=0;
        showHiddenButtons();
      }
      public void reset(View v){
      }
      public void showDate(View v){
        myDatePickerDialog.show();
      }
      public void setToday(View v){
        Calendar newDate = Calendar.getInstance();
        dateETxt.setText(dateFormatter.format(newDate.getTime()));
        setDateTimeField();
        showHiddenButtons();
      }
    }
