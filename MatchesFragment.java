package com.domaininstance.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.domaininstance.R;
import com.domaininstance.config.Constants;
import com.domaininstance.config.Request;
import com.domaininstance.data.api.ApiServices;
import com.domaininstance.data.api.RetrofitConnect;
import com.domaininstance.data.model.CommonParser;
import com.domaininstance.data.model.SearchResultsModel;
import com.domaininstance.database.SharedPreferenceData;
import com.domaininstance.helpers.CustomButton;
import com.domaininstance.helpers.RecyclerItemDecoration;
import com.domaininstance.ui.activities.ChatScreen;
import com.domaininstance.ui.activities.CommonListingActivity;
import com.domaininstance.ui.activities.FilterRefineActivity;
import com.domaininstance.ui.activities.HomeScreenActivity;
import com.domaininstance.ui.activities.PaymentOffersActivity;
import com.domaininstance.ui.adapter.SearchProfileAdapter;
import com.domaininstance.ui.interfaces.ApiRequestListener;
import com.domaininstance.ui.interfaces.ProfileSearchInterface;
import com.domaininstance.utils.CommonServiceCodes;
import com.domaininstance.utils.CommonUtilities;
import com.domaininstance.utils.ExceptionTrack;
import com.domaininstance.utils.GAAnalyticsOperations;
import com.domaininstance.utils.PermissionsHelper;
import com.domaininstance.utils.TimeElapseUtils;
import com.domaininstance.utils.UrlGenerator;
import com.domaininstance.utils.WebServiceUrlParameters;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;


@SuppressLint("ValidFragment")
@SuppressWarnings("deprecation")
public class MatchesFragment extends Fragment implements OnClickListener, ApiRequestListener {

    private FrameLayout mFrameLayout;
    private Snackbar snackbar;
    private Snackbar.SnackbarLayout snakLayout;
    private Activity context = null;
    private View rootView = null;
    public static int page = 1;// ask palani - v2.19
    public static int flag = 0; // ask palani ""
    public static String searchId = "", latitude = "0.0", longitude = "0.0"; // ask palani ""
    public static RecyclerView recyclerView = null; // ask palani - v2.19 - but as of now , since inbox hided in matches dont worry
    private ListScrollListener mListScrollListener;
    private TextView listItemPosition = null, connection_timeout, tvFilterRefine;
    private LinearLayout latestMatches_lstpos_layout = null, latestMatches_layout = null;
    private RelativeLayout connection_timeout_id = null;
    private String requestUrl = null;
    private boolean isloading = false,
            mIsTabChange = false, isLocationAllow = true, mIsLocationAvail = false;
    private ProgressBar loading = null;
    private ArrayList<String> paramValues;
    private String from = "";
    private SearchResultsModel searchResultsModel;
    private String scrollDataValue = "", mStrFilterType = "";
    private static Call<SearchResultsModel> matchesApiCall;
    private ArrayMap<String, String> getParameters = new ArrayMap<>();
    private int profileFirstVisibleItem;
    private int profileLastVisibleItem;
    private ApiServices RetroApiCall = RetrofitConnect.getInstance().retrofit(UrlGenerator.getRetrofitBaseUrl(0));
    private ApiRequestListener mListener = this;
    private ProfileSearchInterface mProfsearchInter;
    private ViewPager viewPager;
    private int selectedTab = 0;
    public static int commTotalCnt = 0;
    private boolean isExtendedMatches = false;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    int REQUEST_CHECK_SETTINGS = 999;
    LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    @SuppressLint("ValidFragment")
    private MatchesFragment(int seletedTab, ViewPager pager) {
        this.selectedTab = seletedTab;
        viewPager = pager;
    }

    @SuppressLint("ValidFragment")
    public MatchesFragment(String filterType, ProfileSearchInterface mProfsearchInter) {
        mStrFilterType = filterType;
        this.mProfsearchInter = mProfsearchInter;
    }

    public MatchesFragment() {
    }

    public static MatchesFragment newInstance(int selectedTab, ViewPager viewPager) {
        return new MatchesFragment(selectedTab, viewPager);
    }

    public void updateFragment() {
        setViewWhenNoResult();
    }

