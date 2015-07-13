package com.tsuru.eiichi.randomwiki2;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by Artnet on 2015/07/09.
 */
public class RandomWikiAccess {

    private boolean _wikiImageMode = false;         //wiki内画像取得する。しない。

    private String _wikiTitle = "";         //wikiタイトル
    private String _wikiBody = "";          //wikiボディー
    private String _wikiJpgUrl = "";        //wikiJpeg

    private String _wikiText = "";          //wikiテキスト全体
    private Elements _wikiClass;            //wikiClassタグ内容保持

    private int _appStatus;               //getWikiData実行自の状態
    public static final int WAIT = 0;
    public static final int HTTPING = 1;
    public static final int SCRAPING = 2;
    public static final int FINISH = 9;
    public static final int ERR = -1;

    //コンストラクタ
    RandomWikiAccess(){
    }
    RandomWikiAccess(boolean mode){
        _wikiImageMode = mode;
    }

    //wikiタイトル名を返す
    public String getWikiTitle()
    {
        return _wikiTitle;
    }

    //wiki本文（取得結果）を返す
    public String getWikiOneLine()
    {
        return _wikiBody;
    }

    //現在処理状態返す
    public int getWikiDataStatus()
    {
        return _appStatus;
    }

    //画像urlを返す
    public String getWikiUrl()
    {
        return _wikiJpgUrl;
    }

    public void getWikiData(){
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {

                //wikiデータ ランダム取得ＵＲＬ
                String url = "http://ja.wikipedia.org/wiki/Special:Randompage";

                //内部変数初期化
                _wikiTitle = "";         //wikiタイトル
                _wikiBody = "";          //wikiボディー
                _wikiJpgUrl = "";        //wikiJpeg
                _wikiText = "";          //wikiテキスト全体

                //状態初期化
                _appStatus = WAIT;

                try {

                    // wikiドキュメントを取得
                    _appStatus = HTTPING;
                    Document document = Jsoup.connect(url).get();

                    //スクレイピング開始
                    _appStatus = SCRAPING;

                    _wikiText = document.text();                         //htmlからテキスト文のみを取り出し
                    _wikiTitle = document.title();                       //wikiのタイトルを取り出し
                    _wikiClass = document.select(".image");              //class=imageが付いてるhtml文を取り出し (画像リンクの取り出しに使う)

                    //Wikiタイトル
                    Log.v("wikiTitle > ", _wikiTitle);

                    //Wiki文取り出し。（最終更新日以降の文字列 ～ 最初の文末（"。"）まで）
                    _wikiBody = _wikiText.substring(_wikiText.indexOf("最終更新"), _wikiText.indexOf("。") + 1);
                    Log.v("wikiBody > ", _wikiBody);

                    //画像リンク部分を取り出し。（最初のリンクがwikiタイトル関連する画像リンク）
                    String w_hoges;
                    for (Element e : _wikiClass) {
                        w_hoges = e.toString();
                        Log.v("wikiClass > ", w_hoges);

                        //拡張子（.jpg）が含まれるリンクを取り出す。
                        if (w_hoges.indexOf(".jpg") != -1) {
                            _wikiJpgUrl = "http://ja.wikipedia.org" + w_hoges.substring(w_hoges.indexOf("\"") + 1 , w_hoges.indexOf(".jpg") + 4);
                            Log.v("wikiLinks_jpg > ", _wikiJpgUrl);
                            break;
                        }

                        //拡張子（.JPG）が含まれるリンクを取り出す。
                        if (w_hoges.indexOf(".JPG") != -1) {
                           _wikiJpgUrl = "http://ja.wikipedia.org" + w_hoges.substring(w_hoges.indexOf("\"") + 1 , w_hoges.indexOf(".JPG") + 4);
                            Log.v("wikiLinks_JPG > ", _wikiJpgUrl);
                            break;
                        }
                    }

                    //スクレイピング終了
                    _appStatus = FINISH;

                } catch (IOException e) {
                    //スクレイピング終了(エラー)
                    _appStatus = ERR;
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result){

                //画像リンクが取得できていれば表示処理。 取得できてない場合は、wiki再検索
                if (_wikiJpgUrl.length() != 0){
                    //etc 再取得がｎ回続いて取れない時は何かする？

                } else {
                    //wiki内画像取得モードならば、再度画像取得処理実行
                    if(_wikiImageMode){
                        //画像URLが取得NG（再度wiki検索）
                        getWikiData();
                    }
                }
            }
        };
        task.execute();

    }

}
