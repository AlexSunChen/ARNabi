package jp.campus_ar.campusar.page;

import android.test.AndroidTestCase;

import com.google.android.gms.maps.model.LatLng;
import com.uphyca.testing.AndroidJUnit4TestAdapter;

import org.junit.Test;
/**
 * Created by shentuweicheng on 西暦14/12/01.
 */
public class HelpPageTest extends AndroidTestCase {
    public static junit.framework.Test suite() {
        //Should use AndroidJUnit4TestAdapter for to running AndroidDependent TestCases.
        return new AndroidJUnit4TestAdapter(HelpPage.class);
    }
    private HelpPage myClass;

    @Test
    public void test(){
//        myClass.onOptionsItemSelected();
    }
}
