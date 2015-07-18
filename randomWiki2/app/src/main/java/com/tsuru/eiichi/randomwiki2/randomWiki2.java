package com.tsuru.eiichi.randomwiki2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class randomWiki2 extends ActionBarActivity {

    private Button downloadButton;
    private String wikiText = "";
    private String wikiTitle="";
    private String wikiJpgUrl="";
    private String wikiBody="";
    private Elements  wikiClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_wiki2);

        downloadButton = (Button) findViewById(R.id.download);
        downloadButton.setOnClickListener(new View.OnClickListener() {

            //wikiランダム取得クラスを使う場合宣言（true:画像未取得時、再取得する。(時間掛かる)  /  false:画像が無くてもwiki１件取得したらそこで終了（時間が掛からない）
            RandomWikiAccess rwa = new RandomWikiAccess(false);

            @Override
            public void onClick(View v) {

                //wikiタイトル＆wiki本文＆wiki画像リンク初期化
                wikiTitle = "";
                wikiBody = "";
                wikiJpgUrl = "";

                /* textオブジェクトを取得 */
                TextView tv = (TextView) findViewById(R.id.textView1);
                tv.setText("wikiをランダム検索中。" +  "\n" +"しばらくお待ちください...");


//クラスを使う時のソース↓
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // マルチスレッドにしたい処理 ここから
                        rwa.getWikiData();
                        while(rwa.getWikiDataStatus() != RandomWikiAccess.FINISH) {
                            //Log.v("status > ", String.valueOf(rwa.getWikiDataStatus()));
                        }
                        // マルチスレッドにしたい処理 ここまで
                    }
                }).start();

                Log.v("wikiTitle > ", rwa.getWikiTitle());
                Log.v("wikiOneLine > ", rwa.getWikiOneLine());
                Log.v("wikiImage > ", rwa.getWikiUrl());
                tv.setText(rwa.getWikiTitle() + "\n" + rwa.getWikiOneLine() + "\n" + "\n" + rwa.getWikiUrl());
//クラスを使う時のソース↑


//クラスを使わない時のソース↓
//                taskExe();
//クラスを使わない時のソース↑

            }
        });

    }

//クラスを使わない時のソース↓
    private void taskExe(){

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {

                //wikiデータ ランダム取得ＵＲＬ
                String url = "http://ja.wikipedia.org/wiki/Special:Randompage";


                try {

                        // wikiドキュメントを取得
                        Document document = Jsoup.connect(url).get();
                        wikiText = document.text();                         //htmlからテキスト文のみを取り出し
                        wikiTitle = document.title();                       //wikiのタイトルを取り出し
                        wikiClass = document.select(".image");              //class=imageが付いてるhtml文を取り出し (画像リンクの取り出しに使う)


                        //Wikiタイトル
                        Log.v("wikiTitle > ", wikiTitle);

                        //Wiki文取り出し。（最終更新日以降の文字列 ～ 最初の文末（"。"）まで）
                        wikiBody = wikiText.substring(wikiText.indexOf("最終更新"), wikiText.indexOf("。") + 1);
                        Log.v("wikiBody > ", wikiBody);

                        //画像リンク部分を取り出し。（最初のリンクがwikiタイトル関連する画像リンク）
                        String w_hoges;
                        for (Element e : wikiClass) {
                            w_hoges = e.toString();
                            Log.v("wikiClass > ", w_hoges);

                            //拡張子（.jpg）が含まれるリンクを取り出す。
                            if (w_hoges.indexOf(".jpg") != -1) {
                                wikiJpgUrl = "http://ja.wikipedia.org" + w_hoges.substring(w_hoges.indexOf("\"") + 1 , w_hoges.indexOf(".jpg") + 4);
                                Log.v("wikiLinks_jpg > ", wikiJpgUrl);
                                break;
                            }

                            //拡張子（.JPG）が含まれるリンクを取り出す。
                            if (w_hoges.indexOf(".JPG") != -1) {
                                wikiJpgUrl = "http://ja.wikipedia.org" + w_hoges.substring(w_hoges.indexOf("\"") + 1 , w_hoges.indexOf(".JPG") + 4);
                                Log.v("wikiLinks_JPG > ", wikiJpgUrl);
                                break;
                            }
                        }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result){

                //画像リンクが取得できていれば表示処理。 取得できてない場合は、wiki再検索
                if (wikiJpgUrl.length() != 0){
                    //画像URLが取得OK
                    /* textオブジェクトを取得 */
                    TextView tv = (TextView) findViewById(R.id.textView1);
                    //wiki本文および、本文関連画像リンクを出力
                    tv.setText(wikiTitle + "\n" + wikiBody + "\n" + "\n" + wikiJpgUrl);
                } else {
                    //画像URLが取得NG（再度wiki検索）
                    taskExe();
                }


            }
        };
        task.execute();
    }
//クラスを使わない時のソース↑

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_random_wiki2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
