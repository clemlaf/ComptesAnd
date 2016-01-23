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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.database.Cursor;

//public class AddActivity extends AppCompatActivity
public class AddActivity extends Activity
{
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
        findViewByIds();
        setDateTimeField();
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new PlaceholderFragment()).commit();
        }*/
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
	MyDatabaseOpenHelper myDBOH= new MyDatabaseOpenHelper(this);
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

    }

    private void setDateTimeField() {
        //dateETxt.setOnClickListener(this);
        Calendar newCalendar=Calendar.getInstance();
        myDatePickerDialog= new DatePickerDialog(this, new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                dateETxt.setText(dateFormatter.format(newDate.getTime()));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }
    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle app bar item clicks here. The app bar
        // automatically handles clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    public void sendForm(View v){
	MyDatabaseOpenHelper myDBOH= new MyDatabaseOpenHelper(this);
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Send Data");
        String message="";
        int cps;
        int cpd;
        int cat;
        int moy;
        Cursor cc;
        cc=(Cursor) cpsSpin.getSelectedItem();
        cps=(cc==null) ? 0 : cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.ComptesEntry.C_ID));
        cc=(Cursor) cpdSpin.getSelectedItem();
        cpd=(cc==null) ? 0 : cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.ComptesEntry.C_ID));
        cc=(Cursor) catSpin.getSelectedItem();
        cat=(cc==null) ? 0 : cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.CategoryEntry.C_ID));
        cc=(Cursor) moySpin.getSelectedItem();
        moy=(cc==null) ? 0 : cc.getInt(cc.getColumnIndex(MyDatabaseOpenHelper.MoyensEntry.C_ID));
        message=dateETxt.getText()+"\n"+
            String.valueOf(cps)+"\n"+
            String.valueOf(cpd)+"\n"+
            String.valueOf(cat)+"\n"+
            String.valueOf(moy)+"\n"+
            commETxt.getText()+"\n"+
            prixETxt.getText();
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // here you can add functions
            }
        });
        alertDialog.show();
    }
    public void reset(View v){
    }
    public void showDate(View v){
            myDatePickerDialog.show();
    }
}

