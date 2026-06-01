package com.teknisio.mobile.view.customer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.util.BackButtonHelper;

public class NewsActivity extends BaseActivity {

    public static final String EXTRA_NEWS_ID = "extra_news_id";
    public static final String NEWS_ID_FEATURED = "featured";

    private static final String[] IDS = {
            NEWS_ID_FEATURED,
            "maintenance",
            "safety"
    };

    private static final String[] TITLES = {
            "Tips merawat alat elektronik rumah tangga",
            "Kapan perangkat perlu diservis?",
            "Cara aman memakai perangkat elektronik"
    };

    private static final String[] SUBTITLES = {
            "Panduan singkat agar perangkat lebih awet dan aman digunakan.",
            "Kenali tanda awal kerusakan sebelum biaya servis membesar.",
            "Langkah sederhana untuk mencegah korsleting dan kerusakan."
    };

    private static final String[] DATES = {
            "2 April 2026",
            "8 April 2026",
            "15 April 2026"
    };

    private static final int[] IMAGES = {
            R.drawable.tips_news,
            R.drawable.tips_news,
            R.drawable.alert_news
    };

    private static final String[] CONTENTS = {
            "Bersihkan perangkat secara berkala, hindari penggunaan berlebihan, dan pastikan ventilasi tidak tertutup. Untuk perangkat seperti kulkas, mesin cuci, kipas, dan AC, pemeriksaan rutin dapat membantu mendeteksi kerusakan lebih awal. Jika muncul suara tidak normal, bau terbakar, atau performa menurun, segera hubungi teknisi.",
            "Perangkat biasanya perlu diservis ketika mulai muncul tanda seperti suara kasar, getaran berlebihan, pendinginan melemah, air bocor, daya tidak stabil, atau perangkat sering mati sendiri. Jangan menunggu sampai perangkat mati total, karena kerusakan kecil bisa berkembang menjadi kerusakan komponen utama.",
            "Gunakan stopkontak yang layak, hindari kabel terkelupas, dan jangan menumpuk terlalu banyak perangkat pada satu colokan. Matikan perangkat ketika tidak digunakan dalam waktu lama. Jika perangkat terkena air atau muncul percikan, hentikan penggunaan dan minta teknisi memeriksa kondisinya."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String newsId = getIntent().getStringExtra(EXTRA_NEWS_ID);
        setContentView(createContent(newsId));
    }

    private View createContent(String newsId) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.parseColor("#EEF7FA"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(38), dp(22), dp(28));

        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        root.addView(createHeader(isBlank(newsId) ? "Artikel" : "Detail Artikel"));

        if (isBlank(newsId)) {
            renderList(root);
        } else {
            renderDetail(root, newsId);
        }

