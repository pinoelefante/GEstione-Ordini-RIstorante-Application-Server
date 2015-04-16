$(document).ready(function() {
	var callback = function(xml){
		if(!logged)
			location.href="./login.html";
	};
	isLogged(callback);
});