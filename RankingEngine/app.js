var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var path = require('path');

var routes = require('./routes/index');

var system_config_path = path.join(__dirname, './config/system-config');
var system_config = require(system_config_path);
var dynamodb = require('./common/dynamodb_util');

var AWS = require('aws-sdk');
var db = new AWS.DynamoDB({region:system_config.dynamodb.region,
                           apiVersion:system_config.dynamodb.apiVersion});
if (system_config.dynamodb.endpoint!=null) {
  db.setEndpoint(system_config.dynamodb.endpoint);
}

var Ranking = dynamodb(db,system_config.schema);

Ranking.delete(function(err,result) {
  // if (err) throw err;
  setTimeout(function() {
    Ranking.describe(function(err,result) {
      if (err) {
        if (err.code == 'ResourceNotFoundException') {
          setTimeout(function() {
            Ranking.create(function(err,result) {
              // if (err) throw err;
            });
          },1000);
        } else {
          throw err;
        }
      }
    });
  },1000);
});

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
//app.use(favicon(__dirname + '/public/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', routes);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      error: err
    });
  });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    error: {}
  });
});


module.exports = app;
