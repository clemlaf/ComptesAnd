package org.clemlaf.comptesand;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;
import android.provider.BaseColumns;

public class MyDatabaseOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "ComptesAnd.db";

    public static abstract class CategoryEntry implements BaseColumns{
        public static final String TABLE_NAME = "category";
        public static final String C_ID = "c_id";
        public static final String C_NAME="c_nam";
    }
    private static final String CATEGORY_TABLE_CREATE =
        "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
        CategoryEntry._ID + " INTEGER PRIMARY KEY," +
        CategoryEntry.C_ID + " INTEGER, " 
        + CategoryEntry.C_NAME + " TEXT);";
    private static final String CATEGORY_TABLE_DELETE =
        "DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME;

    public static abstract class ComptesEntry implements BaseColumns{
        public static final String TABLE_NAME = "comptes";
        public static final String C_ID = "cp_id";
        public static final String C_NAME="cp_nam";
    }
    private static final String COMPTES_TABLE_CREATE =
        "CREATE TABLE " + ComptesEntry.TABLE_NAME + " (" +
        ComptesEntry._ID + " INTEGER PRIMARY KEY," +
        ComptesEntry.C_ID + " INTEGER, " 
        + ComptesEntry.C_NAME + " TEXT);";
    private static final String COMPTES_TABLE_DELETE =
        "DROP TABLE IF EXISTS " + ComptesEntry.TABLE_NAME;

    public static abstract class MoyensEntry implements BaseColumns{
        public static final String TABLE_NAME = "moyens";
        public static final String C_ID = "m_id";
        public static final String C_NAME="m_nam";
    }
    private static final String MOYENS_TABLE_CREATE =
        "CREATE TABLE " + MoyensEntry.TABLE_NAME + " (" +
        MoyensEntry._ID + " INTEGER PRIMARY KEY," +
        MoyensEntry.C_ID + " INTEGER, " 
        + MoyensEntry.C_NAME + " TEXT);";
    private static final String MOYENS_TABLE_DELETE =
        "DROP TABLE IF EXISTS " + MoyensEntry.TABLE_NAME;
    
    public static abstract class EntreesEntry implements BaseColumns{
        public static final String TABLE_NAME = "entrees";
        public static final String C_DAT = "dat_id";
        public static final String C_CPS = "cps_id";
        public static final String C_CPD = "cpd_id";
        public static final String C_CAT="cat_id";
        public static final String C_MOY="moy_id";
        public static final String C_COM="com";
        public static final String C_PRI="prix";
        public static final String C_SYN="sync";
    }
    private static final String ENTREES_TABLE_CREATE =
        "CREATE TABLE " + EntreesEntry.TABLE_NAME + " (" +
        EntreesEntry._ID + " INTEGER PRIMARY KEY," +
        EntreesEntry.C_DAT + " TEXT, " +
        EntreesEntry.C_CPS + " INTEGER, " +
        EntreesEntry.C_CPD + " INTEGER, " +
        EntreesEntry.C_CAT + " INTEGER, " +
        EntreesEntry.C_MOY + " INTEGER, " +
        EntreesEntry.C_COM + " TEXT, " +
        EntreesEntry.C_PRI + " REAL, " +
        EntreesEntry.C_SYN + " INTEGER " +
        ");";
    private static final String ENTREES_TABLE_DELETE =
        "DROP TABLE IF EXISTS " + EntreesEntry.TABLE_NAME;
    
    public MyDatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CATEGORY_TABLE_CREATE);
        db.execSQL(COMPTES_TABLE_CREATE);
        db.execSQL(MOYENS_TABLE_CREATE);
        db.execSQL(ENTREES_TABLE_CREATE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL(CATEGORY_TABLE_DELETE);
        db.execSQL(COMPTES_TABLE_DELETE);
        db.execSQL(MOYENS_TABLE_DELETE);
        db.execSQL(ENTREES_TABLE_DELETE);
        onCreate(db);
    }

    public Cursor getAllCategories(){
        SQLiteDatabase db=this.getReadableDatabase();
        return db.query(CategoryEntry.TABLE_NAME, new String[]{CategoryEntry._ID, CategoryEntry.C_ID, CategoryEntry.C_NAME}, null, null, null, null, null);
    }
    public Cursor getAllComptes(){
        SQLiteDatabase db=this.getReadableDatabase();
        return db.query(ComptesEntry.TABLE_NAME, new String[]{ComptesEntry._ID, ComptesEntry.C_ID, ComptesEntry.C_NAME}, null, null, null, null, null);
    }
    public Cursor getAllMoyens(){
        SQLiteDatabase db=this.getReadableDatabase();
        return db.query(MoyensEntry.TABLE_NAME, new String[]{MoyensEntry._ID, MoyensEntry.C_ID, MoyensEntry.C_NAME}, null, null, null, null, null);
    }
    public Cursor getAllEntrees(){
        SQLiteDatabase db=this.getReadableDatabase();
        String tablename= EntreesEntry.TABLE_NAME +
            " e INNER JOIN " + ComptesEntry.TABLE_NAME + " s ON e." +
            EntreesEntry.C_CPS + " = s." +ComptesEntry.C_ID + 
            " LEFT OUTER JOIN " + ComptesEntry.TABLE_NAME + " d ON e." +
            EntreesEntry.C_CPD + " = d." +ComptesEntry.C_ID + 
            " LEFT OUTER JOIN " + CategoryEntry.TABLE_NAME + " c ON e." +
            EntreesEntry.C_CAT + " = c." +CategoryEntry.C_ID + 
            " LEFT OUTER JOIN " + MoyensEntry.TABLE_NAME + " m ON e." +
            EntreesEntry.C_MOY + " = m." +MoyensEntry.C_ID ; 
        return db.query(tablename,
                new String[]{"e." + EntreesEntry._ID,
                    EntreesEntry.C_DAT,
                    "s." + ComptesEntry.C_NAME + " s_" + ComptesEntry.C_NAME,
                    EntreesEntry.C_CPS,
                    "d." + ComptesEntry.C_NAME + " d_" + ComptesEntry.C_NAME,
                    EntreesEntry.C_CPD,
                    "c." + CategoryEntry.C_NAME,
                    EntreesEntry.C_CAT,
                    "m." + MoyensEntry.C_NAME,
                    EntreesEntry.C_MOY,
                    EntreesEntry.C_COM,
                    EntreesEntry.C_PRI,
                EntreesEntry.C_SYN},
                    null, null, null, null, null);
    }
    public Cursor getUnSyncedEntrees(){
        SQLiteDatabase db=this.getReadableDatabase();
        String tablename= EntreesEntry.TABLE_NAME +
            " e INNER JOIN " + ComptesEntry.TABLE_NAME + " s ON e." +
            EntreesEntry.C_CPS + " = s." +ComptesEntry.C_ID + 
            " LEFT OUTER JOIN " + ComptesEntry.TABLE_NAME + " d ON e." +
            EntreesEntry.C_CPD + " = d." +ComptesEntry.C_ID + 
            " LEFT OUTER JOIN " + CategoryEntry.TABLE_NAME + " c ON e." +
            EntreesEntry.C_CAT + " = c." +CategoryEntry.C_ID + 
            " LEFT OUTER JOIN " + MoyensEntry.TABLE_NAME + " m ON e." +
            EntreesEntry.C_MOY + " = m." +MoyensEntry.C_ID ; 
        String selection= EntreesEntry.C_SYN + "=0";
        return db.query(tablename,
                new String[]{"e." + EntreesEntry._ID,
                    EntreesEntry.C_DAT,
                    "s." + ComptesEntry.C_NAME + " s_" + ComptesEntry.C_NAME,
                    EntreesEntry.C_CPS,
                    "d." + ComptesEntry.C_NAME + " d_" + ComptesEntry.C_NAME,
                    EntreesEntry.C_CPD,
                    "c." + CategoryEntry.C_NAME,
                    EntreesEntry.C_CAT,
                    "m." + MoyensEntry.C_NAME,
                    EntreesEntry.C_MOY,
                    EntreesEntry.C_COM,
                    EntreesEntry.C_PRI,
                EntreesEntry.C_SYN},
                    selection, null, null, null, null);
    }
    public Cursor getSyncedEntrees(int page, int limit){
        SQLiteDatabase db=this.getReadableDatabase();
        String tablename= EntreesEntry.TABLE_NAME +
            " e INNER JOIN " + ComptesEntry.TABLE_NAME + " s ON e." +
            EntreesEntry.C_CPS + " = s." +ComptesEntry.C_ID + 
            " LEFT OUTER JOIN " + ComptesEntry.TABLE_NAME + " d ON e." +
            EntreesEntry.C_CPD + " = d." +ComptesEntry.C_ID + 
            " LEFT OUTER JOIN " + CategoryEntry.TABLE_NAME + " c ON e." +
            EntreesEntry.C_CAT + " = c." +CategoryEntry.C_ID + 
            " LEFT OUTER JOIN " + MoyensEntry.TABLE_NAME + " m ON e." +
            EntreesEntry.C_MOY + " = m." +MoyensEntry.C_ID ; 
        String selection= EntreesEntry.C_SYN + "=1";
	String limits= ""+(page*limit)+", "+limit;
        return db.query(tablename,
                new String[]{"e." + EntreesEntry._ID,
                    EntreesEntry.C_DAT,
                    "s." + ComptesEntry.C_NAME + " s_" + ComptesEntry.C_NAME,
                    EntreesEntry.C_CPS,
                    "d." + ComptesEntry.C_NAME + " d_" + ComptesEntry.C_NAME,
                    EntreesEntry.C_CPD,
                    "c." + CategoryEntry.C_NAME,
                    EntreesEntry.C_CAT,
                    "m." + MoyensEntry.C_NAME,
                    EntreesEntry.C_MOY,
                    EntreesEntry.C_COM,
                    EntreesEntry.C_PRI,
                EntreesEntry.C_SYN},
                    selection, null, null, null,null, limits);
    }
    public Cursor getCompletions(){
        SQLiteDatabase db=this.getReadableDatabase();
        return db.query( EntreesEntry.TABLE_NAME, new String[]{EntreesEntry._ID,EntreesEntry.C_COM}, null, null, null, null, null);
    }
    public long addEntree(long curId, String dat, int cps, int cpd, int cat, int moy, String com, float pri){
        SQLiteDatabase db=this.getWritableDatabase();
        String selection= EntreesEntry._ID + " LIKE ?";
        ContentValues values=new ContentValues();
        values.put(EntreesEntry.C_DAT, dat);
        values.put(EntreesEntry.C_CPS, cps);
        values.put(EntreesEntry.C_CPD, cpd);
        values.put(EntreesEntry.C_CAT, cat);
        values.put(EntreesEntry.C_MOY, moy);
        values.put(EntreesEntry.C_COM, com);
        values.put(EntreesEntry.C_PRI, pri);
        values.put(EntreesEntry.C_SYN, 0);
        if ( curId<0)
            return db.insert(EntreesEntry.TABLE_NAME,null,values);
        else {
            return (long)db.update(EntreesEntry.TABLE_NAME,values,selection,new String[]{String.valueOf(curId)});
        }
    }

    public Cursor getEntree(long id){
        SQLiteDatabase db=this.getReadableDatabase();
        String selection= EntreesEntry._ID + " LIKE ?";
        return db.query(EntreesEntry.TABLE_NAME,
                new String[]{EntreesEntry._ID,
                    EntreesEntry.C_DAT,
                    EntreesEntry.C_CPS,
                    EntreesEntry.C_CPD,
                    EntreesEntry.C_CAT,
                    EntreesEntry.C_MOY,
                    EntreesEntry.C_COM,
                    EntreesEntry.C_PRI,
                    EntreesEntry.C_SYN
                }, selection, new String[]{ String.valueOf(id)},
                    null, null, null);
    }
    public int putSync(long id){
        SQLiteDatabase db=this.getWritableDatabase();
        String selection= EntreesEntry._ID + " LIKE ?";
        ContentValues values=new ContentValues();
        values.put(EntreesEntry.C_SYN, 1);
        return db.update(EntreesEntry.TABLE_NAME,values,selection,new String[]{String.valueOf(id)});
    }
    public int deleteEntree(long id){
        SQLiteDatabase db=this.getWritableDatabase();
        String selection= EntreesEntry._ID + " LIKE ?";
        return db.delete(EntreesEntry.TABLE_NAME, selection, new String[]{ String.valueOf(id)});
    }

    public long addParam(String tableName, int id, String name){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        if (tableName.equals(ComptesEntry.TABLE_NAME)){
            values.put(ComptesEntry.C_ID, id);
            values.put(ComptesEntry.C_NAME, name);
        }
        else if (tableName.equals(CategoryEntry.TABLE_NAME)){
            values.put(CategoryEntry.C_ID, id);
            values.put(CategoryEntry.C_NAME, name);
        }
        else if (tableName.equals(MoyensEntry.TABLE_NAME)){
            values.put(MoyensEntry.C_ID, id);
            values.put(MoyensEntry.C_NAME, name);
        } else {
            return -1;
        }
        return db.insert(tableName,null,values);
    }
    public void addEmptyParams(){
        String tns[]={ ComptesEntry.TABLE_NAME, CategoryEntry.TABLE_NAME, MoyensEntry.TABLE_NAME};
        for(int i=0;i<tns.length; i++){
            addParam(tns[i],0,"------");
        }
    }

    public int emptyCategories(){
        SQLiteDatabase db=this.getWritableDatabase();
        return db.delete(CategoryEntry.TABLE_NAME, null, null);
    }
    public int emptyComptes(){
        SQLiteDatabase db=this.getWritableDatabase();
        return db.delete(ComptesEntry.TABLE_NAME, null, null);
    }
    public int emptyMoyens(){
        SQLiteDatabase db=this.getWritableDatabase();
        return db.delete(MoyensEntry.TABLE_NAME, null, null);
    }
    public void emptyParams(){
        emptyCategories();
        emptyComptes();
        emptyMoyens();
        addEmptyParams();
    }
    public void fillTest(){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put(CategoryEntry.C_ID,1);
        values.put(CategoryEntry.C_NAME,"test_cat1");
        db.insert(CategoryEntry.TABLE_NAME,null, values);

        values=new ContentValues();
        values.put(ComptesEntry.C_ID,1);
        values.put(ComptesEntry.C_NAME,"test_cp1");
        db.insert(ComptesEntry.TABLE_NAME,null, values);

        values=new ContentValues();
        values.put(ComptesEntry.C_ID,2);
        values.put(ComptesEntry.C_NAME,"test_cp2");
        db.insert(ComptesEntry.TABLE_NAME,null, values);

        values=new ContentValues();
        values.put(MoyensEntry.C_ID,2);
        values.put(MoyensEntry.C_NAME,"test_moy2");
        db.insert(MoyensEntry.TABLE_NAME,null, values);
    }

    public void closeDB(){
        SQLiteDatabase db=this.getReadableDatabase();
        if (db!=null && db.isOpen())
            db.close();
    }
}
