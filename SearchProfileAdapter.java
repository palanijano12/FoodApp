package com.domaininstance.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.domaininstance.R;
import com.domaininstance.config.Constants;
import com.domaininstance.database.SharedPreferenceData;
import com.domaininstance.helpers.CustomButton;
import com.domaininstance.helpers.CustomTextView;
import com.domaininstance.ui.activities.ChatScreen;
import com.domaininstance.ui.activities.HomeScreenActivity;
import com.domaininstance.ui.activities.PaymentOffersActivity;
import com.domaininstance.ui.activities.ViewProfileActivity;
import com.domaininstance.ui.fragments.FilterRefineFragment;
import com.domaininstance.ui.fragments.MatchesFragment;
import com.domaininstance.ui.fragments.ShortlistSendinterestDialog;
import com.domaininstance.utils.CommonServiceCodes;
import com.domaininstance.utils.CommonUtilities;
import com.domaininstance.utils.ExceptionTrack;
import com.domaininstance.utils.GAAnalyticsOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

//import com.bumptech.glide.load.resource.drawable.GlideDrawable;

/**
 * Created by Palani on 01-11-2014.
 */
public class SearchProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnClickListener,
        ShortlistSendinterestDialog.Listener {

    private Context context;
    private LayoutInflater inflator;
    private RecyclerView mRecyclerView;
    private Random random = null;
    private ShortlistSendinterestDialog.Listener listener;

    private StringBuilder des;
    private int randMascot = 0, intPaymentDir, intListPromoPos, inNoOfPromo = 0;
    private String strListPromoUrl = "";
    private boolean showLoader = false;
    private ArrayList<String> descriptionContent;
    public static boolean btnDoubleClickFlag = false, btnDoubleClickGridFlag = false;
    private String from = "", strInterContent = "", send_text = "", currentmatriid = "", sNoOfChild = "",
            strShowPromo = "";
    public static int EXTENDED_MATCHES_TOTALRESULT = 0;
    private boolean isfromExtendedmatches = false;

    private ProfileListViewHolder profileListViewHolder;
    private PaymentListViewHolder PaymentListViewHolder;
    private PaymentPromoHolder paymentPromoHodler;
    private TopPaymentPromoHolder topPaymentPromoHodler;
    private LoadingViewHolder loadingViewHolder;
    private ExtendedMatchesHolder extendedMatchesHolder;
    private ListingPromoHolder mListingPromoHolder;
    private DVMProfileListViewHolder dvmProfileListViewHolder;
    private boolean isExtendedMatchesScroll = true, isCheckPromo = false, showpromo = false;
    private Drawable[] drawable;

    private List<Integer> listPromoPos;
    private List<String> listPromoUrl;
    private List<String> listPayDir;

    public SearchProfileAdapter() {
    }

    public SearchProfileAdapter(Context context, String from) {

        //Constants.WVMP_MASK = "1";
        //Constants.WSMP_MASK = "1";
        this.context = context;
        inflator = LayoutInflater.from(context);
        this.from = from;
        listener = this;
        random = new Random();
        //setHasStableIds(true);
        showpromo();
        strShowPromo = SharedPreferenceData.getInstance().getDataInSharedPreferences(context, "show_promo");
        listPromoPos = SharedPreferenceData.getInstance().getIntListSharedPref(context, "promo_pos");
        listPromoUrl = SharedPreferenceData.getInstance().getStrListSharedPref(context, "promo_url");
        listPayDir = SharedPreferenceData.getInstance().getStrListSharedPref(context, "payment_dir");

    }

    public void showLoading(boolean status) {
        showLoader = status;
        notifyAdapterChange();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        int additionalContent = 0;
        if (Constants.alllistdata == null || Constants.alllistdata.size() == 0)
            return 0;
        else {
            if (strShowPromo.equalsIgnoreCase("1") &&
                    showpromo) {
                for (int i = 0; i < listPromoPos.size(); i++) {
                    int insertPos = listPromoPos.get(i);
                    if ((from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("LM")) ?
                            (MatchesFragment.commTotalCnt >= insertPos && Constants.alllistdata.size() >= insertPos) :
                            Constants.alllistdata.size() >= insertPos) {
                        additionalContent = additionalContent + 1;
                    }
                }

            }

            if ((from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("LM")) && EXTENDED_MATCHES_TOTALRESULT > 0)
                additionalContent = additionalContent + 1;
        }
        return Constants.alllistdata.size() + additionalContent + 1;
    }

    public int getscrollPosition(int position) {
        if (position > 0 && Constants.SESSPAIDSTATUS.equalsIgnoreCase("2")
                && strShowPromo.equalsIgnoreCase("1") &&
                (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.blockedProfiles))
                        || Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.ignoredProfiles)))) {
            position = position + 1;
        }
        if (strShowPromo.equalsIgnoreCase("1") & isCheckPromo
                && showpromo &&
                ((from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("LM")) ?
                        MatchesFragment.commTotalCnt >= (position - (listPromoPos.indexOf(intListPromoPos) + 1))
                        : !(from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("LM")))) {
            //If no previllege user condition to check postion greater than equal
            if ((position >= intListPromoPos))
                position = position + listPromoPos.indexOf(intListPromoPos);
            else if (position > 0 && position < intListPromoPos) {
                position = position + (listPromoPos.indexOf(intListPromoPos) + 1);
            }
        }
        if ((from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("LM")) && EXTENDED_MATCHES_TOTALRESULT > 0
                && (position - ((listPromoPos.indexOf(intListPromoPos) + 1) + 1)) > MatchesFragment.commTotalCnt) {
            if (strShowPromo.equalsIgnoreCase("1")
                    && !Constants.SESSPAIDSTATUS.equalsIgnoreCase("1"))
                // position = position + ((listPromoPos.indexOf(intListPromoPos) + 1) + 1);
                position = position + 1;
            else
                position = position + ((listPromoPos.indexOf(intListPromoPos) + 1) + 1);
        }


        return position;
    }

    private int getRealPosition(int position) {
        if ((strShowPromo.equalsIgnoreCase("1") & isCheckPromo)
                && showpromo &&
                ((from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("LM")) ?
                        (MatchesFragment.commTotalCnt >= (position - (listPromoPos.indexOf(intListPromoPos) + 1)))
                        : !(from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("LM")))) {
            //If no previllege user condition to check postion greater than equal
            if ((position >= intListPromoPos))
                position = position - (listPromoPos.indexOf(intListPromoPos) + 1);
            else if (position > 0 && position < intListPromoPos) {
                position = position - listPromoPos.indexOf(intListPromoPos);
            }
        }
        if ((from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("LM")) && EXTENDED_MATCHES_TOTALRESULT > 0
                && (position - ((listPromoPos.indexOf(intListPromoPos) + 1) + 1))
                >= MatchesFragment.commTotalCnt) {
            if (strShowPromo.equalsIgnoreCase("1")
                    && !Constants.SESSPAIDSTATUS.equalsIgnoreCase("1"))
                position = position - ((listPromoPos.indexOf(intListPromoPos) + 1) + 1);
                //  position = position - 1;
            else
                position = position - 1;
        }
        if (Constants.alllistdata.size() > 0 && Constants.alllistdata.size() <= position)
            position = Constants.alllistdata.size() - 1;
        return position > 0 ? position : 0;
    }


    @Override
    public int getItemViewType(int position) {
        if (position != 0 && position == (getItemCount() - 1))
            return Constants.VIEW_TYPE_LOADING;
        else if ((HomeScreenActivity.profileInfo.COOKIEINFO.WVMP_MASK.equalsIgnoreCase("1") && from.equalsIgnoreCase("whoviewed"))
                || (HomeScreenActivity.profileInfo.COOKIEINFO.WSMP_MASK.equalsIgnoreCase("1") && from.equalsIgnoreCase("whoshortlisted"))) {
            return Constants.VIEW_DVM_LIST;
        } else if ((strShowPromo.equalsIgnoreCase("1")) &&
                listPromoPos.contains(position)
                && showpromo
                && ((from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("LM")) ?
                MatchesFragment.commTotalCnt >= position
                : !(from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("LM")))) {
            intListPromoPos = position;
            isCheckPromo = true;
            intPaymentDir = Integer.valueOf(listPayDir.get(listPromoPos.indexOf(position)));
            strListPromoUrl = listPromoUrl.get(listPromoPos.indexOf(position));
            return Constants.VIEW_LISTING_PROMO;
        } else if (MatchesFragment.commTotalCnt < Constants.EXTENDEDMATCHESCOUNT && (from.equalsIgnoreCase("ALL")
                || from.equalsIgnoreCase("LM"))
                && ((!(Constants.SESSPAIDSTATUS.equalsIgnoreCase("1"))
                && MatchesFragment.commTotalCnt <= (inNoOfPromo - 1)
                && (position - ((listPromoPos.indexOf(intListPromoPos)))) == MatchesFragment.commTotalCnt)
                || (!(Constants.SESSPAIDSTATUS.equalsIgnoreCase("1"))
                && MatchesFragment.commTotalCnt > (inNoOfPromo - 1)
                && (position - ((listPromoPos.indexOf(intListPromoPos)))) == MatchesFragment.commTotalCnt + 1)
                || (Constants.SESSPAIDSTATUS.equalsIgnoreCase("1") && position == MatchesFragment.commTotalCnt)))
            return Constants.VIEW_TYPE_EXTENDED_MATCHES;
        return Constants.VIEW_TYPE_LIST_ITEM;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case Constants.VIEW_TYPE_PAYMENT_PROMO:
                view = inflator.inflate(R.layout.payment_expired_list_item, parent, false);
                return new PaymentListViewHolder(view);
            case Constants.VIEW_TYPE_LIST_ITEM:
                view = inflator.inflate(R.layout.home_profile_list_item, parent, false);
                return new ProfileListViewHolder(view);
            case Constants.VIEW_DVM_LIST:
                view = inflator.inflate(R.layout.dvm_common_list, parent, false);
                return new DVMProfileListViewHolder(view);
            case Constants.VIEW_TYPE_EI_PM_PENDING_HEADER:
                view = inflator.inflate(R.layout.inbox_banner, parent, false);
                return new EI_PM_PendingHolder(view);
            case Constants.VIEW_TYPE_LOADING:
                view = inflator.inflate(R.layout.loadmore, parent, false);
                return new LoadingViewHolder(view);
            case Constants.VIEW_TYPE_TOP_PROMO:
                view = inflator.inflate(R.layout.payment_promo_listings, parent, false);
                return new TopPaymentPromoHolder(view);
            case Constants.VIEW_TYPE_INTER_PROMO:
                int randNormal = random.nextBoolean() ? 0 : 1;
                randMascot = random.nextBoolean() ? 0 : 1;
                if (randMascot == 0) {
                    view = inflator.inflate(R.layout.activity_mascot_banner, parent, false);
                } else {
                    if (randNormal == 0)
                        view = inflator.inflate(R.layout.listitem_payment_banner1, parent, false);
                    else
                        view = inflator.inflate(R.layout.listitem_payment_banner2, parent, false);
                }
                return new PaymentPromoHolder(view);

            case Constants.VIEW_LISTING_PROMO:
                return new ListingPromoHolder(inflator.inflate(R.layout.listing_promo_banner, parent, false));
            case Constants.VIEW_TYPE_EXTENDED_MATCHES:
                view = inflator.inflate(R.layout.extended_matches_listing, parent, false);
                return new ExtendedMatchesHolder(view);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case Constants.VIEW_TYPE_PAYMENT_PROMO:
                PaymentListViewHolder = (PaymentListViewHolder) holder;
                PaymentListViewHolder.setIsRecyclable(false);
                PaymentListViewHolder.btnActivate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Dvm_promotion), context.getResources().getString(R.string.latestMatches), context.getResources().getString(R.string.dvm_lebel_activate_view_lock), 1);
                        context.startActivity(new Intent(context, PaymentOffersActivity.class).putExtra("paymentPack", Constants.UPGRADE_PACK));
                    }
                });
                break;

            case Constants.VIEW_DVM_LIST:
                dvmProfileListViewHolder = (DVMProfileListViewHolder) holder;
                dvmProfileListViewHolder.position = getRealPosition(position);
                if (Constants.alllistdata.get(dvmProfileListViewHolder.position).PPMATCHES.size() > 0) {
                    for (int i = 0; i < dvmProfileListViewHolder.textview.length; i++) {
                        if (Constants.alllistdata.get(dvmProfileListViewHolder.position).PPMATCHES.size() > i) {
                            dvmProfileListViewHolder.textview[i].setText(Constants.alllistdata.get(dvmProfileListViewHolder.position).PPMATCHES.get(i).substring(0, 1).toUpperCase()
                                    + Constants.alllistdata.get(dvmProfileListViewHolder.position).PPMATCHES.get(i).substring(1));
                            dvmProfileListViewHolder.textview[i].setVisibility(View.VISIBLE);
                            dvmProfileListViewHolder.dvm_profileUser.setText(context.getResources().getString(R.string.dvm_list_desc));
                        } else {
                            dvmProfileListViewHolder.textview[i].setVisibility(View.GONE);
                        }
                    }
                } else {
                    if (from.equalsIgnoreCase("whoviewed")) {
                        dvmProfileListViewHolder.dvm_profileUser.setText(String.format(context.getResources().getString(R.string.dashboard_top_view), Constants.USER_GENDER.equalsIgnoreCase("1") ? "She" : "He"));
                    } else if (from.equalsIgnoreCase("whoshortlisted")) {
                        dvmProfileListViewHolder.dvm_profileUser.setText(String.format(context.getResources().getString(R.string.dashboard_top_short), Constants.USER_GENDER.equalsIgnoreCase("1") ? "She" : "He"));
                    }
                }
                Glide.with(context).load(Constants.alllistdata.get(dvmProfileListViewHolder.position).THUMBNAME).
                        listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                try {
                                    ExceptionTrack.getInstance().TrackImageFailure(e, "Listings-" + from, Constants.alllistdata.get(dvmProfileListViewHolder.position).THUMBNAME);
                                    if (!Constants.alllistdata.get(dvmProfileListViewHolder.position).NAME.isEmpty()) {
                                        profileListViewHolder.profileimg_reload.setVisibility(View.VISIBLE);
                                        handleRetryImageLoad(context, dvmProfileListViewHolder.profile, dvmProfileListViewHolder.profileReload, Constants.alllistdata.get(dvmProfileListViewHolder.position).THUMBNAME);
                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        }).transition(withCrossFade()).apply(new RequestOptions().fitCenter().priority(Priority.HIGH)
                        .placeholder(Constants.USER_GENDER.equalsIgnoreCase("1") ? R.drawable.add_photo_female : R.drawable.add_photo_male)).into(dvmProfileListViewHolder.profile);
                dvmProfileListViewHolder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paymentDirection();
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getString(R.string.dvm_payment_promo), from.equalsIgnoreCase("whoviewed") ? "MWVMP" : "MWSM", context.getString(R.string.action_click), 1);
                    }
                });
                break;
            case Constants.VIEW_TYPE_LIST_ITEM:
                des = new StringBuilder();
                profileListViewHolder = (ProfileListViewHolder) holder;
                profileListViewHolder.pos = getRealPosition(position);
                profileListViewHolder.laylistBorder.setTag(profileListViewHolder.pos);
                profileListViewHolder.profileimage.setTag(profileListViewHolder.pos);
                profileListViewHolder.shorlist_or_deleteLayout.setTag(profileListViewHolder.pos);
                profileListViewHolder.send_interestLayout.setTag(profileListViewHolder.pos);
                profileListViewHolder.tvUpgradeNow.setTag(profileListViewHolder.pos);
                profileListViewHolder.chatLayout.setTag(profileListViewHolder.pos);
                profileListViewHolder.ivContentPopup.setTag(profileListViewHolder.pos);
                profileListViewHolder.profileimg_reload.setTag(profileListViewHolder.pos);
                profileListViewHolder.profileUsername.setTag(profileListViewHolder.pos);
                profileListViewHolder.profileMatriId.setTag(profileListViewHolder.pos);
                profileListViewHolder.ivlockActivate.setTag(profileListViewHolder.pos);
                profileListViewHolder.profileMatriId.setVisibility(View.VISIBLE);
                profileListViewHolder.profileUsername.setVisibility(View.VISIBLE);
                profileListViewHolder.viewed.setVisibility(View.VISIBLE);
                profileListViewHolder.ivContentPopup.setVisibility(View.VISIBLE);
                profileListViewHolder.layCommAction.setVisibility(View.VISIBLE);
                profileListViewHolder.profileimage.setEnabled(true);
                profileListViewHolder.shorlist_or_deleteLayout.setEnabled(true);
                profileListViewHolder.send_interestLayout.setEnabled(true);
                profileListViewHolder.chatLayout.setEnabled(true);

                if (Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS.equalsIgnoreCase("1")) {
                    //check ismask
                    if (!Constants.alllistdata.get(profileListViewHolder.pos).ISMASK.equals("0"))
                        profileListViewHolder.ivlockActivate.setVisibility(View.VISIBLE);
                    else
                        profileListViewHolder.ivlockActivate.setVisibility(View.GONE);

                    descriptionContent = new ArrayList<>();

                    if ((!Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.blockedProfiles)))
                            && (!Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.ignoredProfiles)))) {
                        if (Constants.alllistdata.get(profileListViewHolder.pos).PROFILEHIGHLIGHTER.equalsIgnoreCase("1")) {
                            //profileListViewHolder.laylistBorder.setBackgroundResource(R.drawable.profile_highlighter_border);
                            profileListViewHolder.tvFeatureProfile.setVisibility(View.VISIBLE);
                        } else {
                            //profileListViewHolder.laylistBorder.setBackgroundResource(R.drawable.non_profile_highlighter);
                            profileListViewHolder.tvFeatureProfile.setVisibility(View.GONE);
                        }
                    }

                    if(Constants.alllistdata!=null&&Constants.alllistdata.get(profileListViewHolder.pos)!=null
                            &&Constants.alllistdata.get(profileListViewHolder.pos).NAME!=null)
                    if (!Constants.alllistdata.get(profileListViewHolder.pos).NAME.isEmpty()) {
                        descriptionContent.add(Constants.alllistdata.get(profileListViewHolder.pos).AGE);
                        descriptionContent.add(Constants.alllistdata.get(profileListViewHolder.pos).HEIGHT);
                        descriptionContent.add(Constants.alllistdata.get(profileListViewHolder.pos).CASTE);
                        descriptionContent.add(Constants.alllistdata.get(profileListViewHolder.pos).MARITALSTATUS);

                        if (Constants.alllistdata.get(profileListViewHolder.pos).NOOFCHILDREN != null &&
                                Constants.alllistdata.get(profileListViewHolder.pos).NOOFCHILDREN.length() != 0 &&
                                !Constants.alllistdata.get(profileListViewHolder.pos).NOOFCHILDREN.equalsIgnoreCase("0")) {
                            sNoOfChild = (Integer.parseInt(Constants.alllistdata.get(profileListViewHolder.pos).NOOFCHILDREN) == 1) ? "Child" : "Children";
                            descriptionContent.add(Constants.alllistdata.get(profileListViewHolder.pos).NOOFCHILDREN + " " + sNoOfChild);
                            if (Constants.alllistdata.get(profileListViewHolder.pos).CHILDRENLIVINGSSTATUS != null &&
                                    Constants.alllistdata.get(profileListViewHolder.pos).CHILDRENLIVINGSSTATUS.trim().length() != 0)
                                descriptionContent.add("(" + Constants.alllistdata.get(profileListViewHolder.pos).CHILDRENLIVINGSSTATUS + ")");
                        }

                        descriptionContent.add(Constants.alllistdata.get(profileListViewHolder.pos).CITY);
                        if (!Constants.alllistdata.get(profileListViewHolder.pos).CITY.equalsIgnoreCase(Constants.alllistdata
                                .get(profileListViewHolder.pos).STATE))
                            descriptionContent.add(Constants.alllistdata.get(profileListViewHolder.pos).STATE);

                        descriptionContent.add(Constants.alllistdata.get(profileListViewHolder.pos).COUNTRY);
                        descriptionContent.add(Constants.alllistdata.get(profileListViewHolder.pos).OCCUPATION);
                        descriptionContent.add(Constants.alllistdata.get(profileListViewHolder.pos).EDUCATION);
                        for (int i = 0; i < descriptionContent.size(); i++) {
                            if (!descriptionContent.get(i).isEmpty()) {
                                if (i == 0)
                                    des.append(descriptionContent.get(i)).append(" yrs, ");
                                else
                                    des.append(descriptionContent.get(i)).append(", ");
                            }
                        }

                    } else if (Constants.alllistdata.get(profileListViewHolder.pos).NAME.isEmpty()) {
                        descriptionContent.add("This profile is currently deleted or unavailable., ");
                        des.append(descriptionContent.get(0));
                    }

                    if (!Constants.alllistdata.get(profileListViewHolder.pos).MASKEDMATRIID.isEmpty()) {
                        profileListViewHolder.profileMatriId.setText(Constants.alllistdata.get(profileListViewHolder.pos).MASKEDMATRIID);
                    } else {
                        profileListViewHolder.profileMatriId.setText("");
                    }
                    profileListViewHolder.profileDesc.setText(CommonUtilities.getInstance().removeLastComma(des.toString()));
                    profileListViewHolder.profileUsername.setText(CommonUtilities.getInstance().toCamelCase(Constants.alllistdata.get(profileListViewHolder.pos).NAME).trim());

                    if (Constants.USER_GENDER.equalsIgnoreCase("1"))

                        Glide.with(context).load(Constants.alllistdata.get(profileListViewHolder.pos).THUMBNAME).
                                listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        try {
                                            ExceptionTrack.getInstance().TrackImageFailure(e, "Listings-" + from, Constants.alllistdata.get(profileListViewHolder.pos).THUMBNAME);
                                            if (!Constants.alllistdata.get(profileListViewHolder.pos).NAME.isEmpty()) {
                                                profileListViewHolder.profileimg_reload.setVisibility(View.VISIBLE);
                                                handleRetryImageLoad(context, profileListViewHolder.profileimage, profileListViewHolder.profileimg_reload, Constants.alllistdata.get(profileListViewHolder.pos).THUMBNAME);
                                            }
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                }).transition(withCrossFade()).apply(new RequestOptions().fitCenter().priority(Priority.HIGH).placeholder(R.drawable.add_photo_female)).into(profileListViewHolder.profileimage);

                    else if (Constants.USER_GENDER.equalsIgnoreCase("2"))
                        Glide.with(context).load(Constants.alllistdata.get(profileListViewHolder.pos).THUMBNAME).
                                listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        try {
                                            ExceptionTrack.getInstance().TrackImageFailure(e, "Listings-" + from, Constants.alllistdata.get(profileListViewHolder.pos).THUMBNAME);
                                            if (!Constants.alllistdata.get(profileListViewHolder.pos).NAME.isEmpty() && Constants.alllistdata.size() > profileListViewHolder.pos) {
                                                profileListViewHolder.profileimg_reload.setVisibility(View.VISIBLE);
                                                handleRetryImageLoad(context, profileListViewHolder.profileimage, profileListViewHolder.profileimg_reload, Constants.alllistdata.get(profileListViewHolder.pos).THUMBNAME);
                                            }
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }

                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                }).transition(withCrossFade()).apply(new RequestOptions().fitCenter().priority(Priority.HIGH).placeholder(R.drawable.add_photo_male)).into(profileListViewHolder.profileimage);


                    if (Constants.selectedTabName.equalsIgnoreCase(context.getString(R.string.shortlisted)) || Constants.selectedTabName.equalsIgnoreCase(context.getString(R.string.whoViewedMyProfile)) ||
                            (Constants.alllistdata.get(profileListViewHolder.pos).MARKASVIEWED.equalsIgnoreCase("0") && !Constants.selectedTabName.equalsIgnoreCase(context.getString(R.string.whoViewedMyProfile)))) {

                        profileListViewHolder.viewed.setVisibility(View.GONE);
                    } else {
                        profileListViewHolder.viewed.setVisibility(View.VISIBLE);
                    }

                    if (Constants.selectedTabName.equalsIgnoreCase(context.getString(R.string.whoViewedMyProfile)) || (Constants.alllistdata.get(profileListViewHolder.pos).PROFILESHORTLISTED.equalsIgnoreCase("Y") && !Constants.selectedTabName.equalsIgnoreCase(context.getString(R.string.whoViewedMyProfile))))
                        profileListViewHolder.ivContentPopup.setVisibility(View.GONE);
                    else
                        profileListViewHolder.ivContentPopup.setVisibility(View.VISIBLE);

                    profileListViewHolder.chatButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.pay_chat), null, null, null);
                    drawable = profileListViewHolder.chatButton.getCompoundDrawables();

                    if (Constants.alllistdata.get(profileListViewHolder.pos).ONLINEFLAG.equalsIgnoreCase("1")) {
                        drawable[0].mutate().setColorFilter(ContextCompat.getColor(context, R.color.colorAccentNew), PorterDuff.Mode.SRC_ATOP);
                        profileListViewHolder.chatButton.setText("Chat Now");
                    } else {
                        drawable[0].mutate().setColorFilter(ContextCompat.getColor(context, R.color.edit_box_color), PorterDuff.Mode.SRC_ATOP);
                        profileListViewHolder.chatButton.setText(Constants.alllistdata.get(profileListViewHolder.pos).ONLINESTATUS);
                    }

                    if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoViewedMyProfile)) && !Constants.alllistdata.get(profileListViewHolder.pos).VIEWED_DATE.equalsIgnoreCase("")) {
                        profileListViewHolder.tvViewedDate.setText("Viewed on\n" + Constants.alllistdata.get(profileListViewHolder.pos).VIEWED_DATE);
                        profileListViewHolder.tvViewedDate.setVisibility(View.VISIBLE);
                    }

                    if (from.equalsIgnoreCase(context.getResources().getString(R.string.label_Search)) || from.equalsIgnoreCase("LM")
                            || from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("whoviewed")
                            || from.equalsIgnoreCase("whoshortlisted") || from.equalsIgnoreCase("today")
                            || from.equalsIgnoreCase(Constants.PURPOSE_BLOCK) || from.equalsIgnoreCase(Constants.PURPOSE_IGNORE)
                            || from.equalsIgnoreCase("VN") || from.equalsIgnoreCase("VC")
                            || from.equalsIgnoreCase("CV") ||
                            from.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches))) {
                        //profileListViewHolder.shorlist_or_deleteLayout.setBackgroundResource(R.drawable.outline_btn_services);
                        profileListViewHolder.date_textView.setVisibility(View.GONE);
                        profileListViewHolder.date_support.setVisibility(View.GONE);
                        if (Constants.alllistdata.get(profileListViewHolder.pos).PROFILESHORTLISTED.equalsIgnoreCase("Y")) {
                            profileListViewHolder.shorlist_or_deleteButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.shortlisted),
                                    null, null, null);
                            profileListViewHolder.shorlist_or_deleteButton.setText("Shortlisted");
                        } else {
                            profileListViewHolder.shorlist_or_deleteButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.shortlist),
                                    null, null, null);
                            profileListViewHolder.shorlist_or_deleteButton.setText("Shortlist");
                        }

                    } else if (from.equalsIgnoreCase("SP")) {
                        profileListViewHolder.ivContentPopup.setVisibility(View.GONE);
                        if (!Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS.equalsIgnoreCase("TD")
                                && !Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS.equalsIgnoreCase("MNV")) {
                            profileListViewHolder.date_textView.setVisibility(View.VISIBLE);
                            profileListViewHolder.date_support.setVisibility(View.VISIBLE);
                            profileListViewHolder.date_textView.setText("Shortlisted On\n" + Constants.alllistdata.get(profileListViewHolder.pos).DATESHORLISTED);
                            //profileListViewHolder.shorlist_or_deleteLayout.setBackgroundResource(R.drawable.btn_gray_stroke);
                            profileListViewHolder.shorlist_or_deleteButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_delete_gray),
                                    null, null, null);
                            profileListViewHolder.shorlist_or_deleteButton.setText("Remove");
                            profileListViewHolder.shorlist_or_deleteLayout.setEnabled(true);
                            profileListViewHolder.send_interestLayout.setEnabled(true);
                            //profileListViewHolder.send_interestLayout.setBackgroundResource(R.drawable.outline_btn_services);
                        } else if (Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS.equalsIgnoreCase("TD")
                                || Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS.equalsIgnoreCase("MNV")) {
//                            profileListViewHolder.send_interestLayout.setBackgroundResource(R.color.block_text_color);
                            //profileListViewHolder.shorlist_or_deleteLayout.setBackgroundResource(R.drawable.btn_gray_stroke);
                            profileListViewHolder.shorlist_or_deleteButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_delete_gray),
                                    null, null, null);
                            profileListViewHolder.date_textView.setText("Shortlisted On\n" + Constants.alllistdata.get(profileListViewHolder.pos).DATESHORLISTED);
                            profileListViewHolder.shorlist_or_deleteButton.setText("Remove");
                            profileListViewHolder.shorlist_or_deleteLayout.setEnabled(false);
                            profileListViewHolder.send_interestLayout.setEnabled(false);
                        }
                    }

                    if (!Constants.SESSPAIDSTATUS.equalsIgnoreCase("1")) {
                        if (!(from.equalsIgnoreCase(Constants.PURPOSE_BLOCK) || from.equalsIgnoreCase(Constants.PURPOSE_IGNORE))) {
                            if (Constants.alllistdata.get(profileListViewHolder.pos).MSGINT.equalsIgnoreCase("1")) {

                                if (profileListViewHolder.pos % 2 == 0) {
                                    profileListViewHolder.send_interestButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.send_mail), null, null, null);
                                    profileListViewHolder.send_interestButton.setText("Send Mail");
                                    profileListViewHolder.layUpgradeOption.setVisibility(View.VISIBLE);
                                    if (Constants.USER_GENDER.equalsIgnoreCase("1")) {
                                        profileListViewHolder.tvUpgradeTo.setText("to send her mail directly");
                                    } else {
                                        profileListViewHolder.tvUpgradeTo.setText("to send him mail directly");
                                    }
                                } else {
                                    profileListViewHolder.send_interestButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.icn_mobile), null, null, null);
                                    drawable = profileListViewHolder.send_interestButton.getCompoundDrawables();
                                    drawable[0].mutate().setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                                    profileListViewHolder.send_interestButton.setText("View Number");
                                    profileListViewHolder.layUpgradeOption.setVisibility(View.VISIBLE);
                                    if (Constants.USER_GENDER.equalsIgnoreCase("1")) {
                                        profileListViewHolder.tvUpgradeTo.setText("to view her number");
                                    } else {
                                        profileListViewHolder.tvUpgradeTo.setText("to view him number");
                                    }
                                }
                            } else {
                                profileListViewHolder.send_interestButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_interest), null, null, null);
                                profileListViewHolder.send_interestButton.setText("Send Interest");
                                profileListViewHolder.layUpgradeOption.setVisibility(View.GONE);
                            }
                        } else if (from.equalsIgnoreCase(Constants.PURPOSE_BLOCK) || from.equalsIgnoreCase(Constants.PURPOSE_IGNORE)) {
//                            profileListViewHolder.send_interestLayout.setBackgroundResource(R.color.block_text_color);
                            profileListViewHolder.shorlist_or_deleteLayout.setBackgroundResource(R.drawable.outline_btn_services);
//                            profileListViewHolder.chatButton.setBackgroundResource(R.color.block_text_color);
                            if (Constants.alllistdata.get(profileListViewHolder.pos).MSGINT.equalsIgnoreCase("1")) {
                                profileListViewHolder.send_interestButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_interest), null, null, null);
                                profileListViewHolder.send_interestButton.setText("Interest Sent");
                            } else {
                                profileListViewHolder.send_interestButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_interest), null, null, null);
                                profileListViewHolder.send_interestButton.setText("Send Interest");
                            }
                            profileListViewHolder.shorlist_or_deleteButton.setTextColor(ContextCompat.getColor(context, R.color.white));
                            profileListViewHolder.shorlist_or_deleteLayout.setEnabled(false);
                            profileListViewHolder.send_interestLayout.setEnabled(false);
                            profileListViewHolder.chatLayout.setEnabled(false);
                            profileListViewHolder.layCommAction.setVisibility(View.GONE);
                        }
                    } else if (Constants.SESSPAIDSTATUS.equalsIgnoreCase("1")) {
                        if (!(from.equalsIgnoreCase(Constants.PURPOSE_BLOCK) || from.equalsIgnoreCase(Constants.PURPOSE_IGNORE))) {
                            profileListViewHolder.send_interestButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.send_mail), null, null, null);
                            profileListViewHolder.send_interestButton.setText("Send Mail");
                        } else if (from.equalsIgnoreCase(Constants.PURPOSE_BLOCK) || from.equalsIgnoreCase(Constants.PURPOSE_IGNORE)) {
//                            profileListViewHolder.send_interestLayout.setBackgroundResource(R.color.block_text_color);
                            profileListViewHolder.shorlist_or_deleteLayout.setBackgroundResource(R.drawable.outline_btn_services);
//                            profileListViewHolder.chatButton.setBackgroundResource(R.color.block_text_color);
                            profileListViewHolder.shorlist_or_deleteButton.setTextColor(ContextCompat.getColor(context, R.color.white));
                            profileListViewHolder.send_interestButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.send_mail), null, null, null);
                            profileListViewHolder.send_interestButton.setText("Send Mail");
                            profileListViewHolder.shorlist_or_deleteLayout.setEnabled(false);
                            profileListViewHolder.send_interestLayout.setEnabled(false);
                            profileListViewHolder.chatLayout.setEnabled(false);
                            profileListViewHolder.layCommAction.setVisibility(View.GONE);

                        }
                    }

                    if (Constants.alllistdata.get(profileListViewHolder.pos).PAIDSTATUS.equalsIgnoreCase("1")
                            || Constants.alllistdata.get(profileListViewHolder.pos).PRIVILAGE.equalsIgnoreCase("10")) {
                        profileListViewHolder.memberShip.setVisibility(View.VISIBLE);
                        //   profileListViewHolder.memberShip.setVisibility(View.VISIBLE);
                        if (Constants.alllistdata.get(profileListViewHolder.pos).PRIVILAGE.equalsIgnoreCase("0")) {
                            profileListViewHolder.memberShip.setText("GOLD");
                            profileListViewHolder.memberShip.setTextColor(ContextCompat.getColor(context, R.color.gold_text_new));
                            profileListViewHolder.memberShip.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.gold), null);
                            drawable = profileListViewHolder.memberShip.getCompoundDrawables();
                            drawable[2].mutate().setColorFilter(ContextCompat.getColor(context, R.color.gold_text_new), PorterDuff.Mode.SRC_ATOP);

                        } else if (Constants.alllistdata.get(profileListViewHolder.pos).PRIVILAGE.equalsIgnoreCase("2")) {
                            profileListViewHolder.memberShip.setText("PLATINUM");
                            profileListViewHolder.memberShip.setTextColor(ContextCompat.getColor(context, R.color.fab_menu_pressed));
                            profileListViewHolder.memberShip.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.gold), null);
                            drawable = profileListViewHolder.memberShip.getCompoundDrawables();
                            drawable[2].mutate().setColorFilter(ContextCompat.getColor(context, R.color.fab_menu_pressed), PorterDuff.Mode.SRC_ATOP);

                        } else if (Constants.alllistdata.get(profileListViewHolder.pos).PRIVILAGE.equalsIgnoreCase("4") || Constants.alllistdata.get(profileListViewHolder.pos)
                                .PRIVILAGE.equalsIgnoreCase("3") || Constants.alllistdata.get(profileListViewHolder.pos).PRIVILAGE.equalsIgnoreCase("10")) {
                            profileListViewHolder.memberShip.setText("ASSISTED");
                            profileListViewHolder.memberShip.setTextColor(ContextCompat.getColor(context, R.color.fab_menu_pressed));
                            profileListViewHolder.memberShip.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.gold), null);
                            drawable = profileListViewHolder.memberShip.getCompoundDrawables();
                            drawable[2].mutate().setColorFilter(ContextCompat.getColor(context, R.color.fab_menu_pressed), PorterDuff.Mode.SRC_ATOP);
                        } else if (Constants.alllistdata.get(profileListViewHolder.pos).PRIVILAGE.equalsIgnoreCase("5")) {
                            profileListViewHolder.memberShip.setText("DIAMOND");
                            profileListViewHolder.memberShip.setTextColor(ContextCompat.getColor(context, R.color.assisted_text));
                            profileListViewHolder.memberShip.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.gold), null);
                            drawable = profileListViewHolder.memberShip.getCompoundDrawables();
                            drawable[2].mutate().setColorFilter(ContextCompat.getColor(context, R.color.assisted_text), PorterDuff.Mode.SRC_ATOP);
                        } else if (Constants.alllistdata.get(profileListViewHolder.pos).PRIVILAGE.equalsIgnoreCase("9")) {
                            profileListViewHolder.memberShip.setText("PREMIUM");
                            profileListViewHolder.memberShip.setTextColor(ContextCompat.getColor(context, R.color.fab_menu_pressed));
                            profileListViewHolder.memberShip.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.gold), null);
                            drawable = profileListViewHolder.memberShip.getCompoundDrawables();
                            drawable[2].mutate().setColorFilter(ContextCompat.getColor(context, R.color.fab_menu_pressed), PorterDuff.Mode.SRC_ATOP);
                        }
                    } else
                        profileListViewHolder.memberShip.setVisibility(View.GONE);

                    profileListViewHolder.laylistBorder.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).PROFILESTATUS != null
                                    && (Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).PROFILESTATUS.equalsIgnoreCase("TD")
                                    || Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).PROFILESTATUS.equalsIgnoreCase("MNV")
                                    || Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).PROFILESTATUS.equalsIgnoreCase("2")))
                                return;
                            Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? profileListViewHolder.pos : position;
