package example.zxing;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

//import com.google.gson.Gson;
//import com.google.zxing.client.android.Intents;
//import com.journeyapps.barcodescanner.CustomDataModel;
//import com.journeyapps.barcodescanner.ScanContract;
//import com.journeyapps.barcodescanner.ScanOptions;
//import com.journeyapps.barcodescanner.Util;
//import com.mikepenz.aboutlibraries.LibsBuilder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.journeyapps.barcodescanner.CustomDataModel;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.Util;
import com.journeyapps.barcodescanner.google.Intents;


public class MainActivity extends AppCompatActivity {
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() == null) {
            Intent originalIntent = result.getOriginalIntent();
            if (originalIntent == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Log.d("MainActivity", "Cancelled scan due to missing camera permission");
                Toast.makeText(MainActivity.this, "Cancelled due to missing camera permission", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("MainActivity", "Scanned");
            Toast.makeText(MainActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void scanBarcode(View view) {
        CustomDataModel aaa = new CustomDataModel();
//        aaa.productImage= R.drawable.icon
        aaa.toolbarTitle = "Title App";
        aaa.productName = "jshdsjjks";
        aaa.productQuantity = "10 Unit";
        aaa.bsMessage1 = "message 1";
        aaa.bsMessage2 = "message222";
        aaa.buttonText = "Button Text";
        aaa.buttonColor = null;
        aaa.buttonTextColor = "#FFFFFF";

        ScanOptions options = new ScanOptions().addExtra(Util.dataTransfer_Key, new Gson().toJson(aaa));
        options.setOrientationLocked(true);

        barcodeLauncher.launch(options);
    }
}
