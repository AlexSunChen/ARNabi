package jp.campus_ar.campusar.page;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import jp.campus_ar.campusar.R;
import jp.campus_ar.campusar.component.animation.HeightAnimation;
import jp.campus_ar.campusar.component.animation.MarginLeftAnimation;
import jp.campus_ar.campusar.component.dialog.ListDialog;
import jp.campus_ar.campusar.component.support.TouchableMapView;
import jp.campus_ar.campusar.layer.*;
import jp.campus_ar.campusar.model.Entry;
import jp.campus_ar.campusar.model.Facility;
import jp.campus_ar.campusar.model.Route;
import jp.campus_ar.campusar.util.*;

public class NavigationPage extends FragmentActivity implements ListDialog.OnListDialogItemSelectedListener, SensorUtil.OnSensorChangedListener, View.OnClickListener, TextView.OnEditorActionListener, View.OnFocusChangeListener, TouchableMapView.OnMapTouchedListener, MenuLayer.OnMenuItemClickListener {

    final private int SHOULD_RETURN_A_GOAL_ENTRY = 0;
    final private int SHOULD_RETURN_AN_ACTIVE_FACILITY = 1;
    final private int ANIMATION_DURATION = 200;

    private int facilityIndex;
    private int routeType;
    private boolean isMenuExpanded;
    private boolean isMenuAnimating;
    private boolean isToolbarAnimating;

    private enum ShowFragmentType {
        MAP,
        AR,
        PREVIEW,
        POP_UP
    }

    private RelativeLayout rootContainer;
    private LinearLayout menuContainer;
    private ImageView backgroundImage;
    private EditText queryTextField;
    private ImageButton cancelButton;
    private ImageButton favoriteButton;
    private ImageButton routeButton;
    private TextView detailLabel;
    private TextView distanceLabel;
    private ImageButton knobButton;
    private Button menuCoverButton;

    private Entry goal;
    private String savedQueryString;

    private ARLayer arLayer;
    private MapLayer mapLayer;
    private MenuLayer menuLayer;
    private PreviewStreetViewLayer previewStreetViewLayer;
    private PopUpStreetViewLayer popUpStreetViewLayer;
    private ShowFragmentType showFragmentType = ShowFragmentType.MAP;

