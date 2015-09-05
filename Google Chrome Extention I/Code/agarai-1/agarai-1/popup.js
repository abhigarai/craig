/*

On-load of the page we retieve the click counts from local storage and if its for the firt time assign it to zero as default.

Then registers an onclick of the thumbs-up image button that calls the function count that increments the local storage value and changes the html-content 
of the counter on popup.html

 the addeventlistener registers the functions loadsitetab and loadsitewindow on click of the respective buttons based on their id in popup.html.
 It also retrieves the value of the local storage and displays it in the html content of the counter.
 
 Further it registers an onclick event on every color of the colorbox which calls the setbgcolor function.
 
 In setbgcolor we utilize the chrome.executescript to change the bg color by passing the necessary parameters.

*/


var defaultClickCount = "0";

window.onload = function () {
	
	var clickCount = localStorage["clickCount"];
	
	if(clickCount == undefined)
	{
		clickCount = defaultClickCount;
	}
	
    var button = document.getElementById("clickCount");
    button.onclick = count();

    function count() {
      
        var demo = document.getElementById("counter");
		
        return function () {
			clickCount++;
            demo.innerHTML = clickCount;
			localStorage["clickCount"] = clickCount;
        }
    }
}

function setBgColor(e) {
  	var color,
		colorOpacity = 0.2,
		curItem = e.target,
		colorRgb;
        colorRgb = getAttrStyle(curItem,"background-color").match(/\d{1,3}/g).join(',');

        color = "rgba("+colorRgb+","+colorOpacity+")";

    chrome.tabs.executeScript(null,
        {code:"document.body.style.backgroundColor='" + color + "'"+";"});

  	window.close();
}

function loadSitesTab(e) {
	chrome.tabs.create({url: "https://www.google.com", selected: false});
}

function loadSitesWindow(e) {
	chrome.windows.create({url: "https://www.google.com/"});
}

document.addEventListener('DOMContentLoaded', function () {

	document.querySelector("[id=newTab]").addEventListener('click', loadSitesTab);
	document.querySelector("[id=newWindow]").addEventListener('click', loadSitesWindow);
	if(localStorage["clickCount"] == undefined)
	{
		document.getElementById("counter").innerHTML = defaultClickCount;
	}
	else
	{
		document.getElementById("counter").innerHTML = localStorage["clickCount"];
	}
	
	var divs = document.getElementsByClassName('colorbox');
		for (var i = 0; i < divs.length; i++) {
			divs[i].addEventListener('click', setBgColor);
		}
});
