package com.teknisio.mobile.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teknisio.mobile.model.response.StatusHistoryResponse;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class StatusHistoryRenderer {

    private StatusHistoryRenderer() {
    }

    public static void render(
            Context context,
            LinearLayout container,
            TextView label,
            List<StatusHistoryResponse> historyList
    ) {
        if (container == null) {
            return;
        }

        container.removeAllViews();

        if (historyList == null || historyList.isEmpty()) {
            if (label != null) {
                label.setVisibility(View.GONE);
            }

            container.setVisibility(View.GONE);
            return;
        }

        if (label != null) {
            label.setVisibility(View.VISIBLE);
        }

        container.setVisibility(View.VISIBLE);

        for (StatusHistoryResponse item : historyList) {
            if (item != null) {
                container.addView(createHistoryRow(context, item));
            }
        }
    }

    private static View createHistoryRow(Context context, StatusHistoryResponse item) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(context, 6), 0, dp(context, 6));

        View dot = new View(context);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(
                dp(context, 10),
                dp(context, 10)
        );
        dotParams.setMargins(0, dp(context, 6), dp(context, 12), 0);
        dot.setLayoutParams(dotParams);

        GradientDrawable dotDrawable = new GradientDrawable();
        dotDrawable.setShape(GradientDrawable.OVAL);
        dotDrawable.setColor(Color.parseColor(OrderStatusHelper.getStatusColor(item.newStatus)));
        dot.setBackground(dotDrawable);

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        TextView txtStatus = new TextView(context);
        txtStatus.setText(OrderStatusHelper.getDisplayStatus(item.newStatus));
        txtStatus.setTextColor(Color.parseColor("#1F2329"));
        txtStatus.setTextSize(14);
        txtStatus.setTypeface(Typeface.DEFAULT_BOLD);

        TextView txtTime = new TextView(context);
        txtTime.setText(formatHistoryTime(item.changedAt));
        txtTime.setTextColor(Color.parseColor("#6B7680"));
        txtTime.setTextSize(12);

        textColumn.addView(txtStatus);
        textColumn.addView(txtTime);

        if (!TextHelper.isBlank(item.note)) {
            TextView txtNote = new TextView(context);
            txtNote.setText(item.note.trim());
            txtNote.setTextColor(Color.parseColor("#5F6B73"));
            txtNote.setTextSize(13);

            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            noteParams.setMargins(0, dp(context, 2), 0, 0);
            txtNote.setLayoutParams(noteParams);

            textColumn.addView(txtNote);
        }

        row.addView(dot);
        row.addView(textColumn);

        return row;
    }

    private static String formatHistoryTime(String isoTime) {
        if (TextHelper.isBlank(isoTime)) {
            return "-";
        }

        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(isoTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, HH:mm",
                    new Locale("id", "ID")
            );

            return dateTime.format(formatter);
        } catch (Exception ignored) {
            return isoTime;
        }
    }

    private static int dp(Context context, int value) {
        return ViewHelper.dp(context, value);
    }
}