    private Handler handler;
    private Route entranceNode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_navigation);
        getActionBar().hide();

        handler = new Handler();

        mapLayer = (MapLayer) getFragmentManager().findFragmentById(R.id.mapLayer);
        arLayer = (ARLayer) getFragmentManager().findFragmentById(R.id.arLayer);
        menuLayer = (MenuLayer) getFragmentManager().findFragmentById(R.id.menuLayer);
        previewStreetViewLayer = (PreviewStreetViewLayer) getFragmentManager().findFragmentById(R.id.previewStreetViewLayer);
        popUpStreetViewLayer = (PopUpStreetViewLayer) getFragmentManager().findFragmentById(R.id.popUpStreetViewLayer);

        rootContainer = (RelativeLayout) findViewById(R.id.rootContainer);
        menuContainer = (LinearLayout) findViewById(R.id.menuContainer);
        backgroundImage = (ImageView) findViewById(R.id.backgroundImage);
        queryTextField = (EditText) findViewById(R.id.queryTextField);
        cancelButton = (ImageButton) findViewById(R.id.cancelButton);
        favoriteButton = (ImageButton) findViewById(R.id.favoriteButton);
        routeButton = (ImageButton) findViewById(R.id.routeButton);
        detailLabel = (TextView) findViewById(R.id.detailLabel);
        distanceLabel = (TextView) findViewById(R.id.distanceLabel);
        knobButton = (ImageButton) findViewById(R.id.knobButton);
        menuCoverButton = (Button) findViewById(R.id.menuCoverButton);

        isToolbarAnimating = isMenuExpanded = isMenuAnimating = false;
        routeType = 1;
        showFragment(ShowFragmentType.MAP);
        expandSearchBackground(false, false);
        expandMenu(false, false);
        setFacilityIndex(1);

        routeButton.setVisibility(View.GONE);

        SensorUtil.getInstance(this, handler).setOnSensorChangedListener(this);

        queryTextField.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        queryTextField.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        queryTextField.setOnEditorActionListener(this);
        queryTextField.setOnFocusChangeListener(this);
        mapLayer.setOnMapTouchedListener(this);
        cancelButton.setOnClickListener(this);
        favoriteButton.setOnClickListener(this);
        routeButton.setOnClickListener(this);
        knobButton.setOnClickListener(this);
        menuCoverButton.setOnClickListener(this);
        menuLayer.setOnMenuItemClickListener(this);

        String entranceNodeJson = "{\"coordinates\":[{\"lat\":\"36.10973568497758\",\"lng\":\"140.101620554924\"}," +
                "{\"lat\":\"36.111356531284954\",\"lng\":\"140.0950638949871\"}," +
                "{\"lat\":\"36.12127156866196\",\"lng\":\"140.098018348217\"}," +
                "{\"lat\":\"36.11456778841489\",\"lng\":\"140.09282559156418\"}," +
                "{\"lat\":\"36.114476782935725\",\"lng\":\"140.09285777807236\"}," +
                "{\"lat\":\"36.107700912115384\",\"lng\":\"140.10470509529114\"}," +
                "{\"lat\":\"36.107631568554446\",\"lng\":\"140.1045548915863\"}," +
                "{\"lat\":\"36.11470212964382\",\"lng\":\"140.10333448648453\"}," +
                "{\"lat\":\"36.11110517228432\",\"lng\":\"140.1056143641472\"}," +
                "{\"lat\":\"36.111321861125816\",\"lng\":\"140.1055070757866\"}," +
                "{\"lat\":\"36.108838571170935\",\"lng\":\"140.10701581835747\"}," +
                "{\"lat\":\"36.106719261630566\",\"lng\":\"140.10258212685585\"}," +
                "{\"lat\":\"36.10659574182648\",\"lng\":\"140.102246850729\"}," +
                "{\"lat\":\"36.10624035042961\",\"lng\":\"140.1008789241314\"}," +
                "{\"lat\":\"36.10619700990536\",\"lng\":\"140.10067239403725\"}," +
                "{\"lat\":\"36.105828614483784\",\"lng\":\"140.0985722243786\"}," +
                "{\"lat\":\"36.1066184954892\",\"lng\":\"140.1023554801941\"}," +
                "{\"lat\":\"36.088054734103984\",\"lng\":\"140.105222761631\"}," +
                "{\"lat\":\"36.084328666574656\",\"lng\":\"140.10554060339928\"}," +
                "{\"lat\":\"36.09247419778015\",\"lng\":\"140.10580077767372\"}," +
                "{\"lat\":\"36.09307456675809\",\"lng\":\"140.10698094964027\"}," +
                "{\"lat\":\"36.09667451702463\",\"lng\":\"140.10620310902596\"}," +
                "{\"lat\":\"36.09821327748309\",\"lng\":\"140.10584637522697\"}," +
                "{\"lat\":\"36.09514006109778\",\"lng\":\"140.09987980127335\"}," +
                "{\"lat\":\"36.091429501477364\",\"lng\":\"140.10357454419136\"}," +
                "{\"lat\":\"36.09118674848103\",\"lng\":\"140.10300055146217\"}," +
                "{\"lat\":\"36.0922260294803\",\"lng\":\"140.09994819760323\"}," +
                "{\"lat\":\"36.094407499538114\",\"lng\":\"140.10672613978386\"}," +
                "{\"lat\":\"36.09892413129395\",\"lng\":\"140.10568007826805\"}," +
                "{\"lat\":\"36.08767270651002\",\"lng\":\"140.10730884969234\"}," +
                "{\"lat\":\"36.08582756837541\",\"lng\":\"140.1081745326519\"}," +
                "{\"lat\":\"36.098742083979346\",\"lng\":\"140.10159507393837\"}," +
                "{\"lat\":\"36.092289968112844\",\"lng\":\"140.10538771748543\"}," +
                "{\"lat\":\"36.09125935771478\",\"lng\":\"140.10320625449822\"}," +
                "{\"lat\":\"36.099634472851\",\"lng\":\"140.1052720694861\"}," +
                "{\"lat\":\"36.121144821823\",\"lng\":\"140.09810619056225\"}," +
                "{\"lat\":\"36.114890640335595\",\"lng\":\"140.10318830609322\"}," +
                "{\"lat\":\"36.12157489359964\",\"lng\":\"140.09874992072582\"}," +
                "{\"lat\":\"36.09733310834797\",\"lng\":\"140.10603982955217\"}," +
                "{\"lat\":\"36.10483827234468\",\"lng\":\"140.10665841400623\"}," +
                "{\"lat\":\"36.104760257991444\",\"lng\":\"140.10639421641827\"}," +
                "{\"lat\":\"36.10115526025067\",\"lng\":\"140.10084874927998\"}," +
                "{\"lat\":\"36.10391943171133\",\"lng\":\"140.10941371321678\"}," +
                "{\"lat\":\"36.10190184535096\",\"lng\":\"140.10970003902912\"}," +
                "{\"lat\":\"36.10213914740207\",\"lng\":\"140.1096437126398\"}," +
                "{\"lat\":\"36.09968482443793\",\"lng\":\"140.10531798005104\"}," +
                "{\"lat\":\"36.099379247735165\",\"lng\":\"140.10564520955086\"}," +
                "{\"lat\":\"36.10411121891024\",\"lng\":\"140.10133020579815\"}," +
                "{\"lat\":\"36.104772176855974\",\"lng\":\"140.10104186832905\"}," +
                "{\"lat\":\"36.106244684480714\",\"lng\":\"140.1018525660038\"}," +
                "{\"lat\":\"36.10463348450213\",\"lng\":\"140.10322652757168\"}," +
                "{\"lat\":\"36.10466815761356\",\"lng\":\"140.1032117754221\"}," +
                "{\"lat\":\"36.10428891962584\",\"lng\":\"140.10431952774525\"}," +
                "{\"lat\":\"36.0974482355568\",\"lng\":\"140.10184988379478\"}," +
                "{\"lat\":\"36.10619917693212\",\"lng\":\"140.102656558156\"}," +
                "{\"lat\":\"36.1051329925356\",\"lng\":\"140.10304547846317\"}," +
                "{\"lat\":\"36.09477919952608\",\"lng\":\"140.10672815144062\"}," +
                "{\"lat\":\"36.094488775195856\",\"lng\":\"140.106795206666\"}," +
                "{\"lat\":\"36.09439883012966\",\"lng\":\"140.10681800544262\"}," +
                "{\"lat\":\"36.10534969785293\",\"lng\":\"140.10648474097252\"}," +
                "{\"lat\":\"36.10532369324639\",\"lng\":\"140.10634526610374\"}," +
                "{\"lat\":\"36.1048642771119\",\"lng\":\"140.10636672377586\"}," +
                "{\"lat\":\"36.10495095960706\",\"lng\":\"140.10644182562828\"}," +
                "{\"lat\":\"36.10494662548459\",\"lng\":\"140.10662958025932\"}," +
                "{\"lat\":\"36.10497696433698\",\"lng\":\"140.10657325387\"}," +
                "{\"lat\":\"36.105471052569506\",\"lng\":\"140.1064284145832\"}," +
                "{\"lat\":\"36.10543204571675\",\"lng\":\"140.1062996685505\"}," +
                "{\"lat\":\"36.105540398037654\",\"lng\":\"140.1062460243702\"}," +
                "{\"lat\":\"36.10558807301152\",\"lng\":\"140.10635867714882\"}," +
                "{\"lat\":\"36.10742353750441\",\"lng\":\"140.1047520339489\"}," +
                "{\"lat\":\"36.10770524608591\",\"lng\":\"140.10480299592018\"}," +
                "{\"lat\":\"36.107586061809336\",\"lng\":\"140.10452672839165\"}," +
                "{\"lat\":\"36.10743003848304\",\"lng\":\"140.1046286523342\"}," +
                "{\"lat\":\"36.10754705600683\",\"lng\":\"140.10491028428078\"}," +
                "{\"lat\":\"36.10748421328425\",\"lng\":\"140.10488346219063\"}," +
                "{\"lat\":\"36.10649822605448\",\"lng\":\"140.10565593838692\"}," +
                "{\"lat\":\"36.10629235902713\",\"lng\":\"140.10564520955086\"}," +
                "{\"lat\":\"36.10617100557932\",\"lng\":\"140.10594561696053\"}," +
                "{\"lat\":\"36.105969471760744\",\"lng\":\"140.10591343045235\"}," +
                "{\"lat\":\"36.099318565695306\",\"lng\":\"140.10568007826805\"}," +
                "{\"lat\":\"36.09895663969861\",\"lng\":\"140.10578736662865\"}," +
                "{\"lat\":\"36.09879843200276\",\"lng\":\"140.10581150650978\"}," +
                "{\"lat\":\"36.09825228791997\",\"lng\":\"140.10593488812447\"}," +
                "{\"lat\":\"36.097396221104404\",\"lng\":\"140.1061199605465\"}," +
                "{\"lat\":\"36.10674309876339\",\"lng\":\"140.10267600417137\"}," +
                "{\"lat\":\"36.10657190464895\",\"lng\":\"140.10278329253197\"}," +
                "{\"lat\":\"36.10645488567249\",\"lng\":\"140.10245069861412\"}," +
                "{\"lat\":\"36.106412628777036\",\"lng\":\"140.10235346853733\"}," +
                "{\"lat\":\"36.10643213196239\",\"lng\":\"140.10257877409458\"}," +
                "{\"lat\":\"36.106527480798775\",\"lng\":\"140.10268338024616\"}," +
                "{\"lat\":\"36.10647980639505\",\"lng\":\"140.10255463421345\"}," +
                "{\"lat\":\"36.10549380655799\",\"lng\":\"140.10291539132595\"}," +
                "{\"lat\":\"36.10587520576488\",\"lng\":\"140.1027625054121\"}," +
                "{\"lat\":\"36.09233006519485\",\"lng\":\"140.10584838688374\"}," +
                "{\"lat\":\"36.09212957958026\",\"lng\":\"140.1054111868143\"}," +
                "{\"lat\":\"36.091767620472794\",\"lng\":\"140.1046548038721\"}," +
                "{\"lat\":\"36.091714518547356\",\"lng\":\"140.10465614497662\"}," +
                "{\"lat\":\"36.0916776722923\",\"lng\":\"140.10444559156895\"}," +
                "{\"lat\":\"36.09164407716235\",\"lng\":\"140.10451264679432\"}," +
                "{\"lat\":\"36.09163215630633\",\"lng\":\"140.10455019772053\"}," +
                "{\"lat\":\"36.091803382973794\",\"lng\":\"140.104366466403\"}," +
                "{\"lat\":\"36.091897665852976\",\"lng\":\"140.1045649498701\"}," +
                "{\"lat\":\"36.090246073538545\",\"lng\":\"140.10552920401096\"}," +
                "{\"lat\":\"36.09091039929137\",\"lng\":\"140.10506249964237\"}," +
                "{\"lat\":\"36.090816115227895\",\"lng\":\"140.1051201671362\"}," +
                "{\"lat\":\"36.091082711253065\",\"lng\":\"140.1051201671362\"}," +
                "{\"lat\":\"36.09102527397448\",\"lng\":\"140.1051563769579\"}," +
                "{\"lat\":\"36.09079660816616\",\"lng\":\"140.10531194508076\"}," +
                "{\"lat\":\"36.090274250601304\",\"lng\":\"140.1056780666113\"}," +
                "{\"lat\":\"36.08976272696615\",\"lng\":\"140.10580949485302\"}," +
                "{\"lat\":\"36.08967602771498\",\"lng\":\"140.10586582124233\"}," +
                "{\"lat\":\"36.09014203506548\",\"lng\":\"140.1057706028223\"}," +
                "{\"lat\":\"36.08986893341846\",\"lng\":\"140.1059503108263\"}," +
                "{\"lat\":\"36.08971829361186\",\"lng\":\"140.10606832802296\"}," +
                "{\"lat\":\"36.089821248906595\",\"lng\":\"140.10582961142063\"}," +
                "{\"lat\":\"36.08965326914571\",\"lng\":\"140.10593958199024\"}," +
                "{\"lat\":\"36.08900518902665\",\"lng\":\"140.10639421641827\"}," +
                "{\"lat\":\"36.089366077110114\",\"lng\":\"140.10631509125233\"}," +
                "{\"lat\":\"36.089057209032774\",\"lng\":\"140.10653100907803\"}," +
                "{\"lat\":\"36.08893149395904\",\"lng\":\"140.10637812316418\"}," +
                "{\"lat\":\"36.08903011528388\",\"lng\":\"140.106615498662\"}," +
                "{\"lat\":\"36.0886768119434\",\"lng\":\"140.10652966797352\"}," +
                "{\"lat\":\"36.08865947186128\",\"lng\":\"140.10662354528904\"}," +
                "{\"lat\":\"36.08878952238403\",\"lng\":\"140.10678313672543\"}," +
                "{\"lat\":\"36.08871474335972\",\"lng\":\"140.10675631463528\"}," +
                "{\"lat\":\"36.08779029534311\",\"lng\":\"140.1072297245264\"}," +
                "{\"lat\":\"36.08731452008652\",\"lng\":\"140.10771956294775\"}," +
                "{\"lat\":\"36.08725220306856\",\"lng\":\"140.10760825127363\"}," +
                "{\"lat\":\"36.086423652978304\",\"lng\":\"140.10817252099514\"}," +
                "{\"lat\":\"36.08647567469284\",\"lng\":\"140.10832138359547\"}," +
                "{\"lat\":\"36.08632827974559\",\"lng\":\"140.10842464864254\"}," +
                "{\"lat\":\"36.086335324363326\",\"lng\":\"140.10822247713804\"}," +
                "{\"lat\":\"36.086249705120316\",\"lng\":\"140.10825131088495\"}," +
                "{\"lat\":\"36.0862968499049\",\"lng\":\"140.1082395762205\"}," +
                "{\"lat\":\"36.08631093914539\",\"lng\":\"140.10849840939045\"}," +
                "{\"lat\":\"36.08622315229818\",\"lng\":\"140.1085426658392\"}," +
                "{\"lat\":\"36.086189554836885\",\"lng\":\"140.1084876805544\"}," +
                "{\"lat\":\"36.086137532933016\",\"lng\":\"140.10844476521015\"}," +
                "{\"lat\":\"36.08587850669084\",\"lng\":\"140.10835625231266\"}," +
                "{\"lat\":\"36.08613861672303\",\"lng\":\"140.10826237499714\"}," +
                "{\"lat\":\"36.085713769891974\",\"lng\":\"140.10830394923687\"}," +
                "{\"lat\":\"36.085738697192916\",\"lng\":\"140.10813631117344\"}," +
                "{\"lat\":\"36.08550893220663\",\"lng\":\"140.10823018848896\"}," +
                "{\"lat\":\"36.0854308986623\",\"lng\":\"140.10830126702785\"}," +
                "{\"lat\":\"36.08527266373741\",\"lng\":\"140.10898120701313\"}," +
                "{\"lat\":\"36.08519896517053\",\"lng\":\"140.10921590030193\"}," +
                "{\"lat\":\"36.08565524489349\",\"lng\":\"140.10811753571033\"}," +
                "{\"lat\":\"36.08534094323038\",\"lng\":\"140.10826237499714\"}," +
                "{\"lat\":\"36.085250987695495\",\"lng\":\"140.1087076216936\"}," +
                "{\"lat\":\"36.08555336794032\",\"lng\":\"140.10808266699314\"}," +
                "{\"lat\":\"36.08524990389327\",\"lng\":\"140.10905496776104\"}," +
                "{\"lat\":\"36.085170786288465\",\"lng\":\"140.10902546346188\"}]}";
        entranceNode = new Gson().fromJson(entranceNodeJson, Route.class);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.hide(mapLayer);
        transaction.hide(arLayer);
        transaction.hide(previewStreetViewLayer);
        transaction.hide(popUpStreetViewLayer);
        transaction.commit();

        this.parseIntent();
    }

    public void onMenuItemClick(String name, int value) {
        Intent i;
        switch (value) {
            case MenuLayer.FAVORITE:
            case MenuLayer.HISTORY:
                i = new Intent(NavigationPage.this, BookmarkPage.class);
                if (value == MenuLayer.FAVORITE) {
                    i.putExtra("title", "お気に入り");
                    i.putExtra("category", Entry.TYPE_FAVORITE);
                } else {
                    i.putExtra("title", "履歴");
                    i.putExtra("category", Entry.TYPE_HISTORY);
                }
                startActivityForResult(i, SHOULD_RETURN_A_GOAL_ENTRY);
                expandMenu(false, false);
                break;

            case MenuLayer.FACILITY:
                i = new Intent(NavigationPage.this, FacilityPage.class);
                i.putExtra("facility_index", facilityIndex);
                startActivityForResult(i, SHOULD_RETURN_AN_ACTIVE_FACILITY);
                expandMenu(false, false);
                break;

            case MenuLayer.HELP:
                i = new Intent(NavigationPage.this, HelpPage.class);
                startActivity(i);
                expandMenu(false, false);
                break;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (goal != null) {
                unsetGoal("");
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case SHOULD_RETURN_A_GOAL_ENTRY:
                Entry entry = (Entry) data.getExtras().getSerializable("entry");
                setGoal(entry);
                HashMap<String, String> params = new HashMap<>();
                params.put("entry_id", entry.identity + "");
                OpenURI.open(this, "/api/goals/set", "GET", params, null);
                break;

            case SHOULD_RETURN_AN_ACTIVE_FACILITY:
                setFacilityIndex(data.getIntExtra("id", 1));
                break;
        }
    }

    public void onSensorOrientationChanged(double yaw, double pitch) {
        switch (showFragmentType) {
            case MAP:
            case AR:
                if (pitch > Math.PI * 0.45 && goal != null) {
                    showFragment(showFragmentType = ShowFragmentType.AR);
                    arLayer.startCamera();
                    arLayer.onSensorOrientationChanged(yaw, pitch);
                }

                if (pitch < Math.PI * 0.15) {
                    showFragment(showFragmentType = ShowFragmentType.MAP);
                    arLayer.stopCamera();
                    mapLayer.onSensorOrientationChanged(yaw, pitch);
                }
                break;
            case PREVIEW:
                showFragment(showFragmentType = ShowFragmentType.PREVIEW);
                previewStreetViewLayer.onSensorOrientationChanged(yaw, pitch);
                break;
            case POP_UP:
                showFragment(showFragmentType = ShowFragmentType.POP_UP);
                popUpStreetViewLayer.onSensorOrientationChanged(yaw, pitch);
                break;
            default:
                break;
        }
    }

    public void onSensorLocationChanged(double lat, double lng) {
        switch (showFragmentType) {
            case MAP:       mapLayer.onSensorLocationChanged(lat, lng);                 break;
            case AR:        arLayer.onSensorLocationChanged(lat, lng);                  break;
            case PREVIEW:   previewStreetViewLayer.onSensorLocationChanged(lat, lng);   break;
            case POP_UP:    popUpStreetViewLayer.onSensorLocationChanged(lat, lng);     break;
            default:                                                                    break;
        }
    }

    public void onSensorAverageOrientationChanged(double yaw, double pitch) {
        switch (showFragmentType) {
            case MAP:       mapLayer.onSensorAverageOrientationChanged(yaw, pitch);                 break;
            case AR:        arLayer.onSensorAverageOrientationChanged(yaw, pitch);                  break;
            case PREVIEW:   previewStreetViewLayer.onSensorAverageOrientationChanged(yaw, pitch);   break;
            case POP_UP:    popUpStreetViewLayer.onSensorAverageOrientationChanged(yaw, pitch);     break;
            default:                                                                                break;
        }
    }

    public void onClick(View view) {
        if (cancelButton.equals(view)) {
            unsetGoal("");
            return;
        }
        if (favoriteButton.equals(view)) {
            if (goal == null) return;

            BookmarkUtil bu = new BookmarkUtil(NavigationPage.this);
            if (bu.isFavorited(goal.identity)) {
                bu.removeFavorite(goal.identity);
            } else {
                bu.pushFavorite(goal);
            }
            refreshFavoriteButton();
            return;
        }
        if (routeButton.equals(view)) {
            final ListDialog d = new ListDialog(NavigationPage.this);
            d.setTitle("ルートを選択");
            d.setOnListDialogItemSelectedListener(this);
            d.add("歩きやすいルート", 1);
            d.add("最短ルート", 2);
            d.add("歩きやすいルート（階段なし）", 3);
            d.add("最短ルート（階段なし）", 4);
            d.setValue(routeType);
            d.show();
            return;
        }
        if (knobButton.equals(view)) {
            expandMenu(true, true);
            return;
        }
        if (menuCoverButton.equals(view)) {
            expandMenu(false, true);
            return;
        }
    }

    public void onMapTouched(MotionEvent ev) {
        hideKeyboard(queryTextField);
        if (goal != null) {
            if (goal.type != 0) {
                if (ev.getAction() == MotionEvent.ACTION_UP) {
                    getFragmentManager()
                            .beginTransaction()
                            .hide(popUpStreetViewLayer)
                            .commit();

                    mapLayer.unSetPopup();
                    showFragment(showFragmentType = ShowFragmentType.MAP);
                    Log.d("tks", "hide pop up street view");
                }
            }
        }
    }

    public void onMapLongPressed(MotionEvent ev, Entry goalEntry) {
        goal = goalEntry;
        if (goal.type != 0) {
            showFragment(showFragmentType = ShowFragmentType.POP_UP);
            popUpStreetViewLayer.setPositionFlag = false;
            popUpStreetViewLayer.setLatLng(new LatLng(goal.lat, goal.lng));
            mapLayer.setPopup();
            return;
        }

        final ListDialog d = new ListDialog(NavigationPage.this);
        d.toggleMainTitleView();
        d.setTitle("ルートを選択");
        d.setOnListDialogItemSelectedListener(this);
        d.add("案内を開始する", 5);
        d.add("下見する", 6);
        d.add("場所をシェアする", 7);
        d.setEditTitleView(goal.detail);
        d.setOnCancelListener((listener) -> {
            goal = null;
            mapLayer.unsetGoal();
        });
        d.show();
    }

    public void onListDialogItemSelected(ListDialog d, String name, int value) {
        if (d.getPlaceName().equals("")) {
            goal.name = goal.detail;
            goal.detail = "";
        } else {
            goal.name = d.getPlaceName();
        }
        d.toggleMainTitleView();

        switch (value) {
            case 1:
            case 2:
            case 3:
            case 4:
                searchConnectRoute(routeType = value, goal);
                break;
            case 5:
                setGoal(goal);
                break;
            case 6:
                previewRoute(new LatLng(goal.lat, goal.lng));
                break;
            case 7:
                shareLocation(goal);
                break;
        }

        d.hide();
    }

    public void onEditTitleEntered(EditText editText) {
        hideKeyboard(editText);
    }

    private void parseIntent() {
        Intent i = this.getIntent();
        if (i == null) return;

        Uri uri = i.getData();
        if (uri == null) return;

        String url = uri.getPath();

        final Entry entry = Entry.getInstance(url);
        if (entry != null) {
            if (entry.name.equals("")) {
                entry.name = entry.detail;
                entry.detail = "";
            } else {
                String swapString = entry.detail;
                entry.detail = entry.name;
                entry.name = swapString;
            }
            // facility share entry
            if (entry.identity != 0) {
                HashMap<String, String> params = new HashMap<>();
                params.put("entry_id", entry.identity + "");
                OpenURI.open(this, "/api/goals/set", "GET", params, null);
            }
            setGoal(entry);
        }
    }

    private void previewRoute(LatLng previewLocation) {
        showFragment(showFragmentType = ShowFragmentType.PREVIEW);
        previewStreetViewLayer.setLatLng(previewLocation);
        previewStreetViewLayer.setPositionFlag = false;
    }

    private void shareLocation(final Entry goalEntry) {
        final ProgressDialog prog = new ProgressDialog(this);
        prog.setMessage(Html.fromHtml("<big>URLを生成しています</big>"));
        prog.show();

        String latString = "lat=" + goalEntry.lat;
        String lngString = "&lng=" + goalEntry.lng;
        String buildingString = "&building=" + urlEncode(goalEntry.detail);
        String roomString = "&room=" + urlEncode(goalEntry.name);
        String encodeString = latString + lngString + buildingString + roomString + "&:-D";
        final String shareString = "http://campus-ar.jp/@/" + Base64.encodeToString(encodeString.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        Log.d("TKS share", shareString);

        final Handler handler = new Handler();

        URLShortenUtil.shorten(shareString, url -> {
            if (!prog.isShowing()) {
                return;
            }
            handler.post(prog::hide);

            if (url == null) {
                url = shareString;
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, "「" + goalEntry.name + "」の行き方");
            intent.putExtra(Intent.EXTRA_TEXT, url);
            intent.setType("text/plain");

            startActivity(Intent.createChooser(intent, "場所をシェアする"));
        });
    }

    private static String urlEncode(String v) {
        try {
            return URLEncoder.encode(v, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        return v;
    }

    public boolean onEditorAction(TextView tv, int id, KeyEvent event) {
        if (id == EditorInfo.IME_ACTION_SEARCH) {
            String str = queryTextField.getText().toString();
            savedQueryString = str;
            hideKeyboard(queryTextField);
            Intent i = new Intent(NavigationPage.this, SearchResultPage.class);
            i.putExtra("query", str);
            i.putExtra("facility", facilityIndex);
            startActivityForResult(i, SHOULD_RETURN_A_GOAL_ENTRY);
            return true;
        }
        return false;
    }

    public void onFocusChange(View view, boolean b) {
        if (b && goal != null) {
            unsetGoal(savedQueryString);
        }
    }

    private void hideKeyboard(EditText field) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(field.getWindowToken(), 0);
        field.clearFocus();
    }

    private void refreshFavoriteButton() {
        if (goal == null) return;
        if (new BookmarkUtil(this).isFavorited(goal.identity)) {
            favoriteButton.setImageResource(R.drawable.toolbar_favorite_on);
        } else {
            favoriteButton.setImageResource(R.drawable.toolbar_favorite_off);
        }
    }

    private void setFacilityIndex(int index) {
        Facility facility = FacilityUtil.getFacilityByIndex(index);
        if (facility == null) return;

        queryTextField.setHint(facility.name + " から検索する");
        facilityIndex = index;
    }

    private void setGoal(Entry entry) {
        new BookmarkUtil(this).pushHistory(entry);

        goal = entry;
        expandSearchBackground(true, false);
        queryTextField.setText(entry.name);
        queryTextField.clearFocus();
        detailLabel.setText(entry.detail);
        distanceLabel.setText("N/A");
        refreshFavoriteButton();
        searchConnectRoute(routeType = 1, goal);

        showFragmentType = ShowFragmentType.MAP;
        arLayer.setGoal(goal);
        mapLayer.setGoal(goal);
    }

    private void unsetGoal(String defaultText) {
        goal = null;
        expandSearchBackground(false, true);
        routeButton.setVisibility(View.GONE);
        queryTextField.setText(defaultText);

        arLayer.unsetGoal();
        mapLayer.unsetGoal();
    }

    private void searchConnectRoute(int routeType, Entry to) {
        HashMap<String, String> params = new HashMap<String, String>();
        to.type = routeType;
        params.put("type", routeType + "");

        // 研究学園駅
//        params.put("from_lat", 36.082094 + "");
//        params.put("from_lng", 140.082417 + "");

//        params.put("from_lat", 35.182713 + "");
//        params.put("from_lng", 136.973398 + "");

//        params.put("from_lat", 36.225325 + "");
//        params.put("from_lng", 140.465333 + "");

        // つくば駅
//        params.put("from_lat", 36.082581 + "");
//        params.put("from_lng", 140.111390 + "");

        params.put("from_lat", SensorUtil.getInstance(this, handler).getLat() + "");
        params.put("from_lng", SensorUtil.getInstance(this, handler).getLng() + "");

        params.put("to_entry_id", to.identity + "");
        params.put("facility_id", facilityIndex + "");

        showFragmentType = ShowFragmentType.MAP;

//        RouteUtil.calcRoute(new LatLng(36.082094, 140.082417), new LatLng(36.082581, 140.111390), (route) -> {
//            arLayer.setRoute(route);
//            mapLayer.setRoute(route);
//            int distance = Integer.parseInt(route.length);
//            distanceLabel.setText("" + String.valueOf(distance) + "m");
//        });


        OpenURI.open(this, "/api/routes/search", "GET", params, (success, data) -> {
            Route facilityRoute = new Gson().fromJson(data, Route.class);
            LatLng startLatLng = new LatLng(SensorUtil.getInstance(this, handler).getLat(), SensorUtil.getInstance(this, handler).getLng());
//            LatLng startLatLng = new LatLng(36.082094, 140.082417);
//            LatLng startLatLng = new LatLng(36.225325, 140.465333);

            // つくば駅
//            LatLng startLatLng = new LatLng(36.082581, 140.111390);

            LatLng goalLatLng;
            // 施設までの経路に対応していない場合
            if (!success || data.contains("error")) {
                Log.d("tks", "facility route search error: may be public route search only.");
                goalLatLng = new LatLng(to.lat, to.lng);

                RouteUtil.calcRoute(startLatLng, goalLatLng, (route) -> {
                    arLayer.setRoute(route);
                    mapLayer.setRoute(route);
                    int distance = Integer.parseInt(route.length);
                    distanceLabel.setText("" + String.valueOf(distance) + "m");
                });
            } else {
                Log.d("tks", "calculate facility entrance node");
                int enterCount = 0;
                for (Route.Coordinate entranceCoordinate : entranceNode.coordinates) {
                    for (Route.Coordinate connectRouteCoordinate : facilityRoute.coordinates) {
                        if (entranceCoordinate.lat == connectRouteCoordinate.lat && entranceCoordinate.lng == entranceCoordinate.lng) {
                            enterCount++;
                        }
                    }
                }
                routeButton.setVisibility(facilityRoute.coordinates != null && facilityRoute.coordinates.length != 0 ? View.VISIBLE : View.GONE);
                if (facilityRoute.coordinates.length == 0) {
                    Toast.makeText(NavigationPage.this, "目的地が見つかりませんでした。", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    goalLatLng = new LatLng(facilityRoute.coordinates[1].lat, facilityRoute.coordinates[1].lng);
                }


                final Route finalFacilityRoute = facilityRoute;
                if (enterCount > 0) {
//                    Log.d("tksaa", "search from facility enterCount = " + enterCount);
//                    Toast.makeText(NavigationPage.this, "施設外から検索しました", Toast.LENGTH_LONG).show();
                    RouteUtil.calcRoute(startLatLng, goalLatLng, (route) -> {
                        arLayer.setRoute(finalFacilityRoute, route);
                        mapLayer.setRoute(finalFacilityRoute, route);
                        int distance = Integer.parseInt(finalFacilityRoute.length) + Integer.parseInt(route.length);
                        distanceLabel.setText("" + String.valueOf(distance) + "m");
                    });
                } else {
//                    Log.d("tksaa", "search from public enterCount = " + enterCount);
//                    Toast.makeText(NavigationPage.this, "施設内から検索しました", Toast.LENGTH_LONG).show();
                    arLayer.setRoute(finalFacilityRoute);
                    mapLayer.setRoute(finalFacilityRoute);
                    int distance = Integer.parseInt(finalFacilityRoute.length);
                    distanceLabel.setText("" + String.valueOf(distance) + "m");
                }
            }
        });
    }

    private void expandMenu(final boolean expand, boolean anime) {
        if (anime && isMenuAnimating) return;
        isMenuAnimating = true;
        int fromLeft = DimenUtil.dp2px(this, expand ? -200 : 0);
        int toLeft = DimenUtil.dp2px(this, expand ? 0 : -200);
        float fromAlpha = expand ? 0 : 1f;
        float toAlpha = expand ? 1f : 0;
        int duration = anime ? ANIMATION_DURATION : 0;
        MarginLeftAnimation marginAnime = new MarginLeftAnimation(menuContainer, fromLeft, toLeft);
        AlphaAnimation coverAnime = new AlphaAnimation(fromAlpha, toAlpha);
        marginAnime.setDuration(duration);
        coverAnime.setDuration(duration);
        rootContainer.startAnimation(marginAnime);
        menuCoverButton.startAnimation(coverAnime);
        coverAnime.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                if (expand) {
                    menuCoverButton.setVisibility(View.VISIBLE);
                }
            }

            public void onAnimationEnd(Animation animation) {
                if (!expand) {
                    menuCoverButton.setVisibility(View.GONE);
                }
                isMenuAnimating = false;
            }

            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void expandSearchBackground(final boolean expand, boolean anime) {
        if (anime && isToolbarAnimating) return;
        isToolbarAnimating = true;
        int h0 = DimenUtil.dp2px(this, 66);
        int h1 = DimenUtil.dp2px(this, 66 + 24);
        int duration = anime ? ANIMATION_DURATION : 0;
        AlphaAnimation expandAnime = expand ? new AlphaAnimation(0, 1) : new AlphaAnimation(1, 0);
        AlphaAnimation contraAnime = expand ? new AlphaAnimation(1, 0) : new AlphaAnimation(0, 1);
        HeightAnimation heightAnime = expand ? new HeightAnimation(backgroundImage, h0, h1) : new HeightAnimation(backgroundImage, h1, h0);
        expandAnime.setDuration(duration);
        contraAnime.setDuration(duration);
        heightAnime.setDuration(duration);
        cancelButton.startAnimation(expandAnime);
        favoriteButton.startAnimation(expandAnime);
        detailLabel.startAnimation(expandAnime);
        distanceLabel.startAnimation(expandAnime);
        rootContainer.startAnimation(heightAnime);
        expandAnime.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                if (expand) {
                    cancelButton.setVisibility(View.VISIBLE);
                    favoriteButton.setVisibility(View.VISIBLE);
                    detailLabel.setVisibility(View.VISIBLE);
                    distanceLabel.setVisibility(View.VISIBLE);
                }
            }

            public void onAnimationEnd(Animation animation) {
                if (!expand) {
                    cancelButton.setVisibility(View.GONE);
                    favoriteButton.setVisibility(View.GONE);
                    detailLabel.setVisibility(View.GONE);
                    distanceLabel.setVisibility(View.GONE);
                }
                isToolbarAnimating = false;
            }

            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showFragment(ShowFragmentType type) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        switch (type) {
            case MAP:
                if (mapLayer.isHidden()) {
                    Log.d("tks", "map fragment show");
                    transaction.show(mapLayer);
                    transaction.hide(arLayer);
                    transaction.hide(previewStreetViewLayer);
                    transaction.hide(popUpStreetViewLayer);
                }
                break;
            case AR:
                if (arLayer.isHidden()) {
                    Log.d("tks", "ar fragment show");
                    transaction.show(arLayer);
                    transaction.hide(mapLayer);
                    transaction.hide(previewStreetViewLayer);
                    transaction.hide(popUpStreetViewLayer);

                    previewStreetViewLayer.showAR();
                    popUpStreetViewLayer.showAR();
                }
                break;
            case PREVIEW:
                if (previewStreetViewLayer.isHidden()) {
                    Log.d("tks", "preview fragment show");
                    transaction.show(previewStreetViewLayer);
                    transaction.hide(mapLayer);
                    transaction.hide(arLayer);
                    transaction.hide(popUpStreetViewLayer);

                    previewStreetViewLayer.hideAR();
                    popUpStreetViewLayer.showAR();
                }
                break;
            case POP_UP:
                if (popUpStreetViewLayer.isHidden()) {
                    Log.d("tks", "pop up fragment show");
                    transaction.show(popUpStreetViewLayer);
                    transaction.show(mapLayer);
                    transaction.hide(arLayer);
                    transaction.hide(previewStreetViewLayer);

                    popUpStreetViewLayer.hideAR();
                    previewStreetViewLayer.showAR();
                }
                break;
            default:
                break;
        }
        transaction.commit();
    }

    protected void onResume() {
        super.onResume();
        SensorUtil.getInstance(this, handler).setOnSensorChangedListener(this);
        SensorUtil.getInstance(this, handler).start();
    }

    protected void onPause() {
        super.onPause();
        SensorUtil.getInstance(this, handler).stop();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_DOWN) {
            if (e.getKeyCode() == 4) {
                showFragment(showFragmentType = ShowFragmentType.MAP);
            }
        }
        return super.dispatchKeyEvent(e);
    }
}