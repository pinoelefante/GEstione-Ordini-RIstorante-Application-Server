$(document).ready(function() {
	var callbackAdmin = function(xml){
		alert("isAdmin\n"+xml);
		if(isResponseTrue(xml)){
			$(".itemAdmin").each(function(){
				$(this).show();
			});
		}
		else {
			$(".itemAdmin").each(function(){
				$(this).hide();
			});
		}
	};
	
	var callbackLogged = function(xml){
		if(isResponseTrue(xml)){
			$(".itemLogged").each(function(){
				$(this).show();
			});
			$(".itemNotLogged").each(function(){
				$(this).hide();
			});
			isAdmin(callbackAdmin);
		}
		else {
			$(".itemLogged").each(function(){
				$(this).hide();
			});
			$(".itemAdmin").each(function(){
				$(this).hide();
			});
			$(".itemNotLogged").each(function(){
				$(this).show();
			});
		}
	};
	isLogged(callbackLogged);
});
function doLogin(){
	var username = $("#username").val();
	var password = $("#password").val();
	login(username,password);
}