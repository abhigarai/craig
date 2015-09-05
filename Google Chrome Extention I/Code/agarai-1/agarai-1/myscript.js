/*

This js is used display the alert on the page that contains the url.

This is done using CSP by directly modifying the DOM through the usage of Content scripts in chrome extension.

*/
window.onload = function (){
var pageInfo = {
  "url": window.location.href
};

alert("This is a webpage at "+ pageInfo.url);
}
