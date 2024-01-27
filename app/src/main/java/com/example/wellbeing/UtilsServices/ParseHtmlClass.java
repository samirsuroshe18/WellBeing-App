package com.example.wellbeing.UtilsServices;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.nio.charset.StandardCharsets;

public class ParseHtmlClass {
    public ParseHtmlClass(VolleyError error, Context context) {
        String htmlErrorMessage = new String(error.networkResponse.data, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(htmlErrorMessage);
        String errorMsg = doc.select("pre").text();
        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
    }
}
