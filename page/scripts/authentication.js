var logged = false;
function login(user, pass){
	AjaxCall("./ServletAuthentication","action=login&username="+user+"&password="+pass,loginSuccess);
}
function logout(){
	AjaxCall("./ServletAuthentication","action=logout",logoutSuccess);
}
function isLogged(callback){
	AjaxCall("./ServletAuthentication","action=isLogged",callback==undefined?isLoggedSuccess:callback);
}
function loginGuest(order){
	AjaxCall("./ServletAuthentication","action=login_guest&orderCode="+order,loginGuestSuccess);
}
function logoutGuest(){
	AjaxCall("./ServletAuthentication","action=logout_guest",logoutGuestSuccess);
}
function register(username, password, nome, cognome, livello){
	AjaxCall("./ServletAuthentication","action=signup&username="+username+"&password="+password+"&nome="+nome+"&cognome="+cognome+"&livello="+livello,registerSuccess);
}
function closeSession(idSessione){
	AjaxCall("./ServletAuthentication","action=kick_user",closeSessionSuccess);
}
function changePassword(oldPassword, newPassword){
	AjaxCall("./ServletAuthentication","action=change_password&newPass="+newPassword+"&oldPass="+oldPassword,changePasswordSuccess);
}
function listSessions(){
	AjaxCall("./ServletAuthentication","action=list_sessions",listSessionsSuccess);
}
function loginSuccess(xml){
	if(isResponseTrue(xml)){
		logged = true;
		location.href = "./index.html";
	}
	else {
		logged = false;
		alert(getError(xml));
	}
}
function logoutSuccess(xml){
	//TODO
}
function isLoggedSuccess(xml){
	logged = isResponseTrue(xml);
	alert(logged?"Login OK":"Login fallito");
	if(logged)
		location.href = "./index.html";
}
function loginGuestSuccess(xml){
	//TODO
}
function logoutGuestSuccess(xml){
	//TODO
}
function registerSuccess(xml){
	//TODO
}
function closeSessionSuccess(xml){
	//TODO
}
function changePasswordSuccess(xml){
	//TODO
}
function listSessionSuccess(xml){
	//TODO
}