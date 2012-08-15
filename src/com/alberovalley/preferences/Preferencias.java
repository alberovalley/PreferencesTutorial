package com.alberovalley.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.alberovalley.utilities.contacts.ContactInfo;
import com.alberovalley.utilities.contacts.ContactosTelefono;
import com.alberovalley.utilities.sharedpreferences.SharedPreferencesUtils;



public class Preferencias extends Activity implements OnClickListener {
	
	public static final String LOGTAG = "preferenciasTutorial";
	
	public static final String PREFS_NAME="preferencias";
	public static final String PREFS_CHECK_NAME="check";
	public static final String PREFS_MENSAJE_NAME="mensaje";
	public static final String PREFS_LISTA_CONTACTOS_NAME="lista_contactos";
	
	
	EditText mensaje;
	CheckBox check;
	AutoCompleteTextView contacto;
	ListView lista;
	Button meteotro;
	Button borralista;
	
	ArrayAdapter<String> contactosAdapter;
	ArrayAdapter<String> contactosGuardadosAdapter;
	
	SharedPreferences settings ;
	SharedPreferences.Editor editor;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias);
        
        mensaje = (EditText) findViewById(R.id.mensaje);
    	check = (CheckBox ) findViewById(R.id.check);
    	contacto = (AutoCompleteTextView ) findViewById(R.id.selector);
    	lista = (ListView) findViewById(R.id.lista_contactos);
    	meteotro = (Button ) findViewById(R.id.meteotro);
    	borralista = (Button ) findViewById(R.id.borralista);
    	// clicks
    	meteotro.setOnClickListener(this);
    	borralista.setOnClickListener(this);
    	// autocomplete
    	contactosAdapter = new ArrayAdapter<String>(this,R.layout.simple_list_item);
    	contacto.setAdapter(contactosAdapter);
    	// lista de guardados    	
    	contactosGuardadosAdapter = new ArrayAdapter<String>(this,R.layout.simple_list_item);
    	lista.setAdapter(contactosGuardadosAdapter);
    	// Restore preferences
        //settings = getSharedPreferences(PREFS_NAME, 0);
    	settings = PreferenceManager
                .getDefaultSharedPreferences(this);

        boolean checked = settings.getBoolean(PREFS_CHECK_NAME, false);
        check.setChecked(checked);
        
        mensaje.setText(settings.getString(PREFS_MENSAJE_NAME, getResources().getString(R.string.mensaje_defecto)));
        String tokenized = settings.getString(PREFS_LISTA_CONTACTOS_NAME, "");
        Log.d(LOGTAG, "tokenized = " + tokenized);
        String[] contactos = SharedPreferencesUtils.tokenizedStringToArray(tokenized);
        if (contactos != null)
        	arrayToAdapter(contactos, contactosGuardadosAdapter);
        
        
    }
    
    

    @Override
	protected void onResume() {
		super.onResume();
		cargaListaContactosConTelefono();
	}

    @Override
    protected void onStop(){
       super.onStop();

      // necesitamos el objeto Editor para hacer cambios en las preferencias.
      guardaPreferencias();
      
    }
    
    
    private void guardaPreferencias() {
    	editor = settings.edit();
        editor.putBoolean(PREFS_CHECK_NAME, check.isChecked());
        editor.putString(PREFS_MENSAJE_NAME, mensaje.getText().toString());      
        
        if (contactosGuardadosAdapter.getCount() > 0){
        	String tokenized=SharedPreferencesUtils.getTokenizedStringFromAdapter(contactosGuardadosAdapter); 
        	editor.putString(
      			  PREFS_LISTA_CONTACTOS_NAME, tokenized
      			  );
        	Log.d(LOGTAG, "guardando: " + tokenized);
    	}else // si no hay nada en la lista, borramos todo rastro en las preferences
      	  editor.remove(PREFS_LISTA_CONTACTOS_NAME);
        // Hacemos Commit para guardar los cambios
        editor.commit();
		
	}



	private void cargaListaContactosConTelefono(){
    	List<String> lista = listaContactosConTelefono();
		Iterator<String> it = lista.iterator();
		while (it.hasNext()){
			String nuevo = it.next();
			Log.d(LOGTAG, "nuevo elemento: " + nuevo);
			contactosAdapter.add(nuevo);
		}
		contactosAdapter.notifyDataSetChanged();
		Log.d(LOGTAG, "cambios notificados" );
    }

	

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.borralista){
			// borrar la lista
			settings = PreferenceManager.getDefaultSharedPreferences(this);
			editor = settings.edit();
			if(settings.contains(PREFS_LISTA_CONTACTOS_NAME)){
				Log.d(LOGTAG, "nº de registros: " + contactosGuardadosAdapter.getCount());
				editor.remove(PREFS_LISTA_CONTACTOS_NAME);
				//editor.putString(PREFS_LISTA_CONTACTOS_NAME, "");
				contactosGuardadosAdapter.clear();
				contactosGuardadosAdapter.notifyDataSetChanged();
				cargaListaContactosConTelefono();
			}else{
				Log.w(LOGTAG, "no contiene : " + PREFS_LISTA_CONTACTOS_NAME);
			}
		}else if (v.getId()==R.id.meteotro){
			// mete otro
			String elemento = contacto.getText().toString();
			contactosGuardadosAdapter.add(elemento);
			contactosGuardadosAdapter.notifyDataSetChanged();
			contacto.setText("");
			contactosAdapter.remove(elemento);
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK){
			guardaPreferencias();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected void arrayToAdapter(String[] array, ArrayAdapter<String> adapter){
		int size = array.length;
		Log.d(LOGTAG, "tamaño array: " + size);
		for (int i = 0; i<size; i++){
			adapter.add(array[i]);
			Log.d(LOGTAG, "elemento[" + i + "] " + array[i]);
		}
		adapter.notifyDataSetChanged();
	}
	
	protected List<String> listaContactosConTelefono(){
		List<String> contactos = new ArrayList<String>();
		ContactosTelefono contactosT = new ContactosTelefono(getContentResolver());
		HashMap<String, ContactInfo> mapaContactos = contactosT.getContactos();
		Log.d(LOGTAG, "nº de entradas: "+ mapaContactos.size());
		Iterator it = mapaContactos.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, ContactInfo> pairs = (Entry<String, ContactInfo>) it.next();
			ContactInfo ci = (ContactInfo) pairs.getValue();
			contactos.add(ci.getDisplayName() + " : " + ci.getPhoneNumber());
		}
		
		return contactos;
	}



	
	
	
	
}
