package com.example.haceasygo.Model.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.example.haceasygo.R;
import com.google.android.gms.maps.model.LatLng;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DatabaseHelper extends SQLiteAssetHelper {
    private static final int DB_VER = 1;
    private static SQLiteDatabase db;
    private static SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    private Cursor cursor;
    private String _number;
    private String _type;

    /** Constructor
     * @param  context head activity context
     */
    public DatabaseHelper(Context context) {
        super(context, context.getString(R.string.database_name), null, DB_VER);
        db = getReadableDatabase();
        qb.setTables(context.getString(R.string.db_table_name));

    }

    /** Get all the required data from the data base by list
     * @param  selectID id for the requierd cursor
     * @param byNumber number for search sites by this number
     * @param byType type description for search sites by this type
     */
    public List<Sites> getListDB(String selectID, String byNumber, String byType) {
        _number = byNumber;
        _type = byType;
        cursor = MakeCursor(selectID);
        List<Sites> result = new ArrayList<>();

        if(cursor.moveToFirst()) {
            do{
                Sites site = getOperation(selectID);
                if(site != null)
                    result.add(site);
            } while(cursor.moveToNext());
        }

        return result;
    }

    /**
     * Get the next required site by different operations
     * @param s operation description
     */
    private Sites getOperation(String s) {
        switch (s) {
            case "Number":
                return getNumbers();

            case "SiteByWIFI":
            case "SitesByType":
                return getSitesByType();

            case "SitesByNumber":
            case "Sites":
                return getSites();

            default:
                return null;
        }
    }

    /**
     * Get the required sql cursor by codec string
     * @param select codec string for cursor indicator
     */
    private Cursor MakeCursor(String select) {
        String[] sqlSelect;
        switch(select) {
            case "Number":
                sqlSelect = new String[]{"Number"};
                return (qb.query(db, sqlSelect,null,
                        null,null,null,null));

            case "Sites":
                sqlSelect = new String[]{"Type" ,"Number"};
                return (qb.query(db, sqlSelect,null,
                        null,null,null,null));

            case "SitesByNumber":
                sqlSelect = new String[]{"Type" ,"Number"};
                return (qb.query(db, sqlSelect,"Number LIKE ?", new String[]{"%"+_number+"%"},null,null,null));

            case "SitesByType":
                sqlSelect = new String[]{"Type" ,"Number"};
                return (qb.query(db, sqlSelect,"Type LIKE ?", new String[]{"%"+_type+"%"},null,null,null));

            case "SiteByWIFI":
                sqlSelect = new String[]{"Type" ,"Number", "WIFI"};
                return (qb.query(db, sqlSelect,"WIFI LIKE ?",new String[]{"%"+_number+"%"},null,null,null));

            default:
                return null;

        }
    }

    /**
     * Operation get required site by id number
     */
    public Sites getNumbers(){
        Sites site = new Sites();
        site.setNumber(cursor.getString(cursor.getColumnIndex("Number")));
        if (site.getNumber().length() > 1)
            return site;

        return null;
    }

    /**
     * Operation get required site by id number and type
     */
    public Sites getSites(){
        if(cursor.getString(cursor.getColumnIndex("Number")).length() > 1)
            return new Sites(cursor.getString(cursor.getColumnIndex("Type")),
                            cursor.getString(cursor.getColumnIndex("Number")));

       return null;
    }

    /**
     * Operation get required site by type
     */
    public Sites getSitesByType(){
        Sites site = new Sites();
        if(cursor.getString(cursor.getColumnIndex("Type")).length() > 1)
            site = new Sites(cursor.getString(cursor.getColumnIndex("Type")),
                    cursor.getString(cursor.getColumnIndex("Number")));

        return site;
    }

    /**
     * Special case : Operation get list of coordinate of the required sites
     * @param number for search sites by this number
     * @param type type description for search sites by this type
     */
    public List<LatLng> getCoorListDB(String number, String type) {
        String[] sqlSelect = {"Number", "Type","Coor_X1", "Coor_Y1", "Coor_X2", "Coor_Y2","Coor_Num", "Coor_X3", "Coor_Y3", "Coor_X4", "Coor_Y4"};

        if(type.length() == 0)
            cursor= qb.query(db, sqlSelect,"Number LIKE ?",new String[]{number},null,null,null);
        else
            cursor = qb.query(db, sqlSelect,"Type LIKE ?",new String[]{type},null,null,null);

        List<LatLng> result = new ArrayList<>();
        LatLng latLng;
        if(cursor.moveToFirst()) {
            do{
                if(cursor.getString(cursor.getColumnIndex("Number")).equalsIgnoreCase(number)) {
                    int CoorNum = cursor.getInt(cursor.getColumnIndex("Coor_Num"));
                    String t1 = "Coor_X", t2 = "Coor_Y";
                    for(int i=1; i <= CoorNum; i++) {
                        latLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex((t1 + Integer.toString(i))))),
                                Double.parseDouble(cursor.getString(cursor.getColumnIndex((t2 + Integer.toString(i))))));
                    result.add(latLng);
                    }
                }
            } while(cursor.moveToNext());
        }
        return result;
    }

    /**
     * Special case : get specific coordinate of site by his id number
     * @param number for search sites by this number
     */
    public LatLng getLatLng(String number) {
        String[] sqlSelect = {"NUMBER", "Coor_X1", "Coor_Y1"};

        Cursor cursor = qb.query(db, sqlSelect, "NUMBER LIKE ?", new String[]{"%" + number + "%"}, null, null, null);
        cursor.moveToFirst();

        LatLng latLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex("Coor_X1"))),
                Double.parseDouble(cursor.getString(cursor.getColumnIndex("Coor_Y1"))));

        return latLng;
    }

}