    public void disableBottomFilter(int... noTabchange) {
        if (noTabchange.length == 0)
            mIsTabChange = true;
        if (snackbar != null && snackbar.isShown())
            snackbar.dismiss();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        mListScrollListener = new ListScrollListener();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            rootView = inflater.inflate(R.layout.matches, container, false);
            if (getActivity() instanceof CommonListingActivity || (viewPager != null && viewPager.getCurrentItem() == selectedTab)) {
                if (Constants.alllistdata == null) {
                    Constants.alllistdata = new ArrayList<>();
                } else {
                    if (Constants.alllistdata != null) {
                        //if (!Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches))) {
                        Constants.alllistdata.clear();
                        Constants.alllistdata = null;
                        Constants.alllistdata = new ArrayList<>();
                        //}
                    } else {
                        Constants.alllistdata.clear();
                        Constants.alllistdata = null;
                        Constants.alllistdata = new ArrayList<>();
                    }
                }
                initializeView(rootView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            if (getActivity() instanceof CommonListingActivity || (viewPager != null && selectedTab == viewPager.getCurrentItem())) {
                if (Constants.SESSPAIDSTATUS != null && Constants.SESSPAIDSTATUS.isEmpty()) {
                    context = getActivity();
                    Constants.SESSPAIDSTATUS = SharedPreferenceData.getInstance().getDataInSharedPreferences(context, Constants.SESPAID_STAUS);
                    Constants.USER_GENDER = SharedPreferenceData.getInstance().getDataInSharedPreferences(context, Constants.GENDER);
                    Constants.MATRIID = SharedPreferenceData.getInstance().getDataInSharedPreferences(context, Constants.USER_MATRID);
                    Constants.COMMUNITYID = SharedPreferenceData.getInstance().getDataInSharedPreferences(context, Constants.COMMUNITY_ID);
                    Constants.PUBLISHSTATUS = SharedPreferenceData.getInstance().getDataInSharedPreferences(context, Constants.PUBLISH_STATUS);
                    Constants.LOGIN_DOMAIN_NAME = SharedPreferenceData.getInstance().getDataInSharedPreferences(context, Constants.DOMAIN_NAME);
                }
                Constants.isInboxActionDone = false;
                context.registerReceiver(callShortlistNodata, new IntentFilter("start.fragment.callShortlistNodataView"));
            }

            if (CommonUtilities.getInstance().isNetAvailable(getActivity()))
                checkFilterRefineOption();
            if (mStrFilterType.equalsIgnoreCase("search") ||
                    mStrFilterType.equalsIgnoreCase("searchagain")) {
                ViewCompat.setNestedScrollingEnabled(recyclerView, false);
            } else
                ViewCompat.setNestedScrollingEnabled(recyclerView, true);

            if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
                if (Constants.lastVisiItmPosListOrGrid > recyclerView.getAdapter().getItemCount())
                    recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());
                else
                    recyclerView.scrollToPosition(Constants.lastVisiItmPosListOrGrid);
                if (ViewProfileFragment.tempAllisData != null) {
                    ViewProfileFragment.tempAllisData = null;
                    ViewProfileFragment.isSimiarClick = false;
                }
            } else if (!CommonUtilities.getInstance().isNetAvailable(getActivity()))
                setViewWhenNoResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void checkFilterRefineOption() {
        if (CommonUtilities.getInstance().getNavigationTab() != null
                && (CommonUtilities.getInstance().getNavigationTab().getMenu().getItem(0).isChecked()
                && (setFrom() != null && (setFrom().equalsIgnoreCase("ALL")
                || setFrom().equalsIgnoreCase("LM")
                || setFrom().equalsIgnoreCase(context.getResources().getString(R.string.nearmatches))))) ||
                (CommonUtilities.getInstance().getNavigationTab().getMenu().getItem(3).isChecked()
                        && (mStrFilterType != null && (mStrFilterType.equalsIgnoreCase("searchagain")
                        || mStrFilterType.equalsIgnoreCase("search")))
                        && !CommonListingActivity.isSavedSearch && !FilterRefineFragment.moreClick)) {
            mIsTabChange = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showFilterRefineBottom();
                }
            }, 600);
            // }
        } else if (CommonUtilities.getInstance().getNavigationTab() != null
                && CommonUtilities.getInstance().getNavigationTab().getMenu().getItem(0).isChecked()
                && setFrom() != null &&
                (HomeScreenActivity.profileInfo.COOKIEINFO.WVMP_MASK.equalsIgnoreCase("1") && setFrom().equalsIgnoreCase("whoviewed")
                        || (HomeScreenActivity.profileInfo.COOKIEINFO.WSMP_MASK.equalsIgnoreCase("1") && setFrom().equalsIgnoreCase("whoshortlisted")))) {
            mIsTabChange = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDvmPromoBottom();
                }
            }, 600);
            if (getContext() != null && recyclerView != null) {
                ViewGroup.MarginLayoutParams marginLayoutParams =
                        (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
                marginLayoutParams.setMargins(0, 0, 0, (int) getContext().getResources().getDimension(R.dimen._35sdp));
                recyclerView.setLayoutParams(marginLayoutParams);
            }
        } else {
            disableBottomFilter();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.FILTER_REFINE_CODE && resultCode == RESULT_OK) {
            if (SharedPreferenceData.getInstance().getApplyFilterStatus(getContext())) {
                clearAllListCallRefine();
            } else if (FilterRefineFragment.isResetApplied) {
                if (!Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))
                    flag = 11;
                FilterRefineFragment.isResetApplied = false;
                clearAllListCallRefine();
            }
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                //callNearByMatches(true,100.00,90.00);
                accessLocation();
            } else {
                isLocationAllow = false;
                callNearByMatches(0.0, 0.0);
            }

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (matchesApiCall != null && !matchesApiCall.isExecuted())
                matchesApiCall.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        try {
            super.onPause();
            if (recyclerView != null) {
                if (!SharedPreferenceData.getInstance().getApplyFilterStatus(context))
                    flag = 0;
                recyclerView.stopScroll();
            }
            if (callShortlistNodata != null) {
                context.unregisterReceiver(callShortlistNodata);
                callShortlistNodata = null;
            }
            disableBottomFilter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver callShortlistNodata = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setViewWhenNoResult();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Initilize the all view
     */
    private void initializeView(View view) {
        try {
            //Constants.TOKENID = CommonUtilities.getInstance().getEncrptionKey();
            Constants.ENCRYPTEDMATRIID = CommonUtilities.getInstance().getEncryptMatriId(SharedPreferenceData.getInstance().getDataInSharedPreferences(context, Constants.USER_MATRID));
            Constants.TimeStamp = "";
            //     SearchProfileAdapter.EXTENDED_MATCHES_TOTALRESULT = 0;
            mFrameLayout = rootView.findViewById(R.id.frame_layout);
            loading = view.findViewById(R.id.loading);
            connection_timeout_id = view.findViewById(R.id.connection_timeout_id);
            connection_timeout = view.findViewById(R.id.connection_timeout);
            recyclerView = view.findViewById(R.id.recyclerview);
            listItemPosition = view.findViewById(R.id.listItemPosition); //only for list
            latestMatches_lstpos_layout = view.findViewById(R.id.latestMatches_lstpos_layout); //only for list
            latestMatches_layout = view.findViewById(R.id.latestMatches_layout); //only for list
            listItemPosition.setVisibility(View.GONE);
            setPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPage() {
        try {
            Constants.lastVisiItmPosListOrGrid = 0;
            connection_timeout_id.setVisibility(View.GONE);
            connection_timeout_id.setOnClickListener(this);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
            recyclerView.addItemDecoration(new RecyclerItemDecoration(30));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.addOnScrollListener(mListScrollListener);
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            if (CommonUtilities.getInstance().isNetAvailable(getActivity())) {
                if (getActivity() instanceof CommonListingActivity || (viewPager != null && selectedTab == viewPager.getCurrentItem())) {
                    if (SharedPreferenceData.getInstance().getApplyFilterStatus(getContext())) {
                        SearchProfileAdapter.EXTENDED_MATCHES_TOTALRESULT = 0;
                        if (setFrom().equalsIgnoreCase("ALL") && Constants.isMatchesRefineApplied) {
                            Constants.alllistdata = SharedPreferenceData.getInstance().getRefineMatchesArray(context, "saveRefineMatch");
                            page = 0;
                            commTotalCnt = Constants.alllistdata.size();
                            getExtendedMatches(Request.EXTENDED_MATCHES);
                            isloading = false;
                        } else if (setFrom().equalsIgnoreCase("LM") && Constants.isLatestMatchesRefineApplied) {
                            Constants.alllistdata = SharedPreferenceData.getInstance().getRefineMatchesArray(context, "saveRefineLatestMatch");
                            page = 0;
                            commTotalCnt = Constants.alllistdata.size();
                            getExtendedMatches(Request.EXTENDED_LATEST_MATCHES);
                            isloading = false;
                        } else if (setFrom().equalsIgnoreCase("ALL") && !Constants.isMatchesRefineApplied) {
                            commTotalCnt = 0;
                            clearAllListCallRefine();
                        } else if (setFrom().equalsIgnoreCase("LM") && !Constants.isLatestMatchesRefineApplied) {
                            commTotalCnt = 0;
                            clearAllListCallRefine();
                        } else {
                            Constants.alllistdata = new ArrayList<>();
                            matchesTask();
                        }
                    } else {
                        Constants.alllistdata = new ArrayList<>();
                        SearchProfileAdapter.EXTENDED_MATCHES_TOTALRESULT = 0;
                        commTotalCnt = 0;
                        matchesTask();
                    }
                }
            } else {
                CommonUtilities.getInstance().displayToastMessage(getResources().getString(R.string.network_msg), context);
                setViewWhenNoResult();
            }
            if (Constants.alllistdata.size() != 0) {
                CommonUtilities.getInstance().showProgressDialog(context, context.getResources().getString(R.string.loading_msg));
                Constants.searchProfileAdapter = new SearchProfileAdapter(context, setFrom());
                recyclerView.setAdapter(Constants.searchProfileAdapter);

                if (Constants.lastVisiItmPosListOrGrid != 0)
                    recyclerView.getLayoutManager().scrollToPosition(Constants.lastVisiItmPosListOrGrid);

                loading.setVisibility(View.GONE);
                CommonUtilities.getInstance().cancelProgressDialog(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearAllListCallRefine() {
        try {
            Constants.alllistdata.clear();
            Constants.alllistdata = null;
            Constants.alllistdata = new ArrayList<>();
            commTotalCnt = 0;
            SearchProfileAdapter.EXTENDED_MATCHES_TOTALRESULT = 0;
            if (SharedPreferenceData.getInstance().getApplyFilterStatus(getContext()))
                flag = 10;
            if (CommonUtilities.getInstance().isNetAvailable(getActivity())) {
                page = 1;
                Constants.lastVisiItmPosListOrGrid = 0;
                Constants.isInboxActionDone = false;
                connection_timeout_id.setVisibility(View.GONE);
                latestMatches_layout.setVisibility(View.VISIBLE);
                loading.setVisibility(View.VISIBLE);
                constructedParams(page, false);
            } else
                showNoFoundData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String setFrom() {
        try {
            if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.shortlisted)))
                from = "SP";
            else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches)))
                from = "LM";
            else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches)))
                from = "ALL";
            else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoViewedMyProfile)))
                from = "whoviewed";
            else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoShortlistedMe)))
                from = "whoshortlisted";
            else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.blockedProfiles)))
                from = Constants.PURPOSE_BLOCK;
            else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.ignoredProfiles)))
                from = Constants.PURPOSE_IGNORE;
            else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.viewedNotContacted)))
                from = "VN";
            else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.searchedProfiles)))
                from = context.getResources().getString(R.string.label_Search);
            else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))
                from = context.getResources().getString(R.string.nearmatches);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return from;
    }

    private void matchesTask() {
        try {
            if (CommonUtilities.getInstance().isNetAvailable(getActivity())) {
                if (Constants.alllistdata.size() == 0) {
                    if (matchesApiCall != null)
                        matchesApiCall.cancel();
                    page = 1;
                    Constants.lastVisiItmPosListOrGrid = 0;
                    Constants.isInboxActionDone = false;
                    connection_timeout_id.setVisibility(View.GONE);
                    latestMatches_layout.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.VISIBLE);
                    constructedParams(page, false);
                }
            } else
                setViewWhenNoResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        try {
            int visibleItemPosition;
            if (CommonUtilities.getInstance().isNetAvailable(context) &&
                    getActivity() instanceof CommonListingActivity || (viewPager != null && viewPager.getCurrentItem() == selectedTab)) {
                visibleItemPosition = firstVisibleItem + 1;

                if ((!Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.blockedProfiles)))
                        && (!Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.ignoredProfiles)))) {
                    listItemPosition.setVisibility(View.GONE);

                    if (SharedPreferenceData.getInstance().getDataInSharedPreferences(context, "show_promo")
                            .equalsIgnoreCase("1")) {
                        int reduceVal = 0;
                        int i = 0;
                        List<Integer> listPromoPos = SharedPreferenceData.getInstance().getIntListSharedPref(context, "promo_pos");
                        while (i < listPromoPos.size()) {
                            int insertPos = listPromoPos.get(i);
                            if (firstVisibleItem >= insertPos) {
                                reduceVal = reduceVal + 1;
                            }
                            i++;
                        }

                        visibleItemPosition = visibleItemPosition - reduceVal;
                    }

                    if (SearchProfileAdapter.EXTENDED_MATCHES_TOTALRESULT > 0
                            && visibleItemPosition > commTotalCnt
                            && (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches))
                            || Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches))))
                        visibleItemPosition = visibleItemPosition - 1;


                    if (Constants.searchProfileAdapter.getItemViewType(firstVisibleItem) == Constants.VIEW_TYPE_LIST_ITEM ||
                            Constants.searchProfileAdapter.getItemViewType(firstVisibleItem) == Constants.VIEW_DVM_LIST)
                        listItemPosition.setVisibility(View.VISIBLE);

                    listItemPosition.setText("" + visibleItemPosition);
                } else {
                    if (Constants.searchProfileAdapter.getItemViewType(firstVisibleItem) == Constants.VIEW_TYPE_PAYMENT_PROMO)
                        listItemPosition.setVisibility(View.GONE);
                    else
                        listItemPosition.setVisibility(View.VISIBLE);
                    listItemPosition.setText("" + visibleItemPosition);
                }

                Constants.lastVisiItmPosListOrGrid = profileLastVisibleItem;
                if (firstVisibleItem + visibleItemCount == (totalItemCount) && totalItemCount != 0) {
                    if (!isloading && isAdded()) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!isExtendedMatches && !(mStrFilterType.equalsIgnoreCase("search") ||
                                        mStrFilterType.equalsIgnoreCase("searchagain")) &&
                                        !(Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))) {
                                    isloading = true;
                                    page = page + 1;
                                    Constants.searchProfileAdapter.showLoading(true);
                                    Constants.searchProfileAdapter.notifyDataSetChanged();
                                    constructedParams(page, true);
                                    matchesApiCall = RetroApiCall.getMatches(requestUrl, getParameters);
                                    RetrofitConnect.getInstance().AddToEnqueue(matchesApiCall, mListener, Request.MATCHES_SCROLL);
                                    disableBottomFilter();
                                } else if ((Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))) {
                                    isloading = true;
                                    page = page + 1;
                                    Constants.searchProfileAdapter.showLoading(true);
                                    Constants.searchProfileAdapter.notifyDataSetChanged();
                                    constructedParams(page, true);
                                } else if ((mStrFilterType.equalsIgnoreCase("search") ||
                                        mStrFilterType.equalsIgnoreCase("searchagain"))) {
                                    if (!isExtendedMatches) {
                                        isloading = true;
                                        page = page + 1;
                                        Constants.searchProfileAdapter.showLoading(true);
                                        Constants.searchProfileAdapter.notifyDataSetChanged();
                                        constructedParams(page, true);
                                        matchesApiCall = RetroApiCall.getMatches(requestUrl, getParameters);
                                        RetrofitConnect.getInstance().AddToEnqueue(matchesApiCall, mListener, Request.MATCHES_SCROLL);
                                        disableBottomFilter();
                                    }
                                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches))) {
                                    getExtendedMatches(Request.EXTENDED_LATEST_MATCHES_SCROLL);
                                    disableBottomFilter();
                                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches))) {
                                    getExtendedMatches(Request.EXTENDED_MATCHES_SCROLL);
                                    disableBottomFilter();
                                }
                            }
                        });
                    }
                }
            } else
                CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connection_timeout_id:
                if (CommonUtilities.getInstance().isNetAvailable(context)) {
                    if (connection_timeout.getText().toString().equalsIgnoreCase(getResources().getString(R.string.network_msg) + " - " + "tap to retry"))
                        matchesTask();
                } else
                    CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);
                break;
            default:
                break;
        }
    }

    private void matchesApiCall(String ApiUrl, ArrayMap<String, String> RequestFor, int Reqtype) {
        try {
            matchesApiCall = RetroApiCall.getMatches(ApiUrl, RequestFor);
            RetrofitConnect.getInstance().AddToEnqueue(matchesApiCall, mListener, Reqtype);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void constructedParams(int page, boolean fromScroll) {
        try {
            disableBottomFilter();
            paramValues = new ArrayList<>();
            paramValues.add("" + page);
            isExtendedMatches = false;
            int matchReqType = 0;
            if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.searchedProfiles)) && flag == 16) { //
                flag = 16;
                paramValues.add(searchId);
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.LATEST_MATCHES);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.MATCHES_SERCHID);
                matchReqType = Request.MATCHES_SERCHID;
            } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.searchedProfiles)))//Refine search
            {
                paramValues.add(CommonServiceCodes.getInstance().frameEncodePP(mStrFilterType));
                paramValues.add(FilterRefineFragment.keywordSearch != null ? FilterRefineFragment.keywordSearch : "");
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.LATEST_MATCHES);
                if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))
                    getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.NEARBY_MATCHES_REFINESEARCH);
                else
                    getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.MATCHES_REFINESEARCH);
                matchReqType = Request.MATCHES_REFINESEARCH;
            }
            if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.premium))) {
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.PREMIUM);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.PREMIUM);
                matchReqType = Request.PREMIUM;
            }
           else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.mutual))) {
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.MUTUAL);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.MUTUAL);
                matchReqType = Request.MUTUAL;
            }
           if(Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.prefprofession))){
               requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.DISCOVER_MATCHES);
               getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.DISCOVER_MATCHES);
               matchReqType = Request.DISCOVER_MATCHES;
           }
            else if ((Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches)) ||
                    Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches))) && flag == 10) {
                //************   Filter Fields   ************//
                isExtendedMatches = false;
                paramValues.add(CommonServiceCodes.getInstance().frameEncodePP(mStrFilterType));
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.LATEST_MATCHES);
                if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches)))
                    getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.FILTER_REFINE_LATESTMATCHES);
                else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches)))
                    getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.FILTER_REFINE_MATCHES);
                matchReqType = Request.MATCHES_REFINESEARCH;
            } else if (flag == 9) { // REMOVE THIS BLOCK FOR TODAY MATCHES
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.LATEST_MATCHES);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.MATCHES_TODAY);
                matchReqType = Request.MATCHES_TODAY;
            } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches))) { // FOR LATEST MATCHES
                if (Constants.isTodayMatches)
                    paramValues.add(Constants.TODAY_MATCHES_DAYS);
                else
                    paramValues.add(Constants.LATEST_MATCHES_DAYS);

                TimeElapseUtils.getInstance(context).trackStart(getString(R.string.name_page_load),
                        new TimeElapseUtils.TimeTrack(TimeUnit.MILLISECONDS,
                                getString(R.string.home),
                                getString(R.string.name_page_load),
                                getString(R.string.category_Latest_Matches)));

                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.LATEST_MATCHES);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.MATCHES_LATEST);
                matchReqType = Request.MATCHES_LATEST;
            } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches)) || flag == 11)//FOR ALL MATCHES
            {
                TimeElapseUtils.getInstance(context).trackStart(getString(R.string.name_page_load),
                        new TimeElapseUtils.TimeTrack(TimeUnit.MILLISECONDS,
                                getString(R.string.home),
                                getString(R.string.name_page_load),
                                getString(R.string.category_All_Matches)));
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.LATEST_MATCHES);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.MATCHES_ALL);
                matchReqType = Request.MATCHES_ALL;

            } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.viewedNotContacted))) {// Need FOR VIEWED NOT CONTACTED
                TimeElapseUtils.getInstance(context).trackStart(getString(R.string.name_page_load),
                        new TimeElapseUtils.TimeTrack(TimeUnit.MILLISECONDS,
                                getString(R.string.home),
                                getString(R.string.name_page_load),
                                getString(R.string.category_ViewedNotContacted_Profiles)));

                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.VIEWED_NOTCONTACTED);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.VIEWED_NOTCONTACTED);
                matchReqType = Request.VIEWED_NOTCONTACTED;
            } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.shortlisted))) {//FOR SHORTLISTED PROFILES
                TimeElapseUtils.getInstance(context).trackStart(getString(R.string.name_page_load),
                        new TimeElapseUtils.TimeTrack(TimeUnit.MILLISECONDS,
                                getString(R.string.home),
                                getString(R.string.name_page_load),
                                getString(R.string.category_Shotlist_Profiles)));

                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.SHORTLIST_PROFILES);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.SHORTLIST_PROFILES);
                matchReqType = Request.SHORTLIST_PROFILES;
            } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoViewedMyProfile))) {//WHO VIEWED MY PROFILE
                TimeElapseUtils.getInstance(context).trackStart(getString(R.string.name_page_load),
                        new TimeElapseUtils.TimeTrack(TimeUnit.MILLISECONDS,
                                getString(R.string.home),
                                getString(R.string.name_page_load),
                                getString(R.string.category_Viewed_My_Profile)));
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.WHO_VIEWED_SHORTLISTED);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.WHO_VIEWED);
                matchReqType = Request.WHO_VIEWED;
            } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoShortlistedMe))) {//WHO SHORTLISTED MY PROFILE
                TimeElapseUtils.getInstance(context).trackStart(getString(R.string.name_page_load),
                        new TimeElapseUtils.TimeTrack(TimeUnit.MILLISECONDS,
                                getString(R.string.home),
                                getString(R.string.name_page_load),
                                getString(R.string.category_Who_Shortlisted_Me)));
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.WHO_VIEWED_SHORTLISTED);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.WHO_SHORTLISTED);
                matchReqType = Request.WHO_SHORTLISTED;
            } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.blockedProfiles))) {//BLOCKED PROFILES
                TimeElapseUtils.getInstance(context).trackStart(getString(R.string.name_page_load),
                        new TimeElapseUtils.TimeTrack(TimeUnit.MILLISECONDS,
                                getString(R.string.home),
                                getString(R.string.name_page_load),
                                getString(R.string.category_Blocked_Profiles)));
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.SHORTLIST_PROFILES);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.BLOCKED_PROFILES);
                matchReqType = Request.BLOCKED_PROFILES;
            } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.ignoredProfiles))) {//IGNORED PROFILES
                TimeElapseUtils.getInstance(context).trackStart(getString(R.string.name_page_load),
                        new TimeElapseUtils.TimeTrack(TimeUnit.MILLISECONDS,
                                getString(R.string.home),
                                getString(R.string.name_page_load),
                                getString(R.string.category_Ignored_Profiles)));
                requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.SHORTLIST_PROFILES);
                getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.IGONERED_PROFILES);
                matchReqType = Request.IGONERED_PROFILES;
            } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches))) { // FOR NEARBY MATCHES
                if (!isLocationAllow)
                    callNearByMatches(0.0, 0.0);
                else {
                    String[] permissionList;
                    permissionList = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                    PermissionsHelper.getInstance().requestPermissions(getActivity(), permissionList, new PermissionsHelper.PermissionCallback() {
                        @Override
                        public void onResponseReceived(final HashMap<String, PermissionsHelper.PermissionGrant> mapPermissionGrants) {
                            if (PermissionsHelper.getInstance().isPermissionGranted(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) &&
                                    PermissionsHelper.getInstance().isPermissionGranted(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                                accessLocation();
                            } else {
                                isLocationAllow = false;
                                callNearByMatches(0.0, 0.0);
                            }
                        }
                    });

                }
            }
            if ((!fromScroll && !Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.dashboard))
                    && !(Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))))
                matchesApiCall(requestUrl, getParameters, matchReqType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API
    private void accessLocation() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setExpirationDuration(TimeUnit.SECONDS.toMillis(10));

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mCurrentLocation = locationResult.getLastLocation();
                if (mCurrentLocation != null && !mIsLocationAvail) {
                    callNearByMatches(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());
                    stopLocationUpdates();
                    mIsLocationAvail = true;
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (mCurrentLocation != null) {
                    callNearByMatches(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());
                    stopLocationUpdates();
                } else {
                    accessLocation();
                    mIsLocationAvail = false;
                }
            }

        };

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context).checkLocationSettings(mLocationSettingsRequest);
        result.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {

            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    mFusedLocationClient.requestLocationUpdates(locationRequest,
                            mLocationCallback, Looper.myLooper());


                }
            }
        }).addOnFailureListener(context, new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        try {
                            if (resolvable != null)
                                resolvable.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        stopLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        stopLocationUpdates();
                        break;
                    default:
                        stopLocationUpdates();
                        break;
                }
            }
        });
    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                    .addOnCompleteListener(context, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
        }

    }

    private void callNearByMatches(Double Longitude, Double Latitude) {
        if (isAdded()) {
            //  paramValues = new ArrayList<>();
            int request = Request.NEARBY_MATCHES;
            latitude = String.valueOf(Latitude);
            longitude = String.valueOf(Longitude);
            TimeElapseUtils.getInstance(context).trackStart(getString(R.string.name_page_load),
                    new TimeElapseUtils.TimeTrack(TimeUnit.MILLISECONDS,
                            getString(R.string.home),
                            getString(R.string.name_page_load),
                            getString(R.string.category_NearBy_Matches)));
            requestUrl = UrlGenerator.getRetrofitRequestUrlForPost(Request.NEARBY_MATCHES);
            if (flag == 10) {
                paramValues.add(CommonServiceCodes.getInstance().frameEncodePP(mStrFilterType));
                request = Request.NEARBY_MATCHES_REFINESEARCH;
            }
            paramValues.add(latitude);
            paramValues.add(longitude);
            getParameters = WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, request);
            if (isloading)
                matchesApiCall(requestUrl, getParameters, Request.MATCHES_SCROLL);
            else
                matchesApiCall(requestUrl, getParameters, request);
        }
    }


    private void setViewWhenException() {
        Toast.makeText(context, "Sorry time out", Toast.LENGTH_SHORT).show();
    }

    public void setViewWhenNoResult() {
        if (Constants.alllistdata != null && Constants.alllistdata.size() <= 0) {
//            connection_timeout_id.setOnClickListener(this);
            connection_timeout_id.setVisibility(View.VISIBLE);
            latestMatches_lstpos_layout.setVisibility(View.GONE);
            listItemPosition.setVisibility(View.GONE);
            latestMatches_layout.setVisibility(View.GONE);
            showNoFoundData();
        }
    }

    public void updateListOnCommChange() {
        try {
            MatchesFragment.recyclerView.invalidate();
            MatchesFragment.recyclerView.getRecycledViewPool().clear();
            MatchesFragment.recyclerView.refreshDrawableState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNoFoundData() {
        if (!CommonUtilities.getInstance().isNetAvailable(getActivity())) {
            connection_timeout.setText(getResources().getString(R.string.network_msg) + " - " + "tap to retry");
            disableBottomFilter();
        } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches))
                || Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches))
                || Constants.selectedTabName.equalsIgnoreCase(getString(R.string.whoShortlistedMe))
                || Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoViewedMyProfile))) {
            checkFilterRefineOption();
            if (SharedPreferenceData.getInstance().getApplyFilterStatus(context)) {
                connection_timeout.setText(context.getResources().getString(R.string.noprof_meet_filter));
            } else
                connection_timeout.setText(context.getResources().getString(R.string.nodatafound_desc));
        } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.viewedNotContacted)))//Need to change
        {
            connection_timeout.setText(context.getResources().getString(R.string.viewed_notcontacted_profile_desc));
        } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.shortlisted))) {
            connection_timeout.setText(context.getResources().getString(R.string.shortlist_profile_desc));
        } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoViewedMyProfile))) {
            connection_timeout.setText(context.getResources().getString(R.string.viewed_profile_desc));
        } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoShortlistedMe))) {
            connection_timeout.setText(context.getResources().getString(R.string.shortlist_me_profile_desc));
        } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.blockedProfiles))) {
            connection_timeout.setText(context.getResources().getString(R.string.blocked_profile_desc));
        } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.ignoredProfiles))) {
            connection_timeout.setText(context.getResources().getString(R.string.ignored_profile_desc));
        } else if (mStrFilterType.equalsIgnoreCase("search") || mStrFilterType.equalsIgnoreCase("searchagain")) {
            connection_timeout.setText(context.getResources().getString(R.string.noprof_meet_filter));
            checkFilterRefineOption();
            if (mProfsearchInter != null) {
                mProfsearchInter.profileSearchEmpyt();
            }
        } else {
            connection_timeout.setText(context.getResources().getString(R.string.nodatafound_search));
        }
    }

    private class ListScrollListener extends RecyclerView.OnScrollListener {
        int initialFirstVisibleItem = 0;

        public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
            try {
                String tempMatriid = "";
                if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    latestMatches_lstpos_layout.setVisibility(View.GONE);
                    checkFilterRefineOption();
                    if (Constants.SCROLLDATA_AVAILABLE.equalsIgnoreCase("0"))
                        return;
                    if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches))
                            || Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches)) ||
                            Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.viewedNotContacted))) {
                        scrollDataValue = "";

                        for (int pos = profileFirstVisibleItem; pos < profileLastVisibleItem; pos++) {
                            if (pos < Constants.alllistdata.size() && !tempMatriid.equalsIgnoreCase(Constants.alllistdata.get(pos).MATRIID)) {
                                scrollDataValue = scrollDataValue + Constants.alllistdata.get(pos).MATRIID + "|" + page + "|" + pos + "|" +
                                        Constants.alllistdata.get(pos).MIMASCORES
                                        + "|" + Constants.alllistdata.get(pos).PHOTOSTATUS + "~";
                                tempMatriid = Constants.alllistdata.get(pos).MATRIID;
                            }
                        }
                        scrollDataValue = (scrollDataValue.length() > 0 ? scrollDataValue.substring(0, scrollDataValue.length() - 1) : "");
                        if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches)))
                            scrollDataValue = "AND_LatestMatches." + UUID.randomUUID().toString().replace("-", "").substring(0, 10) + "-" + Constants.MATRIID
                                    + "@" + scrollDataValue;
                        else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches)))
                            scrollDataValue = "AND_AllMatches." + UUID.randomUUID().toString().replace("-", "").substring(0, 10) + "-" + Constants.MATRIID
                                    + "@" + scrollDataValue;
                        else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.viewedNotContacted)))
                            scrollDataValue = "AND_VNC." + UUID.randomUUID().toString().replace("-", "").substring(0, 10) + "-" + Constants.MATRIID
                                    + "@" + scrollDataValue;
                        else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.searchedProfiles)))
                            scrollDataValue = "AND_SEARCH." + UUID.randomUUID().toString().replace("-", "").substring(0, 10) + "-" + Constants.MATRIID
                                    + "@" + scrollDataValue;// ask palani - refinesearch - scroll - tab name ? - for v2.19

                        if (Constants.SCROLLDATA_AVAILABLE.trim().length() != 0 && Constants.SCROLL_DATA_URL.trim().length() != 0) {
                            if (Constants.SCROLLDATA_AVAILABLE.equalsIgnoreCase("1")) {
                                if (Constants.mSocket == null || !Constants.mSocket.isConnected())
                                    ChatScreen.getInstance().enableOnlineActivityTask();
                                else if (Constants.mSocket != null && Constants.mSocket.isConnected()) {
                                    Constants.mSocket.emit("ltscrolldata", new JSONObject().put("strdata", scrollDataValue)
                                            .put("id", Constants.MATRIID).put("AppVersion", Constants.AppVersion)
                                            .put("PackageName", Constants.PackageName).toString());
                                    scrollDataValue = "";
                                }
                            } else if (Constants.SCROLLDATA_AVAILABLE.equalsIgnoreCase("2"))
                                sendScrollData();
                        }
                    }
                } else
                    latestMatches_lstpos_layout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            int profileVisibleItemCount = recyclerView.getChildCount();
            int profileTotalItemCount = lm.getItemCount();
            profileFirstVisibleItem = lm.findFirstVisibleItemPosition();
            profileLastVisibleItem = lm.findLastVisibleItemPosition();
            initialFirstVisibleItem = profileFirstVisibleItem;
            listScroll(profileFirstVisibleItem, profileVisibleItemCount, profileTotalItemCount);
            recyclerView.getParent().requestDisallowInterceptTouchEvent(true);
            if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches)) ||
                    Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches)) ||
                    Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)) ||
                    Constants.selectedTabName.equalsIgnoreCase(context.getString(R.string.whoViewedMyProfile)) ||
                    Constants.selectedTabName.equalsIgnoreCase(context.getString(R.string.whoShortlistedMe))) {
                if (dy > 0) {
                    /*if (snackbar != null && snackbar.isShown()) {
                        snackbar.dismiss();
                    }*/
                    disableBottomFilter(1);
                } else {
                    if (profileTotalItemCount > 0) {
                        checkFilterRefineOption();
                    } else {
                        /*if (snackbar != null && snackbar.isShown()) {
                            snackbar.dismiss();
                        }*/
                        disableBottomFilter(1);
                    }
                }
            } else {
                if (snackbar != null && snackbar.isShown() && mStrFilterType != null &&
                        !(mStrFilterType.equalsIgnoreCase("search") ||
                                mStrFilterType.equalsIgnoreCase("searchagain"))) {
                    /*snackbar.dismiss();*/
                    disableBottomFilter(1);
                }
                if (mStrFilterType != null && (mStrFilterType.equalsIgnoreCase("search") ||
                        mStrFilterType.equalsIgnoreCase("searchagain"))) {
                    if (dy > 0) {
                        mProfsearchInter.matchesScroll("down", initialFirstVisibleItem);
                        /*if (snackbar != null && snackbar.isShown()) {
                            snackbar.dismiss();
                        }*/
                        disableBottomFilter(1);
                    } else {
                        mProfsearchInter.matchesScroll("up", initialFirstVisibleItem);
                        /*if (snackbar != null && snackbar.isShown()) {
                            snackbar.dismiss();
                        }*/
                        disableBottomFilter(1);
                    }
                }

            }
        }
    }

    private synchronized void sendScrollData() {
        try {
            ArrayList<String> paramValues = new ArrayList<>();
            paramValues.add(scrollDataValue);

            Call<CommonParser> scrollDataApiCall = RetroApiCall.getCommonAPI(
                    UrlGenerator.getRetrofitRequestUrlForPost(Request.SCROLL_DATA_URL),
                    WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.SCROLL_DATA_CAPTURE)
            );
            RetrofitConnect.getInstance().AddToEnqueue(scrollDataApiCall, mListener, Request.SCROLL_DATA_URL);

        } catch (Exception e) {
            e.getMessage();
        }
    }


    @Override
    public void  onReceiveResult(int ReqType, Response response) {
        try {
            CommonUtilities.getInstance().cancelProgressDialog(context);
            switch (ReqType) {
                case Request.MATCHES_REFINESEARCH:
                case Request.NEARBY_MATCHES_REFINESEARCH:
                case Request.MATCHES_SERCHID:
                case Request.MATCHES_TODAY:
                case Request.MATCHES_LATEST:
                case Request.MATCHES_ALL:
                case Request.PREMIUM:
                case Request.MUTUAL:
                case Request.DISCOVER_MATCHES:
                    case Request.NEARBY_MATCHES:
                case Request.VIEWED_NOTCONTACTED:
                case Request.SHORTLIST_PROFILES:
                case Request.WHO_VIEWED:
                case Request.WHO_SHORTLISTED:
                case Request.BLOCKED_PROFILES:
                case Request.IGONERED_PROFILES:
                case Request.EXTENDED_MATCHES:
                case Request.EXTENDED_LATEST_MATCHES:
                    try {
                        searchResultsModel = RetrofitConnect.getInstance().dataConvertor(response, SearchResultsModel.class);
                        if (response != null) {
                            if (searchResultsModel.RESPONSECODE.equalsIgnoreCase("200")) {

                                if (Integer.parseInt(searchResultsModel.TOTALRESULTS) > 0) {
                                    if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.searchedProfiles))) {
                                        CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " Matching Profile(s)", context);
                                        mProfsearchInter.updateMatchesCount(searchResultsModel.TOTALRESULTS);
                                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches)) && ReqType == Request.MATCHES_LATEST)
                                        CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " Latest Matching Profile(s)", context);
                                    else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches))
                                            && ReqType == Request.MATCHES_ALL || ReqType == Request.MATCHES_REFINESEARCH)
                                        CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " All Matching Profile(s)", context);
                                    else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.viewedNotContacted)))//We need to change the content
                                        CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " Viewed and not contacted", context);
                                    else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.shortlisted)))
                                        CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " Shortlisted Profile(s)", context);
                                    else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoViewedMyProfile))) {
                                        if (!searchResultsModel.TOTALMATCHRESULTS.isEmpty()) {// ask palani
                                            CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " Members viewed you - " + searchResultsModel.TOTALMATCHRESULTS + " matches your preference", context);
                                        }
                                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoShortlistedMe))) {
                                        if (!searchResultsModel.TOTALMATCHRESULTS.isEmpty()) {
                                            CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " Members Shortlisted You - " + searchResultsModel.TOTALMATCHRESULTS + " matches your preference", context);
                                        }
                                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.blockedProfiles)))
                                        CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " Blocked Profile(s)", context);
                                    else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.ignoredProfiles)))
                                        CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " Ignored Profile(s)", context);
                                    else if ((Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))) {

                                        if ((longitude.equalsIgnoreCase("0.0") && latitude.equalsIgnoreCase("0.0"))) {
                                            CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.nearby_toast), context);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " Matching Profile(s)", context);
                                                }
                                            }, 3000);
                                        } else
                                            CommonUtilities.getInstance().displayToastMessage("" + searchResultsModel.TOTALRESULTS + " Matching Profile(s)", context);
                                    }

                                    if (!searchResultsModel.SRCHTIME.isEmpty()) {
                                        Constants.TimeStamp = searchResultsModel.SRCHTIME;
                                    }
                                    if ((Constants.alllistdata == null || page == 1) && ReqType != Request.EXTENDED_MATCHES &&
                                            ReqType != Request.EXTENDED_LATEST_MATCHES)
                                        Constants.alllistdata = new ArrayList<>();
                                    Constants.alllistdata.addAll(searchResultsModel.SEARCHRES.PROFILE);
                                    connection_timeout_id.setVisibility(View.GONE);
                                    latestMatches_layout.setVisibility(View.VISIBLE);
                                    listItemPosition.setVisibility(View.VISIBLE);
                                    Constants.searchProfileAdapter = new SearchProfileAdapter(context, setFrom());
                                    recyclerView.setAdapter(Constants.searchProfileAdapter);
                                    isloading = searchResultsModel.SEARCHRES.PROFILE.size() < 20;
                                    if (ReqType == Request.EXTENDED_MATCHES || ReqType == Request.EXTENDED_LATEST_MATCHES)
                                        Constants.lastVisiItmPosListOrGrid = 0;
                                    else if (Constants.lastVisiItmPosListOrGrid != 0)
                                        recyclerView.getLayoutManager().scrollToPosition(Constants.lastVisiItmPosListOrGrid);

                                    if (ReqType == Request.MATCHES_ALL || ReqType == Request.LATEST_MATCHES ||
                                            ReqType == Request.MATCHES_LATEST
                                            || (!mStrFilterType.equalsIgnoreCase("search")
                                            && ReqType == Request.MATCHES_REFINESEARCH)) {
                                        if (isloading) {
                                            page = 0;
                                            if (ReqType == Request.MATCHES_ALL ||
                                                    ReqType == Request.MATCHES_REFINESEARCH)
                                                getExtendedMatches(Request.EXTENDED_MATCHES);
                                            else if (ReqType == Request.LATEST_MATCHES || ReqType == Request.MATCHES_LATEST
                                                    || ReqType == Request.MATCHES_REFINESEARCH)
                                                getExtendedMatches(Request.EXTENDED_LATEST_MATCHES);
                                        } else if (!isloading && ReqType != Request.EXTENDED_MATCHES && ReqType != Request.EXTENDED_LATEST_MATCHES) {
                                            isExtendedMatches = false;
                                        }
                                    }
                                    /*else
                                        isloading = false;*/
                                    if (ReqType == Request.EXTENDED_MATCHES || ReqType == Request.EXTENDED_LATEST_MATCHES)
                                        SearchProfileAdapter.EXTENDED_MATCHES_TOTALRESULT = Integer.parseInt(searchResultsModel.TOTALRESULTS);
                                    else if (ReqType == Request.MATCHES_LATEST || ReqType == Request.MATCHES_ALL || ReqType == Request.MATCHES_REFINESEARCH
                                            || ReqType == Request.NEARBY_MATCHES_REFINESEARCH)
                                        commTotalCnt = Integer.parseInt(searchResultsModel.TOTALRESULTS);
                                    if (ReqType == Request.MATCHES_REFINESEARCH) {
                                        if (from.equalsIgnoreCase("ALL")) {
                                            SharedPreferenceData.getInstance().saveRefineMatchArray(context, Constants.alllistdata, "saveRefineMatch");
                                            Constants.isMatchesRefineApplied = true;
                                        } else if (from.equalsIgnoreCase("LM")) {
                                            SharedPreferenceData.getInstance().saveRefineMatchArray(context, Constants.alllistdata, "saveRefineLatestMatch");
                                            Constants.isLatestMatchesRefineApplied = true;
                                        }
                                       /* else if (from.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches))) {//save nearybylist
                                            SharedPreferenceData.getInstance().saveRefineMatchArray(context, Constants.alllistdata, "saveRefineNearMatch");
                                            Constants.isLatestMatchesRefineApplied = true;
                                        }*/
                                    }
                                    SharedPreferenceData.getInstance().saveRefineMatchArray(context, Constants.alllistdata, "saveMatchesResult");
                                    Constants.searchProfileAdapter.notifyDataSetChanged();
                                    CommonUtilities.getInstance().cancelProgressDialog(context);
                                    checkFilterRefineOption();
                                    if (mStrFilterType.equalsIgnoreCase("search") ||
                                            mStrFilterType.equalsIgnoreCase("searchagain")) {
                                        recyclerView.getLayoutManager().scrollToPosition(Constants.lastVisiItmPosListOrGrid);
                                    }
                                } else {
                                    setViewWhenNoResult();
                                }
                                loading.setVisibility(View.GONE);
                            } else if (searchResultsModel.RESPONSECODE.equalsIgnoreCase("626")) {
                                disableBottomFilter();
                                if (CommonUtilities.getInstance().isNetAvailable(context)) {
                                    CommonUtilities.getInstance().showProgressDialog(context, context.getResources().getString(R.string.loading_msg));
                                    String loginResponse = CommonUtilities.getInstance().loginIntoApp(context);
                                    if (loginResponse != null) {
                                        if (CommonUtilities.getInstance().checkLoginIsValidOrNot(loginResponse, context)) {
                                            Intent intent = new Intent(context, HomeScreenActivity.class);
                                            context.finish();
                                            startActivity(intent);
                                            CommonUtilities.getInstance().cancelProgressDialog(context);
                                        } else {
                                            CommonUtilities.getInstance().cancelProgressDialog(context);
                                        }
                                    }
                                }
                                loading.setVisibility(View.GONE);
                            } else {
                                setNoDataFound(ReqType);
                            }
                        } else if (searchResultsModel == null)
                            setViewWhenException();
                    } catch (Exception e) {
                        e.printStackTrace();
                        setViewWhenNoResult();
                    }
                    loading.setVisibility(View.GONE);
                    break;
                case Request.MATCHES_SCROLL:
                case Request.EXTENDED_MATCHES_SCROLL:
                case Request.EXTENDED_LATEST_MATCHES_SCROLL:
                    searchResultsModel = RetrofitConnect.getInstance().dataConvertor(response, SearchResultsModel.class);
                    mIsTabChange = false;
                    checkFilterRefineOption();
                    //jsonInString = RetrofitConnect.getInstance().objectMapper().writeValueAsString(searchResultsModel);
                    if (isAdded()) {
                        try {
                            if (CommonUtilities.getInstance().isServiceResponseValidOrNot(searchResultsModel.RESPONSECODE,
                                    searchResultsModel.ERRORDESC)) {

                                isloading = searchResultsModel.SEARCHRES.PROFILE.size() < 20;
                                getextendedMatchesScroll(ReqType);

                                if (ReqType == Request.EXTENDED_MATCHES_SCROLL || ReqType == Request.EXTENDED_LATEST_MATCHES_SCROLL)
                                    SearchProfileAdapter.EXTENDED_MATCHES_TOTALRESULT = Integer.parseInt(searchResultsModel.TOTALRESULTS);

                                if (Constants.alllistdata == null)
                                    Constants.alllistdata = new ArrayList<>();
                                Constants.searchProfileAdapter.showLoading(false);
                                Constants.alllistdata.addAll(searchResultsModel.SEARCHRES.PROFILE);
                                Constants.searchProfileAdapter.notifyDataSetChanged();
                                recyclerView.getLayoutManager().scrollToPosition(Constants.lastVisiItmPosListOrGrid);

                                if (ReqType == Request.MATCHES_SCROLL && flag == 10 &&
                                        !Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches))) {
                                    if (from.equalsIgnoreCase("ALL")) {
                                        SharedPreferenceData.getInstance().saveRefineMatchArray(context, Constants.alllistdata, "saveRefineMatch");
                                        Constants.isMatchesRefineApplied = true;
                                    } else if (from.equalsIgnoreCase("LM")) {
                                        SharedPreferenceData.getInstance().saveRefineMatchArray(context, Constants.alllistdata, "saveRefineLatestMatch");
                                        Constants.isLatestMatchesRefineApplied = true;
                                    }
                                }
                                SharedPreferenceData.getInstance().saveRefineMatchArray(context, Constants.alllistdata, "saveMatchesResult");
                            } else if (searchResultsModel.RESPONSECODE.equalsIgnoreCase("626")) {
                                if (CommonUtilities.getInstance().isNetAvailable(context)) {
                                    CommonUtilities.getInstance().showProgressDialog(context, context.getResources().getString(R.string.loading_msg));
                                    String loginResponse = CommonUtilities.getInstance().loginIntoApp(context);
                                    if (loginResponse != null) {
                                        if (CommonUtilities.getInstance().checkLoginIsValidOrNot(loginResponse, context)) {
                                            Intent intent = new Intent(context, HomeScreenActivity.class);
                                            context.finish();
                                            startActivity(intent);
                                            CommonUtilities.getInstance().cancelProgressDialog(context);
                                        } else {
                                            CommonUtilities.getInstance().cancelProgressDialog(context);
                                        }
                                    }
                                }
                            } else {
                                Constants.searchProfileAdapter.showLoading(false);
                                Constants.searchProfileAdapter.notifyDataSetChanged();
                                getextendedMatchesScroll(ReqType);
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (Constants.alllistdata != null && Constants.alllistdata.size() <= 0)
                                setViewWhenNoResult();
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.getMessage();
            ExceptionTrack.getInstance().TrackResponseCatch(e, "" + ReqType, response);
        } finally {
            loading.setVisibility(View.GONE);
            if (SearchProfileAdapter.btnDoubleClickFlag)
                SearchProfileAdapter.btnDoubleClickFlag = false;
            if (SearchProfileAdapter.btnDoubleClickGridFlag)
                SearchProfileAdapter.btnDoubleClickGridFlag = false;
            if (isAdded())
                TimeElapseUtils.getInstance(context).trackStop(getString(R.string.name_page_load));
        }
    }


    @Override
    public void onReceiveError(int ReqType, String Error) {
        if (Error != null && Error.equalsIgnoreCase("1") && isAdded()) {
            if (loading != null && loading.isShown())
                loading.setVisibility(View.GONE);
            CommonUtilities.getInstance().cancelProgressDialog(context);
            connection_timeout.setText(getResources().getString(R.string.network_msg) + " - " + "tap to retry");
            connection_timeout_id.setVisibility(View.VISIBLE);
            disableBottomFilter();
        }
    }

    private void getExtendedMatches(int from) {
        try {
            if (CommonUtilities.getInstance().isNetAvailable(context)) {
                if (from == Request.EXTENDED_MATCHES_SCROLL || from == Request.EXTENDED_LATEST_MATCHES_SCROLL)
                    Constants.searchProfileAdapter.showLoading(true);
                isloading = true;
                page = page + 1;
                isExtendedMatches = true;
                paramValues = new ArrayList<>();
                Call<SearchResultsModel> extetendeMatches;
                if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches))) {
                    paramValues.add("" + page);
                    extetendeMatches = RetroApiCall.getMatches(
                            UrlGenerator.getRetrofitRequestUrlForPost(Request.LATEST_MATCHES),
                            WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.EXTENDED_MATCHES)
                    );
                    RetrofitConnect.getInstance().AddToEnqueue(extetendeMatches, mListener, from);
                } else if (Constants.selectedTabName.equalsIgnoreCase(
                        context.getResources().getString(R.string.latestMatches))) {
                    paramValues.add("" + page);
                    if (Constants.isTodayMatches)
                        paramValues.add(Constants.TODAY_MATCHES_DAYS);
                    else
                        paramValues.add(Constants.LATEST_MATCHES_DAYS);
                    extetendeMatches = RetroApiCall.getMatches(
                            UrlGenerator.getRetrofitRequestUrlForPost(Request.LATEST_MATCHES),
                            WebServiceUrlParameters.getInstance().getRetroFitParameters(paramValues, Request.EXTENDED_LATEST_MATCHES)
                    );
                    RetrofitConnect.getInstance().AddToEnqueue(extetendeMatches, mListener, from);
                }
            } else
                CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDvmPromoBottom() {

        try {
            if (snackbar == null && isAdded() && !mIsTabChange) {
                snackbar = Snackbar.make(mFrameLayout, "", Snackbar.LENGTH_INDEFINITE);

                snakLayout = (Snackbar.SnackbarLayout) snackbar.getView();
                LayoutInflater objLayoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View snackView = objLayoutInflater.inflate(R.layout.dvm_promo_bottom, null);
                CustomButton btnActivate = (CustomButton) snackView.findViewById(R.id.btnActivate);
                TextView btnText = (TextView) snackView.findViewById(R.id.textView);

                if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoViewedMyProfile))) {
                    btnText.setText(String.format(context.getResources().getString(R.string.dvm_stick_bottom), "viewed"));
                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoShortlistedMe))) {
                    btnText.setText(String.format(context.getResources().getString(R.string.dvm_stick_bottom), "shortlisted"));
                }

                btnActivate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtilities.getInstance().isNetAvailable(context)) {
                            context.startActivity(new Intent(context, PaymentOffersActivity.class).putExtra("paymentPack", Constants.UPGRADE_PACK));
                            GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getString(R.string.dvm_payment_promo), from.equalsIgnoreCase("whoviewed") ? "MWVMP" : "MWSM", context.getString(R.string.upgrade_now), 1);
                        } else
                            CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);
                    }
                });
                snakLayout.setBackgroundResource(R.drawable.refine_background);

                snakLayout.addView(snackView, 0);

            }
            if (snackbar != null && !snackbar.isShown() && isAdded() && !mIsTabChange) {
                if (CommonUtilities.getInstance().getNavigationTab() != null
                        && (CommonUtilities.getInstance().getNavigationTab().getMenu().getItem(0).isChecked()
                        || CommonUtilities.getInstance().getNavigationTab().getMenu().getItem(3).isChecked())) {
                    if (!snackbar.isShown())
                        snackbar.show();
                    snakLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            ViewGroup.LayoutParams lp = snakLayout.getLayoutParams();
                            if (lp instanceof CoordinatorLayout.LayoutParams) {
                                ((CoordinatorLayout.LayoutParams) lp).setBehavior(new DisableSwipeBehavior());
                                snakLayout.setLayoutParams(lp);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                snakLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                //noinspection deprecation
                                snakLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                        }
                    });
                } else {
                    disableBottomFilter(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showFilterRefineBottom() {
        try {
            if (snackbar == null && isAdded() && !mIsTabChange) {
                snackbar = Snackbar.make(mFrameLayout, "", Snackbar.LENGTH_INDEFINITE);
                snakLayout = (Snackbar.SnackbarLayout) snackbar.getView();
                LayoutInflater objLayoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View snackView = objLayoutInflater.inflate(R.layout.custom_snack_filter_refine, null);
                snakLayout.setBackgroundResource(R.drawable.refine_background);
                RelativeLayout mRlsnackFilterRefine = snackView.findViewById(R.id.rl_snack_filter_refine);
                tvFilterRefine = snackView.findViewById(R.id.tv_filter_refine);
                if (CommonUtilities.getInstance().getNavigationTab() != null
                        && (CommonUtilities.getInstance().getNavigationTab().getMenu().getItem(3).isChecked())) {
                    tvFilterRefine.setAllCaps(false);
                    tvFilterRefine.setText(getResources().getString(R.string.title_refine));
                } else
                    tvFilterRefine.setText(getResources().getString(R.string.title_filterRefine));
                mRlsnackFilterRefine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (getActivity() != null && CommonUtilities.getInstance().isNetAvailable(context)) {
                                if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches)))
                                    getActivity().startActivityForResult(new Intent(getActivity(), FilterRefineActivity.class).putExtra("filterfrom", "filterLM"), Constants.FILTER_REFINE_CODE);
                                else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches)))
                                    getActivity().startActivityForResult(new Intent(getActivity(), FilterRefineActivity.class).putExtra("filterfrom", "filterALL"), Constants.FILTER_REFINE_CODE);
                                else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches))) {
                                    isloading = false;
                                    getActivity().startActivityForResult(new Intent(getActivity(), FilterRefineActivity.class).putExtra("filterfrom", "filterNearby"), Constants.FILTER_REFINE_CODE);
                                } else if (CommonUtilities.getInstance().getNavigationTab() != null
                                        && (CommonUtilities.getInstance().getNavigationTab().getMenu().getItem(3).isChecked()))
                                    mProfsearchInter.bottomRefineClick();
                            } else {
                                CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), getActivity());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                snakLayout.addView(snackView, 0);
            }
            if (snackbar != null && !snackbar.isShown() && isAdded() && !mIsTabChange) {
                if (SharedPreferenceData.getInstance().getApplyFilterStatus(getContext()) && FilterRefineFragment.isRefineChanged)
                    tvFilterRefine.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_refine_filter_sel), null, null, null);
                else
                    tvFilterRefine.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_refine_filter_normal), null, null, null);

                if (CommonUtilities.getInstance().getNavigationTab() != null
                        && (CommonUtilities.getInstance().getNavigationTab().getMenu().getItem(0).isChecked()
                        || CommonUtilities.getInstance().getNavigationTab().getMenu().getItem(3).isChecked())) {
                    snackbar.getView().setPadding(0, 0, 0, 0);
                    if (!snackbar.isShown())
                        snackbar.show();
                    snakLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            ViewGroup.LayoutParams lp = snakLayout.getLayoutParams();
                            if (lp instanceof CoordinatorLayout.LayoutParams) {
                                ((CoordinatorLayout.LayoutParams) lp).setBehavior(new DisableSwipeBehavior());
                                snakLayout.setLayoutParams(lp);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                snakLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                //noinspection deprecation
                                snakLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                        }
                    });
                } else {
                    /*if (snackbar.isShown())
                        snackbar.dismiss();*/
                    disableBottomFilter(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNoDataFound(int ReqType) {
        try {
            if (ReqType == Request.MATCHES_ALL || (!(mStrFilterType.equalsIgnoreCase("search") || mStrFilterType.equalsIgnoreCase("searchagain") || (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))) && ReqType == Request.MATCHES_REFINESEARCH)) {
                page = 0;
                loading.setVisibility(View.VISIBLE);
                getExtendedMatches(Request.EXTENDED_MATCHES);
            } else if (ReqType == Request.MATCHES_LATEST || (!(mStrFilterType.equalsIgnoreCase("search") || mStrFilterType.equalsIgnoreCase("searchagain") || (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))) && ReqType == Request.MATCHES_REFINESEARCH)) {
                page = 0;
                loading.setVisibility(View.VISIBLE);

                getExtendedMatches(Request.EXTENDED_LATEST_MATCHES);
            } else {
                if (loading != null && loading.isShown())
                    loading.setVisibility(View.GONE);
                setViewWhenNoResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getextendedMatchesScroll(int ReqType) {
        if (ReqType == Request.MATCHES_SCROLL && isloading && Integer.parseInt(searchResultsModel.TOTALRESULTS) < Constants.EXTENDEDMATCHESCOUNT
                && (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches))
                || Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches)))) {
            isloading = true;
            page = 0;
            if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches)))
                getExtendedMatches(Request.EXTENDED_LATEST_MATCHES_SCROLL);
            else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches)))
                getExtendedMatches(Request.EXTENDED_MATCHES_SCROLL);
        }
    }

    private class DisableSwipeBehavior extends SwipeDismissBehavior<Snackbar.SnackbarLayout> {
        @Override
        public boolean canSwipeDismissView(@NonNull View view) {
            return false;
        }
    }


}


