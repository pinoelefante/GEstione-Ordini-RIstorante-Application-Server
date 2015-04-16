function getError(xml){
	var res = $(xml).find("message").text();
	return res;
}
function onError(xml){
	if(xml!=undefined){
		alert(getError(xml));
	}
	else
		alert("Si è verificato un errore");
}
function isResponseTrue(xml){
	var res = $(xml).find("status").text();
	return res=='true';
}
function AjaxCall(url,parameters,onsuccess){
	$.ajax({
		type : "POST",
		url : url,
		data : parameters,
		dataType : "xml",
		success: onsuccess,
		error: onError
	});
}