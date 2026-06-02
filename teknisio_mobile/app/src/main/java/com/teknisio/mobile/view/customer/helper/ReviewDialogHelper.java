package com.teknisio.mobile.view.customer.helper;

import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teknisio.mobile.R;
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

        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(activity, 20), dp(activity, 20), dp(activity, 20), dp(activity, 18));
        container.setBackgroundResource(R.drawable.bg_order_card);

        TextView title = new TextView(activity);
        title.setText("Tulis Ulasan");
        title.setTextColor(Color.parseColor("#1F2329"));
        title.setTextSize(20);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        TextView ratingLabel = new TextView(activity);
        ratingLabel.setText("Rating (1-5)");
        ratingLabel.setTextColor(Color.parseColor("#344054"));
        ratingLabel.setTextSize(14);

        LinearLayout.LayoutParams ratingLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        ratingLabelParams.setMargins(0, dp(activity, 14), 0, dp(activity, 6));
        ratingLabel.setLayoutParams(ratingLabelParams);

        EditText edtRating = new EditText(activity);
        edtRating.setHint("Contoh: 5");
        edtRating.setSingleLine(true);
        edtRating.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtRating.setBackgroundResource(R.drawable.bg_order_input);
        edtRating.setPadding(dp(activity, 14), 0, dp(activity, 14), 0);

        LinearLayout.LayoutParams ratingParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(activity, 48)
        );
        edtRating.setLayoutParams(ratingParams);

        TextView commentLabel = new TextView(activity);
        commentLabel.setText("Komentar");
        commentLabel.setTextColor(Color.parseColor("#344054"));
        commentLabel.setTextSize(14);

        LinearLayout.LayoutParams commentLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        commentLabelParams.setMargins(0, dp(activity, 12), 0, dp(activity, 6));
        commentLabel.setLayoutParams(commentLabelParams);

        EditText edtComment = new EditText(activity);
        edtComment.setHint("Ceritakan pengalaman servis kamu");
        edtComment.setSingleLine(false);
        edtComment.setMinLines(3);
        edtComment.setMaxLines(4);
        edtComment.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        edtComment.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edtComment.setBackgroundResource(R.drawable.bg_order_input);
        edtComment.setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 14), dp(activity, 12));

        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(activity, 104)
        );
        edtComment.setLayoutParams(commentParams);

        TextView errorText = new TextView(activity);
        errorText.setTextColor(Color.parseColor("#D92D20"));
        errorText.setTextSize(12);
        errorText.setVisibility(View.GONE);

        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        errorParams.setMargins(0, dp(activity, 8), 0, 0);
        errorText.setLayoutParams(errorParams);

        LinearLayout btnRow = new LinearLayout(activity);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams btnRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnRowParams.setMargins(0, dp(activity, 18), 0, 0);
        btnRow.setLayoutParams(btnRowParams);

        Button btnCancel = new Button(activity);
        btnCancel.setText("Batal");
        btnCancel.setAllCaps(false);
        btnCancel.setTextColor(Color.parseColor("#344054"));
        btnCancel.setBackgroundResource(R.drawable.bg_order_input);

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, dp(activity, 46), 1f);
        cancelParams.setMargins(0, 0, dp(activity, 8), 0);
        btnCancel.setLayoutParams(cancelParams);
        btnCancel.setOnClickListener(v -> reviewDialog.dismiss());

        Button btnSubmit = new Button(activity);
        btnSubmit.setText("Kirim");
        btnSubmit.setAllCaps(false);
        btnSubmit.setBackground(activity.getResources().getDrawable(R.drawable.bg_order_primary, activity.getTheme()));
        btnSubmit.setTextColor(Color.parseColor("#D4FFFF"));

        LinearLayout.LayoutParams submitParams = new LinearLayout.LayoutParams(0, dp(activity, 46), 1f);
        submitParams.setMargins(dp(activity, 8), 0, 0, 0);
        btnSubmit.setLayoutParams(submitParams);

        btnSubmit.setOnClickListener(v -> {
            Integer rating = parseRating(edtRating, errorText);

            if (rating == null) {
                return;
            }

            String comment = edtComment.getText() == null ? "" : edtComment.getText().toString().trim();

            errorText.setVisibility(View.GONE);
            btnSubmit.setEnabled(false);

            submitReview(
                    activity,
                    serviceRequestId,
                    rating,
                    comment.isEmpty() ? null : comment,
                    reviewDialog,
                    btnSubmit,
                    callback
            );
        });

        btnRow.addView(btnCancel);
        btnRow.addView(btnSubmit);

        container.addView(title);
        container.addView(ratingLabel);
        container.addView(edtRating);
        container.addView(commentLabel);
        container.addView(edtComment);
        container.addView(errorText);
        container.addView(btnRow);

        reviewDialog.setContentView(container);
        reviewDialog.setCanceledOnTouchOutside(true);
        reviewDialog.show();

        Window window = reviewDialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    activity.getResources().getDisplayMetrics().widthPixels - dp(activity, 44),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private static Integer parseRating(EditText edtRating, TextView errorText) {
        String ratingText = edtRating.getText() == null ? "" : edtRating.getText().toString().trim();

        if (ratingText.isEmpty()) {
            showInputError(errorText, "Rating wajib diisi.");
            edtRating.requestFocus();
            return null;
        }

        int rating;

        try {
            rating = Integer.parseInt(ratingText);
        } catch (Exception ignored) {
            showInputError(errorText, "Rating harus berupa angka.");
            edtRating.requestFocus();
            return null;
        }

        if (rating < 1 || rating > 5) {
            showInputError(errorText, "Rating harus antara 1 dan 5.");
            edtRating.requestFocus();
            return null;
        }

        return rating;
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

                            btnSubmit.setEnabled(true);
                            AppToast.error(activity, errorMessage);
                            return;
                        }

                        ApiResponse<ReviewResponse> body = response.body();

                        if (body == null || !body.success) {
                            btnSubmit.setEnabled(true);
                            AppToast.error(activity, ErrorParser.getBestMessage(body, "Gagal mengirim ulasan."));
                            return;
                        }

                        finishAsReviewed(activity, serviceRequestId, dialog, callback);
                        AppToast.success(activity, "Ulasan berhasil dikirim!");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReviewResponse>> call, Throwable t) {
                        btnSubmit.setEnabled(true);
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

    private static void showInputError(TextView errorText, String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private static int dp(BaseActivity activity, int value) {
        return ViewHelper.dp(activity, value);
    }
}
