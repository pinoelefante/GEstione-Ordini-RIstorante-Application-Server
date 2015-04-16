function addMenu(nome){
	AjaxCall("./ServletMenu","action=menu_add&nome="+nome,addMenuSuccess);
}
function deleteMenu(id){
	AjaxCall("./ServletMenu","action=menu_del&id="+id,deleteMenuSuccess);
}
function updateMenu(id, nome){
	AjaxCall("./ServletMenu","action=menu_update&id="+id+"&nome="+nome,updateMenuSuccess);
}
function listMenu(){
	AjaxCall("./ServletMenu","action=menu_list",listMenuSuccess);
}
function createMenuFrom(id, nome){
	AjaxCall("./ServletMenu","action=menu_copy&id="+id+"&nome="+nome,createMenuFromSuccess);
}
function addItemToMenu(menu, item){
	AjaxCall("./ServletMenu","action=menu_add_item&menu="+menu+"&prodotto="+item,addItemToMenuSuccess);
}
function removeItemFromMenu(menu, item){
	AjaxCall("./ServletMenu","action=menu_remove_item&menu="+menu+"&prodotto="+item,removeItemFromMenuSuccess);
}
function getDetailsMenu(id){
	AjaxCall("./ServletMenu","action=menu_list_prodottiid="+id,getDetailsMenuSuccess);
}
function addMenuSuccess(xml){
	//TODO
}
function deleteMenuSuccess(xml){
	//TODO
}
function updateMenuSuccess(xml){
	//TODO
}
function listMenuSuccess(xml){
	var html = "";
	$(xml).find("menu").each(function(){
		var nome = $(this).find("nome").text();
		var id = $(this).find("id").text();
		var data = $(this).find("data").text();
		//TODO
	});
}
function createMenuFromSuccess(xml){
	//TODO
}
function addItemToMenuSuccess(xml){
	//TODO
}
function removeItemFromMenuSuccess(xml){
	//TODO
}
function getDetailsMenuSuccess(xml){
	//TODO
}