/*

This script basically registers a function based on the id value of 'show-all-links'
in the popup.html page to execute another script within the same tab
using the chrome.tabs API and chrome.tabs.executescript to register the script
that displays all the origins available within the page.

*/


$(document).ready(function(){

	document.getElementById('show-all-link').onclick = function() {
		chrome.windows.getCurrent(function(w) {
			chrome.tabs.getSelected(w.id,
			function (response){
					chrome.tabs.executeScript(null, {code: "var options = {enabled:'"+localStorage.enabled+"'};"}, function(){
					chrome.tabs.executeScript(null, {file: "js/allOriginsDisplay.js"}, function(){
					});
				});
			});
		});
		return false;
	};
	
	
});