var express = require('express');
var router = express.Router();
var path = require('path');

var dummy_user = "dummy_user";
var system_config_path = path.join(__dirname, '../config/system-config');
var system_config = require(system_config_path);
var dynamodb = require('../common/dynamodb_util');

var AWS = require('aws-sdk');
var db = new AWS.DynamoDB({region:system_config.dynamodb.region,
                           apiVersion:system_config.dynamodb.apiVersion});
if (system_config.dynamodb.endpoint!=null) {
  db.setEndpoint(system_config.dynamodb.endpoint);
}

var Ranking = dynamodb(db,system_config.schema);

var omikuji_hantei = system_config.omikuji_hantei;

/* GET home page. */
router.get('/regist', function(req, res, next) {
  Ranking.query({
    IndexName: system_config.dynamodb.index_name,
    KeyConditionExpression: "dummy = :Player AND saikoro_point = :zero",
    ExpressionAttributeValues: {
      ":Player": "Player",
      ":zero": 0
    },
    ScanIndexForward: false,
    Select: "ALL_ATTRIBUTES"
  },function(err,result) {
console.log(result);
    var user_name = "";
    if (result.Items.length>0) {
      user_name = result.Items[0].user_name;
    }
    res.render('index', { title: 'Express',user_name:user_name });
  });

});

router.post('/regist',function(req, res, next) {
  var io = req.app.get('io');
  Ranking.query({
    IndexName: system_config.dynamodb.index_name,
    KeyConditionExpression: "dummy = :Player AND saikoro_point = :zero",
    ExpressionAttributeValues: {
      ":Player": "Player",
      ":zero": 0
    },
    ScanIndexForward: false,
    Select: "ALL_ATTRIBUTES"
  },function(err,result) {
    var user_name = "";
    if (result.Items.length>0) {
      user_name = result.Items[0].player_name;
    }
    if (user_name == "" || req.body.saikoro_point=="") {
        io.sockets.emit('error request',{});
        res.send('NG');
    } else {
      Ranking.add({
        Item:{
          user_name:user_name,
          saikoro_point:req.body.saikoro_point-0,
          omikuji_ranking: omikuji_hantei(req.body.saikoro_point-0),
          dummy:"OK"
        }
      },function(err,result) {
        console.log(err);
        console.log(result);
        
        io.sockets.emit('saikoro point',{user_name:user_name,saikoro_point:req.body.saikoro_point-0});
        
        Ranking.update({
          Key:{
            user_name:user_name
          },
          KeyConditionExpression: "SET saikoro_point = :saikoro_point",
          ExpressionAttributeValues: {
            ":saikoro_point": req.body.saikoro_point-0
          }
        },function(err,result) {
          res.send('OK');
        });

      });
    }
  });
});

router.get('/', function(req, res, next) {
  res.render('start', { title: 'スタートページ' });
});

router.post('/',function(req, res, next) {
  console.log(req.body);
  Ranking.add({
    Item:{
      user_name:dummy_user,
      player_name:req.body.user_name,
      saikoro_point:0,
      dummy:"Player"
    }
  },function(err,result) {
    console.log(err);
    console.log(result);
    res.redirect('/wait');
    // res.render('index', { title: 'Express' });
  });
});

router.get('/wait',function(req, res, next) {
  res.render('wait', { title: 'ウエイトページ' });
});

/* GET users listing. */
router.get('/ranking', function(req, res, next) {
  
  Ranking.query({
    IndexName: system_config.dynamodb.index_name,
    KeyConditionExpression: "dummy = :OK AND saikoro_point >= :zero",
    ExpressionAttributeValues: {
      ":OK": "OK",
      ":zero": 0
    },
    ScanIndexForward: false,
    Select: "ALL_ATTRIBUTES"
  },function(err,result) {
    console.log(result);
    res.render('ranking', { title: 'ランキング',users:result.Items });
  });
  
});

router.get('/result/:user_name/:saikoro_point',function(req, res, next) {

  Ranking.query({
    KeyConditionExpression: "user_name = :user_name",
    ExpressionAttributeValues: {
      ":user_name": req.params.user_name
    },
    ScanIndexForward: false,
    Select: "ALL_ATTRIBUTES"
  },function(err,result) {
console.log(result);
    var user = {};
    if (result.Items.length>0) {
      user = result.Items[0];
    }
    res.render('result', { title: '結果ページ',user:user });
  });
});

module.exports = router;
