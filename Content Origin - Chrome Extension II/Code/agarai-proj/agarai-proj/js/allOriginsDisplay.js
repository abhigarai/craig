/*

The idea is to create an array that will store all the links avaialble on a page

so using the chrome API we extract the all the links using the document.links 
and call unique() to delete duplicate links through array content matching
and finally display the whole array within an alert box that displays
all the links pushed in the array available within a page

*/
    links = new Array();
	for (var i = 0; i < document.links.length; i++) {
	links.push("\n\n"+ document.links[i]);
}
var arrUnique = unique(links);
if(arrUnique.length > 0)
{
	alert(arrUnique);
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