//                            RecyclerView.ViewHolder view = mRecyclerView.findViewHolderForAdapterPosition(profileListViewHolder.pos);
//                            gotoViewProfile(Integer.parseInt(v.getTag().toString()), view.itemView.findViewById(R.id.profileimg));
                            gotoViewProfile(Integer.parseInt(v.getTag().toString()));
                        }
                    });

                    profileListViewHolder.profileimage.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (CommonUtilities.getInstance().isNetAvailable(context)) {
                                    // Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? profileListViewHolder.pos : position;
                                    Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? Integer.parseInt(v.getTag().toString()) : position;
                                    CommonServiceCodes.getInstance().profileImageListOrGrid(context, Integer.parseInt(v.getTag().toString()), from, context.getResources().getString(R.string.category_List_View) + " - ");
                                } else {
                                    CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    profileListViewHolder.profileUsername.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                if (CommonUtilities.getInstance().isNetAvailable(context)) {
                                    // Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? profileListViewHolder.pos : position;
                                    Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? Integer.parseInt(view.getTag().toString()) : position;
                                    if (Constants.alllistdata.get(Integer.parseInt(view.getTag().toString())).ISMASK.equals("0"))
                                        gotoViewProfile(Integer.parseInt(view.getTag().toString()));
                                    else {
                                        CommonUtilities.getInstance().showPromoPopup(context, getDVMGA());
                                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Dvm_promotion), getDVMGA(), context.getResources().getString(R.string.dvm_lebel_lock_icon), 1);

                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    profileListViewHolder.profileMatriId.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                if (CommonUtilities.getInstance().isNetAvailable(context)) {
                                    // Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? profileListViewHolder.pos : position;
                                    Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? Integer.parseInt(view.getTag().toString()) : position;
                                    if (Constants.alllistdata.get(Integer.parseInt(view.getTag().toString())).ISMASK.equals("0"))
                                        gotoViewProfile(Integer.parseInt(view.getTag().toString()));
                                    else {
                                        CommonUtilities.getInstance().showPromoPopup(context, getDVMGA());
                                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Dvm_promotion), getDVMGA(), context.getResources().getString(R.string.dvm_lebel_lock_icon), 1);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    });
                    profileListViewHolder.ivlockActivate.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                if (CommonUtilities.getInstance().isNetAvailable(context)) {
                                    CommonUtilities.getInstance().showPromoPopup(context, getDVMGA());
                                    GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Dvm_promotion), getDVMGA(), context.getResources().getString(R.string.dvm_lebel_lock_icon), 1);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });

                    profileListViewHolder.shorlist_or_deleteLayout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (btnDoubleClickFlag)
                                    return;
                                btnDoubleClickFlag = true;
                                if (CommonUtilities.getInstance().isNetAvailable(context)) {
                                    Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? profileListViewHolder.pos : position;
                                    currentmatriid = Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID;
                                    if ((MatchesFragment.commTotalCnt <= position) && (from.equalsIgnoreCase("LM"))) {
                                        CommonServiceCodes.getInstance().shortlistListOrGrid(v, "", listener, context, Integer.parseInt(v.getTag().toString()),
                                                context.getResources().getString(R.string.action_Extended_LatestMatches), Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID, context.getResources().getString(R.string.action_Extended_LatestMatches));
                                    } else if ((MatchesFragment.commTotalCnt <= position) && (from.equalsIgnoreCase("ALL"))) {
                                        CommonServiceCodes.getInstance().shortlistListOrGrid(v, "", listener, context, Integer.parseInt(v.getTag().toString()),
                                                context.getResources().getString(R.string.action_Extended_Matches), Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID, context.getResources().getString(R.string.action_Extended_Matches));
                                    } else
                                        CommonServiceCodes.getInstance().shortlistListOrGrid(((ProfileListViewHolder) holder).laylistBorder, "", listener, context, Integer.parseInt(v.getTag().toString()), from, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID, context.getResources().getString(R.string.category_List_View) + " - ");
                                } else {
                                    if (SearchProfileAdapter.btnDoubleClickFlag)
                                        SearchProfileAdapter.btnDoubleClickFlag = false;
                                    CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    profileListViewHolder.send_interestLayout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (btnDoubleClickFlag)
                                    return;
                                btnDoubleClickFlag = true;
                                Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? profileListViewHolder.pos : position;
                                if (Constants.SESSPAIDSTATUS.equalsIgnoreCase("1")) {
                                    Constants.alllistdata.get(Constants.selected_list_item_position).MSGINT = "1";

                                    if ((MatchesFragment.commTotalCnt < position) && from.equalsIgnoreCase("LM"))
                                        CommonServiceCodes.getInstance().sendMailAutoFill(context, listener, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID,
                                                "paidmember", "sendinterest", context.getResources().getString(R.string.action_Extended_LatestMatches),
                                                "", Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).NAME, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME);
                                    else if ((MatchesFragment.commTotalCnt < position) && from.equalsIgnoreCase("ALL"))
                                        CommonServiceCodes.getInstance().sendMailAutoFill(context, listener, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID,
                                                "paidmember", "sendinterest", context.getResources().getString(R.string.action_Extended_Matches),
                                                "", Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).NAME, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME);
                                    else {

                                        if (Integer.parseInt(v.getTag().toString()) == 0 && (Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).PRIVILAGE.equalsIgnoreCase("4") || Constants.alllistdata.get(Integer.parseInt(v.getTag().toString()))
                                                .PRIVILAGE.equalsIgnoreCase("3") || Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).PRIVILAGE.equalsIgnoreCase("10"))) {
                                            CommonServiceCodes.getInstance().sendMailAutoFill(context, listener, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID,
                                                    "paidmember", "sendinterest~" + context.getResources().getString(R.string.label_Send_Express_Assisted),
                                                    from, "", Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).NAME,
                                                    Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME);

                                        } else
                                            CommonServiceCodes.getInstance().sendMailAutoFill(context, listener, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID,
                                                    "paidmember", "sendinterest", from, "",
                                                    Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).NAME, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME);
                                    }
                                } else {
                                    View parent = (View) v.getParent();
                                    CustomButton tvSendInterest = parent.findViewById(R.id.send_interestButton);
                                    send_text = tvSendInterest.getText().toString();
                                    if (send_text.equalsIgnoreCase("Send Interest")) {
                                        if (send_text.equalsIgnoreCase("Send Interest") && MatchesFragment.commTotalCnt > position) {
                                            String gaCategory = context.getResources().getString(R.string.category_List_View) + " - ";
                                            String gaAction = context.getResources().getString(R.string.action_click);
                                            String gaLabel = "";
                                            if (Integer.parseInt(v.getTag().toString()) == 0 && (Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).PRIVILAGE.equalsIgnoreCase("4") || Constants.alllistdata.get(Integer.parseInt(v.getTag().toString()))
                                                    .PRIVILAGE.equalsIgnoreCase("3") || Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).PRIVILAGE.equalsIgnoreCase("10"))) {
                                                gaLabel = context.getResources().getString(R.string.label_Send_Express_Assisted);
                                            } else
                                                gaLabel = context.getResources().getString(R.string.label_Extended_Send_Intrest);

                                            CommonServiceCodes.getInstance().sendinterestListOrGrid(v, "", context, Integer.parseInt(v.getTag().toString()),
                                                    Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME, from, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID,
                                                    listener, gaCategory, gaAction, gaLabel, "");
                                        } else if (send_text.equalsIgnoreCase("Send Interest") && MatchesFragment.commTotalCnt < position) {
                                            String gaCategory = "", gaAction = "", gaLabel = "";
                                            if ((MatchesFragment.commTotalCnt < position) && from != null && from.equalsIgnoreCase("LM")) {
                                                gaCategory =
                                                        context.getResources().getString(R.string.category_EI);
                                                gaAction = context.getResources().getString(R.string.action_Extended_LatestMatches);
                                                gaLabel = context.getResources().getString(R.string.label_Extended_Send_Intrest);
                                                CommonServiceCodes.getInstance().sendinterestListOrGrid(v, "", context, Integer.parseInt(v.getTag().toString()),
                                                        Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME, context.getResources().getString(R.string.action_Extended_LatestMatches), Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID,
                                                        listener, gaCategory, gaAction, gaLabel, "");
                                            } else if ((MatchesFragment.commTotalCnt < position) && from != null && from.equalsIgnoreCase("ALL")) {
                                                gaCategory = context.getResources().getString(R.string.category_EI);
                                                gaAction = context.getResources().getString(R.string.action_Extended_Matches);
                                                gaLabel = context.getResources().getString(R.string.label_Extended_Send_Intrest);
                                                CommonServiceCodes.getInstance().sendinterestListOrGrid(v, "", context, Integer.parseInt(v.getTag().toString()),
                                                        Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME, context.getResources().getString(R.string.action_Extended_Matches), Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID,
                                                        listener, gaCategory, gaAction, gaLabel, "");
                                            } else {
                                                gaCategory = context.getResources().getString(R.string.category_List_View) + " - ";
                                                gaAction = context.getResources().getString(R.string.action_click);
                                                gaLabel = context.getResources().getString(R.string.label_Extended_Send_Intrest);
                                                CommonServiceCodes.getInstance().sendinterestListOrGrid(v, "", context, Integer.parseInt(v.getTag().toString()),
                                                        Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME, from, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID,
                                                        listener, gaCategory, gaAction, gaLabel, "");
                                            }
                                        } else
                                            CommonServiceCodes.getInstance().sendinterestListOrGrid(v, "", context, Integer.parseInt(v.getTag().toString()),
                                                    Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME, from, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID,
                                                    listener, context.getResources().getString(R.string.category_List_View) + " - ", context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_Extended_Send_Intrest), "");
                                    } else if (send_text.equalsIgnoreCase("View Number")) {
                                        strInterContent = "Become a premium member & contact " + Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).NAME + " directly";
                                        CommonServiceCodes.getInstance().showContexualPaymentPromo(context, strInterContent, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME, null, context.getResources().getString(R.string.interests), context.getResources().getString(R.string.category_List_View), context.getResources().getString(R.string.label_contexual_promo) + " - " + context.getResources().getString(R.string.label_view_number));
                                    } else {
                                        strInterContent = "Become a premium member & contact " + Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).NAME + " directly";

                                        CommonServiceCodes.getInstance().showContexualPaymentPromo(context, strInterContent, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME, null, context.getResources().getString(R.string.interest_send_already), context.getResources().getString(R.string.category_List_View), context.getResources().getString(R.string.label_contexual_promo) + " - " + context.getResources().getString(R.string.label_Send_Mail));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    profileListViewHolder.tvUpgradeNow.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (send_text.equalsIgnoreCase("View Number")) {
                                strInterContent = "Become a premium member & contact " + Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).NAME + " directly";
                                CommonServiceCodes.getInstance().showContexualPaymentPromo(context, strInterContent, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME, null, context.getResources().getString(R.string.interests), context.getResources().getString(R.string.category_List_View), context.getResources().getString(R.string.label_contexual_promo) + " - " + context.getResources().getString(R.string.label_Send_Mail));
                            } else {
                                strInterContent = "Interest already sent. Become a premium member & contact ";
                                if (Constants.USER_GENDER.equalsIgnoreCase("1"))
                                    strInterContent = strInterContent + "her directly";
                                else
                                    strInterContent = strInterContent + "him directly";
                                Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? profileListViewHolder.pos : position;
                                CommonServiceCodes.getInstance().showContexualPaymentPromo(context, strInterContent, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME, null, context.getResources().getString(R.string.interest_send_already), "", context.getResources().getString(R.string.label_contexual_promo) + " - " + context.getResources().getString(R.string.label_Send_Express_Interest));
                            }
                        }
                    });

                    profileListViewHolder.chatLayout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!Constants.alllistdata.get(profileListViewHolder.pos).ISMASK.equals("0")) {
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Dvm_promotion),
                                        getDVMGA(),
                                        context.getResources().getString(R.string.label_Chat_button), 1);
                                CommonUtilities.getInstance().showChatPromoPopup(context, getDVMGA());
                            } else {
                                if (CommonUtilities.getInstance().isNetAvailable(context)) {
                                    if (Constants.ChatStatus == 1) {
                                        ChatScreen.getInstance().showAppUpgradeAlert(context, context.getResources().getString(R.string.chat_under_maintainance), false);
                                        return;
                                    } else if (Constants.ChatStatus == 2) {
                                        ChatScreen.getInstance().showAppUpgradeAlert(context, context.getResources().getString(R.string.chat_off), false);
                                        return;
                                    } else if (Constants.ChatStatus == 0) {
                                        // Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? profileListViewHolder.pos : position;
                                        Constants.lastVisiItmPosListOrGrid = Constants.SESSPAIDSTATUS.equals("1") ? Integer.parseInt(v.getTag().toString()) : position;
                                        if (Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).ONLINEFLAG.equalsIgnoreCase("1")) {
                                            if (Constants.mSocket != null && !Constants.mSocket.isConnected())
                                                ChatScreen.getInstance().enableOnlineActivityTask();
                                            context.startActivity(new Intent(context, ChatScreen.class).putExtra("OppMatriid", Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID).
                                                    putExtra("UserName", Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).NAME).putExtra("UserImage", Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME).
                                                    putExtra("PaidStatus", Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).PAIDSTATUS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                                            if (from.equalsIgnoreCase(context.getResources().getString(R.string.label_Search))) {
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_search_result), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);
                                            }
                                            if (from.equalsIgnoreCase("ALL")) {
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_All_Matches), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);
                                            } else if (from.equalsIgnoreCase("LM")) {
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_Latest_Matches), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);
                                            } else if (from.equalsIgnoreCase("VN")) {
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_ViewedNotContacted_Profiles), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);
                                            } else if (from.equalsIgnoreCase("SP")) {
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_Shotlist_Profiles), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);
                                            } else if (from.equalsIgnoreCase("whoviewed")) {
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_Who_Viewed_My_Profile), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);
                                            } else if (from.equalsIgnoreCase("whoshortlisted")) {
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_Who_Shortlisted_Me), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);
                                            } else if (from.equalsIgnoreCase("today")) {
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_Today_Matches), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);
                                            } else if (from.equalsIgnoreCase("CV")) {
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_contact_viewedbyme), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);
                                            } else if (from.equalsIgnoreCase("VC")) {
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_viewed_mycontact), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);
                                            } else if (from.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))
                                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.action_nearby), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.Chat_Now), 1);

                                        } else {
                                            if (Constants.SESSPAIDSTATUS.equalsIgnoreCase("1"))
                                                CommonUtilities.getInstance().displayToastMessageCenter(context.getResources().getString(R.string.member_logout), context);
                                            else {
                                                String strPromoContent = "";
                                                if (Constants.USER_GENDER.equalsIgnoreCase("1"))
                                                    strPromoContent = Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).NAME + " is currently offline. Become a premium member & contact her directly";
                                                else if (Constants.USER_GENDER.equalsIgnoreCase("2"))
                                                    strPromoContent = Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).NAME + " is currently offline. Become a premium member & contact him directly";
                                                CommonServiceCodes.getInstance().showContexualPaymentPromo(context, strPromoContent, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).THUMBNAME, null, from, context.getResources().getString(R.string.category_List_View), context.getResources().getString(R.string.label_contexual_promo) + " - " + context.getResources().getString(R.string.Chat_Now));
                                            }
                                        }
                                    }
                                } else
                                    CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);

                            }


                        }
                    });

                    profileListViewHolder.ivContentPopup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if ((MatchesFragment.commTotalCnt <= position) && from.equalsIgnoreCase("ALL"))
                                CommonServiceCodes.getInstance().showContentPopUp(listener, v, context, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MASKEDMATRIID,
                                        Integer.parseInt(v.getTag().toString()), Constants.FROM_LISTVIEW, "ExtMatchesinMatches", context.getResources().getString(R.string.category_List_View) + " - ", "", "", true);
                            else if ((MatchesFragment.commTotalCnt <= position) && from.equalsIgnoreCase("LM"))
                                CommonServiceCodes.getInstance().showContentPopUp(listener, v, context, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MASKEDMATRIID,
                                        Integer.parseInt(v.getTag().toString()), Constants.FROM_LISTVIEW, "ExtMatchesinLatestMatches", context.getResources().getString(R.string.category_List_View) + " - ", "", "", true);
                            else
                                CommonServiceCodes.getInstance().showContentPopUp(listener, v, context, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MATRIID, Constants.alllistdata.get(Integer.parseInt(v.getTag().toString())).MASKEDMATRIID,
                                        Integer.parseInt(v.getTag().toString()), Constants.FROM_LISTVIEW, from, context.getResources().getString(R.string.category_List_View) + " - ", "", "", true);
                            return;
                        }
                    });

                    if (Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS != null
                            && (Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS.equalsIgnoreCase("TD")
                            || Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS.equalsIgnoreCase("MNV"))) {
                        profileListViewHolder.profileimage.setClickable(false);
                        profileListViewHolder.shorlist_or_deleteLayout.setClickable(false);
                        profileListViewHolder.send_interestLayout.setClickable(false);
                        profileListViewHolder.chatLayout.setClickable(false);
                        profileListViewHolder.ivContentPopup.setClickable(false);
                        if (Constants.alllistdata.get(profileListViewHolder.pos).MASKEDMATRIID != null)
                            profileListViewHolder.profileMatriId.setText(Constants.alllistdata.get(profileListViewHolder.pos).MASKEDMATRIID);
                    }

                } else if (!Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS.equalsIgnoreCase("1")) {
                    profileListViewHolder.profileMatriId.setVisibility(View.GONE);
                    profileListViewHolder.profileUsername.setVisibility(View.GONE);
                    profileListViewHolder.viewed.setVisibility(View.GONE);
                    profileListViewHolder.ivContentPopup.setVisibility(View.GONE);
                    profileListViewHolder.layCommAction.setVisibility(View.GONE);
                    profileListViewHolder.tvViewedDate.setVisibility(View.GONE);
                    profileListViewHolder.memberShip.setVisibility(View.GONE);
                    profileListViewHolder.lldateSupport.setVisibility(View.GONE);
                    profileListViewHolder.profileDesc.setVisibility(View.VISIBLE);
                    profileListViewHolder.profileimage.setVisibility(View.VISIBLE);
                    profileListViewHolder.profileimg_reload.setVisibility(View.GONE);
                    profileListViewHolder.ivlockActivate.setVisibility(View.GONE);
                    profileListViewHolder.profileimage.setEnabled(false);
                    profileListViewHolder.shorlist_or_deleteLayout.setEnabled(false);
                    profileListViewHolder.send_interestLayout.setEnabled(false);
                    profileListViewHolder.chatLayout.setEnabled(false);
                    profileListViewHolder.layUpgradeOption.setVisibility(View.GONE);
                    if (Constants.USER_GENDER.equalsIgnoreCase("1"))
                        profileListViewHolder.profileimage.setImageResource(R.drawable.add_photo_female);
                    else if (Constants.USER_GENDER.equalsIgnoreCase("2"))
                        profileListViewHolder.profileimage.setImageResource(R.drawable.add_photo_male);

                    if (Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS.equalsIgnoreCase("2"))
                        profileListViewHolder.profileDesc.setText("The profile " + Constants.alllistdata.get(profileListViewHolder.pos).MASKEDMATRIID + " is in hidden.");
                    else if (Constants.alllistdata.get(profileListViewHolder.pos).PROFILESTATUS.equalsIgnoreCase("MNV"))
                        profileListViewHolder.profileDesc.setText("Due to some verification issues, " + Constants.alllistdata.get(profileListViewHolder.pos).MASKEDMATRIID + " has temporarily been put on hold.");
                    else
                        profileListViewHolder.profileDesc.setText("The profile " + Constants.alllistdata.get(profileListViewHolder.pos).MASKEDMATRIID + " is deleted or currently unavailable.");
                }
                break;

            case Constants.VIEW_TYPE_TOP_PROMO:
                topPaymentPromoHodler = (TopPaymentPromoHolder) holder;
                topPaymentPromoHodler.setIsRecyclable(false);
                topPaymentPromoHodler.btnPromoUpgrageNow.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Constants.ADDON_SEPERATE = false;
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_ViewedNotContacted_Profiles), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_Upgrade), 1);
                        context.startActivity(new Intent(context, PaymentOffersActivity.class).putExtra("activity", "viewed not contact"));
                    }
                });
                break;
            case Constants.VIEW_TYPE_INTER_PROMO:
                paymentPromoHodler = (PaymentPromoHolder) holder;
                PaymentListViewHolder.setIsRecyclable(false);
                paymentPromoHodler.upgradeNow.setId(randMascot);
                if (randMascot == 0)
                    paymentPromoHodler.tvMascotDesc.setText("Become a premium member on " + Constants.LOGIN_DOMAIN_NAME.replace("matrimony", "") + " matrimony and connect with your matches directly.");
                paymentPromoHodler.upgradeNow.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtilities.getInstance().isNetAvailable(context)) {
                            Constants.ADDON_SEPERATE = false;
                            context.startActivity(new Intent(context, PaymentOffersActivity.class)
                                    .putExtra("activity", "list_banner").putExtra("ViewFrom", from));
                            String label;
                            if (v.getId() == 0)
                                label = "" + context.getResources().getString(R.string.upgrade_now_mascotBanner);
                            else
                                label = "" + context.getResources().getString(R.string.Upgrade_Now_MatchesTab);

                            if (from.equalsIgnoreCase("ALL")) {
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_All_Matches), context.getResources().getString(R.string.action_click), label, 1);
                            } else if (from.equalsIgnoreCase("LM")) {
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_Latest_Matches), context.getResources().getString(R.string.action_click), label, 1);
                            } else if (from.equalsIgnoreCase("VN")) {
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_ViewedNotContacted_Profiles), context.getResources().getString(R.string.action_click), label, 1);
                            } else if (from.equalsIgnoreCase("SP")) {
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_Shotlist_Profiles), context.getResources().getString(R.string.action_click), label, 1);
                            } else if (from.equalsIgnoreCase("whoviewed")) {
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_Who_Viewed_My_Profile), context.getResources().getString(R.string.action_click), label, 1);
                            } else if (from.equalsIgnoreCase("whoshortlisted")) {
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_Who_Shortlisted_Me), context.getResources().getString(R.string.action_click), label, 1);
                            } else if (from.equalsIgnoreCase("today")) {
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_Today_Matches), context.getResources().getString(R.string.action_click), label, 1);
                            } else if (from.equalsIgnoreCase("CV")) {
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_contact_viewedbyme), context.getResources().getString(R.string.action_click), label, 1);
                            } else if (from.equalsIgnoreCase("VC")) {
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.category_viewed_mycontact), context.getResources().getString(R.string.action_click), label, 1);
                            } else if (from.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches)))
                                GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_List_View) + " - " + context.getResources().getString(R.string.action_nearby), context.getResources().getString(R.string.action_click), label, 1);

                        } else {
                            CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);
                        }
                    }
                });
                break;
            case Constants.VIEW_TYPE_LOADING:
                loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.setIsRecyclable(false);
                loadingViewHolder.layLoadmore.setVisibility(showLoader ? View.VISIBLE : View.GONE);
                break;

            case Constants.VIEW_TYPE_EXTENDED_MATCHES:
                extendedMatchesHolder = (ExtendedMatchesHolder) holder;
                extendedMatchesHolder.setIsRecyclable(false);
                viewItemVisibilityCheck(true, extendedMatchesHolder.layExtendedMatches);
                if (EXTENDED_MATCHES_TOTALRESULT > 0) {
                    extendedMatchesHolder.tvExtendedMatches.setText(context.getResources().getString(R.string.extended_matches_title) + "(" + EXTENDED_MATCHES_TOTALRESULT + ")");
                    if (from.equalsIgnoreCase("ALL") && isExtendedMatchesScroll) {
                        isExtendedMatchesScroll = false;
                        // GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_ExtendedMatches_Scroll), context.getResources().getString(R.string.action_Extendedmatches_Scroll), context.getResources().getString(R.string.label_ExtendedMatches_MatchesList), 1);
                    } else if (from.equalsIgnoreCase("LM") && isExtendedMatchesScroll) {
                        //  GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_ExtendedMatches_Scroll), context.getResources().getString(R.string.action_Extendedmatches_Scroll), context.getResources().getString(R.string.label_ExtendedMatches_LatestMatchesList), 1);
                        isExtendedMatchesScroll = false;
                    }
                } else
                    viewItemVisibilityCheck(false, extendedMatchesHolder.layExtendedMatches);

                extendedMatchesHolder.imgTooltip.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            CommonUtilities.getInstance().displayToastMessageLong(context.getResources().getString(R.string.extended_matches_tooltip), context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case Constants.VIEW_LISTING_PROMO:
                mListingPromoHolder = (ListingPromoHolder) holder;
                mListingPromoHolder.setIsRecyclable(false);
                if (position == 0 && Constants.SESSPAIDSTATUS.equalsIgnoreCase("2")) {
                    mListingPromoHolder.llListPromo.setTag(0);
                } else
                    mListingPromoHolder.llListPromo.setTag(1);
                mListingPromoHolder.llListPromo.setVisibility(View.VISIBLE);
                mListingPromoHolder.llListPromo.setId(intPaymentDir);
                mListingPromoHolder.imgListpromo.layout(0, 0, 0, 0);
                loadPicture(mListingPromoHolder.imgListpromo, strListPromoUrl, position, false);

                mListingPromoHolder.llListPromo.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                           /* getDVMGA(context.getResources().getString(R.string.dvm_lebel_stripBanner_activate_view), "");
                        else {
                            if (Constants.SESSPAIDSTATUS.equalsIgnoreCase("2"))
                                getDVMGA(context.getResources().getString(R.string.dvm_lebel_activate_after_expiry), "PaymentBanner-");
                            else if (Constants.SESSPAIDSTATUS.equalsIgnoreCase("0"))
                                getDVMGA(context.getResources().getString(R.string.dvm_lebel_activate_before_expiry), "PaymentBanner-");
                        }*/
                        if (CommonUtilities.getInstance().isNetAvailable(context)) {
                            context.startActivity(new Intent(context, PaymentOffersActivity.class).putExtra("paymentPack", Constants.UPGRADE_PACK));

                        } else
                            CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);

                    }
                });
                break;

            default:
                break;
        }
    }

    private void paymentDirection() {
        if (CommonUtilities.getInstance().isNetAvailable(context)) {
           /* if (payDir == 1)
                context.startActivity(new Intent(context, PaymentOffersActivity.class).putExtra("paymentPack", Constants.ACTIVATION_PACK));
            else if (payDir == 2)
                context.startActivity(new Intent(context, PaymentOffersActivity.class).putExtra("paymentPack", Constants.UPGRADE_PACK));
*/
            context.startActivity(new Intent(context, PaymentOffersActivity.class).putExtra("paymentPack", Constants.UPGRADE_PACK));

        } else
            CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layLoadmore;

        private LoadingViewHolder(View view) {
            super(view);
            layLoadmore = view.findViewById(R.id.layLoadmore);
        }
    }

    public class PaymentPromoHolder extends RecyclerView.ViewHolder {
        private TextView tvMascotDesc;
        private CustomButton upgradeNow;

        private PaymentPromoHolder(View view) {
            super(view);
            tvMascotDesc = view.findViewById(R.id.tvMascotDesc);
            upgradeNow = view.findViewById(R.id.upgradeNow);
        }
    }

    public class TopPaymentPromoHolder extends RecyclerView.ViewHolder {
        private CustomButton btnPromoUpgrageNow;

        private TopPaymentPromoHolder(View view) {
            super(view);
            btnPromoUpgrageNow = view.findViewById(R.id.btnPromoUpgrageNow);
        }
    }

    public class ExtendedMatchesHolder extends RecyclerView.ViewHolder {
        private CustomTextView tvExtendedMatches;
        private ImageView imgTooltip;
        private LinearLayout layExtendedMatches;

        private ExtendedMatchesHolder(View view) {
            super(view);
            layExtendedMatches = view.findViewById(R.id.layExtendedMatches);
            tvExtendedMatches = view.findViewById(R.id.tvExtendedMatches);
            imgTooltip = view.findViewById(R.id.imgTooltip);
        }
    }

    public class ListingPromoHolder extends RecyclerView.ViewHolder {
        private LinearLayout llListPromo;

        private ImageView imgListpromo;

        private ListingPromoHolder(View view) {
            super(view);
            llListPromo = view.findViewById(R.id.llListPromo);
            imgListpromo = view.findViewById(R.id.imgListpromo);
        }
    }

    public class EI_PM_PendingHolder extends RecyclerView.ViewHolder {
        private RecyclerView inboxBannerView;
        private TextView txtCommunicationViewAll;
        private LinearLayout pendingResponseContainer;
        private RecyclerView.LayoutManager mPendingLayoutManager;

        private EI_PM_PendingHolder(View view) {
            super(view);
            inboxBannerView = view.findViewById(R.id.recyclerview);
            txtCommunicationViewAll = view.findViewById(R.id.txtCommunicationViewAll);
            pendingResponseContainer = view.findViewById(R.id.pendingResponseContainer);
        }
    }

    public class ProfileListViewHolder extends RecyclerView.ViewHolder {
        private int pos;
        //private FrameLayout banner;
        private ImageView profileimage, ivContentPopup, viewed, profileimg_reload, ivlockActivate;
        private TextView memberShip;
        private FrameLayout tvFeatureProfile;
        private CustomTextView profileMatriId, profileUsername, profileDesc, privilage,
                date_textView, date_support, tvUpgradeNow, tvUpgradeTo, tvViewedDate;
        private LinearLayout laylistBorder, layCommAction, layUpgradeOption, lldateSupport;
        private CustomButton shorlist_or_deleteButton, chatButton, send_interestButton;
        private RelativeLayout shorlist_or_deleteLayout, chatLayout, send_interestLayout;

        private ProfileListViewHolder(View view) {
            super(view);
            layCommAction = view.findViewById(R.id.layCommAction);
            lldateSupport = view.findViewById(R.id.lldateSupport);
            shorlist_or_deleteButton = view.findViewById(R.id.shorlist_or_deleteButton);
            shorlist_or_deleteLayout = view.findViewById(R.id.shorlist_or_deleteLayout);
            send_interestButton = view.findViewById(R.id.send_interestButton);
            send_interestLayout = view.findViewById(R.id.send_interestLayout);
            ivlockActivate = view.findViewById(R.id.ivlockActivate);
            profileimage = view.findViewById(R.id.profileimg);
            profileimg_reload = view.findViewById(R.id.profileimg_reload);
            profileMatriId = view.findViewById(R.id.profileMatriId);
            profileUsername = view.findViewById(R.id.profileUsername);
            profileDesc = view.findViewById(R.id.profileDesc);
            date_textView = view.findViewById(R.id.date_textView);
            date_support = view.findViewById(R.id.date_support);
            //banner = view.findViewById(R.id.banner);
            memberShip = view.findViewById(R.id.memberShip);
            ivContentPopup = view.findViewById(R.id.ivContentPopup);
            viewed = view.findViewById(R.id.viewed);
            chatButton = view.findViewById(R.id.chatButton);
            chatLayout = view.findViewById(R.id.chatLayout);
            laylistBorder = view.findViewById(R.id.laylistBorder);
            tvFeatureProfile = view.findViewById(R.id.tvFeatureProfile);
            layUpgradeOption = view.findViewById(R.id.layUpgradeOption);
            tvUpgradeNow = view.findViewById(R.id.tvUpgradeNow);
            tvUpgradeTo = view.findViewById(R.id.tvUpgradeTo);
            tvViewedDate = view.findViewById(R.id.tvViewedDate);
        }
    }

    public class DVMProfileListViewHolder extends RecyclerView.ViewHolder {
        private ImageView profile, profileReload;
        private int position;
        private CustomTextView textview[] = new CustomTextView[6];
        private TextView dvm_profileUser;

        private DVMProfileListViewHolder(View view) {
            super(view);
            profile = view.findViewById(R.id.profileimg);
            dvm_profileUser = view.findViewById(R.id.dvm_profileUser);
            profileReload = view.findViewById(R.id.profileReload);
            textview[0] = view.findViewById(R.id.dvm_list_txt1);
            textview[1] = view.findViewById(R.id.dvm_list_txt2);
            textview[2] = view.findViewById(R.id.dvm_list_txt3);
            textview[3] = view.findViewById(R.id.dvm_list_txt4);
            textview[4] = view.findViewById(R.id.dvm_list_txt5);
            textview[5] = view.findViewById(R.id.dvm_list_txt6);
        }
    }

    public class PaymentListViewHolder extends RecyclerView.ViewHolder {
        private CustomTextView tvExpireddays;
        private CustomButton btnActivate;

        private PaymentListViewHolder(View view) {
            super(view);
            tvExpireddays = view.findViewById(R.id.tvExpireddays);
            btnActivate = view.findViewById(R.id.btnActivate);
        }
    }

    private void gotoViewProfile(int position) {
        try {
            if (CommonUtilities.getInstance().isNetAvailable(context)) {
                if (from.equalsIgnoreCase("CV") || from.equalsIgnoreCase("VC")) {
                    if (from.equalsIgnoreCase("CV"))
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_contact_viewedbyme), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                    else if (from.equalsIgnoreCase("VC"))
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_viewed_mycontact), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                } else {
                    if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches))) {
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Latest_Matches), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                        if (MatchesFragment.commTotalCnt <= position) {
                            isfromExtendedmatches = true;
                            GAAnalyticsOperations.getInstance().sendScreenData(context.getResources().getString(R.string.Extendedmatches_Latest_Matches_ScreenData), context);
                        }
                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches))) {
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_All_Matches), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                        if (MatchesFragment.commTotalCnt <= position) {
                            isfromExtendedmatches = true;
                            GAAnalyticsOperations.getInstance().sendScreenData(context.getResources().getString(R.string.Extendedmatches_Matches_ScreenData), context);
                        }
                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.viewedNotContacted))) {//Need to change
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_ViewedNotContacted_Profiles), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.shortlisted))) {
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Shotlist_Profiles), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoViewedMyProfile))) {
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Viewed_My_Profile), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoShortlistedMe))) {
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Shortlisted_Me), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.blockedProfiles))) {
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Blocked_Profiles), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.ignoredProfiles))) {
                        GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_Ignored_Profiles), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches))) {
                        ///GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.nearmatches), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.action_nearby), 1);
                        GAAnalyticsOperations.getInstance().sendScreenData(context.getResources().getString(R.string.nearby_viewprofile), context);

                    } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.searchedProfiles))) {
                        GAAnalyticsOperations.getInstance().sendScreenData(FilterRefineFragment.isKeywordSearch ? context.getResources().getString(R.string.keyword_search_result_viewprofile) : context.getResources().getString(R.string.Search_Results_ScreenData_viewprofile), context);
                        //GAAnalyticsOperations.getInstance().sendAnalyticsEvent(context, context.getResources().getString(R.string.category_search_result), context.getResources().getString(R.string.action_click), context.getResources().getString(R.string.label_View_Profile), 1);
                    }
                    SharedPreferenceData.getInstance().saveRefineMatchArray(context, Constants.alllistdata, "saveMatchesResult");

                }
                Intent viewProfile = new Intent(context, ViewProfileActivity.class);
                viewProfile.putExtra("selecteditem", position);
                viewProfile.putExtra("from", "listorgrid");
                if ((MatchesFragment.commTotalCnt <= position) && from.equalsIgnoreCase("ALL"))
                    viewProfile.putExtra("isfromExtendedmatches", "ExtendedMatches");
                else if ((MatchesFragment.commTotalCnt <= position) && from.equalsIgnoreCase("LM"))
                    viewProfile.putExtra("isfromExtendedmatches", "ExtendedLatestMatches");
                else if (from.equalsIgnoreCase("NEARBY MATCHES"))
                    viewProfile.putExtra("isfromExtendedmatches", "nearbymatches");
                if (from.equalsIgnoreCase("CV") || from.equalsIgnoreCase("VC")) {
                    viewProfile.putExtra("action", 1);
                    ((Activity) context).startActivityForResult(viewProfile, 101);
                } else {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        ActivityOptionsCompat options = ActivityOptionsCompat.
//                                makeSceneTransitionAnimation((Activity) context, view, "share");
//                        context.startActivity(viewProfile, options.toBundle());
//                    } else
                    context.startActivity(viewProfile);
                }
            } else
                CommonUtilities.getInstance().displayToastMessage(context.getResources().getString(R.string.network_msg), context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        currentmatriid = Constants.alllistdata.get(position).MATRIID;
    }

    @Override
    public void returnData(int result, int pos) {
        if (result == Constants.RESPONSE_CODE_Undo_Success) {
            Constants.alllistdata.get(Constants.selected_list_item_position).PROFILESHORTLISTED = "N";
        } else if (result == Constants.RESPONSE_CODE_SEND_INTEREST_UNDO) {
            Constants.alllistdata.get(Constants.selected_list_item_position).MSGINT = "0";
        } else if (result == Constants.RESPONSE_SEND_INTEREST) {
            Constants.alllistdata.get(Constants.selected_list_item_position).MSGINT = "1";
            ShortlistSendinterestDialog sendInt = new ShortlistSendinterestDialog();
            sendInt.setListener(listener);
            Bundle args = new Bundle();
            args.putString("shortlistmatriid", currentmatriid);
            args.putString("shortlistoperation", "sendinterest");
            args.putString("msgids", Constants.SEND_INTEREST_MESSAGE_ID);
            FragmentActivity activity = (FragmentActivity) (context);
            FragmentManager fm = activity.getSupportFragmentManager();
            sendInt.setArguments(args);
            sendInt.show(fm, "sendinterest");
        }
        notifyAdapterChange();
    }

    @Override
    public void slideUpAnimation(boolean isServerRes, boolean isDialogClose) {
    }

    private void viewItemVisibilityCheck(boolean checkVisible, LinearLayout rlParentItem) {
        try {
            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) rlParentItem.getLayoutParams();
            if (checkVisible) {
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                param.width = LinearLayout.LayoutParams.MATCH_PARENT;
                rlParentItem.setVisibility(View.VISIBLE);
            } else {
                rlParentItem.setVisibility(View.GONE);
                param.height = 0;
                param.width = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRetryImageLoad(final Context context, final ImageView ivPreview, final ImageView ivReload, final String url) {
        try {
            ivPreview.setEnabled(false); // Disable onclick event for preview image
            ivReload.setVisibility(View.VISIBLE);
            if (Constants.USER_GENDER.equalsIgnoreCase("1"))
                ivReload.setImageResource(R.drawable.tap_female);
            else
                ivReload.setImageResource(R.drawable.tap_male);
            ivReload.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (Constants.USER_GENDER.equalsIgnoreCase("1"))
                        ivReload.setImageResource(R.drawable.add_photo_female);
                    else
                        ivReload.setImageResource(R.drawable.add_photo_male);
                    Glide.with(context).load(Constants.alllistdata.get(Integer.parseInt(view.getTag().toString())).THUMBNAME).
                            listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    handleRetryImageLoad(context, ivPreview, ivReload, Constants.alllistdata.get(Integer.parseInt(view.getTag().toString())).THUMBNAME);

                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                    ivPreview.setEnabled(true); // Enable onclick event for preview image
                                    ivReload.setVisibility(View.GONE);
                                    ivReload.setOnClickListener(null);
                                    return false;
                                }
                            }).into(ivPreview);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPicture(final ImageView profileimage, final String listPromoUrl, final int position, final boolean shouldLoadAgain) {

        Glide.with(context).load(listPromoUrl).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (shouldLoadAgain)
                    loadPicture(profileimage, listPromoUrl, position, false);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).apply(new RequestOptions().fitCenter().placeholder(null)).into(profileimage);
    }

    private String getDVMGA() {
        try {
            if (from.equalsIgnoreCase("CV") || from.equalsIgnoreCase("VC")) {
                if (from.equalsIgnoreCase("CV"))
                    return context.getResources().getString(R.string.category_contact_viewedbyme);
                else if (from.equalsIgnoreCase("VC"))
                    return context.getResources().getString(R.string.category_viewed_mycontact);
            } else {
                if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.latestMatches))) {
                    return context.getResources().getString(R.string.category_Latest_Matches);
                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.allMatches))) {
                    return context.getResources().getString(R.string.category_All_Matches);
                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.viewedNotContacted))) {//Need to change
                    return context.getResources().getString(R.string.category_ViewedNotContacted_Profiles);
                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.shortlisted))) {
                    return context.getResources().getString(R.string.category_Shotlist_Profiles);
                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoViewedMyProfile))) {
                    return context.getResources().getString(R.string.category_Viewed_My_Profile);
                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.whoShortlistedMe))) {
                    return context.getResources().getString(R.string.category_Shortlisted_Me);
                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.blockedProfiles))) {
                    return context.getResources().getString(R.string.category_Blocked_Profiles);
                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.ignoredProfiles))) {
                    return context.getResources().getString(R.string.category_Ignored_Profiles);
                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.searchedProfiles))) {
                    return context.getResources().getString(R.string.category_search_result);
                } else if (Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.nearmatches))) {
                    return context.getResources().getString(R.string.category_NearBy_Matches);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void showpromo() {
        showpromo = (!Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.blockedProfiles))
                && (!Constants.selectedTabName.equalsIgnoreCase(context.getResources().getString(R.string.ignoredProfiles)))
                && !from.equalsIgnoreCase("CV")
                && !from.equalsIgnoreCase("VC"));
        if ((Constants.selectedTabName.equalsIgnoreCase(context.getString(R.string.whoShortlistedMe)) && HomeScreenActivity.profileInfo.COOKIEINFO.WSMP_MASK.equalsIgnoreCase("1"))
                || (Constants.selectedTabName.equalsIgnoreCase(context.getString(R.string.whoViewedMyProfile)) && HomeScreenActivity.profileInfo.COOKIEINFO.WVMP_MASK.equalsIgnoreCase("1")))
            showpromo = false;
    }

    private void notifyAdapterChange() {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.getRecycledViewPool().clear();
                    notifyDataSetChanged();
                }
            }, 50);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}