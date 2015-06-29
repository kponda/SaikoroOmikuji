$(function() {
	
  var socket = io();
  socket.on('saikoro point', function(msg){
    console.log(msg);
	  window.location = "/result/"+msg.user_name+"/"+msg.saikoro_point;
  });
  
  socket.on('error request', function(msg) {
    console.log(msg);
	  window.location = "/";
  });
  
  $("#dummy_result_button").click(function() {
	  
	  var saikoro_point = $("#saikoro_point").val()-0;
	  
	  $.post('/regist',{saikoro_point:saikoro_point},function() {
	  });
	  
	  return false;
	  
  });
	
});
