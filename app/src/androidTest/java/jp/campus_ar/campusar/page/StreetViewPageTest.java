package jp.campus_ar.campusar.page;

import android.test.AndroidTestCase;

import com.google.android.gms.maps.model.LatLng;
import com.uphyca.testing.AndroidJUnit4TestAdapter;

import org.junit.Test;

import java.util.IllegalFormatException;

/**
 * Created by shentuweicheng on 西暦14/11/28.
 */
public class StreetViewPageTest extends AndroidTestCase {
    public static junit.framework.Test suite() {
        //Should use AndroidJUnit4TestAdapter for to running AndroidDependent TestCases.
        return new AndroidJUnit4TestAdapter(StreetViewPage.class);
    }
    private StreetViewPage myClass;

//    public void setUp() throws Exception{
//        myClass = new StreetViewPage();
//        myClass.setLatLng();
//    }


    @Test
    public void test(){
        LatLng latLng = new LatLng(36.094304, 140.105956);
        myClass.setLatLng(latLng);
//        assertEquals(myClass.getAbc(), abc);
        assertNotNull(latLng );
    }
}
