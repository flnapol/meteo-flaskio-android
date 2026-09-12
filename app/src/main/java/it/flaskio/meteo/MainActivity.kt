package it.flaskio.meteo
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
class MainActivity: Activity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val w=WebView(this)
  w.settings.javaScriptEnabled=true
  w.settings.domStorageEnabled=true
  w.webViewClient=WebViewClient()
  w.loadUrl("https://meteo-italia-360.flavio-napoleoni.chatgpt.site")
  setContentView(w)
 }
}