var path = require('path');
var system_config_path = path.join(__dirname, '../config/system-config');
var system_config = require(system_config_path);

var AWS = require('aws-sdk');
var db = new AWS.DynamoDB({region:system_config.dynamodb.region,
                           apiVersion:system_config.dynamodb.apiVersion});
if (system_config.dynamodb.endpoint!=null) {
  db.setEndpoint(system_config.dynamodb.endpoint);
}

function obj2dynamo(obj)
{
   var t = {};
   for (var k in obj) {
     if (typeof obj[k] == "string") {
       t[k] = {S: ""+obj[k]};
     } else {
       t[k] = {N: ""+obj[k]};
     }
   }
   return t;
}

function dynamo2obj(dy)
{
   var t = {};
   for (var k in dy) {
     if ("S" in dy[k]) {
       t[k] = ""+dy[k].S;
     } else {
       t[k] = dy[k].N-0;
     }
   }
   return t;
}

function filter(obj,format) {
  var t = {};
  for (var k in format) {
    if (k in obj) {
      t[k] = obj[k];
    } else {
      if (format[k] != "option") {
        throw new Error('not found a required parameter "'+k+'"');
      }
    }
  }
  return t;
};

var model = function(table) {
	var t = {};
	t.table = table;
	t.params = {};
	t.delete = function() {
	};
	t.update = function() {
	};
	return t;
};

var db = function(db,schema) {
	var t = {};
	t.db = db;
	t.schema = schema;
  
  t.obj2dynamo = obj2dynamo;
  t.dynamo2obj = dynamo2obj;
	
	t.describe = function(callback) {
    db.describeTable({TableName:schema.TableName},function(err,result) {
      callback(err,result);
    });
	};
	t.create = function(callback) {
    db.createTable(schema,callback);
	};
  t.delete = function(callback) {
    db.deleteTable({TableName:schema.TableName},callback);
  };
	t.query = function(request,callback) {
    var r = filter(request,{
      FilterExpression: "option",
      ExpressionAttributeNames: "option",
      ExpressionAttributeValues: "option",
      ReturnConsumedCapacity: "option",
      IndexName: "option",
      ProjectionExpression: "option",
      ExclusiveStartKey: "option",
      Limit: "option",
      Select: "option",
      ConsistentRead: "option",
      ScanIndexForward: "option",
      KeyConditionExpression: "option"
    });
    r.TableName = schema.TableName;
    if ("ExpressionAttributeValues" in r) {
      r.ExpressionAttributeValues = obj2dynamo(r.ExpressionAttributeValues);
    }
    db.query(r,function(err,result) {
      if (err) {
        callback(err,result);
        return;
      }
      if ("Items" in result) {
        var t = [];
        for (var i in result.Items) {
          t.push(dynamo2obj(result.Items[i]));
        }
        result.Items = t;
      }
      callback(err,result);
    });
	};
  t.get = function(request,callback) {
    var r = filter(request,{
      ProjectionExpression: "option",
      ExpressionAttributeNames: "option",
      ReturnConsumedCapacity: "option",
      ConsistentRead: "option"
    });
    r.Key = {};
    function findKey(schema,keytype) {
      for (var i in schema) {
        if (schema[i].KeyType == keytype) {
          r.Key[schema[i].AttributeName] = request.Key[schema[i].AttributeName];
          return true;
        }
      }
      return false;
    }
    console.log(request);
    console.log(schema.KeySchema);
    if (!findKey(schema.KeySchema,"HASH")) {
      callback(null,null);
      return;
    }
    if (!findKey(schema.KeySchema,"RANGE")) {
      callback(null,null);
      return;
    }
    r.Key = obj2dynamo(r.Key);
    r.TableName = schema.TableName;
    console.log(r);
    db.getItem(r,function(err,result) {
      if (err) {
        callback(err,result);
        return;
      }
      result.Item = dynamo2obj(result.Item);
      callback(err,result);
    });
  };
	t.add = function(request,callback) {
    var r = filter(request,{
      Item: "required",
      ConditionExpression: "option",
      ExpressionAttributeNames: "option",
      ExpressionAttributeValues: "option",
      ReturnConsumedCapacity: "option",
      ReturnItemCollectionMetrics: "option",
      ReturnValues: "option"
    });
    r.TableName = schema.TableName;
    r.Item = obj2dynamo(r.Item);
    if ("ExpressionAttributeValues" in r) {
      r.ExpressionAttributeValues = obj2dynamo(r.ExpressionAttributeValues);
    }
    db.putItem(r,function(err,result) {
      if (err) {
        callback(err,result);
        return;
      }
      if ("Attributes" in result) {
        result.Attributes = dynamo2obj(result.Attributes);
      }
      callback(err,result);
    });
	};
	t.update = function(request,callback) {
    var r = filter(request,{
      Key: "required",
      ConditionExpression: "option",
      ExpressionAttributeNames: "option",
      ExpressionAttributeValues: "option",
      ReturnConsumedCapacity: "option",
      ReturnItemCollectionMetrics: "option",
      UpdateExpression: "option",
      ReturnValues: "option"
    });
    r.TableName = schema.TableName;
    r.Key = obj2dynamo(r.Key);
    if ("ExpressionAttributeValues" in r) {
      r.ExpressionAttributeValues = obj2dynamo(r.ExpressionAttributeValues);
    }
    db.updateItem(r,function(err,result) {
      if (err) {
        callback(err,result);
        return;
      }
      if ("Attributes" in result) {
        result.Attributes = dynamo2obj(result.Attributes);
      }
      callback(err,result);
    });
	};
	t.scan = function(request,callback) {
    var r = filter(request,{
      FilterExpression: "option",
      ExpressionAttributeNames: "option",
      ExpressionAttributeValues: "option",
      ReturnConsumedCapacity: "option",
      IndexName: "option",
      ProjectionExpression: "option",
      ExclusiveStartKey: "option",
      Limit: "option",
      Select: "option",
      ConsistentRead: "option",
      ScanIndexForward: "option"
    });
    r.TableName = schema.TableName;
    if ("ExpressionAttributeValues" in r) {
      r.ExpressionAttributeValues = obj2dynamo(r.ExpressionAttributeValues);
    };
    db.scan(r,function(err,result) {
console.log(err);
console.log(result);
      if (err) {
        callback(err,result);
        return;
      }
      if ("Items" in result) {
        var t = [];
        for (var i in result.Items) {
          t.push(dynamo2obj(result.Items[i]));
        }
        result.Items = t;
      }
      callback(err,result);
    });
	};
  
  t.filter = filter;
	
	return t;
};

module.exports = db;
