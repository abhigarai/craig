/*

The idea is to create an array that will store all the links avaialble on a page

so using the chrome API we extract the all the links using the document.links 
and call unique() to delete duplicate links through array content matching

Further, create an XMLHttp Request to enumerate the on-click event of the 
user and send the request to open the site. Based on the response value
we enumerate whether the link is accessible or not and increment the 
necessary counter and finally display them within the alert box
with the values of the respective variables

*/

    links = new Array();
	var r = 0;
	var unr = 0;
	for (var i = 0; i < document.links.length; i++) 
	{
		links.push(document.links[i]);
	}

	for (var j = 0; j < document.links.length; j++) 
	{
		var link = document.links[j];
		var request = false;
		request=new XMLHttpRequest();

        if (request) 
		{
                request.open("POST", document.links[j]);
                if (request.status != 200) { r += 1; }
				else { unr += 1; }
        }
	}
	
var arrUnique = unique(links);
if(arrUnique.length > 0)
{
	alert("NUMBER OF LINKS:  "+document.getElementsByTagName('a').length+"\n\n"+"NUMBER OF VALID LINKS:  "+document.links.length+"\n\n"+"NUMBER OF REACHABLE LINKS:  "+r);
}

function unique(origArr) {
    var newArr = [],
        origLen = origArr.length,
        found, x, y;

    for (x = 0; x < origLen; x++) {
        found = undefined;
        for (y = 0; y < newArr.length; y++) {
            if (origArr[x] === newArr[y]) {
                found = true;
                break;
            }
        }
        if (!found) {
            newArr.push(origArr[x]);
        }
    }
    return newArr;
}