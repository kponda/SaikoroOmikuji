var request = require("request");
var $ = require('jquery')(require("jsdom").jsdom().parentWindow);

exports.getLuckyKeyword = getLuckyKeyword;

/**
 * Wikipediaからランダムにタイトル、画像等を取得する
 *
 * callbackには、Wikipediaから取得したデータをオブジェクトで渡す。
 *
 * オブジェクトのキーは title, text, img
 *
 * ex: getLuckyKeyword(function(result){
 *  console.log(result.title, result.text, result.img);
 * });
 */
var getLuckyKeyword = function(callback) {
  request("http://ja.wikipedia.org/wiki/Special:Randompage", function (error, response, body) {
      if (!error) {
          var result = parseHtml(body);

          if(result) {
            callback(result);
          } else {
            getLuckyKeyword(callback);
          }
      } else {
          console.log(error);
          callback(null);
      }
  });
}

var parseHtml = function(html, callback) {
    // タイトル取得
    var re = new RegExp("<title>(.*?) - Wikipedia</title>", "i");
    var title = html.match(re)[1];

    var doc = $(html);

    // 一文取得
    var text = $('#mw-content-text > p', doc).first().text().split('。')[0];
    // console.log(text);

    // 画像URL
    var img = $('#mw-content-text .thumbinner img', doc).first().attr('src');
    // console.log(img);

    if (!img || img.length == 0) {
      return false;
    }

    return {
      title: title,
      text: text,
      img: img
    };
};

var main = function(){
  getLuckyKeyword(function(result) {
    console.log(result);
  });
}

if (require.main === module) {
    main();
}
