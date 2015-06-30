var twitter = require('twitter');

exports.post = function (message) {
    // 外に出す
    var twit = new twitter({
        consumer_key: process.env.TWITTER_CONSUMER_KEY,
        consumer_secret: process.env.TWITTER_CONSUMER_SECRET,
        access_token_key: process.env.TWITTER_ACCESS_TOKEN_KEY,
        access_token_secret: process.env.TWITTER_ACCESS_TOKEN_SECRET,
        });

        console.log(process.env.TWITTER_CONSUMER_KEY);
        console.log(process.env.TWITTER_CONSUMER_SECRET);
        console.log(process.env.TWITTER_ACCESS_TOKEN_KEY);
        console.log(process.env.TWITTER_ACCESS_TOKEN_SECRET);


    // tweet
    twit.post('statuses/update', {status: message},  function(error, tweet, response){
      if(error){
          console.log(error);
          throw error;
      }
      //console.log(tweet);     // Tweet body.
      //console.log(response);  // Raw response object.
    });
};
