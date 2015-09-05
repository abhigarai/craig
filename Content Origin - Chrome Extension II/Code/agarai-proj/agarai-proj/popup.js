/*

The idea is to register an EventListener onclick of the button on pop-up page with id 'trackOrigin' 
that calls a function trackorigin() which extracts the page origin through the URl property 
of the chrome.tabs API and sends the message to the background page to display it
within the alert box.

The idea of sending the message to the background page is to display it within 
the context of the extension rather than at the centre of the screen.

*/
document.addEventListener('DOMContentLoaded', function () {
	document.querySelector("[id=trackOrigin]").addEventListener('click', trackOrigin);
});

function trackOrigin(e) {
chrome.tabs.query({'active': true, 'lastFocusedWindow': true}, function (tabs) {
    var url = tabs[0].url;
	chrome.extension.sendMessage(url);
	alert("CONTENT ORIGIN: \n "+url);
});
}




