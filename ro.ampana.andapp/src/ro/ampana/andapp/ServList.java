/**
 * Application name : Recipes App
 * Author			: Taufan Erfiyanto
 * Date				: March 2012
 */
package ro.ampana.andapp;

import java.io.IOException;
import java.util.ArrayList;

import ro.ampana.andapp.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ServList extends Activity {
	
	ImageView imgAbout, imgSearchNav;
	Button btnSearch;
	EditText edtSearch;
	LinearLayout lytSearchForm;
	ListView listRecipes;
	ProgressBar prgLoading;
	
	String RecipeNameKeyword = "";
	
	static DBServ dbhelper;
	ArrayList<ArrayList<Object>> data;
	ListAdapter la;
	
	static Long[] id;
	static String[] ServName;
	static String[] Descriere;
	static String[] Telefon;
	
	
	/** This class is used to create custom listview */
	static class ListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private Context ctx;
		
		public ListAdapter(Context context) {
			inflater = LayoutInflater.from(context);
			ctx = context;
		}
		
		public int getCount() {
			// TODO Auto-generated method stub
			return ServName.length;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			
			if(convertView == null){
				convertView = inflater.inflate(R.layout.row, null);
				holder = new ViewHolder();
				holder.txtRecipeName = (TextView) convertView.findViewById(R.id.txtRecipeName);
				holder.txtReadyIn = (TextView) convertView.findViewById(R.id.txtReadyIn);
				holder.imgPreview = (ImageView) convertView.findViewById(R.id.imgPreview);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			
			holder.txtRecipeName.setText(ServName[position]);
			holder.txtReadyIn.setText("Cook time "+Telefon[position]);
//			int imagePreview = ctx.getResources().getIdentifier(Description[position], "drawable", ctx.getPackageName());
//			holder.imgPreview.setImageResource(imagePreview);
			
			
			return convertView;
		}
		
		static class ViewHolder {
			TextView txtRecipeName, txtReadyIn;
			ImageView imgPreview;
		}
		
	}

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipes_list);
        

        la = new ListAdapter(this);
        
        imgAbout = (ImageView) findViewById(R.id.imgAbout);
        imgSearchNav = (ImageView) findViewById(R.id.imgSearchNav);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        edtSearch = (EditText) findViewById(R.id.edtSearch);
        lytSearchForm = (LinearLayout) findViewById(R.id.lytSearchForm);
        listRecipes = (ListView) findViewById(R.id.listRecipes);
        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
        
        /**
         * when this app's installed at the first time, code below will
         * copy database stored in assets to
         * /data/data/com.recipes.app/databases/
         */
        try {
            dbhelper = new DBServ(this);
			dbhelper.mDatabaseOpenHelper.createDataBase();
		}catch(SQLException sqle){
			throw new Error("Unable to create database");
		}
		
        /** then, the database will be open to use */
		try{
			dbhelper.mDatabaseOpenHelper.openDataBase();
		}catch(SQLException sqle){
			throw sqle;
		}
		
		new getDataTask().execute();
		
		listRecipes.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				
				/**
				 * when one of item in the list is clicked, this app will access 
				 * RecipeDetail.class. it also send id value to that class
				 */
				Intent i = new Intent(ServList.this, RecipeDetail.class);
				i.putExtra("id_for_detail", id[position]);
				startActivity(i);
			}
		});
        
        
        imgSearchNav.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				/** this code is used to hide and show the search form */
				if(lytSearchForm.getVisibility() == 8){
					lytSearchForm.setVisibility(0);
					imgSearchNav.setImageResource(R.drawable.nav_down);
				}else{
					lytSearchForm.setVisibility(8);
					imgSearchNav.setImageResource(R.drawable.nav_up);
				}
			}
		});
        
        btnSearch.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				RecipeNameKeyword = edtSearch.getText().toString();
//				try{
//					dbhelper.openDataBase();
//				}catch(SQLException sqle){
//					throw sqle;
//				}
//				new getDataTask().execute();
			}
		});
        
        imgAbout.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				/** when about icon is clicked, it will access AboutApp.class */
				Intent i = new Intent(ServList.this, AboutApp.class);
				startActivity(i);
			}
		});
    }
    
    /** this class is used to handle thread */
    public class getDataTask extends AsyncTask<Void, Void, Void>{
    	
    	
    	@Override
		 protected void onPreExecute() {
		  // TODO Auto-generated method stub
    		
    	}
    	
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			getDataFromDatabase(RecipeNameKeyword);
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			//dialog.dismiss();
			prgLoading.setVisibility(8);
			listRecipes.setVisibility(0);
	        listRecipes.setAdapter(la);
//	    	dbhelper.close();
			
		}
    }
    
    /**
     * this code is used to get data from database and store them
     * to array attributes
     */
    public void getDataFromDatabase(String RecipeNameKeyword){
    	data = dbhelper.mDatabaseOpenHelper.getAllData(RecipeNameKeyword);
    	
    	id = new Long[data.size()];
    	ServName = new String[data.size()];
    	Descriere = new String[data.size()];
    	Telefon = new String[data.size()];
    	
    	for(int i=0;i<data.size();i++){
    		ArrayList<Object> row = data.get(i);
    		
    		id[i] = Long.decode(row.get(0).toString());
    		ServName[i] = row.get(1).toString();
    		Descriere[i] = row.get(2).toString().trim();
    		Telefon[i] = row.get(3).toString();
    		
    	}
    }
   
    
    @Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}
    
    
}