        return scrollView;
    }

    private View createHeader(String titleText) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setMinimumHeight(dp(54));

        View backView = LayoutInflater.from(this)
                .inflate(R.layout.component_back_button, header, false);

        if (backView instanceof FrameLayout) {
            BackButtonHelper.setup((FrameLayout) backView, this::finish);
        } else {
            backView.setOnClickListener(v -> finish());
        }

        header.addView(backView, new LinearLayout.LayoutParams(dp(54), dp(54)));

        TextView title = new TextView(this);
        title.setText(titleText);
        title.setTextColor(Color.parseColor("#1F2329"));
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        titleParams.setMargins(dp(16), 0, 0, 0);
        header.addView(title, titleParams);

        return header;
    }

    private void renderList(LinearLayout root) {
        TextView subtitle = new TextView(this);
        subtitle.setText("Kumpulan informasi singkat untuk membantu pelanggan merawat perangkat elektronik.");
        subtitle.setTextColor(Color.parseColor("#6B7680"));
        subtitle.setTextSize(14);
        subtitle.setLineSpacing(dp(3), 1f);

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, dp(18), 0, dp(12));
        root.addView(subtitle, subtitleParams);

        for (int i = 0; i < IDS.length; i++) {
            root.addView(createNewsCard(i));
        }
    }

    private void renderDetail(LinearLayout root, String newsId) {
        int index = findIndex(newsId);

        ImageView hero = createArticleImage(index, 178);

        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(178)
        );
        heroParams.setMargins(0, dp(22), 0, 0);
        root.addView(hero, heroParams);

        TextView articleTitle = new TextView(this);
        articleTitle.setText(TITLES[index]);
        articleTitle.setTextColor(Color.parseColor("#1F2329"));
        articleTitle.setTextSize(22);
        articleTitle.setTypeface(Typeface.DEFAULT_BOLD);
        articleTitle.setLineSpacing(dp(3), 1f);

        LinearLayout.LayoutParams articleTitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        articleTitleParams.setMargins(0, dp(18), 0, 0);
        root.addView(articleTitle, articleTitleParams);

        TextView date = new TextView(this);
        date.setText(DATES[index]);
        date.setTextColor(Color.parseColor("#8B949C"));
        date.setTextSize(12);

        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dateParams.setMargins(0, dp(8), 0, 0);
        root.addView(date, dateParams);

        TextView articleSubtitle = new TextView(this);
        articleSubtitle.setText(SUBTITLES[index]);
        articleSubtitle.setTextColor(Color.parseColor("#6B7680"));
        articleSubtitle.setTextSize(14);
        articleSubtitle.setLineSpacing(dp(3), 1f);

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, dp(10), 0, 0);
        root.addView(articleSubtitle, subtitleParams);

        TextView content = new TextView(this);
        content.setText(CONTENTS[index]);
        content.setTextColor(Color.parseColor("#4E5B63"));
        content.setTextSize(15);
        content.setLineSpacing(dp(6), 1f);
        content.setBackground(makeRounded("#FFFFFF", 18));
        content.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        contentParams.setMargins(0, dp(18), 0, dp(18));
        root.addView(content, contentParams);

        TextView otherTitle = new TextView(this);
        otherTitle.setText("Artikel lainnya");
        otherTitle.setTextColor(Color.parseColor("#1F2329"));
        otherTitle.setTextSize(18);
        otherTitle.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(otherTitle);

        for (int i = 0; i < IDS.length; i++) {
            if (i != index) {
                root.addView(createNewsCard(i));
            }
        }
    }

    private View createNewsCard(int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(makeRounded("#FFFFFF", 18));
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setClickable(true);
        card.setOnClickListener(v -> {
            Intent intent = new Intent(NewsActivity.this, NewsActivity.class);
            intent.putExtra(EXTRA_NEWS_ID, IDS[index]);
            startActivity(intent);
        });

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, dp(12), 0, 0);
        card.setLayoutParams(cardParams);

        ImageView image = createArticleImage(index, 136);

        card.addView(image, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(136)
        ));

        TextView title = new TextView(this);
        title.setText(TITLES[index]);
        title.setTextColor(Color.parseColor("#1F2329"));
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setMaxLines(2);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, dp(12), 0, 0);
        card.addView(title, titleParams);

        TextView subtitle = new TextView(this);
        subtitle.setText(SUBTITLES[index]);
        subtitle.setTextColor(Color.parseColor("#6B7680"));
        subtitle.setTextSize(13);
        subtitle.setMaxLines(2);
        subtitle.setLineSpacing(dp(3), 1f);

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, dp(7), 0, 0);
        card.addView(subtitle, subtitleParams);

        TextView date = new TextView(this);
        date.setText(DATES[index]);
        date.setTextColor(Color.parseColor("#8B949C"));
        date.setTextSize(12);

        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dateParams.setMargins(0, dp(10), 0, 0);
        card.addView(date, dateParams);

        return card;
    }

    private ImageView createArticleImage(int index, int heightDp) {
        ImageView image = new ImageView(this);
        image.setImageResource(IMAGES[index]);
        image.setAdjustViewBounds(false);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundColor(Color.parseColor("#F5FBFD"));
        image.setContentDescription(TITLES[index]);
        image.setMinimumHeight(dp(heightDp));
        return image;
    }

    private int findIndex(String id) {
        for (int i = 0; i < IDS.length; i++) {
            if (IDS[i].equals(id)) {
                return i;
            }
        }

        return 0;
    }

    private GradientDrawable makeRounded(String color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }


    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
