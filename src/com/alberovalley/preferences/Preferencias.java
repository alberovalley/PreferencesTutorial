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
        // controles de UI
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
    	// Restaurar preferencias
        /*
         *  Sólo usamos un fichero de preferencias, por lo que usamos getPreferences
         *  Además, como queremos que estas preferencias sean legibles por otras aplicaciones, usamos el modo
         *   MODE_WORLD_READABLE
         */
    	
    	settings = getPreferences( MODE_WORLD_READABLE);
    			
    	// el check
        boolean checked = settings.getBoolean(PREFS_CHECK_NAME, false);
        check.setChecked(checked);
        // el mensaje
        mensaje.setText(settings.getString(PREFS_MENSAJE_NAME, getResources().getString(R.string.mensaje_defecto)));
        String tokenized = settings.getString(PREFS_LISTA_CONTACTOS_NAME, "");
        Log.d(LOGTAG, "tokenized = " + tokenized);
        // la lista de contactos
        String[] contactos = SharedPreferencesUtils.tokenizedStringToArray(tokenized);
        if (contactos != null) // si es la 1ª vez o se ha borrado la lista, contactos será null
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

      guardaPreferencias();
      
    }
    
    
    private void guardaPreferencias() {
    	settings = getPreferences( MODE_WORLD_READABLE);
    	editor = settings.edit();
    	// guardamos el check y el mensaje
        editor.putBoolean(PREFS_CHECK_NAME, check.isChecked());
        editor.putString(PREFS_MENSAJE_NAME, mensaje.getText().toString());      
        // guardamos la lista de contactos, si tuviera contactos
        if (contactosGuardadosAdapter.getCount() > 0){
        	String tokenized=SharedPreferencesUtils.getTokenizedStringFromAdapter(contactosGuardadosAdapter); 
        	editor.putString(
      			  PREFS_LISTA_CONTACTOS_NAME, tokenized
      			  );
        	Log.d(LOGTAG, "guardando: " + tokenized);
    	}else 
    		// si no hay nada en la lista, borramos todo rastro en las preferences
    		/*
    		 * Para borrar la lista de las preferences podemos hacer dos cosas
    		 * Una es eliminar la entrada (el objeto preferences ya no tendrá un elemento llamado PREFS_LISTA_CONTACTOS_NAME
    		 * Otra es sobrescribir el mismo elemento pero con una cadena vacía
    		 * Si se elije el primero, hay que tenerlo en cuenta y usar el método contains(String nombre) del objeto sharedpreferences
    		 */
      	  editor.remove(PREFS_LISTA_CONTACTOS_NAME); 
        // Hacemos Commit para guardar los cambios
        editor.commit();
		
	}



	private void cargaListaContactosConTelefono(){
		/*
		 * Carga en el autocomplete la lista íntegra de contactos que tengan teléfono
		 */
    	List<String> lista = listaContactosConTelefono();
		Iterator<String> it = lista.iterator();
		while (it.hasNext()){
			String nuevo = it.next();
			Log.d(LOGTAG, "nuevo elemento: " + nuevo);
			contactosAdapter.add(nuevo);
		}
		// eliminar los ya guardados que se hayan cargado por las preferences
		settings = getPreferences( MODE_WORLD_READABLE);
		String[] contactos = SharedPreferencesUtils.tokenizedStringToArray(settings.getString(PREFS_LISTA_CONTACTOS_NAME, ""));
		if(contactos != null){
			for (String contacto : contactos){
				// retiramos del autocomplete cada uno de los contactos ya guardados para evitar repeticiones
				contactosAdapter.remove(contacto);
			}
		}
		contactosAdapter.notifyDataSetChanged();
		Log.d(LOGTAG, "cambios notificados" );
    }

	

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.borralista){
			// borrar la lista
			contactosGuardadosAdapter.clear();
			contactosGuardadosAdapter.notifyDataSetChanged();
			cargaListaContactosConTelefono();
			settings = getPreferences( MODE_WORLD_READABLE);
			editor = settings.edit();
			
			if(settings.contains(PREFS_LISTA_CONTACTOS_NAME)){
				Log.d(LOGTAG, "nº de registros: " + contactosGuardadosAdapter.getCount());
				editor.remove(PREFS_LISTA_CONTACTOS_NAME);
				/*
				 * otra forma de 'borrar' sería sobreescribir con un valor vacío
				 * Ej:  editor.putString(PREFS_LISTA_CONTACTOS_NAME, "");
				 */
			}else{
				Log.w(LOGTAG, "no contiene : " + PREFS_LISTA_CONTACTOS_NAME);
			}
		}else if (v.getId()==R.id.meteotro){
			// añade otro contacto a la lista
			String elemento = contacto.getText().toString();
			contactosGuardadosAdapter.add(elemento);
			contactosGuardadosAdapter.notifyDataSetChanged();
			contacto.setText("");
			// y lo eliminamos de la lista del autocomplete para no repetir
			contactosAdapter.remove(elemento);
			contactosAdapter.notifyDataSetChanged();
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			
			/*
			 *  para esta práctica queremos que la aplicación "muera" y asegurarnos así de que no estamos viendo
			 *  la misma pantalla. 
			 *  Antes de salir, nos aseguramos que se guarden las preferencias.
			 */
			
			guardaPreferencias();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected void arrayToAdapter(String[] array, ArrayAdapter<String> adapter){
		/*
		 * Itera un array para ir introduciéndo sus elementos en el adaptador
		 * A partir de la API 11 hay un método para que el adaptador reciba una colección
		 * adapter.addAll(collection)
		 * pero así damos soporte a las API anteriores 
		 */
		
		int size = array.length;
		Log.d(LOGTAG, "tamaño array: " + size);
		for (int i = 0; i<size; i++){
			adapter.add(array[i]);
			Log.d(LOGTAG, "elemento[" + i + "] " + array[i]);
		}
		adapter.notifyDataSetChanged();
	}
	
	protected List<String> listaContactosConTelefono(){
		/*
		 * simplemente toma la lista de objetos ContactosTelefono y las convierte en pares
		 * "nombre : teléfono" para nuestras listas 
		 */
		List<String> contactos = new ArrayList<String>();
		ContactosTelefono contactosT = new ContactosTelefono(getContentResolver());
		HashMap<String, ContactInfo> mapaContactos = contactosT.getContactos();
		Log.d(LOGTAG, "nº de entradas: "+ mapaContactos.size());
		Iterator<Entry<String, ContactInfo>> it = mapaContactos.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, ContactInfo> pairs = (Entry<String, ContactInfo>) it.next();
			ContactInfo ci = (ContactInfo) pairs.getValue();
			String mobileNum =ci.getMobilePhoneNumber(); 
			if (!mobileNum.equalsIgnoreCase("")){
				Log.d(LOGTAG, "móvil: " + mobileNum);
				contactos.add(ci.getDisplayName() + "(móvil) : " + mobileNum);
			}
			String homeNum =ci.getHomePhoneNumber(); 
			if (!homeNum.equalsIgnoreCase("")){
				Log.d(LOGTAG, "móvil: " + homeNum);
				contactos.add(ci.getDisplayName() + "(casa) : " + homeNum);
			}
			String workNum =ci.getWorkPhoneNumber(); 
			if (!workNum.equalsIgnoreCase("")){
				Log.d(LOGTAG, "trabajo: " + workNum);
				contactos.add(ci.getDisplayName() + "(trabajo) : " + workNum);
			}
		}
		
		return contactos;
	}



	
	
	
	
}
