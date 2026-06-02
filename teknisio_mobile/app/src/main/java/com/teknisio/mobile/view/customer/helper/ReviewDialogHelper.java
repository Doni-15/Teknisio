package com.teknisio.mobile.view.customer.helper;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.model.request.CreateReviewRequest;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.ReviewResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.ErrorParser;
import com.teknisio.mobile.util.ReviewStateStore;
import com.teknisio.mobile.util.TextHelper;
import com.teknisio.mobile.util.ViewHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ReviewDialogHelper {

    private static final int MAX_COMMENT_LENGTH = 1000;

    public interface ReviewCallback {
        void onReviewCompleted();
    }

    private ReviewDialogHelper() {
    }

    public static void show(
            BaseActivity activity,
            String serviceRequestId,
            ReviewCallback callback
    ) {
        if (activity == null || TextHelper.isBlank(serviceRequestId)) {
            return;
        }

        Dialog reviewDialog = new Dialog(activity);
        reviewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        reviewDialog.setCanceledOnTouchOutside(true);

        ScrollView scrollView = new ScrollView(activity);
        scrollView.setFillViewport(false);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(activity, 24), dp(activity, 24), dp(activity, 24), dp(activity, 20));
        container.setBackground(makeRounded(activity, "#FFFFFF", null, 28, 0));

        scrollView.addView(container, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        TextView title = new TextView(activity);
        title.setText("Bagaimana pengalaman servisnya?");
        title.setTextColor(Color.parseColor("#182230"));
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setLineSpacing(dp(activity, 2), 1.0f);

        TextView subtitle = new TextView(activity);
        subtitle.setText("Ulasanmu membantu pelanggan lain memilih teknisi yang tepat.");
        subtitle.setTextColor(Color.parseColor("#667085"));
        subtitle.setTextSize(13);
        subtitle.setLineSpacing(dp(activity, 2), 1.0f);

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, dp(activity, 8), 0, dp(activity, 18));
        subtitle.setLayoutParams(subtitleParams);

        TextView ratingLabel = new TextView(activity);
        ratingLabel.setText("Rating");
        ratingLabel.setTextColor(Color.parseColor("#344054"));
        ratingLabel.setTextSize(14);
        ratingLabel.setTypeface(Typeface.DEFAULT_BOLD);

        TextView ratingDescription = new TextView(activity);
        ratingDescription.setTextColor(Color.parseColor("#667085"));
        ratingDescription.setTextSize(13);
        ratingDescription.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams ratingDescriptionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        ratingDescriptionParams.setMargins(0, dp(activity, 6), 0, dp(activity, 16));
        ratingDescription.setLayoutParams(ratingDescriptionParams);

        LinearLayout starRow = new LinearLayout(activity);
        starRow.setOrientation(LinearLayout.HORIZONTAL);
        starRow.setGravity(Gravity.CENTER);
        starRow.setPadding(0, dp(activity, 10), 0, dp(activity, 4));

        LinearLayout.LayoutParams starRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        starRowParams.setMargins(0, dp(activity, 4), 0, 0);
        starRow.setLayoutParams(starRowParams);

        final int[] selectedRating = {5};
        TextView[] stars = new TextView[5];

        for (int index = 0; index < 5; index++) {
            final int ratingValue = index + 1;

            TextView star = new TextView(activity);
            star.setText("★");
            star.setTextSize(38);
            star.setGravity(Gravity.CENTER);
            star.setIncludeFontPadding(false);
            star.setClickable(true);
            star.setFocusable(true);
            star.setContentDescription("Rating " + ratingValue + " bintang");

            LinearLayout.LayoutParams starParams = new LinearLayout.LayoutParams(
                    dp(activity, 48),
                    dp(activity, 48)
            );
            starParams.setMargins(dp(activity, 2), 0, dp(activity, 2), 0);
            star.setLayoutParams(starParams);

            star.setOnClickListener(v -> {
                selectedRating[0] = ratingValue;
                refreshStars(stars, selectedRating[0], ratingDescription);
            });

            stars[index] = star;
            starRow.addView(star);
        }

        refreshStars(stars, selectedRating[0], ratingDescription);

        TextView commentLabel = new TextView(activity);
        commentLabel.setText("Komentar");
        commentLabel.setTextColor(Color.parseColor("#344054"));
        commentLabel.setTextSize(14);
        commentLabel.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout.LayoutParams commentLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        commentLabelParams.setMargins(0, dp(activity, 2), 0, dp(activity, 8));
        commentLabel.setLayoutParams(commentLabelParams);

        EditText edtComment = new EditText(activity);
        edtComment.setHint("Ceritakan pengalaman servis kamu");
        edtComment.setTextColor(Color.parseColor("#182230"));
        edtComment.setHintTextColor(Color.parseColor("#98A2B3"));
        edtComment.setTextSize(14);
        edtComment.setGravity(Gravity.TOP | Gravity.START);
        edtComment.setSingleLine(false);
        edtComment.setMinLines(4);
        edtComment.setMaxLines(6);
        edtComment.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edtComment.setBackground(makeRounded(activity, "#FFFFFF", "#D0D5DD", 18, 1));
        edtComment.setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 14), dp(activity, 12));

        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(activity, 132)
        );
        edtComment.setLayoutParams(commentParams);

        TextView counterText = new TextView(activity);
        counterText.setText("0/" + MAX_COMMENT_LENGTH);
        counterText.setTextColor(Color.parseColor("#98A2B3"));
        counterText.setTextSize(12);
        counterText.setGravity(Gravity.END);

        LinearLayout.LayoutParams counterParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        counterParams.setMargins(0, dp(activity, 6), 0, 0);
        counterText.setLayoutParams(counterParams);

        TextView errorText = new TextView(activity);
        errorText.setTextColor(Color.parseColor("#B42318"));
        errorText.setTextSize(13);
        errorText.setVisibility(View.GONE);

        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        errorParams.setMargins(0, dp(activity, 8), 0, 0);
        errorText.setLayoutParams(errorParams);

        edtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Tidak digunakan.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s == null ? 0 : s.length();
                counterText.setText(length + "/" + MAX_COMMENT_LENGTH);
                counterText.setTextColor(Color.parseColor(
                        length > MAX_COMMENT_LENGTH ? "#B42318" : "#98A2B3"
                ));

                if (length <= MAX_COMMENT_LENGTH) {
                    errorText.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Tidak digunakan.
            }
        });

        LinearLayout buttonRow = new LinearLayout(activity);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams buttonRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonRowParams.setMargins(0, dp(activity, 20), 0, 0);
        buttonRow.setLayoutParams(buttonRowParams);

        Button btnCancel = new Button(activity);
        btnCancel.setText("Batal");
        btnCancel.setAllCaps(false);
        btnCancel.setTextSize(14);
        btnCancel.setTextColor(Color.parseColor("#344054"));
        btnCancel.setTypeface(Typeface.DEFAULT_BOLD);
        btnCancel.setBackground(makeRounded(activity, "#FFFFFF", "#D0D5DD", 24, 1));
        btnCancel.setOnClickListener(v -> reviewDialog.dismiss());

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, dp(activity, 50), 1f);
        cancelParams.setMargins(0, 0, dp(activity, 10), 0);
        btnCancel.setLayoutParams(cancelParams);

        Button btnSubmit = new Button(activity);
        btnSubmit.setText("Kirim Ulasan");
        btnSubmit.setAllCaps(false);
        btnSubmit.setTextSize(14);
        btnSubmit.setTextColor(Color.WHITE);
        btnSubmit.setTypeface(Typeface.DEFAULT_BOLD);
        btnSubmit.setBackground(makeGradient(activity));

        LinearLayout.LayoutParams submitParams = new LinearLayout.LayoutParams(0, dp(activity, 50), 1.25f);
        submitParams.setMargins(dp(activity, 10), 0, 0, 0);
        btnSubmit.setLayoutParams(submitParams);

        btnSubmit.setOnClickListener(v -> {
            String comment = edtComment.getText() == null
                    ? ""
                    : edtComment.getText().toString().trim();

            if (selectedRating[0] < 1 || selectedRating[0] > 5) {
                showInputError(errorText, "Pilih rating terlebih dahulu.");
                return;
            }

            if (comment.length() > MAX_COMMENT_LENGTH) {
                showInputError(errorText, "Komentar maksimal 1000 karakter.");
                edtComment.requestFocus();
                return;
            }

            errorText.setVisibility(View.GONE);
            setSubmitLoading(btnSubmit, true);

            submitReview(
                    activity,
                    serviceRequestId,
                    selectedRating[0],
                    comment,
                    reviewDialog,
                    btnSubmit,
                    callback
            );
        });

        buttonRow.addView(btnCancel);
        buttonRow.addView(btnSubmit);

        container.addView(title);
        container.addView(subtitle);
        container.addView(ratingLabel);
        container.addView(starRow);
        container.addView(ratingDescription);
        container.addView(commentLabel);
        container.addView(edtComment);
        container.addView(counterText);
        container.addView(errorText);
        container.addView(buttonRow);

        reviewDialog.setContentView(scrollView);
        reviewDialog.show();

        Window window = reviewDialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.48f);
            window.setLayout(
                    activity.getResources().getDisplayMetrics().widthPixels - dp(activity, 40),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private static void refreshStars(TextView[] stars, int selectedRating, TextView ratingDescription) {
        if (stars == null) {
            return;
        }

        for (int index = 0; index < stars.length; index++) {
            TextView star = stars[index];

            if (star == null) {
                continue;
            }

            boolean filled = index < selectedRating;
            star.setText(filled ? "★" : "☆");
            star.setTextColor(Color.parseColor(filled ? "#F59E0B" : "#D0D5DD"));
        }

        if (ratingDescription != null) {
            ratingDescription.setText(getRatingDescription(selectedRating));
        }
    }

    private static String getRatingDescription(int rating) {
        switch (rating) {
            case 1:
                return "Sangat kurang";
            case 2:
                return "Kurang memuaskan";
            case 3:
                return "Cukup baik";
            case 4:
                return "Baik";
            case 5:
                return "Sangat baik";
            default:
                return "Ketuk bintang untuk memberi rating";
        }
    }

    private static void submitReview(
            BaseActivity activity,
            String serviceRequestId,
            int rating,
            String comment,
            Dialog dialog,
            Button btnSubmit,
            ReviewCallback callback
    ) {
        ApiClient.getApiService(activity)
                .createReview(serviceRequestId, new CreateReviewRequest(rating, comment))
                .enqueue(new Callback<ApiResponse<ReviewResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ReviewResponse>> call,
                            Response<ApiResponse<ReviewResponse>> response
                    ) {
                        if (!response.isSuccessful()) {
                            String errorMessage = ErrorParser.parseError(response, "Gagal mengirim ulasan.");

                            if (isDuplicateReviewMessage(errorMessage)) {
                                finishAsReviewed(activity, serviceRequestId, dialog, callback);
                                AppToast.warning(activity, "Order ini sudah pernah diulas.");
                                return;
                            }

                            setSubmitLoading(btnSubmit, false);
                            AppToast.error(activity, errorMessage);
                            return;
                        }

                        ApiResponse<ReviewResponse> body = response.body();

                        if (body == null || !body.success) {
                            setSubmitLoading(btnSubmit, false);
                            AppToast.error(activity, ErrorParser.getBestMessage(body, "Gagal mengirim ulasan."));
                            return;
                        }

                        finishAsReviewed(activity, serviceRequestId, dialog, callback);
                        AppToast.success(activity, "Ulasan berhasil dikirim!");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReviewResponse>> call, Throwable t) {
                        setSubmitLoading(btnSubmit, false);
                        AppToast.error(activity, "Tidak bisa terhubung ke server.");
                    }
                });
    }

    private static void finishAsReviewed(
            BaseActivity activity,
            String serviceRequestId,
            Dialog dialog,
            ReviewCallback callback
    ) {
        ReviewStateStore.markReviewed(activity, serviceRequestId);

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (callback != null) {
            callback.onReviewCompleted();
        }
    }

    private static boolean isDuplicateReviewMessage(String message) {
        if (TextHelper.isBlank(message)) {
            return false;
        }

        String normalized = message.toLowerCase();

        return normalized.contains("already")
                || normalized.contains("duplicate")
                || normalized.contains("pernah")
                || normalized.contains("sudah")
                || normalized.contains("review");
    }

    private static void setSubmitLoading(Button button, boolean loading) {
        if (button == null) {
            return;
        }

        button.setEnabled(!loading);
        button.setAlpha(loading ? 0.65f : 1f);
        button.setText(loading ? "Mengirim..." : "Kirim Ulasan");
    }

    private static void showInputError(TextView errorText, String message) {
        if (errorText == null) {
            return;
        }

        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private static GradientDrawable makeRounded(
            BaseActivity activity,
            String fillColor,
            String strokeColor,
            int radiusDp,
            int strokeWidthDp
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.parseColor(fillColor));
        drawable.setCornerRadius(dp(activity, radiusDp));

        if (strokeColor != null && strokeWidthDp > 0) {
            drawable.setStroke(dp(activity, strokeWidthDp), Color.parseColor(strokeColor));
        }

        return drawable;
    }

    private static GradientDrawable makeGradient(BaseActivity activity) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#243B7A"),
                        Color.parseColor("#4F6DF5")
                }
        );
        drawable.setCornerRadius(dp(activity, 24));
        return drawable;
    }

    private static int dp(BaseActivity activity, int value) {
        return ViewHelper.dp(activity, value);
    }
}
