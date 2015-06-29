var def = {
	dynamodb: {
		region: "us-west-2",
		apiVersion: "2012-08-10",
		endpoint: "http://localhost:8000/",
		
		table_name: "saikoro_omikuji_ranking",
		index_name: "saikoro_omikuji_ranking_sgi",
	}
};

//本番環境の時はDynamoDB Localは使わない
if (process.env["EC2_HOME"] != null) {
	delete edef.dynamodb.endpoint;
}

def.schema = {
	TableName: def.dynamodb.table_name,
	KeySchema: [
	    {
	        AttributeName: 'user_name',
	        KeyType: 'HASH'
	    }
	],
	AttributeDefinitions: [
	    {
	        AttributeName: 'dummy',
	        AttributeType: 'S'
	    },
	    {
	        AttributeName: 'user_name',
	        AttributeType: 'S'
	    },
	    {
	        AttributeName: 'saikoro_point',
	        AttributeType: 'N'
	    }
	],
	GlobalSecondaryIndexes: [
	  {
	    IndexName: def.dynamodb.index_name,
	    KeySchema: [
	      {
	          AttributeName: 'dummy',
	          KeyType: 'HASH'
	      },
	      {
	          AttributeName: 'saikoro_point',
	          KeyType: 'RANGE'
	      }
	    ],
	    Projection: {
		  ProjectionType : "ALL"
	    },
	    ProvisionedThroughput: {
	      ReadCapacityUnits: 1,
	      WriteCapacityUnits: 1
	    }
	  }
	],
	ProvisionedThroughput:  {
	    ReadCapacityUnits: 1,
	    WriteCapacityUnits: 1
	}
};

def.omikuji_hantei = function(point) {
	
	var table1 = ["大吉","吉","中吉","小吉","末吉","凶"];
	var table2 = [10,10,10,10,10,10];

	table1.reverse();
	table2.reverse();

	var sum = 0;
	for (var i in table2) {
		sum += table2[i];
		if (point <= table2[i]) return table1[i];
	}
	
	return "吉";
	
};

module.exports = def;
