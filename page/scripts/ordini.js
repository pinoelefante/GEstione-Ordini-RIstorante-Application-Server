function addOrder(xml){
	AjaxCallXML("./ServletAuthentication","action=order_add",xml, function(){});
}