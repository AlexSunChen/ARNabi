package jp.campus_ar.campusar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import jp.campus_ar.campusar.page.NavigationPage;

public class FromShareActivity extends Activity {

    final private static int RESULT = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        this.start();
    }

    private void start() {
        Intent i = new Intent(this, NavigationPage.class);
        i.setData(this.getIntent().getData());
        this.startActivityForResult(i, RESULT);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT) {
            this.finish();
        }
    }
}
