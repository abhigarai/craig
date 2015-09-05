
/*

This script basically registers a function based on the id value of 'all-links'
in the popup.html page to execute another script within the same tab
using the chrome.tabs API and chrome.tabs.executescript to register the script
to provide the option to download all the available links on the page to the
user local machine.

*/


$(document).ready(function(){

	document.getElementById('all-links').onclick = function() {
		chrome.windows.getCurrent(function(w) {
			chrome.tabs.getSelected(w.id,
			function (response){
				chrome.tabs.executeScript(null, {code: "var options = {enabled:'"+localStorage.enabled+"'};"}, function(){
					chrome.tabs.executeScript(null, {file: "js/Extract.js"}, function(){
					});
				});
			});
		});
		return false;
	};
		
});