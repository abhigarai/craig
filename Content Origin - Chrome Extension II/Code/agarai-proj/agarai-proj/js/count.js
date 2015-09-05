/*

This script basically registers a function based on the id value of 'links-count'
in the popup.html page to execute another script within the same tab
using the chrome.tabs API and chrome.tabs.executescript to register the script
to provides statistics based on the category of links within a page.

*/



$(document).ready(function(){

	document.getElementById('links-count').onclick = function() {
		chrome.windows.getCurrent(function(w) {
			chrome.tabs.getSelected(w.id,
			function (response){
					chrome.tabs.executeScript(null, {code: "var options = {enabled:'"+localStorage.enabled+"'};"}, function(){
					chrome.tabs.executeScript(null, {file: "js/countLinks.js"}, function(){
					});
				});
			});
		});
		return false;
	};
	
	
});