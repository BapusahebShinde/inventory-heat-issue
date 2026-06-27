package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.itek.retail.R;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.ui.customviews.custprogress.CircularProgressBar;

public class CountTotalView extends ConstraintLayout {

    Context context;
    TypedArray typedArray;

    ConstraintLayout clCountTotalProgress;
    TextView txtScore, txtTotal;
    CircularProgressBar progressCountTotal;
    View divScore;

    long score = 0L;
    long total = -1;
    String totalStr = "";

    boolean isShowProgress = false;

    public CountTotalView(@NonNull Context context) {
        this(context, null);
    }

    public CountTotalView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountTotalView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public CountTotalView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    void init(Context context, @Nullable AttributeSet attrs) {
        CountTotalView.this.context = context;
        final View root = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_count_total_progress, this, true);
        clCountTotalProgress = root.findViewById(R.id.cl_count_total_progress);
        txtScore = root.findViewById(R.id.txt_score);
        divScore = root.findViewById(R.id.div_score);
        txtTotal = root.findViewById(R.id.txt_total);
        progressCountTotal = root.findViewById(R.id.progress_count_total);
        if (attrs != null)
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.CountTotalView, 0, 0);
        if (typedArray != null) {
            isShowProgress = typedArray.getBoolean(R.styleable.CountTotalView_isShowProgress, true);
            score = typedArray.getInt(R.styleable.CountTotalView_score, 0);
            totalStr = chkNull(typedArray.getString(R.styleable.CountTotalView_total), "");
            final float txtSize = typedArray.getDimension(R.styleable.CountTotalView_android_textSize, 0.0f);
            int txtColor = typedArray.getColor(R.styleable.CountTotalView_android_textColor, 0);
            final float txtSizeScore = typedArray.getDimension(R.styleable.CountTotalView_textSizeScore, 0.0f);
            final float txtSizeTotal = typedArray.getDimension(R.styleable.CountTotalView_textSizeTotal, 0.0f);

            final int txtColorScore = typedArray.getColor(R.styleable.CountTotalView_textColorScore, 0);
            final int txtColorTotal = typedArray.getColor(R.styleable.CountTotalView_textColorTotal, 0);

            final int txtAppearanceScore = typedArray.getResourceId(R.styleable.CountTotalView_textAppearanceScore, 0);
            final int txtAppearanceTotal = typedArray.getResourceId(R.styleable.CountTotalView_textAppearanceTotal, 0);

            final int backgroundImg = typedArray.getResourceId(R.styleable.CountTotalView_android_background, 0);


            if (backgroundImg != 0) {
                root.setBackgroundResource(backgroundImg);
            }

            if (txtSize > 0) {
                txtScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
                txtTotal.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
            }
            if (txtColor > 0) {
                txtScore.setTextColor(txtColor);
                txtTotal.setTextColor(txtColor);
            }

            if (txtAppearanceScore != 0) setTextAppearance(txtScore, txtAppearanceScore);
            if (txtAppearanceTotal != 0) setTextAppearance(txtTotal, txtAppearanceTotal);

            if (txtSizeScore > 0) txtScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSizeScore);
            if (txtSizeTotal > 0) txtTotal.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSizeTotal);

            //if (txtColorScore != 0) txtScore.setTextColor(ContextCompat.getColor(context,txtColorScore));
            //if (txtColorTotal != 0) txtTotal.setTextColor(ContextCompat.getColor(context,txtColorTotal));


            if (txtColorTotal != 0) txtTotal.setTextColor(txtColorTotal);

            if (chkNull(totalStr, "").matches("[0-9]+")) total = Long.parseLong(totalStr);
            setupScoreTotalProgress();
        }

    }

    private void setTextAppearance(TextView textView, int styleId) {
        if (textView != null && styleId > 0) {
            if (context != null && context instanceof CommonActivity)
                ((CommonActivity) context).setTextAppearance(textView, styleId);
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                textView.setTextAppearance(styleId);
            else if (context != null) textView.setTextAppearance(context, styleId);
        }
    }

    private void setupScoreTotalProgress() {
        txtTotal.setVisibility(total > 0 || isNonEmpty(totalStr) ? View.VISIBLE : View.GONE);
        divScore.setVisibility(total > 0 || isNonEmpty(totalStr) ? View.VISIBLE : View.GONE);
        progressCountTotal.setVisibility(isShowProgress && score > 0 && total > 0 ? View.VISIBLE : View.GONE);

        txtScore.setText("" + chkNull(score < 0 ? 0 : score, 0));
        txtTotal.setText(chkZero(total, chkNull(totalStr, "-")));

        if (isShowProgress && score > 0 && total > 0) {
            double per = total > 0 ? (chkNull(score, 0L) * 100) / total : 0;
            int percentage = (int) per;
            progressCountTotal.setProgress(percentage);
        }
        updateConstraint();
    }

    private void updateConstraint() {
        final ConstraintLayout root = clCountTotalProgress;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(root);
        constraintSet.clear(txtScore.getId(), ConstraintSet.TOP);
        constraintSet.clear(txtScore.getId(), ConstraintSet.BOTTOM);
        if (total <= 0)
            constraintSet.connect(txtScore.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(txtScore.getId(), ConstraintSet.BOTTOM, total > 0 || isNonEmpty(totalStr) ? divScore.getId() : ConstraintSet.PARENT_ID, total > 0 || isNonEmpty(totalStr) ? ConstraintSet.TOP : ConstraintSet.BOTTOM);
        constraintSet.applyTo(root);
    }

    public void setTextColorScore(@ColorRes int colorResId) {
        txtScore.setTextColor(ContextCompat.getColor(getContext(), colorResId));
    }

    public void setTextColorTotal(@ColorRes int colorResId) {
        txtTotal.setTextColor(ContextCompat.getColor(getContext(), colorResId));
    }

    /*public void setScoreColor(int color) {
        txtScore.setTextColor(color);
    }*/


    public void setShowProgress(boolean showProgress) {
        isShowProgress = showProgress;
    }

    public void setScoreTotal(int score, int total) {
        this.score = score;
        this.total = total;
        setupScoreTotalProgress();
    }

    public String getScoreStr() {
        return String.valueOf(score);
    }

    public Long getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        setupScoreTotalProgress();
    }

    public void setScore(int score, String scoreTxt) {
        this.score = score;
        setupScoreTotalProgress();
        if (isNonEmpty(scoreTxt)) txtScore.setText(scoreTxt);
    }

    public String getTotal() {
        return String.valueOf(total);
    }

    public void setTotal(int total) {
        this.total = total;
        setupScoreTotalProgress();
    }

    public void setTotal(String totalStr) {
        if (chkNull(totalStr, "").matches("[0-9]+")) total = Long.parseLong(totalStr);
        setupScoreTotalProgress();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


}