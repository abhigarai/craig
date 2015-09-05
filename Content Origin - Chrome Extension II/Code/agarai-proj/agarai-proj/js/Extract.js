/*

The idea is to create an array that will store all the links avaialble on a page

so using the chrome API we extract the all the links using the document.links 
and call unique() to delete duplicate links through array content matching

Further, individual rows based on the values of the unique array
and push it inside another array separating by comma to enumerate next row

Initialize a name for the file and download it by enumerating an onclick on the link
and removing the node from the DOM of the page to avoid any infinite loop

This downloads the file to the local machine as it has the permissions through
local storage containing all the links.

*/

var csv = [];
var extsArray = [];

links = new Array();
for (var i = 0; i < document.links.length; i++) {
	links.push(document.links[i].href);
}
var arrUnique = unique(links);

for (var i = 0; i < arrUnique.length; i++) {
	
	var buildRow = ['"' + arrUnique[i] + '"'];
	csv.push(buildRow.join(','));
}

var name = "All links.csv";
var link = document.createElement('a');
document.body.appendChild(link);

var myBlob = new Blob([csv.join('\n')], {"type": "text\/plain"});
link.href = window.URL.createObjectURL(myBlob);

link.download = name;
link.click(); 
element = link;
element.parentNode.removeChild(element);

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