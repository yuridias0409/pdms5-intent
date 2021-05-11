package pdms5.at2.intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import pdms5.at2.intent.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    //instância
    private ActivityMainBinding activityMainBinding;

    // constante para passagem de parâmetro e retorno
    public static final String PARAMETRO = "PARAMETRO";

    // request code para OutraActivity
    private final int OUTRA_ACTIVITY_REQUEST_CODE = 0;

    // request code para permissão CALL_PHONE
    private final int CALL_PHONE_PERMISSION_REQUEST_CODE = 1;

    // request code para pegar um arquivo de imagem
    private final int PICK_IMAGE_FILE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());

        getSupportActionBar().setTitle("Tratando Intents");
        getSupportActionBar().setSubtitle("Tem subtítulo também");

        setContentView(activityMainBinding.getRoot());

        Log.v(getString(R.string.app_name) + "/" + getLocalClassName(), "onCreate: Iniciando ciclo completo");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(getString(R.string.app_name) + "/" + getLocalClassName(), "onStart: Iniciando ciclo visível");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(getString(R.string.app_name) + "/" + getLocalClassName(), "onResume: Iniciando ciclo foreground");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(getString(R.string.app_name) + "/" + getLocalClassName(), "onPause: Finalizando ciclo foreground");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(getString(R.string.app_name) + "/" + getLocalClassName(), "onStop: Finalizando ciclo visível");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(getString(R.string.app_name) + "/" + getLocalClassName(), "onDestroy: Finalizando ciclo completo");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.outraActivityMi:
                //Abrir a OutraActivity
                // Intent outraActivityIntent = new Intent(this, OutraActivity.class);
                Intent outraActivityIntent = new Intent("RECEBER_E_RETORNAR_ACTION");

                // Forma #1 de passagem de parâmetro
                /*
                Bundle parametrosBundle = new Bundle();
                parametrosBundle.putString(PARAMETRO, activityMainBinding.parametroEt.getText().toString());
                outraActivityIntent.putExtras(parametrosBundle);
                */

                // Forma #2 de passagem de parâmetro
                outraActivityIntent.putExtra(PARAMETRO, activityMainBinding.parametroEt.getText().toString());

                startActivityForResult(outraActivityIntent, OUTRA_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.viewMi:
                //Abrindo navegador
                Intent abrirNavegadorIntent = new Intent(Intent.ACTION_VIEW);
                abrirNavegadorIntent.setData(Uri.parse(activityMainBinding.parametroEt.getText().toString()));
                startActivity(abrirNavegadorIntent);
                return true;
            case R.id.callMi:
                // Fazendo uma ligação
                verifyCallPhonePermission();
                return true;
            case R.id.dialMi:
                Intent discarIntent = new Intent(Intent.ACTION_DIAL);
                discarIntent.setData(Uri.parse("tel: " + activityMainBinding.parametroEt.getText().toString()));
                startActivity(discarIntent);
                return true;
            case R.id.pickMi:
                startActivityForResult(getPickageImageIntent(), PICK_IMAGE_FILE_REQUEST_CODE);
                return true;
            case R.id.chooserMi:
                // Força o usuário escolha entre uma lista de aplicativos MESMO QUE JÁ EXISTA UM APP PADRÃO
                Intent escolherActivityIntent = new Intent(Intent.ACTION_CHOOSER);
                escolherActivityIntent.putExtra(Intent.EXTRA_INTENT, getPickageImageIntent());
                escolherActivityIntent.putExtra(Intent.EXTRA_TITLE, "Escolha um app para selecionar imagem");
                startActivityForResult(escolherActivityIntent, PICK_IMAGE_FILE_REQUEST_CODE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent getPickageImageIntent(){
        Intent pegarImagemIntent = new Intent(Intent.ACTION_PICK);
        String diretorioImagens = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        pegarImagemIntent.setDataAndType(Uri.parse(diretorioImagens), "image/*");

        return pegarImagemIntent;
    }

    private void verifyCallPhonePermission() {
        Intent ligarIntent = new Intent(Intent.ACTION_CALL);
        ligarIntent.setData(Uri.parse("tel: " + activityMainBinding.parametroEt.getText().toString()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                //Usuário já concedeu a permissão
                startActivity(ligarIntent);
            }   else{
                //Solicitar permissão para o usuário em tempo de execução
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_PERMISSION_REQUEST_CODE);
            }
        }   else{
            // A permissão foi solicitada no Manifest
            startActivity(ligarIntent);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CALL_PHONE_PERMISSION_REQUEST_CODE){
            if(permissions[0].equals(Manifest.permission.CALL_PHONE) && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                //Usuário não deu permissão
                Toast.makeText(this, "Permissão de ligação é necessária para essa funcionalidade", Toast.LENGTH_SHORT).show();
            }
            verifyCallPhonePermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == OUTRA_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            String retorno = data.getStringExtra(OutraActivity.RETORNO);
            if(retorno != null){
                activityMainBinding.retornoTv.setText(retorno);
            }
        }   else if(requestCode == PICK_IMAGE_FILE_REQUEST_CODE && resultCode == RESULT_OK){
            Uri imagemUri = data.getData();

            // Vizualizando imagem
            Intent vizualizarImagem = new Intent(Intent.ACTION_VIEW, imagemUri);
            startActivity(vizualizarImagem);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}