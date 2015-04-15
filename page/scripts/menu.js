function addMenu(nome){
	
}
function deleteMenu(id){
	
}
function updateMenu(id, nome){
	
}
function listMenu(){
	$.ajax({
		type : "POST",
		url : "http://localhost:9001/ServletMenu",
		data : "action=menu_list",
		dataType : "xml",
		success : function(msg) {
			alert("OK");
		}//,
		//error : onError
	});
}
function createMenuFrom(id, nome){
	
}
function addItemToMenu(menu, item){
	
}
function removeItemFromMenu(menu, item){
	
}
function getDetailsMenu(id){
	
}