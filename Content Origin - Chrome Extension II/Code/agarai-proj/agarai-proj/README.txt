The Extension provides the below functionalities:

Extracts the source/origin of a video/image when user clicks on the extension button on the menu-bar.

For multiple instances of objects such as result page of google image search, retrieves source of all the images in the page.

Provide the user to Download a file containing the source.

Provides the count of the number of source links available in the page and provide a list of all the links


Attribute 1: Extract the Content Origin of a single object 

Permissions:	
chrome.tabs
message passing
context types and menus
Implementation Details:
We query the tabs API of chrome extension to retrieve specific page properties that are provided access through it.
Here, we access the url property of the API and send a message to the background page to display the contents i.e. the link.
Chrome context type property provides the required context and permission for the messages to be passed across components securely.


Attribute 2: Extract Content Origin of a multiple objects


Permissions:	
chrome.tabs
chrome.executescript
chrome.localstorage
chrome.document.links ( multiple functions )
Implementation Details: 
The chrome API provides functions to traverse the page DOM to extract required details 
For Eg. Number of links available on a page, uniques links among them etc.
The links extracted, if they are not encrypted using https, simple DOM traversal may leak the url parameters.


Attribute 3: Download All Valid Source Links

Permissions:	
chrome.tabs
chrome.executescript
chrome.localstorage
access to page DOM structure
Implementation Details: 
Create an immutable file using the Blob class available in javascript and through the chrome localStorage class we provide access to the local machine.
Further, enumerate a click and download by traversing the DOM of the page through API available in javascript.
And, since the DOM is structured like a tree, we API needs the visited nodes to b deleted else it moves into an infinite loop.

 
Attribute 4: Provide Statistics

Permissions:	
chrome.tabs
chrome.executescript
access to page DOM structure & APIs to extract details
Implementation Details: 
Multiple API routines available to retrieve class of links in a page
Create XMLHttpRequest to validate the accessibility of the links through methods of POST & GET
Above API functions traverses the complete page DOM and hence are slow in case of pages having mashups and advertisements


Some Interesting Facts

Chrome extension interfaces do not have direct access to the DOM structures of popular sites like youtube, dailymotion. 
These sites implement policies that block any API to traverse their DOM structures directly. ( Mostly through some variation of CSP )
Programmatically generating HTTP requests are sometimes blocked by websites suspecting web-crawlers.
Accessing the extracted links also inherited the state within the browser local storage and hence it may be a security threat.
The data based on the page characteristics are never directly displayed from the page, they are always stored in the browser local storage and then displayed from there.(HTTP cookies and Flash cookies for tracking )

